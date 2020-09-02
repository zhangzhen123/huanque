package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.app.update.AppChecker
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions

/**
 *@创建者   dong
 *@创建时间 2020/6/30 13:56
 *@描述 欢迎页
 */
class WelcomeActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_welcome

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        logger.info("WelcomeActivity initViews")
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
        //移除缓存的私信气泡数据
        SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
        checkPermissions()
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                //申请存储权限，无论成功还是失败，直接跳转
                startActivity()
            }
    }

    private fun startActivity() {
        val registerUser = SessionUtils.getIsRegUser()
        val intent = if (registerUser && SessionUtils.getRegComplete()) {
            RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
            //登录成功并且数据已经填写完成
            Intent(this, MainActivity::class.java)
        } else {
            if (registerUser) {
                SessionUtils.clearSession()
            }
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}