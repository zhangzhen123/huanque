package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.fragment.MessageFragment
import kotlinx.android.synthetic.main.act_stranger.*

/**
 *@创建者   dong
 *@创建时间 2020/7/18 17:15
 *@描述 陌生人会话列表
 */
class StrangerActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_stranger

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "陌生人消息 "
        initFragment()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_view.imageViewBack.onClickNew {
            finish()
        }
    }

    /**
     * 设置相关Fragment
     */
    private fun initFragment() {
        val fragment = MessageFragment.newInstance(true)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frame, fragment)
        transaction.show(fragment).commitAllowingStateLoss()
    }

}