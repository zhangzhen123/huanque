package com.julun.huanque.app.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.init.CommonInit

/**
 * @author zhangzhen
 * *
 * @data 2017/8/18
 *
 * 判断手机的下载器是否可用 不可用提示跳转到相应界面去开启
 */
object DownloadManagerResolver {

    private const val DOWNLOAD_MANAGER_PACKAGE_NAME = "com.android.providers.downloads"

    /**
     * Resolve whether the DownloadManager is enable in current devices.

     * @return true if DownloadManager is enable,false otherwise.
     */
    fun resolve(context: Context): Boolean {
        val enable = resolveEnable(context)
        if (!enable) {
            val alertDialog = createDialog(context)
            alertDialog?.show()
        }
        return enable
    }

    /**
     * Resolve whether the DownloadManager is enable in current devices.

     * @param context
     * *
     * @return true if DownloadManager is enable,false otherwise.
     */
    private fun resolveEnable(context: Context): Boolean {
        val state = context.packageManager
                .getApplicationEnabledSetting(DOWNLOAD_MANAGER_PACKAGE_NAME)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED)
        } else {
            return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER)
        }
    }

    private fun createDialog(context: Context): MyAlertDialog? {
//        val messageTextView = AppCompatTextView(context)
//        messageTextView.textSize = 16f
//        messageTextView.text = "DownloadManager is disabled. Please enable it."
//        return AlertDialog.Builder(context)
//                .setPositiveButton("ok") { dialog, which -> enableDownloadManager(context) }
//                .setCancelable(false)
//                .create()
        val activity = CommonInit.getInstance().getCurrentActivity()
        if (activity != null) {
            val dialog = MyAlertDialog(activity)
            dialog.showAlertWithOKAndCancel("你的手机下载器不可用，是否前往开启吧"
                    , MyAlertDialog.MyDialogCallback(
                    onCancel = {
                    },
                    onRight = {
                        enableDownloadManager(context)
                    }
            ), okText = "前往设置", noText = "取消")
            return dialog
        }
        return null
    }

    /**
     * Start activity to Settings to enable DownloadManager.
     */
    private fun enableDownloadManager(context: Context) {
        try {
            //Open the specific App Info page:
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$DOWNLOAD_MANAGER_PACKAGE_NAME")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()

            //Open the generic Apps page:
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

    }
}
