package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/7/2 17:52
 *@描述 联系人页面
 */
class ContactsActivity : BaseActivity() {
    companion object {
        fun newInstance(activity: Activity) {
            activity.startActivity(Intent(activity, ContactsActivity::class.java))
        }
    }

    override fun getLayoutId() = R.layout.act_contacts

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "联系人"
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
    }
}