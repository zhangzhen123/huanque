package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.MessageSettingActivity
import kotlinx.android.synthetic.main.fragment_message.*

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:19
 *@描述  消息
 */
class MessageFragment : BaseFragment() {

    companion object {
        fun newInstance() = MessageFragment()
    }

    override fun getLayoutId() = R.layout.fragment_message
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
    }

    override fun initEvents(rootView: View) {
        tv_setting.onClickNew {
            //设置
            activity?.let { act ->
                val intent = Intent(act, MessageSettingActivity::class.java)
                act.startActivity(intent)
            }
        }
        tv_contacts.onClickNew {
            //联系人
        }

    }
}