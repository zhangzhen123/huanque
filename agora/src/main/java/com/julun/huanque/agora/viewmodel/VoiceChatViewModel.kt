package com.julun.huanque.agora.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.NetcallBean
import com.julun.huanque.common.bean.forms.CreateCommunicationForm
import com.julun.huanque.common.bean.forms.NetcallCancelForm
import com.julun.huanque.common.bean.forms.NetcallHangUpForm
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.CancelType
import com.julun.huanque.common.constant.VoiceResultType
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.suger.whatEver
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
    val targetUserBean: MutableLiveData<ChatUser> by lazy { MutableLiveData<ChatUser>() }

    //会话详情数据
    val netcallBeanData: MutableLiveData<NetcallBean> by lazy { MutableLiveData<NetcallBean>() }

    //会话结束  发送模拟消息使用
    val voiceBeanData: MutableLiveData<VoiceConmmunicationSimulate> by lazy { MutableLiveData<VoiceConmmunicationSimulate>() }

    //余额不足标识，显示余额不足弹窗
    val notEnoughData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //token
    var agoraToken = ""

    //channelId
    var channelId = ""

    //语音通话ID
    var callId = 0L

    //通话时长
    var duration = 0L

    //创建者ID
    var createUserId = 0L


    /**
     * 创建会话
     */
    fun createConmmunication(userId: Long) {
//        viewModelScope.launch {
//            request({
//                val result = socialService.createCommunication(CreateCommunicationForm(userId)).dataConvert()
//                netcallBeanData.value = result
//            }, {
//                if (it is ResponseError) {
//                    //对方忙
//                    when (it.busiCode) {
//                        1403 -> {
//                            //对方忙
//                            voiceBeanData.value = VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_BUSY)
//                        }
//
//                    }
//
//                    currentVoiceState.value = VOICE_CLOSE
//                }
//            })
//        }
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
     * @param timeOut 是否已超时
     */
    fun refuseVoice(timeOut: Boolean = false) {
        viewModelScope.launch {
            request({
                socialService.netcallRefuse(NetcallIdForm(callId)).dataConvert()
                //挂断当前语音
                currentVoiceState.value = VOICE_CLOSE
                if (timeOut) {
                    voiceBeanData.value = VoiceConmmunicationSimulate(VoiceResultType.CANCEL)
                } else {
                    voiceBeanData.value = VoiceConmmunicationSimulate(VoiceResultType.MINE_REFUSE)
                }
            })
        }
    }

    /**
     * 被叫忙
     */
    fun busy(callId: Long) {
        viewModelScope.launch {
            request({
                socialService.voiceBusy(NetcallIdForm(callId)).dataConvert()
            }, {})
        }
    }

    /**
     * 主叫取消通话
     */
    fun calcelVoice(type: String) {
        viewModelScope.launch {
            request({
                socialService.netcallCancel(NetcallCancelForm(callId, type)).dataConvert()
                if (type == CancelType.Timeout) {
                    ToastUtils.show("对方无应答")
                    voiceBeanData.value = VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_NOT_ACCEPT)
                } else {
                    voiceBeanData.value = VoiceConmmunicationSimulate(VoiceResultType.CANCEL)
                }
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
                val result = socialService.netcallHangUp(NetcallHangUpForm(callId, duration)).dataConvert()
                voiceBeanData.value = VoiceConmmunicationSimulate(
                    VoiceResultType.CONMMUNICATION_FINISH,
                    result.duration,
                    billUserId = result.billUserId,
                    totalBeans = result.totalBeans
                )
            }, {
                voiceBeanData.value = VoiceConmmunicationSimulate(
                    VoiceResultType.CONMMUNICATION_FINISH,
                    duration,
                    totalBeans = 0
                )
            }, { currentVoiceState.value = VOICE_CLOSE })
        }
    }

    /**
     * 标记付费弹窗已经显示过
     */
    fun markFeeRemind() {
        socialService.markFeeRemind().whatEver()
    }

}