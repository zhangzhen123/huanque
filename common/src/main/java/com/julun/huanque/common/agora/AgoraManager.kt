package com.julun.huanque.common.agora

import android.content.Context
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.agora.handler.AgoraEventHandler
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

    fun initAgora(context: Context) {
        try {
            val appId = if (BuildConfig.DEBUG) {
                "78fb9516fe5b4e759c51a5d880e1fd1b"
            } else {
                "aa91c22c10004e66b7a1bee69fbb2ccd"
            }
            mRtcEngine = RtcEngine.create(context, appId,
                mHandler
            )
            //设置直播属性(直播属性可以用与语音)
            mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            ULog.i("AGORA 初始化完成")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}