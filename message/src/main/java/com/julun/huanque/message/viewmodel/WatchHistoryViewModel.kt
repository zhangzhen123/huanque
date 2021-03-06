package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.bean.beans.WatchListExt
import com.julun.huanque.common.bean.beans.WatchListInfo
import com.julun.huanque.common.bean.forms.GuideInfoForm
import com.julun.huanque.common.bean.forms.WatchForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:56
 *@描述 观看历史ViewModel
 */
class WatchHistoryViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //访问历史
    val historyData: MutableLiveData<WatchListInfo<WatchHistoryBean>> by lazy { MutableLiveData<WatchListInfo<WatchHistoryBean>>() }

    //额外配置参数
    val watchExtra: MutableLiveData<WatchListExt> by lazy { MutableLiveData<WatchListExt>() }

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
                val watchHistory = socialService.visitLog(WatchForm(type, mOffset)).dataConvert()
                watchHistory.isPull = refresh
                mOffset += watchHistory.list.size
                watchExtra.value = watchHistory.extData
                historyData.value = watchHistory
            }, {},needLoadState = true)
        }
    }

    /**
     * 刷新引导文案
     */
    fun refreshGuide() {
        viewModelScope.launch {
            request({
                val tempBean = socialService.guideInfo(GuideInfoForm(GuideInfoForm.Visit)).dataConvert()
                val extra = WatchListExt(guideText = tempBean.guideText, touchType = tempBean.touchType,viewNum = tempBean.viewNum)
                watchExtra.value = extra
            })
        }
    }
}