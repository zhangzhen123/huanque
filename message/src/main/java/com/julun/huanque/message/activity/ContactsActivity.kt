package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew

/**
 *@创建者   dong
 *@创建时间 2020/7/2 17:52
 *@描述 联系人页面
 */
class ContactsActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_contacts

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "聊天详情"
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
    }
}