package com.julun.huanque.message.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.viewmodel.PlayerMessageViewModel
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.*
import com.julun.huanque.message.adapter.ConversationListAdapter
import com.julun.huanque.message.viewmodel.MessageViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.luck.picture.lib.tools.StatusBarUtil
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.fragment_message.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.topPadding
import kotlin.math.log

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:19
 *@描述  消息
 */
@Route(path = ARouterConstant.MessageFragment)
class MessageFragment : BaseFragment() {

    private val mMessageViewModel: MessageViewModel by activityViewModels<MessageViewModel>()

    private val mPlayerMessageViewModel: PlayerMessageViewModel by activityViewModels<PlayerMessageViewModel>()

    private var mAdapter = ConversationListAdapter()


    companion object {
        /**
         * @param stranger 是否是陌生人
         */
        fun newInstance(stranger: Boolean = false): MessageFragment {
            val fragment = MessageFragment()
            fragment.arguments = Bundle().apply { putBoolean(ParamConstant.STRANGER, stranger) }
            return fragment
        }
    }

    override fun isRegisterEventBus() = true

    override fun getLayoutId() = R.layout.fragment_message

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        initRecyclerView()
//        initEvents()
        mMessageViewModel.foldStrangerMsg = SharedPreferencesUtils.getBoolean(SPParamKey.FOLD_STRANGER_MSG, false)
        mMessageViewModel.mStranger = arguments?.getBoolean(ParamConstant.STRANGER, false) ?: false
        mMessageViewModel.player = arguments?.getBoolean(ParamConstant.PLAYER, false) ?: false
        if (mMessageViewModel.mStranger) {
            view_top.hide()
            iv_setting.hide()
            iv_contacts.hide()
            tv_message_unread.hide()
        }

        if (mMessageViewModel.player) {
            //直播间内使用
            view_top.hide()
            iv_setting.hide()
            iv_contacts.hide()
            tv_message_unread.hide()

            view_top_player.show()
            tv_title_player.show()
            iv_contacts_player.show()
            view_bg.backgroundColor = GlobalUtils.getColor(R.color.color_gray_three)
        }

        if (!mMessageViewModel.mStranger && !mMessageViewModel.player) {
            //设置头部边距
            msg_container.topPadding = StatusBarUtil.getStatusBarHeight(requireContext())
        }
        if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED == RongIMClient.getInstance().currentConnectionStatus) {
            //融云已经连接
            //查询会话列表和免打扰列表
            mPlayerMessageViewModel.getBlockedConversationList()
            mMessageViewModel.getConversationList()
        }

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
                when (lmc.conversation.targetId) {
                    SystemTargetId.systemNoticeSender, SystemTargetId.friendNoticeSender -> {
                        //系统消息 or 好友通知消息
                        val intent = Intent(activity, SysMsgActivity::class.java)
                        intent.putExtra(IntentParamKey.SYS_MSG_ID.name, lmc.conversation.targetId)
                        startActivityForResult(intent, ActivityCodes.REQUEST_CODE_NORMAL)
                    }
                    null -> {
                        //陌生人折叠消息
                        requireActivity().startActivity<StrangerActivity>()
                    }
                    else -> {
                        //首页IM
                        try {
                            if (mMessageViewModel.player) {
                                //直播间内
                                mPlayerMessageViewModel.privateConversationData.value =
                                    OpenPrivateChatRoomEvent(lmc.conversation.targetId.toLong(), "")
                            } else {
                                //非直播间内
                                activity?.let { act ->

                                    PrivateConversationActivity.newInstance(
                                        act,
                                        lmc.conversation.targetId.toLong()
                                    )

                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val targetId = mAdapter.getItem(position).conversation.targetId
            try {
                val longId = targetId.toLong()
                if (view.id == R.id.sdv_header) {
                    //跳转他人主页
                    if (longId == SessionUtils.getUserId()) {
                        //本人主页
                        RNPageActivity.start(
                            requireActivity(),
                            RnConstant.MINE_HOMEPAGE
                        )
                    } else {
                        //他人主页
                        RNPageActivity.start(
                            requireActivity(),
                            RnConstant.PERSONAL_HOMEPAGE,
                            Bundle().apply { putLong("userId", longId) })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mAdapter.setOnItemLongClickListener { adapter, view, position ->
            val realPosition = position
            val data = mAdapter.data
            if (ForceUtils.isIndexNotOutOfBounds(realPosition, data)) {
                val tId = data[realPosition].conversation.targetId
                if (tId == SystemTargetId.friendNoticeSender || tId == SystemTargetId.systemNoticeSender || tId == mAdapter.curAnchorId) {
                    //系统消息和鹊友通知不显示弹窗
                    return@setOnItemLongClickListener false
                }
                MyAlertDialog(
                    activity ?: return@setOnItemLongClickListener true
                ).showAlertWithOKAndCancel(
                    resources.getString(R.string.delete_conversation_or_not),
                    MyAlertDialog.MyDialogCallback(onRight = {
                        //确定删除
                        if (tId == null || tId == SystemTargetId.friendNoticeSender || tId == SystemTargetId.systemNoticeSender) {
                            ToastUtils.show("该消息无法删除")
                            return@MyDialogCallback
                        }
                        mMessageViewModel.removeConversation(tId, Conversation.ConversationType.PRIVATE)
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
                    mAdapter.notifyItemChanged(it + mAdapter.headerLayoutCount)
                }
                mMessageViewModel.changePosition.value = null
            }
        })

        mMessageViewModel.unreadMsgCount.observe(this, Observer {
            if (it != null) {
                val str = if (it > 0) {
                    "消息($it)"
                } else {
                    "消息"
                }
                tv_message_unread.text = str
            }
        })


        mPlayerMessageViewModel.blockListData.observe(this, Observer {
            if (it != null) {
                mAdapter.blockList = it
                mAdapter.notifyDataSetChanged()
            }
        })

        mPlayerMessageViewModel.anchorData.observe(this, Observer {
            if (it != null) {
                mMessageViewModel.anchorData = it
                mAdapter.curAnchorId = "${it.programId}"
            }
        })

        mPlayerMessageViewModel.unreadCountInPlayer.observe(this, Observer {
            if (it != null) {
                val str = if (it > 0) {
                    "消息($it)"
                } else {
                    "消息"
                }
                tv_title_player.text = str
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

        iv_contacts_player.onClickNew {
            //联系人
            mPlayerMessageViewModel.contactsData.value = true
        }

        tv_message_unread.onClickNew {
            activity?.let { act ->
//                PrivateConversationActivity.newInstance(act, 20000516)
                PrivateConversationActivity.newInstance(act, 10)
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateMessageReceive(bean: EventMessageBean) {
//        if (bean.onlyRefreshUnReadCount) {
//            mMessageViewModel.refreshUnreadCount(bean.targetId)
//        } else {
            mMessageViewModel.refreshConversation(bean.targetId, bean.stranger)
//        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun foldMessage(bean: FoldStrangerMessageEvent) {
        val fold = SharedPreferencesUtils.getBoolean(SPParamKey.FOLD_STRANGER_MSG, false)
        if (fold == mMessageViewModel.foldStrangerMsg) {
            return
        } else {
            //折叠状态变化
            mMessageViewModel.foldStrangerMsg = fold
            mMessageViewModel.getConversationList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userInfoChangeEvent(bean: UserInfoChangeEvent) {
        //用户数据发生变化
        mMessageViewModel.userInfoUpdate(bean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun loginChange(event: LoginEvent) {
        if (!event.result) {
            //退出登录，清空会话列表
            mMessageViewModel.conversationListData.value = null
            mAdapter.setNewInstance(null)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectSuccess(event: RongConnectEvent) {
        if (RongCloudManager.RONG_CONNECTED == event.state) {
            //融云连接成功，加载数据
            mPlayerMessageViewModel.getBlockedConversationList()
            mMessageViewModel.getConversationList()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun blockChange(event: MessageBlockEvent) {
        //先清空免打扰列表，再重新获取
        mPlayerMessageViewModel.blockListData.value = null
        mPlayerMessageViewModel.unreadList.clear()
        mPlayerMessageViewModel.getBlockedConversationList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ActivityCodes.REQUEST_CODE_NORMAL -> {
                if (resultCode == ActivityCodes.RESPONSE_CODE_REFRESH) {
                    //也许是从系统消息页面返回，刷新列表一次
                    mMessageViewModel.getConversationList()
                    mMessageViewModel.queryRongPrivateCount()
                }
            }
        }
    }
}