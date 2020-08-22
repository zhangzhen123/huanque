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

    //声网token
    var agoraToken = ""

    //声网频道
    var channelId = ""

    //语音通话ID
    var callId = 0L

    //邀请的用户ID
    var inviteUserId = 0L


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
                closeSoundFlag.value = true
            }, {}, { currentState.value = WAIT })
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
            })
        }
    }

    /**
     *
     * 拒绝邀请
     */
    fun avoiceReject(inviteUserId: Long){
        viewModelScope.launch {
            request({
                socialService.avoiceReject(InviteUserIdForm(inviteUserId)).dataConvert()
            })
        }
    }
}