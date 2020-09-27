package com.julun.huanque.common.manager

import android.app.Activity
import androidx.core.app.NotificationManagerCompat
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.utils.DateHelper
import com.julun.huanque.common.utils.NotificationUtils
import com.julun.huanque.common.utils.ULog

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/9/18 11:35
 *
 *@Description : 推送检查开启管理器
 *
 */
object NotifyManager {


    /**
     * 检查推送是否开启 没开启就提醒并返回true
     *
     */
    fun checkNotifyOpen(activity: Activity): Boolean {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            MyAlertDialog(activity).showAlertWithOKAndCancel("欢鹊交友会用通知消息不定期给您送福利。请打开系统通知，不让福利溜走。", MyAlertDialog.MyDialogCallback({

            }, {
                val intent = NotificationUtils.gotoNotificationSetting(activity)
                activity.startActivity(intent)
            }),
                    "欢鹊提醒", "马上设置", "不要福利")
            return true
        }
        return false
    }

    /**
     *
     * 周期性的检查是否打开推送 如果需要提醒返回true
     *
     */
    fun checkNotifyPeriodical(activity: Activity) :Boolean{
        val date = StorageHelper.getNotifyRefreshDate()

        val today = DateHelper.formatNow()

        val diff = if (date.isNotEmpty()) {
            DateHelper.getDifferDays(date, today)
        } else {
            -1
        }

        ULog.i("刷新间隔时间：$diff")

        if (diff == -1 || diff >= 7) {
            if (checkNotifyOpen(activity = activity)) {
                StorageHelper.setNotifyRefreshDate(today)
                return true
            }
        }
        return false
    }

    /**
     *
     * 检查是不是版本升级了 如果升级了就检查推送提醒
     *
     */
    fun checkIsNewVersion(activity: Activity) {
        //todo
//        val lastVersion = SessionUtils.getNotifyVersion()
//        val currentVersion = BuildConfig.VERSION_CODE
//        if (lastVersion < currentVersion) {
//            checkNotifyOpen(activity = activity)
//            SessionUtils.setNotifyVersion(currentVersion)
//
//        }
    }
}