package com.julun.huanque.core.manager

import android.content.Intent
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.core.service.FloatingService

/**
 *@创建者   dong
 *@创建时间 2020/8/5 17:26
 *@描述 悬浮窗管理类
 */
object FloatingManager {

    /**
     * 显示悬浮窗
     * @param playInfo 播放地方
     * @param programId 直播间Id
     * @param bgPic 背景图
     */
    fun showFloatingView(playInfo: String, programId: Long, bgPic: String, vertical: Boolean = false, draft: String = "") {
        CommonInit.getInstance().getCurrentActivity()?.let { act ->
            val intent = Intent(act, FloatingService::class.java)
            intent.putExtra(ParamConstant.PROGRAM_ID, programId)
            intent.putExtra(ParamConstant.PLAY_INFO, playInfo)
            intent.putExtra(ParamConstant.PIC, bgPic)
            intent.putExtra(ParamConstant.FLOATING_VERTICAL, vertical)
            intent.putExtra(ParamConstant.Program_Draft, draft)
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
            val intent = Intent(act, FloatingService::class.java)
            act.stopService(intent)
        }

    }

}