package com.julun.huanque.common.net


/**
 * 保留可取消的网络请求设计
 */
interface RequestCaller {
    /**
     * 获取远程请求的调用者的唯一标识符.
     */
    fun getRequestCallerId(): String

    /**
     * 取消当前的对象发起的所有请求
     */
    fun cancelAllRequest() = RxCallerHelper.cancelRelatedRequest(this.getRequestCallerId())

    fun onRequestFinished(observableSubscriber: CancelableObservableSubscriber<*>)
//            = Requests.removeFromCacheWhenRequestIsCompleted(this.getRequestCallerId(), observableSubscriber)
            = RxCallerHelper.removeFromCacheWhenRequestIsCompleted(
        this.getRequestCallerId(),
        observableSubscriber
    )

    fun <T> makeSubscriber(whenSuccess:NSuccess<T> = {}): CancelableObservableSubscriber<T> {
        return CancelableObservableSubscriber.create<T>(this,whenSuccess)
    }
}