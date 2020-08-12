package com.julun.huanque.activity

import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import kotlinx.android.synthetic.main.act_setting.*


/**
 *@创建者   dong
 *@创建时间 2020/8/12 14:50
 *@描述 账号与安全
 */
class AccountAndSecurityActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_account_and_security

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "账号与安全"
    }

    override fun initEvents(rootView: View) {
        header_view.imageViewBack.onClickNew {
            finish()
        }

    }

}