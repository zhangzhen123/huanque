package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/10/10 17:27
 *@描述 常用语页面
 */
class UsefulWordActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity) {
            val intent = Intent(act, UsefulWordActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                act.startActivity(intent)
            }
        }
    }

    override fun getLayoutId() = R.layout.act_useful_word

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
    }
}