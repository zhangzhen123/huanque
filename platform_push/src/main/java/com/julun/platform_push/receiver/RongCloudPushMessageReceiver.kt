package com.julun.platform_push.receiver

import android.content.Context
import com.julun.huanque.common.bean.message.PushAppData
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.JsonUtil
import io.rong.push.PushType
import io.rong.push.notification.PushMessageReceiver
import io.rong.push.notification.PushNotificationMessage
import java.lang.Exception

/**
 *@创建者   dong
 *@创建时间 2019/5/16 11:13
 *@描述 自定义广播接收器
 *    当您的应用处于后台运行或者和融云服务器 disconnect() 的时候，如果收到消息，融云 SDK 会以通知形式提醒您。
 *    所以您还需要自定义一个继承融云 PushMessageReceiver 的广播接收器，用来接收提醒通知。
 */
class RongCloudPushMessageReceiver : PushMessageReceiver() {
    override fun onNotificationMessageArrived(p0: Context?, p1: PushType?, p2: PushNotificationMessage?): Boolean {
        logger("onNotificationMessageArrived p=${p2.toString()}")
        return false // 返回 false, 会弹出融云 SDK 默认通知; 返回 true, 融云 SDK 不会弹通知, 通知需要由您自定义。
    }

    override fun onNotificationMessageClicked(p0: Context?, p1: PushType?, p2: PushNotificationMessage?): Boolean {
        logger("onNotificationMessageClicked p=${p2.toString()}")
        try {
            val pushDate = p2?.pushData
            if (p0 != null) {
                RPushUtil.parseJson(pushDate?:"", p0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true // 返回 false, 会走融云 SDK 默认处理逻辑, 即点击该通知会打开会话列表或会话界面; 返回 true, 则由您自定义处理逻辑。
    }
}