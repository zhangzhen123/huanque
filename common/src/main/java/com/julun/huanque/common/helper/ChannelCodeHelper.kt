package com.julun.huanque.common.helper

import android.util.Log
import com.fm.openinstall.OpenInstall
import com.fm.openinstall.listener.AppInstallListener
import com.julun.huanque.common.constant.MetaKey
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ULog

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/27 11:39
 *
 *@Description: 用于获取渠道号  存储和读取当前的场景渠道号
 * todo 等接入openInstall再处理
 */
object ChannelCodeHelper {
    private const val H5SID = "h5Sid"
    private const val H5PID = "h5Pid"
    private const val HqChannelCode = "hqChannelCode"

    val logger = ULog.getLogger("ChannelHelper")

    //是否需要调用OpenInstall getInstallData
    private const val NEEDINSTALL = "needInstall"

    //存储的渠道号
    private const val CHANNELCODE = "channelCode"

    //存储的参数渠道号
    private const val CHANNELPARAM = "channelParam"

//    //活动渠道号
//    private const val CHANNELACTIVE = "channelActive"
//
//    //存储的额外参数(包含渠道参数以及其他参数)
//    private const val OPENINSTALLAPPDATA = "openInstallAppData"

//    //存储的本地的内置渠道号 对于自动安装时老的渠道号会被覆盖 安装前保存老的内置号
//    private const val CHANNELNATIVE = "channelNative"

//    private const val FORCE_LOGIN = "FORCE_LOGIN" //渠道强制登录的标识字段

    /**
     * 获取安装参数
     */
    fun getInstallData() {
        val needInstall = getNeedInstall()
        Log.i("OpenInstallManager", "开始获取渠道号：$needInstall")
        if (needInstall) {
            OpenInstall.getInstall(AppInstallListener { appData, error ->
                if (error == null) {
                    if (appData == null || appData.isEmpty) return@AppInstallListener
                    //获取渠道数据
                    val channelCode = appData.getChannel()
                    //获取个性化安装数据
                    val bindData = appData.getData()
                    setChannelCode(channelCode)

                    var channelParam = ""
                    if (bindData.isNotBlank()) {
                        val map = JsonUtil.toJsonMap(bindData)
                        if (map != null) {
                            channelParam = map[HqChannelCode] as? String ?: ""
                        }
                    }

                    if (channelParam.isNotEmpty()) {
                        setChannelParam(channelParam)
                    }
                    //使用数据后，不想再调用，将needInstall设置为false
                    SharedPreferencesUtils.commitBoolean(NEEDINSTALL, false)
                } else {
                    Log.i("OpenInstallManager", "errorMsg : " + error.toString())
                }

            })

        }
//        //每次启动 保存老的metachannel
//        setChannelNative()
    }

    /**
     * 保存外部渠道HqChannelCode
     */
    fun saveWakeParams(extra: String) {
        if (extra.isEmpty()) {
            return
        }
        val map = JsonUtil.toJsonMap(extra)
        if (map != null) {
            val channelParam = map[HqChannelCode] as? String ?: ""
            if (channelParam.isNotEmpty()) {
                setChannelParam(channelParam)
            }
        }
    }

    private fun getNeedInstall(): Boolean {
        return SharedPreferencesUtils.getBoolean(NEEDINSTALL, true)
    }

    //渠道号设置
    fun setChannelCode(channelCode: String) {
        SharedPreferencesUtils.commitString(CHANNELCODE, channelCode)
    }

    //获取渠道号 channelCode
    fun getChannelCode(): String {
        val cha = SharedPreferencesUtils.getString(CHANNELCODE, "")
//        Log.i("OpenInstallManager","当前的渠道号$cha")
        return cha
    }

    //参数渠道号设置 通过前端传参配置的渠道号  hqChannelCode
    private fun setChannelParam(channelParams: String) {
        SPUtils.commitBoolean(SPParamKey.QueryGuessYouLike, false)
        SharedPreferencesUtils.commitString(CHANNELPARAM, channelParams)
    }

    //获取参数渠道号 通过前端传参配置的渠道号
    fun getChannelParam(): String {
        val cha = SharedPreferencesUtils.getString(CHANNELPARAM, "")
//        Log.i("OpenInstallManager","当前的渠道号$cha")
        return cha
    }

//    //活动渠道号  通过点击活动页面拉起app传回的渠道号
//    private fun setChannelActive(channelActive: String) {
//        SharedPreferencesUtils.commitString(CHANNELACTIVE, channelActive)
//    }

//    //CHANNELACTIVE
//    fun getChannelActive(): String {
//        return SharedPreferencesUtils.getString(CHANNELACTIVE, "")
//    }

//    //渠道 额外参数设置
//    private fun setOpenInstallData(bindData: String) {
//        SharedPreferencesUtils.commitString(OPENINSTALLAPPDATA, bindData)
//    }

//    //获取渠道 额外参数
//    fun getOpenInstallData(): String {
//        val data = SharedPreferencesUtils.getString(OPENINSTALLAPPDATA, "")
////        Log.i("OpenInstallManager","渠道额外数据$data")
//        return data
//    }

//    //本地渠道号
//    private fun setChannelNative() {
//        val channelNative = AppHelper.getAppMetaData(MetaKey.DOWNLOAD_CHANNEL)
//        if (channelNative.isNotBlank())
//            SharedPreferencesUtils.commitString(CHANNELNATIVE, channelNative)
//    }
//
//    //获取本地渠道号
//    private fun getChannelNative(): String {
//        return SharedPreferencesUtils.getString(CHANNELNATIVE, "")
//    }

//    private var jExtraChannelCode: String? = null

//    //获取外置渠道
//    fun getRemoteChannelId(): String {
//
////        if (jExtraChannelCode != null)
////            return jExtraChannelCode!!
////        val activeCode = getChannelActive()
////        val param = getChannelParam()
////        val code = getChannelCode()
////        jExtraChannelCode = when {
////            activeCode.isNotBlank() -> activeCode
////            param.isNotBlank() -> param
////            code.isNotBlank() -> code
////            else -> ""
////        }
////        return jExtraChannelCode!!
//        return getChannelParam()
//    }


    private var jAppChannelCode: String? = null

    //获取内置渠道 对于安装升级的情况 如果新包有新的内置渠道使用新包的  没有则使用保存的安装前上一版的渠道
    fun getNativeChannelId(): String {

//        if (jAppChannelCode != null)
//            return jAppChannelCode!!
//        val metaChannel = AppHelper.getAppMetaData(MetaKey.DOWNLOAD_CHANNEL)
//        val native = getChannelNative()
//        jAppChannelCode = when {
//            metaChannel.isNotBlank() -> metaChannel
//            native.isNotBlank() -> native
//            else -> ""
//        }
        return ""
    }

    /**
     * 外部渠道号
     */
    fun getExternalChannel(): String? {
        return getChannelParam()
//        return "channel"
    }

    /**
     * 内部渠道号
     */
    fun getInnerChannel(): String? {
//        return "channel"
        return AppHelper.getAppMetaData(MetaKey.DOWNLOAD_CHANNEL)
    }
}