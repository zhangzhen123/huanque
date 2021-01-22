package com.julun.huanque.common.helper

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.MD5Util
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ULog


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
    fun getAppMetaData(key: String, context: Context): String {
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
        if (StringHelper.isHttpUrl(postfix)) {
            return postfix
        }
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

    /**
     * 全局通用的点击跳转操作
     */
    fun openTouch(touchType: String, touchValue: String = "", activity: Activity? = null) {
        when (touchType) {
            PushDataActionType.EditMineHomePage -> {
                ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation(activity)
            }
            PushDataActionType.Url -> {
                logger("开始跳转到网页:" + touchValue)
                val extra = Bundle()
                extra.putString(BusiConstant.WEB_URL, touchValue)
                extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY).with(extra).navigation(activity)
            }
            PushDataActionType.LiveRoom -> {
                val programId: Long = touchValue.toLongOrNull() ?: return
                logger("开始跳转到房间:$programId")
                val bundle = Bundle().apply {
                    putLong(IntentParamKey.PROGRAM_ID.name, programId)
                    putString(ParamConstant.FROM, PlayerFrom.Push)
                }
                ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(bundle).navigation(activity)
            }
            PushDataActionType.MineHomePage -> {
                val bundle = Bundle().apply {
                    var userId = touchValue.toLongOrNull()
                    if (userId != null) {
                        userId = SessionUtils.getUserId()
                    }
                    if (userId != null) {
                        putLong(ParamConstant.UserId, userId)
                    }
                }
                ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
            }
            PushDataActionType.AnchorCertPage -> {

            }
            PushDataActionType.FriendNotice -> {
                ARouter.getInstance().build(ARouterConstant.SysMsgActivity).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()
            }
            PushDataActionType.SystemNotice -> {
                ARouter.getInstance().build(ARouterConstant.SysMsgActivity).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()

            }
            PushDataActionType.OfficialCertPage -> {
            }
            PushDataActionType.PlumFlower -> {
                ARouter.getInstance().build(ARouterConstant.PLUM_FLOWER_ACTIVITY).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()
            }
            PushDataActionType.PrivateChat -> {
                val targetId = touchValue.toLongOrNull() ?: return
                ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(Bundle().apply {
                    this.putLong(ParamConstant.TARGET_USER_ID, targetId)
                }).navigation()
            }
            PushDataActionType.AccostWords -> {
                ARouter.getInstance().build(ARouterConstant.USE_FUL_WORD_ACTIVITY).navigation()
            }
            PushDataActionType.FateCome -> {
                //缘分页面
                ARouter.getInstance().build(ARouterConstant.YUAN_FEN_ACTIVITY).navigation()
            }
            PushDataActionType.Message -> {
                //消息列表
                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).with(Bundle().apply {
                    putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
                }).navigation()
            }
            PushDataActionType.FriendHome -> {
                //交友页面
                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).with(Bundle().apply {
                    putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX)
                }).navigation()
            }
            PushDataActionType.MyLikeTag -> {
                //我喜欢的标签
                ARouter.getInstance().build(ARouterConstant.MY_TAGS_ACTIVITY).with(Bundle().apply {
                    putString(ManagerTagCode.MANAGER_PAGER_TYPE, MyTagType.LIKE)
                }).navigation()
            }
            PushDataActionType.MyAuthTag -> {
                //我拥有的标签
                ARouter.getInstance().build(ARouterConstant.MY_TAGS_ACTIVITY).with(Bundle().apply {
                    putString(ManagerTagCode.MANAGER_PAGER_TYPE, MyTagType.AUTH)
                }).navigation()
            }
            else -> {
                logger("没有操作可做")
            }
        }

    }
}
