package com.julun.huanque.common.helper

import com.julun.huanque.common.constant.MetaKey
import com.julun.huanque.common.utils.SPUtils
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
object ChannelCodeHelper{
    private const val H5SID = "h5Sid"
    private const val H5PID = "h5Pid"
    private const val H5ChannelCode = "h5ChannelCode"

    val logger = ULog.getLogger("ChannelHelper")
    //是否需要调用OpenInstall getInstallData
    private const val NEEDINSTALL = "needInstall"
    //存储的渠道号
    private const val CHANNELCODE = "channelCode"
    //存储的参数渠道号
    private const val CHANNELPARAM = "channelParam"
    //活动渠道号
    private const val CHANNELACTIVE = "channelActive"
    //存储的额外参数(包含渠道参数以及其他参数)
    private const val OPENINSTALLAPPDATA = "openInstallAppData"

    //存储的本地的内置渠道号 对于自动安装时老的渠道号会被覆盖 安装前保存老的内置号
    private const val CHANNELNATIVE = "channelNative"

    private const val FORCE_LOGIN = "FORCE_LOGIN" //渠道强制登录的标识字段
    private fun getNeedInstall(): Boolean {
        return SPUtils.getBoolean(NEEDINSTALL, true)
    }

    //渠道号设置
    private fun setChannelCode(channelCode: String) {
        SPUtils.commitString(CHANNELCODE, channelCode)
    }

    //获取渠道号
    fun getChannelCode(): String {
        val cha = SPUtils.getString(CHANNELCODE, "")
//        Log.i("OpenInstallManager","当前的渠道号$cha")
        return cha
    }

    //参数渠道号设置 通过前端传参配置的渠道号
    private fun setChannelParam(channelParams: String) {
        SPUtils.commitString(CHANNELPARAM, channelParams)
    }

    //获取参数渠道号 通过前端传参配置的渠道号
    fun getChannelParam(): String {
        val cha = SPUtils.getString(CHANNELPARAM, "")
//        Log.i("OpenInstallManager","当前的渠道号$cha")
        return cha
    }

    //活动渠道号  通过点击活动页面拉起app传回的渠道号
    private fun setChannelActive(channelActive: String) {
        SPUtils.commitString(CHANNELACTIVE, channelActive)
    }

    //CHANNELACTIVE
    fun getChannelActive(): String {
        return SPUtils.getString(CHANNELACTIVE, "")
    }

    //渠道 额外参数设置
    private fun setOpenInstallData(bindData: String) {
        SPUtils.commitString(OPENINSTALLAPPDATA, bindData)
    }

    //获取渠道 额外参数
    fun getOpenInstallData(): String {
        val data = SPUtils.getString(OPENINSTALLAPPDATA, "")
//        Log.i("OpenInstallManager","渠道额外数据$data")
        return data
    }

    //本地渠道号
    private fun setChannelNative() {
        val channelNative = AppHelper.getAppMetaData(MetaKey.DOWNLOAD_CHANNEL)
        if (channelNative.isNotBlank())
            SPUtils.commitString(CHANNELNATIVE, channelNative)
    }

    //获取本地渠道号
    private fun getChannelNative(): String {
        return SPUtils.getString(CHANNELNATIVE, "")
    }
    private var jExtraChannelCode: String? = null

    //获取外置渠道
    fun getRemoteChannelId(): String {

        if (jExtraChannelCode != null)
            return jExtraChannelCode!!
        val activeCode = getChannelActive()
        val param = getChannelParam()
        val code = getChannelCode()
        jExtraChannelCode = when {
            activeCode.isNotBlank() -> activeCode
            param.isNotBlank() -> param
            code.isNotBlank() -> code
            else -> ""
        }
        return jExtraChannelCode!!
    }


    private var jAppChannelCode: String? = null
    //获取内置渠道 对于安装升级的情况 如果新包有新的内置渠道使用新包的  没有则使用保存的安装前上一版的渠道
    fun getNativeChannelId(): String {

        if (jAppChannelCode != null)
            return jAppChannelCode!!
        val metaChannel = AppHelper.getAppMetaData(MetaKey.DOWNLOAD_CHANNEL)
        val native = getChannelNative()
        jAppChannelCode = when {
            metaChannel.isNotBlank() -> metaChannel
            native.isNotBlank() -> native
            else -> ""
        }
        return jAppChannelCode!!
    }
    /**
     * 外部渠道号
     */
    fun getExternalChannel(): String?{
//        return getRemoteChannelId()
        return "channel"
    }

    /**
     * 内部渠道号
     */
    fun getInnerChannel(): String? {
        return "channel"
//        return getNativeChannelId()
    }
}