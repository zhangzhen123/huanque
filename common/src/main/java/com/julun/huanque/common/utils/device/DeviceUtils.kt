package com.julun.huanque.common.utils.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.telephony.TelephonyManager
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.init.CommonInit
import java.lang.Exception


@SuppressLint("MissingPermission")
/**
 * 需要配合获取权限使用 否则6.0会报错   2017/5/18 已经弃用 因为权限需要拨打电话读取电话信息 太昂贵 不值得
 * Created by nirack on 17-5-11.
 */

object DeviceUtils {
    private val CHINA_MOBILE_CODES = arrayOf("46000", "46002", "46007")   //移动
    private val CHINA_UNICOM_CODES = "46001"                            //联通
    private val CHINA_TELECOMMUNICATIONS_CODES = "46003"                //电信

    private val telManager by lazy {
        CommonInit.getInstance().getApp()
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }


    val deviceId: String by lazy { telManager.deviceId }

    /**
     * 运营商
     */
    val operator: String
        get() {
            if (telManager.simState == TelephonyManager.SIM_STATE_READY) {
                return telManager.simOperatorName
            } else {
                return ""
            }
            /*
            val operator = telManager.simOperator
            if (operator != null) {
                if(operator in CHINA_MOBILE_CODES) return "中国移动"
                if(operator.equals(CHINA_UNICOM_CODES)) return "中国联通"
                if(operator.equals(CHINA_TELECOMMUNICATIONS_CODES)) return "中国电信"
            }
            return "未知"*/
        }

    /**
     * 这是一个用于测试的方法......
     */
    private fun getHandSetInfo(): String {
        var handSetInfo = "手机型号:" + android.os.Build.MODEL +
                "\n设备名称:" + OSUtils.getRomType() +
                "\n系统版本:" + android.os.Build.VERSION.RELEASE +
                "\n产品型号:" + android.os.Build.PRODUCT +
                "\n版本显示:" + android.os.Build.DISPLAY +
//                     "\n基带版本:" + reflect() +
                "\n系统定制商:" + android.os.Build.BRAND +
                "\n设备参数:" + android.os.Build.DEVICE +
                "\n开发代号:" + android.os.Build.VERSION.CODENAME +
                "\nSDK版本号:" + android.os.Build.VERSION.SDK_INT +
                "\nCPU类型:" + android.os.Build.CPU_ABI +
                "\n硬件类型:" + android.os.Build.HARDWARE +
                "\n主机:" + android.os.Build.HOST +
                "\n生产ID:" + android.os.Build.ID +
                "\nROM制造商:" + android.os.Build.MANUFACTURER // 这行返回的是rom定制商的名称
        return handSetInfo
    }

    /**
     * 获取设备IMEI，需要权限
     */
    fun getIMEI(): String {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            //Android Q 及以上系统
            return ""
        }
        return if (ContextCompat.checkSelfPermission(
                CommonInit.getInstance().getApp(),
                Manifest.permission.READ_PHONE_STATE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            //拥有读取手机状态权限，获取唯一标识
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telManager.imei ?: ""
            } else {
                telManager.deviceId ?: ""
            }
        } else {
            ""
        }

    }

    @SuppressLint("HardwareIds")
    fun getUdid(): String {
        val androidID = Settings.Secure.getString(
            CommonInit.getInstance().getApp().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val udid = androidID + Build.SERIAL
        return udid ?: "unknown"    //有用 不要删
    }

    /**
     * 不校验权限的情况下获取唯一标识
     * 兼容某些机型，不能立即显示权限弹窗的问题
     */
    fun getUdidNoCheckPermission() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telManager.imei
            } else {
                telManager.deviceId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun getCpuAbis(): String {
        var abis = arrayOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS
        } else {
            abis = arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        }
        val abiStr = StringBuilder()
        abis.forEachIndexed { index, s ->
            abiStr.append(s)
            if (index < abis.size - 1)
                abiStr.append(',')
        }

        ULog.i("当前的cpu架构支持：$abiStr")
        return abiStr.toString()
    }

}
