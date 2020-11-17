package com.julun.huanque.common.manager

import android.content.Intent
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.service.VoiceFloatingService
import com.julun.huanque.common.suger.logger

/**
 *@创建者   dong
 *@创建时间 2020/11/17 15:27
 *@描述 语音悬浮窗管理类
 */
object VoiceFloatingManager {
    /**
     * 显示悬浮窗
     * @param playInfo 播放地方
     * @param programId 直播间Id
     * @param bgPic 背景图
     */
    fun showFloatingView() {
        CommonInit.getInstance().getCurrentActivity()?.let { act ->
            val intent = Intent(act, VoiceFloatingService::class.java)
            //使用Intent传值
            act.startService(intent)
        }
    }

    /**
     * 关闭悬浮窗
     */
    fun hideFloatingView() {
        logger("关闭悬浮窗")
        CommonInit.getInstance().getCurrentActivity()?.let { act ->
            val intent = Intent(act, VoiceFloatingService::class.java)
            act.stopService(intent)
        }

    }
}