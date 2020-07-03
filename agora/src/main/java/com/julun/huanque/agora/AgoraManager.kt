package com.julun.huanque.agora

import android.app.Application
import com.julun.huanque.agora.handler.AgoraEventHandler
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.utils.ULog
import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine

/**
 *@创建者   dong
 *@创建时间 2020/7/2 15:34
 *@描述 声网统一管理类
 */
object AgoraManager {

    var mRtcEngine: RtcEngine? = null
    val mHandler = AgoraEventHandler()

    fun initAgora(app: Application) {
        try {
//            val appId = if (BuildConfig.DEBUG) {
//                "fbc90bda63d84f29b514dceb0ce0bc2b"
//            } else {
//                "ac6d4391e1534b6482327e4baea03e08"
//            }
            val appId = "78fb9516fe5b4e759c51a5d880e1fd1b"
            mRtcEngine = RtcEngine.create(app.applicationContext, appId, mHandler)
            //设置直播属性(直播属性可以用与语音)
            mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            ULog.i("AGORA 初始化完成")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}