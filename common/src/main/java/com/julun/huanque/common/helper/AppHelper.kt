package com.julun.huanque.common.helper

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import androidx.core.content.ContextCompat.startActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.R
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.events.RHVerifyResult
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.MD5Util
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity


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
            TouchTypeConstants.EditMineHomePage -> {
                ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation(activity)
            }
            TouchTypeConstants.ACTION_URL -> {
                logger("开始跳转到网页:" + touchValue)
                val extra = Bundle()
                extra.putString(BusiConstant.WEB_URL, touchValue)
                extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY).with(extra).navigation(activity)
            }
            TouchTypeConstants.LiveRoom -> {
                val programId: Long = touchValue.toLongOrNull() ?: return
                logger("开始跳转到房间:$programId")
                val bundle = Bundle().apply {
                    putLong(IntentParamKey.PROGRAM_ID.name, programId)
                    putString(ParamConstant.FROM, PlayerFrom.Push)
                }
                ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(bundle).navigation(activity)
            }
            TouchTypeConstants.MineHomePage -> {
                val bundle = Bundle().apply {
                    var userId = touchValue.toLongOrNull()
                    if (userId == null) {
                        userId = SessionUtils.getUserId()
                    }
                    putLong(ParamConstant.UserId, userId)
                }
                ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
            }
            TouchTypeConstants.AnchorCertPage -> {

            }
            TouchTypeConstants.FriendNotice -> {
                ARouter.getInstance().build(ARouterConstant.SysMsgActivity).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()
            }
            TouchTypeConstants.SystemNotice -> {
                ARouter.getInstance().build(ARouterConstant.SysMsgActivity).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()

            }
            TouchTypeConstants.OfficialCertPage -> {
            }
            TouchTypeConstants.PlumFlower -> {
                ARouter.getInstance().build(ARouterConstant.PLUM_FLOWER_ACTIVITY).with(Bundle().apply {
                    this.putString(ParamConstant.TYPE, touchValue)
                }).navigation()
            }
            TouchTypeConstants.PrivateChat -> {
                val targetId = touchValue.toLongOrNull() ?: return
                ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(Bundle().apply {
                    this.putLong(ParamConstant.TARGET_USER_ID, targetId)
                }).navigation()
            }
            TouchTypeConstants.AccostWords -> {
                ARouter.getInstance().build(ARouterConstant.USE_FUL_WORD_ACTIVITY).navigation()
            }
            TouchTypeConstants.FateCome -> {
                //缘分页面
                ARouter.getInstance().build(ARouterConstant.YUAN_FEN_ACTIVITY).navigation()
            }
            TouchTypeConstants.Message -> {
                //消息列表
                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).with(Bundle().apply {
                    putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
                }).navigation()
            }
            TouchTypeConstants.FriendHome -> {
                //交友页面
                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).with(Bundle().apply {
                    putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX)
                }).navigation()
            }
            TouchTypeConstants.MyLikeTag -> {
                //我喜欢的标签
                ARouter.getInstance().build(ARouterConstant.MY_TAGS_ACTIVITY).with(Bundle().apply {
                    putString(ManagerTagCode.MANAGER_PAGER_TYPE, MyTagType.LIKE)
                }).navigation()
            }
            TouchTypeConstants.MyAuthTag -> {
                //我拥有的标签
                ARouter.getInstance().build(ARouterConstant.MY_TAGS_ACTIVITY).with(Bundle().apply {
                    putString(ManagerTagCode.MANAGER_PAGER_TYPE, MyTagType.AUTH)
                }).navigation()
            }
            TouchTypeConstants.UserTagPicPreview -> {
                val strs = touchValue.split(",")
                val useId = strs.getOrNull(0)?.toLongOrNull() ?: return
                val tagId = strs.getOrNull(1)?.toIntOrNull() ?: return
                //我拥有的标签
                ARouter.getInstance().build(ARouterConstant.TAG_USER_PICS_ACTIVITY).with(Bundle().apply {
                    putLong(ManagerTagCode.TAG_INFO, useId)
                    putInt(IntentParamKey.USER_ID.name, tagId)
                }).navigation()
            }
            TouchTypeConstants.AuthTagPic -> {
                val strs = touchValue.split(",")
                val useId = strs.getOrNull(0)?.toLongOrNull() ?: return
                val tagId = strs.getOrNull(1)?.toIntOrNull() ?: return
                //我拥有的标签
                val tag = UserTagBean(tagId = tagId)
                ARouter.getInstance().build(ARouterConstant.TAG_PICS_ACTIVITY).with(Bundle().apply {
                    putLong(IntentParamKey.USER_ID.name, useId)
                    putSerializable(ManagerTagCode.TAG_INFO, tag)
                }).navigation()
            }
            TouchTypeConstants.MySign -> {
                //个性签名
                ARouter.getInstance().build(ARouterConstant.UpdateSignActivity).navigation()
            }
            TouchTypeConstants.Voice -> {
                //语音签名
                ARouter.getInstance().build(ARouterConstant.VOICE_SIGN_ACTIVITY).navigation()
            }
            TouchTypeConstants.HomeTown -> {
                //家乡
                ARouter.getInstance().build(ARouterConstant.HomeTownActivity).navigation()
            }
            TouchTypeConstants.Birthday -> {
                //生日
                ARouter.getInstance().build(ARouterConstant.UpdateBirthdayActivity).navigation()
            }
            TouchTypeConstants.Figure -> {
                //身材
                ARouter.getInstance().build(ARouterConstant.FigureActivity).navigation()
            }
            TouchTypeConstants.School -> {
                //学校
                ARouter.getInstance().build(ARouterConstant.SchoolActivity).navigation()
            }
            TouchTypeConstants.Professional -> {
                //职业
                ARouter.getInstance().build(ARouterConstant.ProfessionActivity).navigation()
            }
            TouchTypeConstants.PostDetail -> {
                //动态详情
                try {
                    val bundle = Bundle().apply {
                        putLong(IntentParamKey.POST_ID.name, touchValue.toLong())
                    }
                    ARouter.getInstance().build(ARouterConstant.DYNAMIC_DETAIL_ACTIVITY).with(bundle).navigation()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            TouchTypeConstants.AuthTagDetail -> {

                val tagID = touchValue.toIntOrNull() ?: return
                val bundle = Bundle()
                bundle.putInt(ManagerTagCode.TAG_INFO, tagID)
                bundle.putBoolean("isMe", true)
                ARouter.getInstance().build(ARouterConstant.AUTH_TAG_PIC_ACTIVITY).with(bundle).navigation()

            }
            TouchTypeConstants.RealHead -> {
                //真人
                val service = ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                    .navigation() as? IRealNameService ?: return
                val act = activity ?: CommonInit.getInstance().getCurrentActivity()
                act ?: return
                service.checkRealHead { e ->
                    if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                        MyAlertDialog(act, false).showAlertWithOKAndCancel(
                            e.busiMessage.toString(),
                            title = "修改提示",
                            okText = "修改头像",
                            noText = "取消",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation(activity)
                            })
                        )
                    }
                }

//                service?.startRealHead(CommonInit.getInstance().getCurrentActivity() ?: return, object : RealNameCallback {
//                    override fun onCallback(status: String, des: String, percent: Int?) {
//                        EventBus.getDefault().post(RHVerifyResult(status))
//                        when (status) {
//                            RealNameConstants.TYPE_SUCCESS -> {
//                                //认证成功
//                                ToastUtils.show(des)
//
//                            }
//                            RealNameConstants.TYPE_FAIL, RealNameConstants.TYPE_ERROR -> {
//                                //认证失败 or 认证网络请求异常
//                                ToastUtils.show(des)
//                            }
//                            RealNameConstants.TYPE_CANCEL -> {
//                                //认证取消
//                                ToastUtils.show(
//                                    if (des.isNotEmpty()) {
//                                        des
//                                    } else {
//                                        "认证取消"
//                                    }
//                                )
//                            }
//                        }
//                    }
//                })
            }

            else -> {
                logger("没有操作可做")
            }
        }

    }

    /**
     * 获取用户图标  官方  真人  实名
     * @param realProple 真人
     * @param realName 实名
     * @param userType 用户类型
     */
    fun getUserIcon(realPeople: Boolean, realName: Boolean, userType: String): Int? {
        if (userType == UserType.Manager) {
            //官方
            return R.mipmap.icon_guan_home_page
        }
        if (realName) {
            return R.mipmap.icon_real_name_home_page
        }
        if (realPeople) {
            return R.mipmap.icon_real_people_home_page
        }
        return null
    }

}
