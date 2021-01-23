package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.CommentListAdapter
import com.julun.huanque.message.adapter.FriendsAdapter
import com.julun.huanque.message.adapter.PraiseListAdapter
import com.julun.huanque.message.adapter.SysMsgAdapter
import com.julun.huanque.message.viewmodel.SysMsgViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.message.TextMessage
import kotlinx.android.synthetic.main.activity_sys_msg.*
import org.jetbrains.anko.backgroundResource

/**
 * 官方系统消息
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/13
 */
@Route(path = ARouterConstant.SysMsgActivity)
class SysMsgActivity : BaseActivity() {

    private var mViewModel: SysMsgViewModel? = null

    //系统消息adapter
    private val mSysAdapter: SysMsgAdapter by lazy { SysMsgAdapter() }

    //鹊友通知adapter
    private val mFriendsAdapter: FriendsAdapter by lazy { FriendsAdapter() }

    //评论adapter
    private val mCommentListAdapter: CommentListAdapter by lazy { CommentListAdapter() }

    //点赞Adapter
    private val mPraiseListAdapter: PraiseListAdapter by lazy { PraiseListAdapter() }


    private var mAdapter: BaseQuickAdapter<Message, BaseViewHolder>? = null

    private var targetID = ""

    override fun getLayoutId(): Int = R.layout.activity_sys_msg

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val targetId = intent.getStringExtra(IntentParamKey.SYS_MSG_ID.name) ?: ""
        targetID = targetId

        MixedHelper.setSwipeRefreshStyle(rlRefreshView)
        rvList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        when (targetId) {
            SystemTargetId.systemNoticeSender -> {
                //系统消息
                mAdapter = mSysAdapter
                clMsgRootView.backgroundResource = R.color.color_gray_three
                tvTitle.text = "系统消息"
                tv_clear.hide()
            }
            SystemTargetId.friendNoticeSender -> {
                //好友通知
                mAdapter = mFriendsAdapter
                clMsgRootView.backgroundResource = R.color.white
                tvTitle.text = "鹊友通知"
                tv_clear.hide()
            }
            SystemTargetId.praiseNoticeSender -> {
                //点赞
                mAdapter = mPraiseListAdapter
                clMsgRootView.backgroundResource = R.color.white
                tvTitle.text = "点赞"
                tv_clear.show()
            }
            SystemTargetId.commentNoticeSender -> {
                //评论消息
                mAdapter = mCommentListAdapter
                clMsgRootView.backgroundResource = R.color.white
                tvTitle.text = "评论"
                tv_clear.show()
            }
            else -> {

            }
        }
        rvList.adapter = mAdapter
        mAdapter?.loadMoreModule?.isEnableLoadMore = true

        prepareViewModel()
        queryData(true, targetId)
        registerMessageEventProcessor()
        mViewModel?.clearUnReadCount(targetId)
    }

    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            onBackPressed()
        }
        rlRefreshView.setOnRefreshListener {
            queryData(true, mViewModel?.targetId ?: return@setOnRefreshListener)
        }
        //添加加载更多的监听
        mAdapter?.loadMoreModule?.setOnLoadMoreListener {
            val lastItem = mAdapter?.data?.last() as? Message
            queryData(
                false,
                mViewModel?.targetId ?: return@setOnLoadMoreListener,
                lastItem?.messageId ?: return@setOnLoadMoreListener
            )
        }
//        mAdapter.onAdapterClickNew { _, view, _ ->
//        }
        mAdapter?.onAdapterChildClickNew { adapter, view, position ->
            when (view?.id) {
                R.id.llSysRootView -> {
                    val item = view.getTag(R.id.msg_bean_id) as? SysMsgBean
                    customAction(item ?: return@onAdapterChildClickNew)
                }
                R.id.clFriendsRootView -> {
                    val item = view.getTag(R.id.msg_bean_id) as? FriendBean
                    //打开他人主页
                    try {
                        val bundle = Bundle().apply {
                            putLong(ParamConstant.UserId, item?.friendId?.toLong() ?: return@onAdapterChildClickNew)
                        }
                        ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                R.id.tvMessage -> {
                    val item = view.getTag(R.id.msg_bean_id) as? FriendBean
                    //打开私聊
                    item ?: return@onAdapterChildClickNew
                    try {
                        PrivateConversationActivity.newInstance(this, item.friendId.toLong(), item.friendNickname)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                R.id.view_header_info -> {
                    //打开他们主页
                    val tempData = adapter?.getItemOrNull(position) as? Message ?: return@onAdapterChildClickNew
                    val tempContent = tempData.content as? TextMessage ?: return@onAdapterChildClickNew
                    val customBean: ActionMessageContent? =
                        MessageFormatUtils.parseJsonFromTextMessage(ActionMessageContent::class.java, tempContent.content ?: "")
                    //打开他人主页
                    val targetUserId = customBean?.context?.targetUserId ?: return@onAdapterChildClickNew
                    try {
                        val bundle = Bundle().apply {
                            putLong(ParamConstant.UserId, targetUserId)
                        }
                        ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                R.id.con_post_action -> {
                    //打开动态详情
                    val tempData = adapter?.getItemOrNull(position) as? Message ?: return@onAdapterChildClickNew
                    val tempContent = tempData.content as? TextMessage ?: return@onAdapterChildClickNew
                    val customBean: ActionMessageContent? =
                        MessageFormatUtils.parseJsonFromTextMessage(ActionMessageContent::class.java, tempContent.content ?: "")
                    //打开他人主页
                    val postId = customBean?.context?.postId ?: return@onAdapterChildClickNew
                    try {
                        val bundle = Bundle().apply {
                            putLong(IntentParamKey.POST_ID.name, postId)
                        }
                        ARouter.getInstance().build(ARouterConstant.DYNAMIC_DETAIL_ACTIVITY).with(bundle).navigation()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        tv_clear.onClickNew {
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "确定清空所有消息？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    //清空所有消息
                    if (targetID.isNotEmpty()) {
                        mViewModel?.clearMessage()
                    }
                }), "提示", "确定"
            )
        }

    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(this).get(SysMsgViewModel::class.java)
        mViewModel?.getSysMsgList?.observe(this, Observer {
            it ?: return@Observer
            if (it.list.isNotEmpty()) {
                if (it.isPull) {
                    mAdapter?.setList(it.list)
                } else {
                    mAdapter?.addData(it.list)
                }
                if (!it.hasMore) {
                    mAdapter?.loadMoreModule?.loadMoreEnd()
                } else {
                    mAdapter?.loadMoreModule?.loadMoreComplete()
                }
                hideCommonView()
            } else {
                isShowError(true, isError = false)
            }
        })
        mViewModel?.refreshErrorStats?.observe(this, Observer {
            it ?: return@Observer
            if (mAdapter?.data?.isEmpty() == true) {
                isShowError(isShow = true, isError = true)
                return@Observer
            }
            ToastUtils.show("网络异常，请重试~！")
        })
        mViewModel?.loadMoreErrorStats?.observe(this, Observer {
            it ?: return@Observer
            mAdapter?.loadMoreModule?.loadMoreFail()
        })
        mViewModel?.finalState?.observe(this, Observer {
            it ?: return@Observer
            finishRefresh()
        })
    }

    /**
     * 注册消息相关
     */
    private fun registerMessageEventProcessor() {
        // 私聊消息
        MessageProcessor.privateTextProcessor = object : MessageProcessor.PrivateMessageReceiver {
            override fun processMessage(msg: Message) {
                if (msg.targetId == mViewModel?.targetId) {
                    //就是当前的消息，直接显示
                    mAdapter?.addData(0, msg)
                    scrollToTop()
                    //获取会话消息直接置为已读
                    RongIMClient.getInstance()
                        .clearMessagesUnreadStatus(
                            Conversation.ConversationType.PRIVATE,
                            msg.targetId,
                            null
                        )
                }
            }
        }
    }

    private fun queryData(isPull: Boolean, targetId: String, lastMessageId: Int = -1) {
        isShowLoading(true)
        if (isPull) {
            mViewModel?.getHistoryMessages(targetId)
        } else {
            mViewModel?.getHistoryMessages(targetId, lastMessageId)
        }
    }

    private fun customAction(sysBean: SysMsgBean) {
        val touchType = sysBean.touchType
        if (touchType.isEmpty()) {
            return
        }
        AppHelper.openTouch(sysBean.touchType,sysBean.touchValue)
        //RN跳转的保留
        when (touchType) {
//            TouchTypeConstants.ACTION_URL -> {
//                //H5
//                WebActivity.startWeb(this, sysBean.touchValue)
//            }
            TouchTypeConstants.OfficialCertPage -> {
                //跳转官方认证
                RNPageActivity.start(this, RnConstant.OFFICIAL_CERT_PAGE)
            }
            TouchTypeConstants.AnchorCertPage -> {
                //跳转到主播认证
                RNPageActivity.start(this, RnConstant.ANCHOR_CERT_PAGE)
            }
//            TouchTypeConstants.EditMineHomePage -> {
//                //跳转到我的资料编辑
////                RNPageActivity.start(this, RnConstant.EDIT_MINE_HOMEPAGE)
//                ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation()
//            }
            TouchTypeConstants.RoyalPage -> {
                //跳转到贵族
                RNPageActivity.start(this, RnConstant.ROYAL_PAGE)
            }
//            TouchTypeConstants.MineHomePage -> {
//                //跳转到我的主页
//                val bundle = Bundle().apply {
//                    putLong(ParamConstant.UserId, SessionUtils.getUserId())
//                }
//                ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
//            }

//            TouchTypeConstants.ACTION_None -> {
//            }
//            TouchTypeConstants.PlumFlower -> {
//                //花魁榜
//                val bundle = Bundle()
//                bundle.putString(ParamConstant.TYPE, sysBean.touchValue)
//                ARouter.getInstance().build(ARouterConstant.PLUM_FLOWER_ACTIVITY).with(bundle).navigation()
//            }
//            TouchTypeConstants.AccostWords -> {
//                //搭讪常用语
//                UsefulWordActivity.newInstance(this)
//            }
//            TouchTypeConstants.FateCome -> {
//                //缘分来了页面
//                YuanFenActivity.newInstance(this, 0)
//            }
//            TouchTypeConstants.Message -> {
//                //消息列表页面
//                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
//                    .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX).navigation()
//            }
//            TouchTypeConstants.FriendHome -> {
//                //交友页面
//                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
//                    .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
//            }
//            TouchTypeConstants.PrivateChat -> {
//                //跳转私信
//                try {
//                    PrivateConversationActivity.newInstance(this, sysBean.touchValue.toLong())
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            }
//            TouchTypeConstants.PostDetail -> {
//                //动态详情
//                try {
//                    val bundle = Bundle().apply {
//                        putLong(IntentParamKey.POST_ID.name, sysBean.touchValue.toLong())
//                    }
//                    ARouter.getInstance().build(ARouterConstant.DYNAMIC_DETAIL_ACTIVITY).with(bundle).navigation()
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            }
//            TouchTypeConstants.AuthTagDetail -> {
//                try {
//                    val bundle = Bundle()
//                    bundle.putInt(ManagerTagCode.TAG_INFO, sysBean.touchValue.toInt())
//                    bundle.putBoolean("isMe", true)
//                    ARouter.getInstance().build(ARouterConstant.AUTH_TAG_PIC_ACTIVITY).with(bundle).navigation()
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//
//            }
//            else -> {
//                ToastUtils.show("没有记录的action类型 -> ${touchType}")
//            }
        }
    }

    private fun scrollToTop() {
        if ((mAdapter?.itemCount ?: 0) > 0) {
            rvList?.post {
                rvList?.smoothScrollToPosition(0)
            }
        }
    }

    private fun isShowLoading(isShow: Boolean) {
        if (isShow) {
            commonView.show()
            commonView.showLoading("加载中")
        } else {
            commonView.hide()
        }
    }

    private fun isShowError(isShow: Boolean, isError: Boolean) {
        if (isShow) {
            commonView.show()
            if (isError) {
                commonView.showError(
                    errorTxt = "网络异常，请重试~！",
                    showBtn = true,
                    btnClick = View.OnClickListener {
                        queryData(true, mViewModel?.targetId ?: return@OnClickListener)
                    })
            } else {
                commonView.showEmpty(emptyTxt = "暂无消息")
            }
        } else {
            commonView.hide()
        }
    }

    private fun hideCommonView() {
        if (commonView.isVisible()) {
            commonView.hide()
        }
    }

    private fun finishRefresh() {
        rlRefreshView.post {
            rlRefreshView?.let {
                it.isRefreshing = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageProcessor.privateTextProcessor = null
    }
}