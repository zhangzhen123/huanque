package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.app.update.AppChecker
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils

/**
 *@创建者   dong
 *@创建时间 2020/6/30 13:56
 *@描述 欢迎页
 */
class WelcomeActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_welcome

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
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