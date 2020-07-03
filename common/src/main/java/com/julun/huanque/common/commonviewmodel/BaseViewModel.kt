package com.julun.huanque.common.commonviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.QueryType
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
    /**
     * 这个loadState只供页面加载的请求使用 通过它可以直接反馈请求的状态去更新页面 加载中 成功 失败 网络异常
     * 其他请求不需要带
     */
    val loadState by lazy {
        MutableLiveData<NetState>()
    }

    /**
     * 普通请求的开关标志
     */
    protected val queryState = MutableLiveData<QueryType>()

    fun queryInfo(queryType: QueryType = QueryType.INIT) {
        queryState.value = queryType
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