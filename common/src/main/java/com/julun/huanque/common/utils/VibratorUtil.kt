package com.julun.huanque.common.utils

import android.app.Service
import android.content.Context
import android.os.Vibrator

/**
 *@创建者   dong
 *@创建时间 2020/9/27 13:59
 *@描述 手机震动工具类
 */
object VibratorUtil {

    fun Vibrate(activity: Context, milliseconds: Long) {
        val vib = activity.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(milliseconds)
    }

}