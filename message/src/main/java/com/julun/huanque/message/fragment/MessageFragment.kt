package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.MessageHeaderBean
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ContactsTabType
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.ContactsActivity
import com.julun.huanque.message.activity.MessageSettingActivity
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.ConversationListAdapter
import com.julun.huanque.message.viewmodel.MessageViewModel
import com.julun.huanque.message.widget.MessageHeaderView
import com.luck.picture.lib.tools.StatusBarUtil
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.fragment_message.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.topPadding
import java.lang.Exception

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:19
 *@描述  消息
 */
class MessageFragment : BaseFragment() {

    private val mMessageViewModel: MessageViewModel by activityViewModels<MessageViewModel>()

    private var mAdapter = ConversationListAdapter()

    //头部视图
    private var headerView: LinearLayout? = null

    private val headerViewList: ArrayList<MessageHeaderView> by lazy { arrayListOf<MessageHeaderView>() }

    companion object {
        fun newInstance() = MessageFragment()
    }

    override fun isRegisterEventBus() = true

    override fun getLayoutId() = R.layout.fragment_message
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //设置头部边距
        msg_container.topPadding= StatusBarUtil.getStatusBarHeight(requireContext())
        initViewModel()
        initRecyclerView()
        initHeaderView()
        mMessageViewModel.getConversationList()
    }

    /**
     * 初始化HeaderView
     */
    private fun initHeaderView() {
        headerView = LayoutInflater.from(context).inflate(R.layout.header_conversions_view, null) as? LinearLayout
        headerView?.let {
            val view1 = it.findViewById<MessageHeaderView>(R.id.system_msg)
            headerViewList.add(view1)
            view1.setViewData(
                MessageHeaderBean(
                    headRes = R.mipmap.icon_message_system,
                    title = "系统消息",
                    content = "暂无系统消息"
                )
            )
            val view2 = it.findViewById<MessageHeaderView>(R.id.friend_msg)
            headerViewList.add(view2)
            view2.setViewData(
                MessageHeaderBean(
                    headRes = R.mipmap.icon_message_friend,
                    title = "好友通知",
                    content = "暂无好友通知"
                )
            )
            view1.onClickNew {
                ToastUtils.show("系统消息")
//                ARouter.getInstance().build(ARouterConstant.MESSAGE_ACTIVITY).navigation()
            }
            view2.onClickNew {
                ToastUtils.show("好友通知")
//                activity?.startActivity<InteractionNewActivity>()
            }
        }
        mAdapter.addHeaderView(headerView?:return)
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
                    try {
                        PrivateConversationActivity.newInstance(act, lmc.conversation.targetId.toLong())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

        }
        mAdapter.setOnItemLongClickListener { adapter, view, position ->
            val realPosition = position
            val data = mAdapter.data
            if (ForceUtils.isIndexNotOutOfBounds(realPosition, data)) {
                val tId = data[realPosition].conversation.targetId
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
//                mAdapter.setNewData(it)
                mAdapter.setList(it)
            }
        })

        mMessageViewModel.changePosition.observe(this, Observer {
            if (it != null) {
                if (it < 0) {
                    //刷新整个列表
                    mAdapter.notifyDataSetChanged()
                } else {
                    //单个变动
//                    mAdapter.refreshNotifyItemChanged(it)
                    mAdapter.notifyItemChanged(it+mAdapter.headerLayoutCount)
                }
                mMessageViewModel.changePosition.value = null
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
            activity?.let { act ->
                ContactsActivity.newInstance(act, ContactsTabType.Intimate)
            }
        }

        tv_message_unread.onClickNew {
            activity?.let { act ->
                PrivateConversationActivity.newInstance(act, 20000041)
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateMessageReceive(bean: EventMessageBean) {
        mMessageViewModel.queryRongPrivateCount()
        mMessageViewModel.refreshConversation(bean.tardetId)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun connectSuccess(event: RongConnectEvent) {
//        //融云连接成功通知
//        conversationListViewModel?.getConversationList()
//    }
}