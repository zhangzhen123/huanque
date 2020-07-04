package com.julun.huanque.common.suger

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.NAction
import com.julun.huanque.common.net.NError
import com.julun.huanque.common.net.NSuccess
import com.julun.huanque.common.net.NetExceptionHandle
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

fun <T> Root<T>.dataConvert(): T {
    return mapper(this)
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
 * [final]最终返回
 *[needLoadState]是否需要初始化加载状态回调
 *
 */
suspend fun <T> BaseViewModel.requestBack(
    block: SBlock<Root<T>>,
    error: SNError? = null,
    final: SNAction? = null,
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

    } finally {
        final?.invoke()
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

fun Throwable.errorMsg(): String {
    return if (this is ResponseError) {
        this.busiMessage
    } else {
        "网络错误了"
    }
}

