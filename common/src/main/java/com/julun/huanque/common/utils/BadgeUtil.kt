//package com.julun.huanque.common.utils
//
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import java.util.*
//
///**
// *@创建者   dong
// *@创建时间 2020/10/13 16:16
// *@描述
// */
//object BadgeUtil {
//    /**
//     * 设置Badge 目前支持Launcher
//     */
//    fun setBadgeCount(context: Context, count: Int, iconResId: Int) {
//        var count = count
//        count = if (count <= 0) {
//            0
//        } else {
//            Math.max(0, Math.min(count, 99))
//        }
//        if (Build.MANUFACTURER.toLowerCase(Locale.ROOT) == "xiaomi") {
//            setBadgeOfMIUI(context, count, iconResId)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT) == "sony") {
//            setBadgeOfSony(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("samsung") ||
//            Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("lg")
//        ) {
//            setBadgeOfSumsung(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("htc")) {
//            setBadgeOfHTC(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("nova")) {
//            setBadgeOfNova(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("OPPO")) { //oppo
//            //setBadgeOfOPPO(context, count);
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("LeMobile")) { //乐视
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("vivo")) {
//            setBadgeOfVIVO(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("HUAWEI") || Build.BRAND.equals("Huawei") || Build.BRAND.equals("HONOR")) { //华为
//            setHuaweiBadge(context, count)
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("")) { //魅族
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("")) { //金立
//        } else if (Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("")) { //锤子
//        } else {
//            //Toast.makeText(context, "Not Found Support Launcher", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    /**
//     * 设置MIUI的Badge
//     */
//    private fun setBadgeOfMIUI(context: Context, count: Int, iconResId: Int) {
//        val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val builder: NotificationCompat.Builder = Builder(context)
//        builder.setContentTitle("标题").setContentText("消息正文").setSmallIcon(iconResId)
//        val notification: Notification = builder.build()
//        try {
//            val field: Field = notification.getClass().getDeclaredField("extraNotification")
//            val extraNotification: Any = field.get(notification)
//            val method: Method = extraNotification.javaClass.getDeclaredMethod("setMessageCount", Int::class.javaPrimitiveType)
//            method.invoke(extraNotification, count)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        mNotificationManager.notify(0, notification)
//    }
//
//    /**
//     * 设置索尼的Badge
//     * 需添加权限：<uses-permission android:name="com.sonyericsson.home.permission.BROADCAST_BADGE"></uses-permission>
//     */
//    private fun setBadgeOfSony(context: Context, count: Int) {
//        val launcherClassName = getLauncherClassName(context) ?: return
//        var isShow = true
//        if (count == 0) {
//            isShow = false
//        }
//        val localIntent = Intent()
//        localIntent.setAction("com.sonyericsson.home.action.UPDATE_BADGE")
//        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", isShow) //是否显示
//        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName) //启动页
//        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", count.toString()) //数字
//        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName()) //包名
//        context.sendBroadcast(localIntent)
//    }
//
//    /**
//     * 设置三星的Badge\设置LG的Badge
//     */
//    private fun setBadgeOfSumsung(context: Context, count: Int) {
//        // 获取你当前的应用
//        val launcherClassName = getLauncherClassName(context) ?: return
//        val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
//        intent.putExtra("badge_count", count)
//        intent.putExtra("badge_count_package_name", context.getPackageName())
//        intent.putExtra("badge_count_class_name", launcherClassName)
//        context.sendBroadcast(intent)
//    }
//
//    /**
//     * 设置HTC的Badge
//     */
//    private fun setBadgeOfHTC(context: Context, count: Int) {
//        val intentNotification = Intent("com.htc.launcher.action.SET_NOTIFICATION")
//        val localComponentName = ComponentName(context.getPackageName(), getLauncherClassName(context))
//        intentNotification.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString())
//        intentNotification.putExtra("com.htc.launcher.extra.COUNT", count)
//        context.sendBroadcast(intentNotification)
//        val intentShortcut = Intent("com.htc.launcher.action.UPDATE_SHORTCUT")
//        intentShortcut.putExtra("packagename", context.getPackageName())
//        intentShortcut.putExtra("count", count)
//        context.sendBroadcast(intentShortcut)
//    }
//
//    /**
//     * 设置Nova的Badge
//     */
//    private fun setBadgeOfNova(context: Context, count: Int) {
//        val contentValues = ContentValues()
//        contentValues.put("tag", context.getPackageName().toString() + "/" + getLauncherClassName(context))
//        contentValues.put("count", count)
//        context.getContentResolver().insert(
//            Uri.parse("content://com.teslacoilsw.notifier/unread_count"),
//            contentValues
//        )
//    }
//
//    /**
//     * 设置vivo的Badge :vivoXplay5 vivo x7无效果
//     */
//    private fun setBadgeOfVIVO(context: Context, count: Int) {
//        try {
//            val intent = Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM")
//            intent.putExtra("packageName", context.getPackageName())
//            val launchClassName: String =
//                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent().getClassName()
//            intent.putExtra("className", launchClassName)
//            intent.putExtra("notificationNum", count)
//            context.sendBroadcast(intent)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    /**
//     * 设置oppo的Badge :oppo角标提醒目前只针对内部软件还有微信、QQ开放，其他的暂时无法提供
//     */
//    private fun setBadgeOfOPPO(context: Context, count: Int) {
//        try {
//            val extras = Bundle()
//            extras.putInt("app_badge_count", count)
//            context.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", count.toString(), extras)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    /**
//     * 设置华为的Badge :mate8 和华为 p7,honor畅玩系列可以,honor6plus 无效果
//     */
//    fun setHuaweiBadge(context: Context, count: Int) {
//        try {
//            val bundle = Bundle()
//            bundle.putString("package", context.getPackageName())
//            val launchClassName: String =
//                context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent().getClassName()
//            bundle.putString("class", launchClassName)
//            bundle.putInt("badgenumber", count)
//            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bundle)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun setBadgeOfMadMode(context: Context, count: Int, packageName: String?, className: String?) {
//        val intent = Intent("android.intent.action.BADGE_COUNT_UPDATE")
//        intent.putExtra("badge_count", count)
//        intent.putExtra("badge_count_package_name", packageName)
//        intent.putExtra("badge_count_class_name", className)
//        context.sendBroadcast(intent)
//    }
//
//    /**
//     * 重置Badge
//     */
//    fun resetBadgeCount(context: Context, iconResId: Int) {
//        setBadgeCount(context, 0, iconResId)
//    }
//
//    fun getLauncherClassName(context: Context): String {
//        val packageManager: PackageManager = context.getPackageManager()
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.setPackage(context.getPackageName())
//        intent.addCategory(Intent.CATEGORY_LAUNCHER)
//        var info: ResolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
//        if (info == null) {
//            info = packageManager.resolveActivity(intent, 0)
//        }
//        return info.activityInfo.name
//    }
//}