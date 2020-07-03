package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity

/**
 *@创建者   dong
 *@创建时间 2020/6/30 13:56
 *@描述 欢迎页
 */
class WelcomeActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_welcome

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}