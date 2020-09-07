package com.julun.huanque.common.helper

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.constant.EnvironmentAddress
import com.julun.huanque.common.constant.MsgTag
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.MD5Util


/**
 *@Anchor zhangzhen
 *@Date 2019/7/22 22:39
 *@Description
 *
 **/

object AppHelper {
    //stetho是否准备好
    fun isStethoPresent(): Boolean {
        try {
            Class.forName("com.facebook.stetho.Stetho")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }

    }

    /**
     * 获取application中指定的meta-data
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
     */
    fun getAppMetaData(key: String): String {
        if (TextUtils.isEmpty(key)) {
            return ""
        }
        var resultData: String? = null
        try {
            val packageManager = CommonInit.getInstance().getApp().packageManager
            if (packageManager != null) {
                val applicationInfo = packageManager.getApplicationInfo(
                    CommonInit.getInstance().getApp().packageName,
                    PackageManager.GET_META_DATA
                )
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key)
                    }
                }

            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return resultData ?: ""
    }

    /**
     * 获取application中指定的meta-data
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
     */
    fun getAppMetaData(key: String,context: Context): String {
        if (TextUtils.isEmpty(key)) {
            return ""
        }
        var resultData: String? = null
        try {
            val packageManager = context.packageManager
            if (packageManager != null) {
                val applicationInfo = packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key)
                    }
                }

            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return resultData ?: ""
    }
    //获取当前版本号
    fun getAppVersionName(): String? {
        var versionName = ""
        try {
            val packageManager = CommonInit.getInstance().getApp().packageManager
            val packageInfo =
                packageManager.getPackageInfo(CommonInit.getInstance().getApp().packageName, 0)
            versionName = packageInfo.versionName
            if (TextUtils.isEmpty(versionName)) {
                return ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return versionName
    }

    /**
     * 获取application中指定的meta-data
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回默认值
     */
    fun getAppMetaBooleanData(key: String, default: Boolean = false): Boolean {
        if (TextUtils.isEmpty(key)) {
            return default
        }
        var resultData: Boolean = default
        try {
            val packageManager = CommonInit.getInstance().getApp().packageManager
            if (packageManager != null) {
                val applicationInfo = packageManager.getApplicationInfo(
                    CommonInit.getInstance().getApp().packageName,
                    PackageManager.GET_META_DATA
                )
                if (applicationInfo != null) {
                    if (applicationInfo!!.metaData != null) {
                        resultData = applicationInfo!!.metaData.getBoolean(key, default)
                    }
                }

            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return resultData
    }

    fun isMainProcess(context: Context): Boolean {
        val processName =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.runningAppProcesses
                ?.firstOrNull { it.pid == Process.myPid() }?.processName
        if (context.applicationInfo.packageName == processName) {
            return true
        }
        return false
    }

    /**
     * 获取MD5的值
     * 该方法为专用方法，其它功能请勿使用
     */
    fun getMD5(str: String): String {
        val temp =
            MD5Util.encodePassword(str + MsgTag.PREFIX) + MsgTag.TAG
        return MD5Util.encodePassword(temp)
    }

    /**
     * 获取h5对应的域名
     * @param postfix 后缀
     */
    fun getDomainName(postfix: String): String {
        //获取BaseUrl
        val baseUrl = CommonInit.getInstance().getBaseUrl()
        val header = if (TextUtils.isEmpty(baseUrl)) {
            if (BuildConfig.DEBUG) {
                EnvironmentAddress.H5_DEVELOP_NAME
            }
            EnvironmentAddress.H5_PRODUCT_NAME
        } else {
            when {
                baseUrl == BuildConfig.SERVICE_BASE_URL_PRODUCT -> EnvironmentAddress.H5_PRODUCT_NAME
                baseUrl.startsWith(EnvironmentAddress.Environment251) -> EnvironmentAddress.H5_TEST_NAME_OFFICE251
                baseUrl.startsWith(EnvironmentAddress.Environment250) -> EnvironmentAddress.H5_TEST_NAME_OFFICE205
                baseUrl.startsWith(EnvironmentAddress.Environment252) -> EnvironmentAddress.H5_TEST_NAME_OFFICE2
                baseUrl.startsWith(EnvironmentAddress.Environment248) -> EnvironmentAddress.H5_TEST_NAME_OFFICE248
                baseUrl.startsWith(EnvironmentAddress.Environment245) -> EnvironmentAddress.H5_TEST_NAME_OFFICE245
                baseUrl.startsWith(EnvironmentAddress.Environment246) -> EnvironmentAddress.H5_TEST_NAME_OFFICE246
                baseUrl.startsWith(EnvironmentAddress.Environment247) -> EnvironmentAddress.H5_TEST_NAME_OFFICE247
                else -> EnvironmentAddress.H5_PRODUCT_NAME
            }
        }

        val sbResult = StringBuffer()
        sbResult.append(header)
        if (!postfix.startsWith("/")) {
            //后缀没有以“/”开头，添加一个
            sbResult.append("/")
        }
        sbResult.append(postfix)
        //部分协议需要拼接一个字符串
        return sbResult.toString()
    }
}
