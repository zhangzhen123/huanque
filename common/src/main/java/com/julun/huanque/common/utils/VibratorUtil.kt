package com.julun.huanque.common.utils

import android.app.Service
import android.content.Context
import android.os.Vibrator
import com.julun.huanque.common.init.CommonInit

/**
 *@创建者   dong
 *@创建时间 2020/9/27 13:59
 *@描述 手机震动工具类
 */
object VibratorUtil {

    fun Vibrate(milliseconds: Long) {
        val act = CommonInit.getInstance().getCurrentActivity() ?: return
        val vib = act.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(milliseconds)
    }

}