package com.julun.huanque.agora.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/7/2 11:56
 *@描述 语音聊天ViewModel
 */
class VoiceChatViewModel : BaseViewModel() {

    //当前语音状态
    val currentVoiceState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

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


}