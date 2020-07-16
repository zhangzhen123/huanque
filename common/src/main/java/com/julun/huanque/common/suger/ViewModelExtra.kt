package com.julun.huanque.common.suger

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.*
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.NAction
import com.julun.huanque.common.net.NError
import com.julun.huanque.common.net.NSuccess
import com.julun.huanque.common.net.NetExceptionHandle
import com.julun.huanque.common.utils.ToastUtils
import io.reactivex.rxjava3.core.Observable

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/24 12:27
 *
 *@Description: RootExtra
 *
 */
typealias SBlock<T> = suspend () -> T
typealias SNError = suspend (t: Throwable) -> Unit
typealias SNAction = suspend () -> Unit

fun <T> Root<T>.dataConvert( intArray: IntArray? = null): T {
    return mapper(this,intArray)
}

/**
 * 协程请求的统一封装
 * [block]要执行的block
 * [error]错误返回
 * [final]最终返回
 *[needLoadState]是否需要初始化加载状态回调
 *
 */
suspend fun BaseViewModel.request(
    block: SNAction,
    error: SNError? = null,
    final: SNAction? = null,
    needLoadState: Boolean = false
) {
    if (needLoadState) {
        loadState.postValue(NetState(state = NetStateType.LOADING))
    }
    try {
        block()
        if (needLoadState) {
            loadState.postValue(NetState(state = NetStateType.SUCCESS))
        }
    } catch (e: Throwable) {
        if (needLoadState) {
            NetExceptionHandle.handleException(e, loadState)
        }

        if(e is ResponseError){
            ToastUtils.show(e.busiMessage)
        }
        error?.invoke(e)
        e.printStackTrace()
    } finally {
        final?.invoke()
    }
}

/**
 * 协程请求的统一封装
 * [block]要执行的block
 * [error]错误返回
 *[needLoadState]是否需要初始化加载状态回调
 *
 */
suspend fun <T> BaseViewModel.requestResult(
    block: SBlock<Root<T>>,
    error: SNError? = null,
    needLoadState: Boolean = false
): T? {
    if (needLoadState) {
        loadState.postValue(NetState(state = NetStateType.LOADING))
    }
    try {
        val result = block().dataConvert()
        if (needLoadState) {
            loadState.postValue(NetState(state = NetStateType.SUCCESS))
        }
        return result
    } catch (e: Throwable) {
        if (needLoadState) {
            NetExceptionHandle.handleException(e, loadState)
        }
        e.printStackTrace()
        error?.invoke(e)

    }
    return null
}

/**
 * rxJava请求的统一封装
 */
fun <T> BaseViewModel.requestRx(
    request: () -> Observable<Root<T>>,
    onSuccess: NSuccess<T>,
    error: NError? = null,
    final: NAction = {},
    loadState: MutableLiveData<NetState>? = null
) {
    loadState?.postValue(NetState(state = NetStateType.LOADING))
    request().handleResponse(makeSubscriber<T> {
        onSuccess(it)
        loadState?.postValue(NetState(state = NetStateType.SUCCESS))
    }.ifError {
        NetExceptionHandle.handleException(it, loadState)
        error?.invoke(it)
        it.printStackTrace()
    }.withFinalCall(final)
    )
}

/**
 * 错误 封装为ReactiveData
 */
fun <T>Throwable.coverError(): ReactiveData<T>{
    val err= if (this is ResponseError) {
        this
    } else {
        ResponseError (OTHER_DEF_ERROR,"异常错误")
    }
    return ReactiveData(state = NetStateType.ERROR,error = err)
}

/**
 * 正确的数据 封装为ReactiveData
 */
fun <T>T.coverRtData():ReactiveData<T>{
    return ReactiveData(state = NetStateType.SUCCESS,data = this)
}

