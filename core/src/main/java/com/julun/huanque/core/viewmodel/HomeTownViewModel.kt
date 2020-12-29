package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.HomeTownInfo
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/28 20:41
 *@描述 家乡ViewModel
 */
class HomeTownViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //家乡数据
    val homeTownInfo: MutableLiveData<HomeTownInfo> by lazy { MutableLiveData<HomeTownInfo>() }

    /**
     * 获取家乡数据
     */
    fun queryHomeTownInfo(userId: Long) {
        viewModelScope.launch {
            request({
                homeTownInfo.value = socialService.homeTown(FriendIdForm(userId)).dataConvert()
            }, {})
        }
    }

}