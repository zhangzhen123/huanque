package com.julun.huanque.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew


/**
 *@创建者   dong
 *@创建时间 2020/7/3 16:47
 *@描述 填写资料页面
 */
class FillInformationActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_fill_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "消息设置"


    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew { finish() }
    }
}