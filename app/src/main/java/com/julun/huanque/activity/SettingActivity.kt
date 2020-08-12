package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.support.LoginManager
import kotlinx.android.synthetic.main.act_setting.*
import kotlinx.android.synthetic.main.act_setting.header_view

/**
 *@创建者   dong
 *@创建时间 2020/7/18 10:33
 *@描述 设置页面
 */
class SettingActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "设置"

        if (!BuildConfig.DEBUG) {
            tvChange.hide()
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        header_view.imageViewBack.onClickNew {
            finish()
        }

        tvChange.onClickNew {
            ARouter.getInstance().build(ARouterConstant.ENVIRONMENT_CONFIGURATION_ACTIVITY).navigation()
        }

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

        view_accountandsecurity.onClickNew {
            //账号与安全
            val intent = Intent(this, AccountAndSecurityActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }
    }
}