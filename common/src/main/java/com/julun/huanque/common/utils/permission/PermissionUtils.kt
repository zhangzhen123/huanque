package com.julun.huanque.common.utils.permission

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import java.lang.reflect.Field
import java.lang.reflect.Method

object PermissionUtils {

    private val logger = ULog.getLogger(this.javaClass.name)!!

    //判断单个权限是否存在
    fun checkSinglePermission(context: Context, permissionStr: String) =
        ContextCompat.checkSelfPermission(
            context,
            permissionStr
        ) == PackageManager.PERMISSION_GRANTED

    /***
     * 检查悬浮窗开启权限
     * @param context
     * @return
     */
    fun checkFloatPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return true
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                var cls = Class.forName("android.content.Context")
                val declaredField: Field = cls.getDeclaredField("APP_OPS_SERVICE")
                declaredField.setAccessible(true)
                var obj: Any? = declaredField.get(cls) as? String ?: return false
                val str2 = obj as String
                obj = cls.getMethod("getSystemService", String::class.java).invoke(context, str2)
                cls = Class.forName("android.app.AppOpsManager")
                val declaredField2: Field = cls.getDeclaredField("MODE_ALLOWED")
                declaredField2.setAccessible(true)
                val checkOp: Method = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String::class.java)
                val result = checkOp.invoke(obj, 24, Binder.getCallingUid(), context.packageName) as Int
                result == declaredField2.getInt(cls)
            } catch (e: Exception) {
                false
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appOpsMgr: AppOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                    ?: return false
                val mode: Int = appOpsMgr.checkOpNoThrow(
                    "android:system_alert_window", Process.myUid(), context
                        .packageName
                )
//                Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED
                Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED
            } else {
                Settings.canDrawOverlays(context)
            }
        }
    }

    /**
     * 检查是否有拍照相关权限
     * @author WanZhiYuan
     * @date 2020/04/22
     */
    fun checkPhotoPermission(context: Activity, callback: (Boolean) -> Unit = {}) {
        val rxPermissions = RxPermissions(context)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        callback(true)
                    }
                    permission.shouldShowRequestPermissionRationale -> { // Oups permission denied
                        ToastUtils.show("权限无法获取")
                        callback(false)
                    }
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                        callback(false)
                    }
                }
            }
    }
}