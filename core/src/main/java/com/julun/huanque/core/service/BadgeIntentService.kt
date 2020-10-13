package com.julun.huanque.core.service

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.julun.huanque.core.R
import me.leolin.shortcutbadger.ShortcutBadger


/**
 *@创建者   dong
 *@创建时间 2020/10/13 19:33
 *@描述 显示notification的service
 */
class BadgeIntentService(val name: String = "BadgeIntentService") : IntentService(name) {
    private val NOTIFICATION_CHANNEL = "com.julun.huanque"
    private var mNotificationManager: NotificationManager? = null

    private var notificationId = 0


    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }


    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val badgeCount = intent.getIntExtra("badgeCount", 0)

//            mNotificationManager.cancel(notificationId);
            mNotificationManager!!.cancelAll()
            if (badgeCount == 0) {
                return
            }
            notificationId++
            val builder = Notification.Builder(applicationContext)
                .setContentTitle("NewMessages")
                .setContentText("You've received $badgeCount messages.")
                .setSmallIcon(R.mipmap.ic_launcher)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupNotificationChannel()
                builder.setChannelId(NOTIFICATION_CHANNEL)
            }
            val notification: Notification = builder.build()
            ShortcutBadger.applyNotification(applicationContext, notification, badgeCount)
            mNotificationManager!!.notify(notificationId, notification)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL, "ShortcutBadger Sample",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mNotificationManager!!.createNotificationChannel(channel)
    }
}