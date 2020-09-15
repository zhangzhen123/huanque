package com.julun.platform_push.receiver

import android.content.Context
import com.vivo.push.model.UPSNotificationMessage

/**
 *@创建者   dong
 *@创建时间 2020/9/15 14:10
 *@描述 兼容 vivo push和融云冲突 使用的Receiver
 */
class VivoPushReceiver : io.rong.push.platform.vivo.VivoPushMessageReceiver() {

    override fun onNotificationMessageClicked(context: Context?, message: UPSNotificationMessage?) {
        super.onNotificationMessageClicked(context, message)
    }

    override fun onReceiveRegId(context: Context?, token: String?) {
        super.onReceiveRegId(context, token)
    }

}