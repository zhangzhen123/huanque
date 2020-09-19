package com.julun.huanque.manager

import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.jiguang.verifysdk.api.PreLoginListener
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.LocalPreLoginListener
import com.julun.huanque.common.suger.logger

/**
 *@创建者   dong
 *@创建时间 2020/9/18 14:14
 *@描述 一键登录使用
 */
object FastLoginManager {
    //预取号成功的code
    val CODE_PRELOGIN_SUCCESS = 7000
    private var mPreviewCode = 0
    fun getPreviewCode() = mPreviewCode

    var mPreListener : LocalPreLoginListener? = null

    /**
     *预取号
     */
    fun preLogin() {
        //判断是否初始化成功
        if (!JVerificationInterface.isInitSuccess()) {
            //初始化未成功，直接返回
            mPreListener?.preLoginResult(false)
            return
        }

        //判断环境是否可用
        val verifyEnable = JVerificationInterface.checkVerifyEnable(CommonInit.getInstance().getContext())
        if (!verifyEnable) {
            //环境不可用，直接返回
            logger("当前网络环境不支持认证")
            mPreListener?.preLoginResult(false)
            return
        }
        //预取号
        JVerificationInterface.preLogin(
            CommonInit.getInstance().getContext(),
            5000
        ) { code, content ->
            //预取号结果
            mPreviewCode = code
            mPreListener?.preLoginResult(code == CODE_PRELOGIN_SUCCESS)
            logger("onResult [$code] message=$content")
        }
    }

}