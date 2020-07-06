package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.jiguang.verifysdk.api.JVerificationInterface
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.android.synthetic.main.act_login.*

/**
 *@创建者   dong
 *@创建时间 2020/7/3 10:04
 *@描述 登录页面
 */
class LoginActivity : BaseActivity() {
    //一键登录是否可用
    private var verifyEnable: Boolean = false

    //是否处于测试模式。用于控制Toast的显示
    private var mTest = false

    //预取号成功标识
    private var mPreviewSuccess = false


    override fun getLayoutId() = R.layout.act_login

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initFastLogin()
    }

    override fun initEvents(rootView: View) {
        view_phone_number_fast_login.onClickNew {
            ToastUtils.show("mPreviewSuccess = $mPreviewSuccess")
            if (mPreviewSuccess) {
                //预取号成功，跳转一键登录页面
            } else {
                //预取号未成功，跳转手机号登录页面
                val intent = Intent(this, PhoneNumLoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * 初始化一键登录相关
     */
    private fun initFastLogin() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //没有对应权限，直接退出
            return
        }
        //判断是否初始化成功
        if (!JVerificationInterface.isInitSuccess()) {
            //初始化未成功，直接返回
            return
        }

        //判断环境是否可用
        verifyEnable = JVerificationInterface.checkVerifyEnable(this)
        if (!verifyEnable) {
            //环境不可用，直接返回
            logger.info("当前网络环境不支持认证")
            return
        }

        //预取号
        JVerificationInterface.preLogin(this, 5000) { code, content ->
            //预取号结果
            mPreviewSuccess = code == 7000
            logger.info("onResult [$code] message=$content")
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
            //注册成功，跳转首页
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            SessionUtils.clearSession()
        }
        finish()
    }

}