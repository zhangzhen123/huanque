package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.viewmodel.PrivateConversationSettingViewModel
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.act_private_conversation_setting.*

/**
 *@创建者   dong
 *@创建时间 2020/7/2 9:02
 *@描述  私聊会话设置页面
 */
class PrivateConversationSettingActivity : BaseActivity() {

    private var mPrivateConversationSettingViewModel: PrivateConversationSettingViewModel? = null

    companion object {
        const val TARGETID = "TARGETID"
        fun newInstance(activity: Activity, targetID: String) {
            val intent = Intent(activity, PrivateConversationSettingActivity::class.java)
            intent.putExtra(TARGETID, targetID)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.act_private_conversation_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "聊天详情"

        initViewModel()
        mPrivateConversationSettingViewModel?.targetId = intent?.getStringExtra(TARGETID) ?: ""
        mPrivateConversationSettingViewModel?.getConversationDisturbStatus()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationSettingViewModel = ViewModelProvider(this).get(PrivateConversationSettingViewModel::class.java)
        mPrivateConversationSettingViewModel?.disturbStatus?.observe(this, Observer {
            if (it != null) {
                iv_no_disturbing.isSelected = it == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB.value
            }
        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
        iv_no_disturbing.onClickNew {
            mPrivateConversationSettingViewModel?.conversationDisturbSetting(iv_no_disturbing.isSelected)
        }
    }
}