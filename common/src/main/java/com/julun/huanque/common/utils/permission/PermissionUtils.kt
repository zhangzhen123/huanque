package com.julun.huanque.common.utils.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions

object PermissionUtils {

    private val logger = ULog.getLogger(this.javaClass.name)!!

    //判断单个权限是否存在
    fun checkSinglePermission(context: Context, permissionStr: String) =
        ContextCompat.checkSelfPermission(
            context,
            permissionStr
        ) == PackageManager.PERMISSION_GRANTED

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