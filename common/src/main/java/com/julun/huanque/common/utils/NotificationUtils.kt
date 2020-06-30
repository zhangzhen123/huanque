package com.julun.huanque.common.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import java.util.logging.Logger


/**
 * Created by WanZhiYuan on 2019/04/13 0013.
 * 通知栏工具类
 */
object NotificationUtils {

    private val logger: Logger = ULog.getLogger("NotificationUtils")

    /**
     * 获取当前通知栏开启状态
     * Api 19 以前是没有通知管理的，默认都是开启，不用管。如果关闭了，那也没辙了。
     * Api 19 – 24 虽加入了通知管理功能，但没有开放检测是否开启了通知的接口，开发者只能用反射来获取权限值。
     * Api 24 以上，NotifyManager 提供了 areNotificationsEnabled（）方法检测通知权限。
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        context ?: return false
        when {
            Build.VERSION.SDK_INT >= 24 -> return NotificationManagerCompat.from(context).areNotificationsEnabled()
            Build.VERSION.SDK_INT >= 19 -> {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = context.applicationInfo
                val pkg = context.applicationContext.packageName
                val uid = appInfo.uid
                try {
                    val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                    val checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE,
                            Integer.TYPE, String::class.java)
                    val opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")
                    val value = opPostNotificationValue.get(Int::class.java) as Int
                    return checkOpNoThrowMethod.invoke(appOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED
                } catch (e: Exception) {
                    logger.info("Android api ${Build.VERSION.SDK_INT} 获取通知栏开启状态服务失败：${e}")
                }
                return false
            }
            else -> return true
        }
    }

    /**
     * 跳转通知栏
     * Api 19 以下只能跳转到设置页，因为那个时候没有跳转到通知栏的api
     * Api 21 以下只能跳转app详情页
     * 部分机型有自己的手机管家来控制权限的开启，这一些没有办法处理了
     */
    fun gotoNotificationSetting(activity: Activity): Intent {
        val intent = Intent()
        activity ?: return intent
        val appInfo = activity.applicationInfo
        val pkg = activity.applicationContext.packageName
        val uid = appInfo.uid
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid)
                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", pkg)
                intent.putExtra("app_uid", uid)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$pkg")
            }
            else -> intent.action = Settings.ACTION_SETTINGS
        }
        return intent
    }
}
