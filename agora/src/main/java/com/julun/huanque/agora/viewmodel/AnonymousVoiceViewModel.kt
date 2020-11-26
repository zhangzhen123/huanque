package com.julun.huanque.agora.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.AnonymousBasicInfo
import com.julun.huanque.common.bean.beans.CheckBeansData
import com.julun.huanque.common.bean.beans.UserInfoInRoom
import com.julun.huanque.common.bean.forms.InviteUserIdForm
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/8/19 20:53
 *@描述 匿名语音使用的ViewModel
 */
class AnonymousVoiceViewModel : BaseViewModel() {

    companion object {
        //刚进入页面的状态
        const val WAIT = "WAIT"

        //匹配中状态
        const val MATCH = "MATCH"

        //通话中状态
        const val VOICE = "VOICE"

        //等待接听状态（被叫的时候使用）
        const val WAIT_ACCEPT = "WAIT_ACCEPT"
    }

    private val socialService by lazy { Requests.create(SocialService::class.java) }

    val basicData: MutableLiveData<AnonymousBasicInfo> by lazy { MutableLiveData<AnonymousBasicInfo>() }

    //当前状态
    val currentState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //显示本人信息的标记位
    val showMineInfoData: MutableLiveData<UserInfoInRoom> by lazy { MutableLiveData<UserInfoInRoom>() }

    //检测余额结果
    val checkBeansData: MutableLiveData<CheckBeansData> by lazy { MutableLiveData<CheckBeansData>() }

    //揭秘身份
    val unveilIdentityData: MutableLiveData<UserInfoInRoom> by lazy { MutableLiveData<UserInfoInRoom>() }

    //结束声音
    val closeSoundFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //匿名语音结束标记位
    val voiceEndFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //关注结果
//    val followStatusData: MutableLiveData<UserInfoChangeResult> by lazy { MutableLiveData<UserInfoChangeResult>() }

    //关闭页面的标识位
    val finishFlag : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //声网token
    var agoraToken = ""

    //声网频道
    var channelId = ""

    //语音通话ID
    var callId = 0L

    //邀请的用户ID
    var inviteUserId = 0L

    var targetUserId = 0L


    fun getBasicData() {
        viewModelScope.launch {
            request({
                val result = socialService.avoiceHomeInfo().dataConvert()
                basicData.value = result
            })
        }
    }

    /**
     * 开始匹配
     */
    fun startMatch() {
        viewModelScope.launch {
            request({
                socialService.startMatch().dataConvert()
            }, {
                currentState.value = WAIT
            })
        }
    }

    /**
     * 取消匹配
     */
    fun cancelMatch() {
        viewModelScope.launch {
            request(
                {
                    socialService.cancelMatch().dataConvert()
                    currentState.value = WAIT
//                    finishFlag.value = true
                }
            )
        }
    }

    /**
     * 挂断匿名语音
     */
    fun hangUp() {
        viewModelScope.launch {
            request({
                socialService.avoiceHangUp(NetcallIdForm(callId)).dataConvert()
            }, {}, {
                ToastUtils.show("匿名语音已结束")
                closeSoundFlag.value = true
                voiceEndFlag.value = true
            })
        }
    }

    /**
     * 公开身份
     */
    fun openIdentify() {
        viewModelScope.launch {
            request({
                val result = socialService.openIdentify(NetcallIdForm(callId)).dataConvert()
                showMineInfoData.value = result
            })
        }
    }

    /**
     * 检测余额
     */
    fun checkBeans() {
        viewModelScope.launch {
            request({
                val result = socialService.checkBeans().dataConvert()
                checkBeansData.value = result
            })
        }
    }

    /**
     * 揭秘身份
     */
    fun unveilIdentity() {
        viewModelScope.launch {
            request({
                val result = socialService.unveilIdentity(NetcallIdForm(callId)).dataConvert()
                unveilIdentityData.value = result
            })
        }
    }

    /**
     * 接受匿名语音
     */
    fun avoiceAccept(inviteUserId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.avoiceAccept(InviteUserIdForm(inviteUserId)).dataConvert()
//                currentState.value = VOICE
            }, {
                currentState.value = WAIT
            }, {

            })
        }
    }

    /**
     *
     * 拒绝邀请
     */
    fun avoiceReject(inviteUserId: Long) {
        viewModelScope.launch {
            request({
                socialService.avoiceReject(InviteUserIdForm(inviteUserId)).dataConvert()
            }, {}, {
                closeSoundFlag.value = true
                voiceEndFlag.value = true
            })
        }
    }

//    /**
//     * 关注
//     */
//    fun follow(userId: Long) {
//        viewModelScope.launch {
//            request({
//                val follow = socialService.follow(FriendIdForm(userId)).dataConvert()
//                val followBean = UserInfoChangeResult(follow = follow.follow, userId = userId)
//                followStatusData.value = followBean
//                EventBus.getDefault().post(UserInfoChangeEvent(userId, follow.stranger))
//                EventBus.getDefault().post(SendRNEvent(RNMessageConst.FollowUserChange, hashMapOf("userId" to userId, "isFollowed" to true)))
//            }, {
//
//            })
//        }
//    }
}