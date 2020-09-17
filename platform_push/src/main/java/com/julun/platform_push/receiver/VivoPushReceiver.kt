package com.julun.platform_push.receiver

import android.content.Context
import com.julun.huanque.common.suger.logger
import com.vivo.push.model.UPSNotificationMessage
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/9/15 14:10
 *@描述 兼容 vivo push和融云冲突 使用的Receiver
 */
class VivoPushReceiver : io.rong.push.platform.vivo.VivoPushMessageReceiver() {

    override fun onNotificationMessageClicked(context: Context?, message: UPSNotificationMessage?) {
        logger("onNotificationMessageClicked=${message}")
        super.onNotificationMessageClicked(context, message)
        val pushData = message?.params?.get("appData")
        if (context != null) {
            //vivo会默认启动启动页 导致会打开首页  所以这里一定要延迟打开相应页面 不然会被首页覆盖掉
            Observable.timer(700, TimeUnit.MILLISECONDS).subscribe {
                RPushUtil.parseJson(pushData ?: "", context)
            }

        }
    }

    override fun onReceiveRegId(context: Context?, token: String?) {
        super.onReceiveRegId(context, token)
    }

}