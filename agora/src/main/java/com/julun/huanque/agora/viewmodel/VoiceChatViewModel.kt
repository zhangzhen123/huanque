package com.julun.huanque.agora.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.ChatUserBean
import com.julun.huanque.common.bean.beans.NetcallBean
import com.julun.huanque.common.bean.forms.CreateCommunicationForm
import com.julun.huanque.common.bean.forms.NetcallCancelForm
import com.julun.huanque.common.bean.forms.NetcallHangUpForm
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
 *@创建时间 2020/7/2 11:56
 *@描述 语音聊天ViewModel
 */
class VoiceChatViewModel : BaseViewModel() {

    companion object {
        //语音通话状态
        //拨号状态
        const val VOICE_CALLING = "VOICE_CALLING"

        //接听状态
        const val VOICE_ACCEPT = "VOICE_ACCEPT"

        //待接听状态
        const val VOICE_WAIT_ACCEPT = "VOICE_WAIT_ACCEPT"

        //挂断状态
        const val VOICE_CLOSE = "VOICE_CLOSE"
    }

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //当前语音状态
    val currentVoiceState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //对方用户数据
    val targetUserBean: MutableLiveData<ChatUserBean> by lazy { MutableLiveData<ChatUserBean>() }

    //会话详情数据
    val netcallBeanData: MutableLiveData<NetcallBean> by lazy { MutableLiveData<NetcallBean>() }

    //token
    var agoraToken = ""

    //channelId
    var channelId = ""

    //语音通话ID
    var callId = 0L

    //通话时长
    var duration = 0L


    /**
     * 创建会话
     */
    fun createConmmunication(userId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.createCommunication(CreateCommunicationForm(userId)).dataConvert()
                netcallBeanData.value = result
            })
        }
    }

    /**
     * 接受会话
     */
    fun acceptVoice() {
        viewModelScope.launch {
            request({
                socialService.netcallAccept(NetcallIdForm(callId)).dataConvert()
            })
        }
    }

    /**
     * 被叫直接拒绝或者加入频道失败，调用该接口 挂断通话
     */
    fun refuseVoice() {
        viewModelScope.launch {
            request({
                socialService.netcallRefuse(NetcallIdForm(callId)).dataConvert()
                currentVoiceState.value = VOICE_CLOSE
            })
        }
    }

    /**
     * 主叫取消通话
     */
    fun calcelVoice(type: String) {
        viewModelScope.launch {
            request({
                socialService.netcallCancel(NetcallCancelForm(callId, type)).dataConvert()
                currentVoiceState.value = VOICE_CLOSE
            })
        }
    }

    /**
     * 挂断语音通话
     */
    fun hangUpVoice() {
        viewModelScope.launch {
            request({
                socialService.netcallHangUp(NetcallHangUpForm(callId,duration)).dataConvert()
                currentVoiceState.value = VOICE_CLOSE
            })
        }
    }

}