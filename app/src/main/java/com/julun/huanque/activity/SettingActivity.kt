package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.support.LoginManager
import kotlinx.android.synthetic.main.act_setting.*

/**
 *@创建者   dong
 *@创建时间 2020/7/18 10:33
 *@描述 设置页面
 */
class SettingActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "设置"
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        tv_logout.onClickNew {
            //退出登录
            LoginManager.doLoginOut {
                if (it) {
                    //退出登录成功
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}