package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.activity.MessageSettingActivity
import com.julun.huanque.message.activity.PrivateChatActivity
import com.julun.huanque.message.viewmodel.MessageViewModel
import kotlinx.android.synthetic.main.fragment_message.*

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:19
 *@描述  消息
 */
class MessageFragment : BaseFragment() {

    private val mMessageViewModel: MessageViewModel by activityViewModels<MessageViewModel>()

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

        tv_message.onClickNew {
            activity?.let { act ->
                val intent = Intent(act, PrivateChatActivity::class.java)
                act.startActivity(intent)
            }

        }

    }
}