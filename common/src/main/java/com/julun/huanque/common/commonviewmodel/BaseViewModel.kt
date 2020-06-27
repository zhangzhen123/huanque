package com.julun.huanque.common.commonviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.net.RequestCaller

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/24 11:23
 *
 *@Description: BaseViewModel 依然支持rx网络请求框架自动解绑
 *
 */

open class BaseViewModel : RequestCaller, ViewModel() {
    val loadState by lazy {
        MutableLiveData<NetState>()
    }

    override fun getRequestCallerId(): String {
        return StringHelper.uuid()
    }

    /**
     * ViewModel进入onCleared方法的时候，是否需要取消请求
     * 默认 true  取消请求
     * 如需要页面关闭，继续请求，请override该方法， return false
     */
    open fun needClearRequest() = true

    override fun onCleared() {
        super.onCleared()
        if (needClearRequest()) {
            cancelAllRequest()
        }

    }
}