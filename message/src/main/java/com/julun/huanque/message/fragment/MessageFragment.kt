package com.julun.huanque.message.fragment

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ChatRoomBean
import com.julun.huanque.common.bean.beans.AdInfoBean
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.viewmodel.PlayerMessageViewModel
import com.julun.huanque.common.widgets.bgabanner.BGABanner
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
import org.jetbrains.anko.*

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

    //是否选中了缘分布局
    private var isclick = false

    //touch事件的开始时间
    private var startTime: Long = 0

    //touch事件的结束时间
    private var endTime: Long = 0

    //距离屏幕的间距
    private val mMargin = 40

    //x最小值
    private val xMin = mMargin

    //X最大值
    private var xMax = ScreenUtils.getScreenWidth() - mMargin - dp2px(61)

    //y最小值
    private val yMin = StatusBarUtil.getStatusBarHeight(CommonInit.getInstance().getContext())

    //y最大值
    private var yMax = ScreenUtils.getScreenHeight() - dp2px(60)


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
        MixedHelper.setSwipeRefreshStyle(swipe_refresh)
        initViewModel()
        initRecyclerView()
//        initEvents()
        if (mMessageViewModel.player) {
            bannerAD.hide()
        }
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
        mMessageViewModel.queryDataFlag.value = true
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerview.layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = mAdapter


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
                                    OpenPrivateChatRoomEvent(lmc.conversation.targetId.toLong(), "", "")
                            } else {
                                //非直播间内
                                activity?.let { act ->

                                    PrivateConversationActivity.newInstance(
                                        act,
                                        lmc.conversation.targetId.toLong(),
                                        lmc.showUserInfo?.nickname ?: "",
                                        headerPic = lmc.showUserInfo?.headPic ?: ""
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
                        val bundle = Bundle().apply {
                            putLong(ParamConstant.UserId, longId)
                        }
                        ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
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
                        //刷新未读数
                        if (mMessageViewModel.player) {
                            //直播间内
                            mPlayerMessageViewModel.queryRongPrivateCount(tId)
                        } else {
                            mMessageViewModel.queryUnreadCountFlag.value = true
                        }
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
        mMessageViewModel.queryDataFlag.observe(this, Observer {
            if (it == true) {
                if (mAdapter.itemCount == 0) {
                    pageView.showLoading()
                }
                if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED == RongIMClient.getInstance().currentConnectionStatus) {
                    //融云已经连接
                    //查询会话列表和免打扰列表
                    if (mAdapter.itemCount == 0) {
                        mPlayerMessageViewModel.getBlockedConversationList()
                    }
                    mMessageViewModel.getConversationList()
                } else if (RongCloudUtils.RongCloudNeedConnectedManually()) {
                    //手动连接一次
                    RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = false)
                }
//                else {
//                    logger.info("Message 融云未连接 ${RongIMClient.getInstance().currentConnectionStatus}")
//                    showEmptyView()
//                }
            }
        })
        mMessageViewModel.conversationListData.observe(this, Observer {
            if (it != null) {
//                mAdapter.setNewData(it)
                if (it.isEmpty()) {
                    showEmptyView()
                } else {
                    pageView.showSuccess()
                }
                mAdapter.setList(it)
            } else {
                showEmptyView()
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
                    val unreadCount = mAdapter.getItem(it + mAdapter.headerLayoutCount)?.conversation?.unreadMessageCount
                    logger.info("Message unreadCount = $unreadCount")
                    mAdapter.notifyItemChanged(it + mAdapter.headerLayoutCount)
                }
                mMessageViewModel.changePosition.value = null
            }
        })

        mMessageViewModel.unreadMsgCount.observe(this, Observer {
            if (it != null) {
                val str = if (it > 99) {
                    "消息(99+)"
                } else if (it > 0) {
                    "消息($it)"
                } else {
                    "消息"
                }
                tv_message_unread.text = str
            }
        })

        mMessageViewModel.chatRoomData.observe(this, Observer {
            if (it != null) {
                val noReplyNum = it.fateNoReplyNum
                tv_yuanfen_count.text = "$noReplyNum"
                if (noReplyNum >= 1) {
                    tv_yuanfen_count.show()
                } else {
                    tv_yuanfen_count.hide()
                }
                val onLineResource = if (it.onlineStatus == ChatRoomBean.Online) {
                    R.mipmap.icon_online
                } else {
                    R.mipmap.icon_unline
                }
                iv_online_status.imageResource = onLineResource

                if (it.showFate == BusiConstant.True && !mMessageViewModel.player) {
                    rl_yuanfen.show()
                } else {
                    rl_yuanfen.hide()
                }
                //显示广告
                if (!mMessageViewModel.player) {
                    loadAd(it.adList)
                }
            }
        })

        mMessageViewModel.loadState.observe(this, Observer {
            if (it != null) {
                if (it.state == NetStateType.SUCCESS) {
                    swipe_refresh.isRefreshing = false
                }
            }
        })


        mPlayerMessageViewModel.blockListData.observe(this, Observer {
            if (it != null) {
                mMessageViewModel.blockListData = it
                if (mMessageViewModel.needQueryConversation) {
                    mMessageViewModel.getConversationList()
                }
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
                val str = if (it > 99) {
                    "消息(99+)"
                } else if (it > 0) {
                    "消息($it)"
                } else {
                    "消息"
                }
                tv_title_player.text = str
            }
        })
        mPlayerMessageViewModel.needRefreshConversationFlag.observe(this, Observer {
            if (it != null) {
                mMessageViewModel.refreshConversation(it.targetId, it.stranger)
            }
        })
    }

    private fun showEmptyView() {
        pageView.showEmpty(
            false,
            R.mipmap.icon_default_empty,
            emptyTxt = "快去找个有趣的人聊吧~",
            onClick = View.OnClickListener {
                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                    .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
            },
            btnTex = "去看看"
        )
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

        rl_yuanfen.onClickNew {
            YuanFenActivity.newInstance(requireActivity(), mMessageViewModel.chatRoomData.value?.fateNoReplyNum ?: 0)
        }

        rl_online.onClickNew {
            //在线按钮
            if (mOnLineStatusSettingPopupWindow?.isShowing != true) {
                showOnLinePopupWindow()
            }
        }

        swipe_refresh.setOnRefreshListener {
            mMessageViewModel.chatRoom()
            mMessageViewModel.queryDataFlag.value = true
        }
//        if(BuildConfig.DEBUG){
//            tv_message_unread.onClickNew {
//                activity?.let { act ->
////                PrivateConversationActivity.newInstance(act, 20000767)
//                    PrivateConversationActivity.newInstance(act, 10)
//                }
//            }
//        }

        rl_yuanfen.setOnTouchListener(object : View.OnTouchListener {
            private var x = 0f
            private var y = 0f
            override fun onTouch(view: View?, event: MotionEvent): Boolean {
                if (view == null) {
                    return false
                }
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.getRawX()
                        y = event.getRawY()
                        isclick = false //当按下的时候设置isclick为false
                        startTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        isclick = true //当按钮被移动的时候设置isclick为true
                        val nowX = event.getRawX()
                        val nowY = event.getRawY()
                        val movedX = nowX - x
                        val movedY = nowY - y
                        x = nowX
                        y = nowY
                        var tempX = (view.x + movedX).toInt()
                        if (tempX < xMin) {
                            tempX = xMin
                        } else if (tempX > xMax) {
                            tempX = xMax
                        }
                        view.x = tempX.toFloat()

                        var tempY = view.y + movedY.toInt()
                        if (tempY < yMin) {
                            tempY = yMin.toFloat()
                        } else if (tempY > yMax) {
                            tempY = yMax.toFloat()
                        }
                        view.y = tempY
//                        windowManager?.updateViewLayout(view, layoutParams)
                    }
                    MotionEvent.ACTION_UP -> {
                        endTime = System.currentTimeMillis()
                        //当从点击到弹起小于半秒的时候,则判断为点击,如果超过则不响应点击事件
                        if (endTime - startTime > 0.1 * 1000L) {
                            isclick = true
                        } else {
                            isclick = false
                        }
                        if (isclick) {
                            yuanFenAnimationToSide()
                        }
                    }
                }

                return isclick
            }
        })


    }

    //设置在线状态的PopupWindow
    private var mOnLineStatusSettingPopupWindow: PopupWindow? = null

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            mOnLineStatusSettingPopupWindow?.dismiss()
        }
    }

    private fun loadAd(adList: MutableList<AdInfoBean>?) {
        if (adList != null) {
            if (adList.isEmpty()) {
                if (bannerAD.isVisible()) {
                    bannerAD?.setAutoPlayAble(false)
                    bannerAD?.setAllowUserScrollable(false)
                    bannerAD?.setData(adList, null)
                    bannerAD.hide()
                }
                return
            }
            bannerAD.show()
            bannerAD?.setAdapter(bannerAdapter)
            bannerAD?.setDelegate(bannerItemClick)
            bannerAD?.setData(adList, null)
            bannerAD?.setAutoPlayAble(adList.size > 1)
//            bannerAD?.viewPager?.pageMargin = dp2px(10)
            if (adList.size > 1) {
                bannerAD?.currentItem = 0
            }
        } else {
            bannerAD?.hide()
        }
    }

    private val bannerAdapter by lazy {
        BGABanner.Adapter<SimpleDraweeView, AdInfoBean> { _, itemView, model, _ ->
            when (model?.resType) {
                BannerResType.Pic -> {
                    val screenWidth = ScreenUtils.screenWidthFloat.toInt()
                    val height = dp2px(72f)
                    ImageUtils.loadImageInPx(itemView, model.resUrl, screenWidth, height)
                }
            }
        }
    }

    private val bannerItemClick by lazy {
        BGABanner.Delegate<SimpleDraweeView, AdInfoBean> { _, _, model, _ ->
            when (model?.touchType) {
                BannerTouchType.Url -> {
                    val extra = Bundle()
                    extra.putString(BusiConstant.WEB_URL, model.touchValue)
                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                    jump(WebActivity::class.java, extra = extra)
                }
                BannerTouchType.Recharge -> {
                    //充值页面
                    ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
                }
                BannerTouchType.InviteFriend -> {
                    //邀请好友
                    RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                }
                else -> {
                }
            }
        }
    }

    //缘分View 移动动画
    private var mYuanFenTranslationAnimation: ObjectAnimator? = null

    /**
     * 缘分图标  动画移动到屏幕边侧
     */
    private fun yuanFenAnimationToSide() {
        val screenMiddleX = ScreenUtils.getScreenWidth() / 2
        val viewMiddleX = rl_yuanfen.x + rl_yuanfen.width / 2
        val targetX = if (viewMiddleX > screenMiddleX) {
            //View在屏幕右侧
            xMax
        } else {
            //View在屏幕左侧
            xMin
        }
        mYuanFenTranslationAnimation?.cancel()
        mYuanFenTranslationAnimation = ObjectAnimator.ofFloat(rl_yuanfen, "x", rl_yuanfen.x, targetX.toFloat())
        mYuanFenTranslationAnimation?.duration = 200
        mYuanFenTranslationAnimation?.start()
    }

    /**
     * 显示切换的PopopWindow
     */
    private fun showOnLinePopupWindow() {
        val status = mMessageViewModel.chatRoomData.value

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_online, null)
        mOnLineStatusSettingPopupWindow = PopupWindow(view, dp2px(90), dp2px(79))
        val drawable = GlobalUtils.getDrawable(R.drawable.bg_online_setting)
        mOnLineStatusSettingPopupWindow?.setBackgroundDrawable(drawable)
        mOnLineStatusSettingPopupWindow?.isOutsideTouchable = true
        val tv_unline = view.findViewById<View>(R.id.tv_unline)
        val tv_online = view.findViewById<View>(R.id.tv_online)
        if (status?.onlineStatus == ChatRoomBean.Invisible) {
            //不在线
            tv_unline.isSelected = true
        } else {
            tv_online.isSelected = true
        }
        tv_unline.onClickNew {
            //设置隐身
            mMessageViewModel.updateOnlineStatus(false)
            mOnLineStatusSettingPopupWindow?.dismiss()
        }
        tv_online.onClickNew {
            //设置在线
            mMessageViewModel.updateOnlineStatus(true)
            mOnLineStatusSettingPopupWindow?.dismiss()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mOnLineStatusSettingPopupWindow?.showAsDropDown(iv_online_arrow, 26, 5, Gravity.BOTTOM or Gravity.RIGHT)
        } else {
            mOnLineStatusSettingPopupWindow?.showAsDropDown(iv_online_arrow)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateMessageReceive(bean: EventMessageBean) {
        if (bean.onlyRefreshUnReadCount && mMessageViewModel.mStranger) {
            //在陌生人页面，只刷新未读数，解决异步导致的异常
            mMessageViewModel.refreshUnreadCount(bean.targetId)
        } else {
            mMessageViewModel.refreshConversation(bean.targetId, bean.stranger)
        }
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

    /**
     * 刷新系统消息和鹊友消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshSystemMsg(bean: SystemMessageRefreshBean) {
        mMessageViewModel.refreshSysMessage(bean.targetId)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun loginChange(event: LoginEvent) {
        if (!event.result) {
            //退出登录，清空会话列表
            mMessageViewModel.conversationListData.value = null
            mAdapter.setNewInstance(null)
        } else {
            //登录成功，重新获取数据
            mMessageViewModel.queryDataFlag.value = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectSuccess(event: RongConnectEvent) {
        if (RongCloudManager.RONG_CONNECTED == event.state) {
            //融云连接成功，加载数据
            mMessageViewModel.queryDataFlag.value = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun blockChange(event: MessageBlockEvent) {
        //先清空免打扰列表，再重新获取
        mPlayerMessageViewModel.blockListData.value = null
        mPlayerMessageViewModel.unreadList.clear()
        mPlayerMessageViewModel.getBlockedConversationList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun draftChange(event: DraftEvent) {
        //刷新草稿数据
        mMessageViewModel.updateDraft(event.userId)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            ActivityCodes.REQUEST_CODE_NORMAL -> {
//                if (resultCode == ActivityCodes.RESPONSE_CODE_REFRESH) {
//                    //也许是从系统消息页面返回，刷新列表一次
//                    mMessageViewModel.getConversationList()
//                    mMessageViewModel.queryRongPrivateCount()
//                }
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        mYuanFenTranslationAnimation?.cancel()
    }
}