package com.julun.huanque.agora.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.AnonymousBasicInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/8/19 20:53
 *@描述 匿名语音使用的ViewModel
 */
class AnonymousVoiceViewModel : BaseViewModel() {
    private val socialService by lazy { Requests.create(SocialService::class.java) }

    val basicData : MutableLiveData<AnonymousBasicInfo> by lazy { MutableLiveData<AnonymousBasicInfo>() }


    fun getBasicData() {
        viewModelScope.launch {
            request({
                val result = socialService.avoiceHomeInfo().dataConvert()
                basicData.value = result
            })
        }
    }

}