package com.julun.huanque.common.net

import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParseException
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import org.apache.http.conn.ConnectTimeoutException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/24 17:25
 *
 *@Description: NetExceptionHandle
 *
 */
object NetExceptionHandle {
    fun handleException(e: Throwable?, loadNetState: MutableLiveData<NetState>?) {
        loadNetState ?: return
        e?.let {
            when (it) {
                is ResponseError -> {
                    loadNetState.postValue(NetState(NetStateType.ERROR, it.busiMessage))
                }
                is HttpException -> {
                    loadNetState.postValue(NetState(NetStateType.NETWORK_ERROR))
                }
                is ConnectException -> {
                    loadNetState.postValue(NetState(NetStateType.NETWORK_ERROR))
                }
                is ConnectTimeoutException -> {
                    loadNetState.postValue(NetState(NetStateType.NETWORK_ERROR))
                }
                is UnknownHostException -> {
                    loadNetState.postValue(NetState(NetStateType.NETWORK_ERROR))
                }
                is JsonParseException -> {
                    loadNetState.postValue(NetState(NetStateType.NETWORK_ERROR))
                }
            }
        }
    }
}