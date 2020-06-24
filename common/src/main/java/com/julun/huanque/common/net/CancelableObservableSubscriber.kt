package com.julun.huanque.common.net


import io.reactivex.rxjava3.core.FlowableSubscriber
import io.reactivex.rxjava3.core.MaybeObserver
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import org.reactivestreams.Subscription

/**
 * Created by nirack on 17-5-24.
 *
 * 修改于 2020/6/24 by zhangzhen rx3.0版本
 */


//定义几个常见的函数
typealias NSuccess<T> = (t: T) -> Unit
typealias NError = (t: Throwable) -> Unit
typealias NAction = () -> Unit


class CancelableObservableSubscriber<T> : Observer<T>, FlowableSubscriber<T>, SingleObserver<T>,
    MaybeObserver<T> {
    private var subscription: Subscription? = null
    private var disposable: Disposable? = null

    /**
     * 如果是作为 FlowableSubscriber ,onSubscriber的时候请求的次数,如果为null抛出异常,如果为0,则认为是Long.MAX_VALUE
     */
    var flowableRequestTimes: Long? = null
        private set

    /**
     * @see .flowableRequestTimes
     *
     * @param flowableRequestTimes
     * @return
     */
    fun withFlowableRequestTimes(flowableRequestTimes: Long?): CancelableObservableSubscriber<T>? {
        this.flowableRequestTimes = flowableRequestTimes
        return this
    }

    override fun onSubscribe(s: Subscription) {
        subscription = s
        if (flowableRequestTimes == null) {
            throw Exception("使用 FlowableSubscriber 的时候一定要设置 flowableRequestTimes ")
        }
        if (flowableRequestTimes == 0L) {
            flowableRequestTimes = Long.MAX_VALUE
        }
        s.request(flowableRequestTimes!!)
        onSubscribe()
    }

    override fun onSubscribe(d: Disposable) {
        disposable = d
        onSubscribe()
    }

    private fun onSubscribe() {
        RxCallerHelper.bindCaller(this)
    }

    /**
     * 几个需要特殊处理的 ,非 200 的 业务 code
     */
    var specifiedCodes: IntArray = IntArray(0)

    /**
     * 调用者的id
     */
    val requestCaller: String?

    private var onSuccess: NSuccess<T>? = null
    private var onError: NError? = null
    private var onCompleted: NAction? = null
    private var finalCall: NAction? = null

    private constructor(requestCaller: RequestCaller?) {
        this.requestCaller = requestCaller!!.getRequestCallerId()
    }

    constructor() {
        requestCaller = ""
    }

    fun ifError(onError: NError): CancelableObservableSubscriber<T> {
        this.onError = onError
        return this
    }

    fun withOnSuccess(onSuccess: NSuccess<T>): CancelableObservableSubscriber<T> {
        this.onSuccess = onSuccess
        return this
    }

    fun withOnCompleted(call: NAction): CancelableObservableSubscriber<T> {
        this.onCompleted = call
        return this
    }

    fun withFinalCall(call: NAction): CancelableObservableSubscriber<T> {
        finalCall = call
        return this
    }

    fun withSpecifiedCodes(vararg specifiedCodes: Int): CancelableObservableSubscriber<T> {
        this.specifiedCodes = specifiedCodes
        return this
    }

    override fun onSuccess(t: T) {
        onNext(t)
    }

    override fun onNext(t: T) {
        doCleanUp()
        onSuccess?.invoke(t)
        finalCall?.invoke()
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
        doCleanUp()
        onError?.invoke(e)
        finalCall?.invoke()
    }

    override fun onComplete() {
        doCleanUp()
        onCompleted?.invoke()
    }

    private fun doCleanUp() {
        RxCallerHelper.removeFromCacheWhenRequestIsCompleted(requestCaller ?: return, this)
    }

    /**
     * 取消请求的响应
     */
    fun cancel() {
        subscription?.cancel()
        disposable?.dispose()
    }

    companion object {
        fun <T> create(requestCaller: RequestCaller?): CancelableObservableSubscriber<T> {
            return CancelableObservableSubscriber(requestCaller)
        }

        fun <T> createWithoutCaller(): CancelableObservableSubscriber<T> {
            return CancelableObservableSubscriber()
        }

        fun <T> create(requestCaller: RequestCaller, whenSuccess: NSuccess<T>): CancelableObservableSubscriber<T> {
            return CancelableObservableSubscriber<T>(requestCaller).withOnSuccess(whenSuccess)
        }
    }
}
