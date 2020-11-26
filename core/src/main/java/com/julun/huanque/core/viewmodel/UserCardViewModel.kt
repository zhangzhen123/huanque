package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.UserInfoInRoom
import com.julun.huanque.common.bean.forms.UserProgramForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/30 14:50
 *@描述 用户名片使用
 */
class UserCardViewModel : BaseViewModel() {

    private val mLiveService: LiveRoomService by lazy { Requests.create(LiveRoomService::class.java) }

    //用户数据
    val userInfoData: MutableLiveData<UserInfoInRoom> by lazy { MutableLiveData<UserInfoInRoom>() }

    //用户ID
    var mUserId = 0L

    //用户昵称
    var mNickname = ""

    //当前用户所在的直播间ID
    var programId = 0L


    /**
     * 查询用户数据
     */
    fun queryUserInfo() {
        viewModelScope.launch {
            request({
                val result = mLiveService.userInfo(UserProgramForm(targetUserId = mUserId, programId = programId)).dataConvert()
                userInfoData.value = result
            }, needLoadState = true)
        }
    }


}