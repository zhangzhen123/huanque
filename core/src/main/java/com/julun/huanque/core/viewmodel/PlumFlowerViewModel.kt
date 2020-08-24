package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.FlowerDayListBean
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/8/21 21:06
 *@描述 花魁使用的ViewModel
 */
class PlumFlowerViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    val listData: MutableLiveData<FlowerDayListBean> by lazy { MutableLiveData<FlowerDayListBean>() }

    /**
     * 获取今日榜
     */
    fun getToadyList() {
        viewModelScope.launch {
            request({
                listData.value = socialService.flowerToday().dataConvert()
            }, {}, {}, true)
        }
    }

    /**
     * 获取昨日榜
     */
    fun getYesterdayList() {
        viewModelScope.launch {
            request({
                listData.value = socialService.flowerYesterday().dataConvert()
            }, {}, {}, true)
        }
    }

    /**
     * 获取名人榜
     */
    fun getFamousList() {
        viewModelScope.launch {
            request({
                socialService.flowerFamous().dataConvert()
            }, {}, {}, true)
        }
    }
}