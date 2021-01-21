package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.HeartListInfo
import com.julun.huanque.common.bean.beans.SingleHeartBean
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.bean.beans.WatchListInfo
import com.julun.huanque.common.bean.forms.HeartBeanForm
import com.julun.huanque.common.bean.forms.WatchForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2021/1/21 14:31
 *@描述 心动页面使用的ViewModel
 */
class HeartBeatViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //访问历史
    val heartBeatData: MutableLiveData<HeartListInfo<SingleHeartBean>> by lazy { MutableLiveData<HeartListInfo<SingleHeartBean>>() }

    private var mOffset = 0

    /**
     * 查询数据
     */
    fun queryData(type: String, refresh: Boolean = false) {
        if (refresh) {
            mOffset = 0
        }
        viewModelScope.launch {
            request({
                val watchHistory = socialService.hearttouchList(HeartBeanForm(type, mOffset)).dataConvert()
                watchHistory.isPull = refresh
                mOffset += watchHistory.list.size
                heartBeatData.value = watchHistory
            }, {})
        }
    }

}