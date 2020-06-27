package com.julun.huanque.common.suger

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
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

fun <T> Root<T>.dataConvert(): T {
    return mapper(this)
}

/**
 * 协程请求的统一封装
 */
suspend fun BaseViewModel.request(
    block: suspend () -> Unit,
    error: NError? = null,
    final: NAction? = null,
    loadState: MutableLiveData<NetState>? = null
) {
    loadState?.postValue(NetState(state = NetStateType.LOADING))
    runCatching {
        block()
    }.onSuccess {
        loadState?.postValue(NetState(state = NetStateType.SUCCESS))
        final?.invoke()
    }.onFailure {
        NetExceptionHandle.handleException(it, loadState)
        it.printStackTrace()
        error?.invoke(it)
        final?.invoke()
    }
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
        error?.invoke(it)
    }.withFinalCall(final)
    )
}

