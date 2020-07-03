package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.MessageSettingActivity
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.ConversationListAdapter
import com.julun.huanque.message.viewmodel.MessageViewModel
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.fragment_message.*

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:19
 *@描述  消息
 */
class MessageFragment : BaseFragment() {

    private val mMessageViewModel: MessageViewModel by activityViewModels<MessageViewModel>()

    private var mAdapter = ConversationListAdapter()

    companion object {
        fun newInstance() = MessageFragment()
    }

    override fun getLayoutId() = R.layout.fragment_message
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        initRecyclerView()
        mMessageViewModel.getConversationList()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = mAdapter
        //设置空页面的提示文案
        view?.findViewById<TextView>(R.id.emptyText)?.text = getString(R.string.no_data)

        mAdapter.setOnItemClickListener { adapter, view, position ->
            //            val realPosition = position - mAdapter.headerLayoutCount
            mAdapter.getItem(position)?.let { lmc ->
                //首页IM
                activity?.let { act ->
                    PrivateConversationActivity.newInstance(act, lmc.conversation.targetId)
                }
            }

        }
        mAdapter.setOnItemLongClickListener { adapter, view, position ->
            val realPosition = position
            val data = mAdapter.data
            if (ForceUtils.isIndexNotOutOfBounds(realPosition, data)) {
                val tId = data[realPosition].conversation.targetId
                if (tId == mAdapter.curAnchorId) {
                    return@setOnItemLongClickListener true
                }
                MyAlertDialog(
                        activity ?: return@setOnItemLongClickListener true
                ).showAlertWithOKAndCancel(
                        resources.getString(R.string.delete_conversation_or_not),
                        MyAlertDialog.MyDialogCallback(onRight = {
                            //确定删除
                            mMessageViewModel?.removeConversation(
                                    tId, Conversation.ConversationType.PRIVATE
                            )
                        })
                )

            }

            return@setOnItemLongClickListener true
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mMessageViewModel.conversationListData.observe(this, Observer {
            if (it != null) {
                mAdapter.setNewData(it)
            }
        })
    }

    override fun initEvents(rootView: View) {
        iv_setting.onClickNew {
            //设置
            activity?.let { act ->
                val intent = Intent(act, MessageSettingActivity::class.java)
                act.startActivity(intent)
            }
        }
        iv_contacts.onClickNew {
            //联系人
        }

        tv_message_unread.onClickNew {
            ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).navigation()
        }

    }
}