package com.julun.huanque.core.ui.live.manager

import android.animation.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.alibaba.fastjson.JSONObject
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.ChatMessageBean
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.TplHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.viewmodel.ConnectMicroViewModel
import com.julun.huanque.common.viewmodel.PlayerMessageViewModel
import com.julun.huanque.common.viewmodel.VideoChangeViewModel
import com.julun.huanque.common.viewmodel.VideoViewModel
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.dialog.BirdDialogFragment
import com.julun.huanque.core.ui.live.fragment.BalanceNotEnoughFragment
import com.julun.huanque.core.ui.live.fragment.FirstRechargeFragment
import com.julun.huanque.core.ui.live.fragment.PrivateFragment
import com.julun.huanque.core.ui.share.LiveShareActivity
import com.julun.huanque.core.viewmodel.AnchorNoLiveViewModel
import com.julun.huanque.common.viewmodel.FirstRechargeViewModel
import com.julun.huanque.core.viewmodel.OrientationViewModel
import com.julun.huanque.core.viewmodel.PropViewModel
import com.julun.huanque.core.widgets.live.LiveRunwayView
import com.julun.huanque.core.widgets.live.message.MessageRecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_live_room.*
import kotlinx.android.synthetic.main.player_gesture_guide.*
import kotlinx.android.synthetic.main.view_live_bottom_action.view.*
import kotlinx.android.synthetic.main.view_live_header.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.topPadding
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2018/4/20
 * 直播间视图管理器  为了分离庞大的直播间[PlayerActivity]代码
 *
 **/
class PlayerViewManager(val context: PlayerActivity) {


    companion object {
        //屏幕宽度 固定常量
        val SCREEN_WIDTH = ScreenUtils.getScreenWidth()

        //屏幕高度 固定常量
        val SCREEN_HEIGHT = ScreenUtils.getScreenHeight()

        // 视频高度(屏幕高度的四分之三)
        val LIVE_HEIGHT: Int by lazy { SCREEN_WIDTH * 3 / 4 }

        // 手机横屏直播时视频高度(屏幕高度的四分之三)
        val LIVE_HEIGHT_NARROW: Int by lazy { SCREEN_WIDTH * 9 / 16 }

        // 状态栏高度
        val STATUS_TOP: Int by lazy { StatusBarUtil.getStatusBarHeight(CommonInit.getInstance().getContext()) }

        // 底部栏高度
        val ACTION_HEIGHT: Int by lazy {
            CommonInit.getInstance().getContext().resources.getDimensionPixelSize(R.dimen.live_action_view_height)
        }

        // 顶部栏高度
        val HEADER_HEIGHT: Int by lazy {
            CommonInit.getInstance().getContext().resources.getDimensionPixelSize(R.dimen.live_header_height)
        }
        val PK_PROCESS_HEIGHT: Int by lazy {
            CommonInit.getInstance().getContext().resources.getDimensionPixelSize(R.dimen.pk_process_high)
        }

        // 公聊区宽度
        val PUBLIC_CHAT_CLIENT_WIDTH =
            (SCREEN_WIDTH - CommonInit.getInstance()
                .getContext().resources.getDimensionPixelSize(R.dimen.pk_width) - DensityHelper.dp2px(8f))

        val DARK_COLOR = Color.parseColor("#80000000")

    }

    private val logger = ULog.getLogger(this.javaClass.name)


    private val viewModel: PlayerViewModel by context.viewModels()

    //首充ViewModel
    private val mFirstRechargeViewModel: FirstRechargeViewModel by context.viewModels()

    //    private var conversationViewModel: ConversationViewModel by context.viewModels()

    //    private val mBasePlayerViewModel: BasePlayerViewModel by context.viewModels()
    private val anchorNoLiveViewModel: AnchorNoLiveViewModel by context.viewModels()
    private val videoPlayerViewModel: VideoChangeViewModel by context.viewModels()
    private val mOrientationViewModel: OrientationViewModel by context.viewModels()

    //连麦ViewModel
    private val connectMicroViewModel: ConnectMicroViewModel by context.viewModels()

    //消息页面ViewModel
    private val mPlayerMessageViewModel: PlayerMessageViewModel by context.viewModels()
    //ZEGO推流相关
//    private lateinit var mZegoPublishViewModel: ZegoPublishViewModel by context.viewModels()

    //播放器相关的viewmodel
    private val mVideoViewModel: VideoViewModel by context.viewModels()

//    private val liveFollowListViewModel: LiveFollowListViewModel by context.viewModels()

    //道具相关ViewModel
    private val propViewModel: PropViewModel by context.viewModels()

    //屏幕高度 包含虚拟键
    private var screenHeight = ScreenUtils.getScreenHeightHasVirtualKey()

    // 屏幕高度
    private var screenWidth = ScreenUtils.getScreenWidth()


    // 公聊区高度
//    private var public_chat_height: Int = 0
    private var programId: Long = 0

    var isAppShow: Boolean = false

    //记录横竖屏状态的
    var isHorizontal: Boolean = false

    //是不是主播进入
    var isAnchorSelf: Boolean = false

    //是否需要显示公聊
    private var noNeedPublicView: Boolean = false

    //dialog管理器
    var mDialogManager: PlayerDialogManager


//    private val followAdapter by lazy { LiveFollowAdapter() }

    private val chatMessageClickCallback by lazy {
        object : MessageRecyclerView.OnChatMessageItemClickListener {
            override fun onOtherAction(action: String, extra: Any?) {
                showHeaderAndHideChatView()
                when (action) {
                    MessageRecyclerView.ACTION_FOLLOW -> {
                        viewModel?.subscribeSource = "公屏引导"
                        //todo
//                        subscribe()
//                        viewModel.guideToJoinFans()
                    }
                    MessageRecyclerView.ACTION_GRAB_BOX -> {
                        val programId = extra as? Long
                        if (programId != null) {
                            viewModel.checkoutRoom.value = programId
                        }
                    }
                    MessageRecyclerView.ACTION_JOIN_FANS -> {
                        if (extra is FansBean) {
                            viewModel?.actionBeanData?.value = BottomActionBean(ClickType.FANS)
                        }
                    }
                }
            }

            override fun onChatMessageItemClick(user: UserInfoBean?, bean: TplBean?) {
                //
                showHeaderAndHideChatView()
                viewModel.processOnTouch(bean ?: return)
            }
        }
    }

    init {
        logger.info("管理器初始化了")
        initViewModel()
        initView()
        initEvent()
        mDialogManager = PlayerDialogManager(context)
    }

    private fun initViewModel() {
        viewModel.userInfo.observe(context, Observer {
            refreshUserViewData(it ?: return@Observer)
        })
        propViewModel.colorfulState.observe(context, Observer {
//            if (it == true) {
//                //炫彩功能开启 并且不是神秘人状态
//                chatInputView.setColorfulState(true)
//            } else {
//                chatInputView.setColorfulState(false)
//            }
        })
        propViewModel.mysteriousState.observe(context, Observer {
//            if (it == true) {
//                chatInputView.mystery = true
//            } else {
//                chatInputView.mystery = false
//                //刷新用户信息，
//                viewModel.refreshUserInfoMystery()
//            }
        })
        mOrientationViewModel.screenTypeData.observe(context, Observer {
            screenSwitch(it)
        })


        viewModel.followStatusData.observe(context, Observer {
            logger.info("Player 关注状态 status = $it")
            if (it != null && it.isSuccess() && it.getT()?.userId != viewModel.programId) {
                //不是主播的关注数据
                return@Observer
            }
            context.liveHeader.setSubscribeEnable(true)
            if (it != null && it.isSuccess()) {
                modifySubscribe(it.requireT().follow != FollowStatus.False)
            }

        })

        viewModel.openOtherAction.observe(context, Observer {
            if (it != null) {
                logger.info("打开其他更多item:${it.itemName}")
                openOtherAction(it)
                viewModel.openOtherAction.value = null
            }
        })
        viewModel.chatModeState.observe(context, Observer {
            switchChatMode(it ?: false)
            SharedPreferencesUtils.commitBoolean(
                "${SessionUtils.getUserId()}${ParamConstant.CHAT_MODE}", it
                    ?: false
            )
        })

        mFirstRechargeViewModel.firstRechargeFlag.observe(context, Observer {
            if (it == true) {
                //首充按钮
                context.actionView.iv_first_recharge.show()
                //开始倒计时
                viewModel.firstRechargeCountDown()
            } else {
                context.actionView.iv_first_recharge.hide()
                viewModel.mFirstRechargeDisposable?.dispose()
            }
        })

        viewModel.notEnoughBalance.observe(context, Observer {
            if (CommonInit.getInstance().inSDK) {
                //todo
            } else {
//                val oneInfo = mFirstRechargeViewModel?.oneYuanInfo?.value
//                if (mConfigViewModel.horizonState.value != true && oneInfo?.status == OneYuanInfo.Unclaimed) {
//                    //横屏 不显示该弹窗
//                    var confirm = false
//                    val myDialog = NewAlertDialog(context,dissmissWhenTouchOutSide = true).setDialogBg(R.drawable.lm_common_shape_bg_rectangle_white_10)
//                        .setPromptFirstText(GlobalUtils.getString(R.string.one_yuan_balance_not_enough_title), 16f, R.color.black_333)
//                        .setPromptSecondText(GlobalUtils.getString(R.string.one_yuan_balance_not_enough_attention), 14f, R.color.black_333)
//                        .setPromptBtnCancel(GlobalUtils.getString(R.string.one_yuan_give_up), 16f, R.color.light_gray)
//                        .setPromptBtnRight(GlobalUtils.getString(R.string.receive_immediately), 16f, R.color.btn_color,isBold = false)
//                    myDialog.showAlert(NewAlertDialog.NewAlertDialogCallback(
//                        onRight = {
//                            confirm = true
//                            showOneYuanDialog(oneInfo, false)
//                        }
//                    ))
//                    myDialog.setOnDismissListener { _ ->
//                        if (!confirm) {
//                            showNotEnoughBalanceAlert(it ?: return@setOnDismissListener)
//                        }
//                    }
//                } else {
                showNotEnoughBalanceAlert(it ?: return@Observer)
//                }
            }
        })



        viewModel.actionBeanData.observe(context, Observer {
            if (it != null) {
                val actionValue = it.actionValue
                when (it.type) {
                    ClickType.CHAT_INPUT_BOX -> {
                        //打开公聊
                        if (mOrientationViewModel.horizonState.value == true && isAnchorSelf) {
                            //主播使用横屏下使用@功能 切回竖屏
                            viewModel.actionBeanData.value = BottomActionBean(ClickType.SWITCH_SCREEN, ScreenType.SP)
                        }
//                        closeGuardView()
                        val defaultContent = actionValue as? String ?: ""
                        openChatInputBox(defaultContent, it.innerActionType)
                    }
                    ClickType.GIFT -> {
                        //打开送礼弹窗
                        mDialogManager.openGiftDialog()
                    }
                    ClickType.CLOSE -> {
                        //关闭直播间
                        context.finish()
                    }
                    ClickType.GAME -> {
                        //打开游戏界面

                    }
                    ClickType.BIRD -> {
                        //打开养鹊
                        mDialogManager.openDialog(BirdDialogFragment::class.java)
                    }
                    ClickType.PRIVATE_MESSAGE -> {
                        //打开私聊
                        openPrivateDialog()
                    }


                    ClickType.PUBLISH_SETTING -> {
                        //推流设置页面
//                        mDialogManager.openBeautySetFragment()
                    }
                    ClickType.USER_MORE_SETTING -> {
                        //用户更多页面
//                        mDialogManager.showUserMoreSetting()
                        //打开分享
                        val baseData = viewModel.roomBaseData ?: return@Observer
//                        ARouter.getInstance().build(ARouterConstant.INVITE_SHARE_ACTIVITY)
//                            .withString(IntentParamKey.TYPE.name, ShareFromModule.Program).withSerializable(
//                                IntentParamKey.LIVE_INFO.name,
//                                MicAnchor(prePic = baseData.prePic).apply {
//                                    programName = baseData.programName
//                                    programId = baseData.programId
//                                    headPic = baseData.headPic
//                                }
//                            ).navigation()
                        context.startActivity(Intent(context, LiveShareActivity::class.java).apply {
                            putExtra(IntentParamKey.LIVE_INFO.name, MicAnchor(prePic = baseData.prePic).apply {
                                programName = baseData.programName
                                programId = baseData.programId
                                headPic = baseData.headPic
                            })
                        })
                    }
                    ClickType.ANCHOR_MORE_SETTING -> {
                        //主播更多页面
//                        showAnchorMoreSetting(actionValue == true)
                    }
                    ClickType.SWITCH_SCREEN -> {
                        //屏幕横竖屏切换
                        if (isAnchorSelf && !videoPlayerViewModel.isSingleAnchor()) {
                            val strId =
                                if (connectMicroViewModel.inPk.value == true) {
                                    //处于PK当中
                                    R.string.attention_switch_pk
                                } else {
                                    R.string.attention_switch_connect
                                }
                            ToastUtils.show(strId)
                            return@Observer
                        }

                        var screenType: String
                        if (actionValue == ScreenType.SP && context.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            //切换竖屏
                            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            screenType = ScreenType.SP
                            if (isAnchorSelf) {
//                                viewModel.cutoverScreenType(screenType)
                            }
                        } else if (actionValue == ScreenType.HP && context.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            //切换横屏
                            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            screenType = ScreenType.HP
                            if (isAnchorSelf) {
//                                viewModel.cutoverScreenType(screenType)
                            }
                        }
                    }
                    ClickType.FIRST_RECHARGE -> {
                        //首充
                        val firstRechargeBean = mFirstRechargeViewModel.firstRechargeBean.value
                        if (firstRechargeBean == null) {
                            mFirstRechargeViewModel.getFirstRechargeData()
                        } else {
                            mFirstRechargeViewModel.firstRechargeBean.value = firstRechargeBean
                        }
                    }
//                    ClickType.SHIELD -> {
//                        //屏蔽功能
//                        if (!isAnchorSelf && mConfigViewModel.horizonState.value == true) {
//                            //用户身份
//                            if (actionValue is Boolean) {
//                                if (actionValue) {
//                                    //屏蔽弹幕
//                                    context.frame_danmu?.hide()
//                                } else {
//                                    //开启弹幕
//                                    context.frame_danmu?.show()
//                                }
//                            }
//                        }
//                    }
                    else -> {
                    }
                }
//                viewModel.actionBeanData.value = null
            }
        })

        viewModel.bagChangeFlag.observe(context, Observer {
            context.actionView.showGiftRedDot(it == true)
        })

        viewModel.gotoWeb.observe(context, Observer {
            goToWeb(it ?: return@Observer)
        })
        //conversationViewModel
        //清屏功能
        viewModel.clearScreen.observe(context, Observer {
            if (it != null) {
                showOrHideContentView(it)
                viewModel.clearScreen.value = null
            }
        })

        //刷新布局
        videoPlayerViewModel.refreshLayout.observe(context, Observer {
            if (it == true) {
                logger.info("需要刷新布局")
                context.rootContainer.post { changeShowTypeLayout2() }
                videoPlayerViewModel.refreshLayout.value = null
            }
        })

        connectMicroViewModel.notarizePKShowState.observe(context, Observer {
            if (it != null) {
                mDialogManager.openNotarizePKFragment(it)
            }
        })

//        viewModel.guideToFollow.observe(context, Observer {
//            if (it != null) {
//                val baseData = viewModel.baseData.value ?: return@Observer
//                val data = viewModel.loginSuccessData.value ?: return@Observer
//                if (it == PlayerViewModel.messageGuideToFollowDelay) {
//                    if (data.user == null || !SessionUtils.getIsRegUser() || !data.isFollowed) {
//                        addOtherPublicMessageItem(
//                            ChatMessageBean(
//                                ChatFollow(anchorUrl = baseData.headPic),
//                                MessageRecyclerView.STYLE_FOLLOW
//                            )
//                        )
//                    }
//                } else if (it == PlayerViewModel.dialogGuideToFollowDelay) {
//                    if (data.user == null || !SessionUtils.getIsRegUser() || !data.isFollowed) {
////                        viewModel.openDialog.value = DialogTypes.DIALOG_GUIDE_FOLLOW
//                        if (!mDialogManager.isFragmentShow(PlanetFragment::class.java)) {
//                            //星球霸主 弹窗处于显示状态
//                            mDialogManager.openDialog(
//                                GuideFollowFragment::class.java,
//                                reuse = true,
//                                order = OrderDialogManager.LOCAL_ORDER
//                            )
//                        }
//                    }
//                }
//                viewModel.guideToFollow.value = null
//            }
//        })
        viewModel.guideToSpeak.observe(context, Observer {
            if (it != null) {
                if (it) {
                    //显示引导发言
                    //横屏情况下，不需要显示引导发言弹窗
                    if (!isHorizontal) {
                        StorageHelper.setNeedGuideToSpeak(false)
                        context.guideToSendMessageView.show()
                    }
                } else {
                    //隐藏引导发言
                    context.guideToSendMessageView.hide()
                }
                viewModel.guideToSpeak.value = null
            }
        })


        viewModel.guideToFansJoin.observe(context, Observer {
            it ?: return@Observer
            if (it) {
                val baseData = viewModel.baseData.value ?: return@Observer
                val data = viewModel.loginSuccessData.value ?: return@Observer
                if (data.user == null || !SessionUtils.getIsRegUser() || !data.groupMember) {
                    addOtherPublicMessageItem(
                        ChatMessageBean(
                            FansBean(anchorUrl = baseData.headPic),
                            MessageRecyclerView.STYLE_FANS_JOIN
                        )
                    )
                }
                viewModel.guideToFansJoin.value = null
            }
        })
        viewModel.addCustomMsgData.observe(context, Observer {
            if (it != null && it is PlanetMessageBean) {
                //打开星球霸主消息
                addOtherPublicMessageItem(ChatMessageBean(it, MessageRecyclerView.STYLE_PLANET))
            }
        })

        viewModel.switchList.observe(context, Observer {
            it ?: return@Observer
            preUpAndDownData()
        })
//        liveFollowListViewModel.queryDataList.observe(context, Observer {
//            showFollowData(it)
//        })
//        if (!liveFollowListViewModel.finalState.hasObservers()) {
//            liveFollowListViewModel.finalState.observe(context, Observer {
//                followListFinal()
//            })
//        }

        viewModel.openGiftAndSelPack.observe(context, Observer {
            it ?: return@Observer
            //打开礼物面板并锁定背包
//            openGiftView()
        })


        mFirstRechargeViewModel.firstRechargeBean.observe(context, Observer {
            if (it != null) {
                FirstRechargeFragment().show(context.supportFragmentManager, "FirstRechargeFragment")
            }
        })

    }


    /**
     * 设置ProgramId
     */
    fun setProgramId(programId: Long) {
        this.programId = programId
    }

    /**
     * 显示主播更多设置页面
     */

    /***
     * 加载view跟playerActivity一样
     */
    private fun initView() {
//        val followList = context.follow_recyclerView
//        val followRefresh = context.liveFollowRefreshView
//        followList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//        followList.adapter = followAdapter
//        MixedHelper.setSwipeRefreshStytle(followRefresh, context)
    }

    private fun initEvent() {
//        val followRefresh = context.liveFollowRefreshView
//        followRefresh.setOnRefreshListener {
//            getFollowList(true)
//        }
//
//        followAdapter.loadMoreModule.setOnLoadMoreListener {
//            getFollowList(false)
//        }
//
//        followAdapter.setOnItemClickListener { _, _, position ->
//            val item = followAdapter.getItem(position)
//            item?.let {
//                viewModel.checkoutRoom.value = it.programId
//            }
//        }

        context.publicMessageView.messageRecyclerView.setOnChatMessageItemClickListener(chatMessageClickCallback)

    }

    //处理关注列表数据显示
//    private fun followListFinal() {
//        val followRefresh = context.liveFollowRefreshView
//        followRefresh.isRefreshing = false
//        val emptyView = LayoutInflater.from(context).inflate(R.layout.layout_empty_data_text, null)
//        followAdapter.setEmptyView(emptyView)
//    }

    fun addOtherPublicMessageItem(chatMessageBean: ChatMessageBean) {
        val publicMessageView = context.publicMessageView
        publicMessageView.messageRecyclerView.addOtherItem(chatMessageBean)
    }

    fun startRunwayAnimation(bean: TplBean, delaySeconds: Int = 0) {
        val liveRunwayView = context.liveRunwayView

        //如果是答题活动过滤跑道消息
//        val roomData = viewModel.loginSuccessData.value
//        if (roomData?.teaMap?.teaDayProgram == true) {
//            return
//        }
        bean.context ?: return
        bean.context?.cacheStartTime = System.currentTimeMillis()
//        val beanExtraContext = bean.context ?: return
//        val runwayCache = RunwayCache(beanExtraContext.cacheIt, beanExtraContext.seconds, beanExtraContext.cacheValue)
//        giftFragment?.handleRefreshRunwayPriceChange(runwayCache)
        liveRunwayView.loadLastRunwayMessage(bean, delaySeconds, onCachedRunwayMessageChangesListener)
    }

    //记录当前的跑道缓存值 主要是在跑道发生变化时记录给SendGiftFragment重建时使用
    private var currentRunwayCache: RunwayCache? = null
    private val onCachedRunwayMessageChangesListener: LiveRunwayView.OnCachedMessageChangesListener
            by lazy {
                object : LiveRunwayView.OnCachedMessageChangesListener {
                    override fun onCachedMessageChanges(messageContext: TplBeanExtraContext) {
                        val runwayCache =
                            RunwayCache(messageContext.cacheIt, messageContext.seconds, messageContext.cacheValue.toLong())
                        logger.info("发送跑道缓存变化${messageContext.cacheValue}")
                        //时间为零是清空的意思 currentRunwayCache保留的缓存值是请求后台获取的 SendGiftFragment创建时会用到
                        // 而该监听会通知所有的新的跑道缓存值
                        //此处的处理是为了达到缓存值的同步
                        currentRunwayCache = if (messageContext.seconds > 0) runwayCache else null
                    }
                }
            }

    /**
     *   打开更多操作 触发点[UserMoreActionFragment.openAction]
     */
    private fun openOtherAction(action: MenuActionItem) {
//        when (action.itemCode) {
//            MenuActionType.MOECOINTASK -> {
//                openTaskView(TaskType.Daily)
//            }
//            MenuActionType.MOECOINMARKET -> {
//                showNotLoginAlert {
//                    ARouter.getInstance().build(ARouterConstant.NEW_USER_STORE_ACTIVITY).navigation()
//                }
//            }
//            MenuActionType.LUCKYROTATE -> {
////                viewModel.openDialog.value = DialogTypes.DIALOG_LUCKYPAN
//                mDialogManager.openDialog(LuckyPanFragment::class.java)
//            }
//            MenuActionType.KINGWAR -> {
//                showNotLoginAlert {
//                    if (action.itemUrl.isNotEmpty()) {
//                        val map = JsonUtil.toJsonMap(action.extJsonCfg)
//                        val data = map?.get("ratio") as? BigDecimal?
//                        val ratio = data?.toFloat()
//                        mDialogManager.openKingGameDialog(action.itemUrl, ratio)
//                    } else {
//                        ToastUtils.show(context.resources.getString(R.string.not_support_game))
//                    }
//                }
//            }
//            MenuActionType.FACTORYCAR -> {
//                //豪车工厂
////                viewModel.openDialog.value = DialogTypes.DIALOG_LUXURY_CAR
//                viewModel.showDialog.value = LuxuryCarFactoryFragment::class.java
//            }
//            MenuActionType.TURNCARD -> {
//                //翻牌游戏
////                viewModel.openDialog.value = DialogTypes.DIALOG_FLIP_CARD
//                viewModel.showDialog.value = FlipCardsDialogFragment::class.java
//            }
//            MenuActionType.PASSLEVEL -> {
////                mDialogManager.controlDialog(DialogTypes.DIALOG_PASSTHROUGH, Operators.OPEN, BaseDialogBean().apply { paramToInt = PassThroughFragment.PASS_PAGE_MAIN })
//                mDialogManager.openDialog(clazz = PassThroughFragment::class.java, builder = {
//                    PassThroughFragment.newInstance(viewModel.programId, PassThroughFragment.PASS_PAGE_MAIN)
//                })
//            }
//            MenuActionType.STAGEPK -> {
//                //pk段位赛
////                viewModel.openDialog.value = DialogTypes.DIALOG_PK_RANK
//                viewModel.showDialog.value = PkRankMainDialogFragment::class.java
//            }
//            MenuActionType.TACTGAMEYY -> {
//                //邀友活动
//                huanqueService.getService(IShareService::class.java)?.onAwakenMini(context, action.miniUserName
//                        ?: "", action.itemUrl)
//            }
//        }
    }

    // 余额不够
    fun showNotEnoughBalanceAlert(bean: NotEnoughBalanceBean) {
//        ToastUtils.show(R.string.balance_not_enough)
//            if (!mDialogManager.isFragmentShow(RechargeDialogFragment::class.java)) {
        mDialogManager.openDialog(
            BalanceNotEnoughFragment::class.java,
            builder = { BalanceNotEnoughFragment.newInstance(true) },
            reuse = true
        )
//
//            }

    }

    //计算高度 需要网络回调roomdata
    fun calComponentHeight() {
        val roomData = viewModel.loginSuccessData.value
        roomData?.runway?.let {
            try {
                if (it.message.isEmpty()) {
                    return@let
                }
                val runwayMessage: RunWayMessage = it
                val baseData: BaseData = JsonUtil.deserializeAsObject(runwayMessage.message, BaseData::class.java)
                val jsonObject = baseData.data as JSONObject
                val jsonString = jsonObject.toJSONString()
                val tplBean = TplHelper.decodeMessageContent(jsonString)
                tplBean.preProcess()
                //这里的跑到消息,都可以只播放一遍
                tplBean.context?.canOnlyPlayOneTime = true
                tplBean.context?.seconds = roomData?.runway?.seconds ?: 0//将缓存秒数赋值
                startRunwayAnimation(tplBean, 3)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //这里先初始化一次高度 防止一次都不初始化 公聊就变成全屏的了
        if (viewModel.chatModeState.value == true) {
            switchChatMode(true)
        } else {
            val viewHeight = ScreenUtils.computeUsableHeight(context.rootContainer)
            val pmViewLp = context.publicMessageView.layoutParams as ConstraintLayout.LayoutParams
            pmViewLp.height = viewHeight - SCREEN_WIDTH * 3 / 4 - HEADER_HEIGHT - ACTION_HEIGHT
            context.publicMessageView.requestLayout()
        }


    }

    private fun refreshUserViewData(data: UserInfo) {
//        roomData!!.speakCount = data.speakCount
        viewModel.roomData?.user = data
        // 重新设置聊天框输入最大字数
        val speakCount = if (data.mystery) {
            40
        } else {
            GlobalUtils.speakCount(viewModel.isAnchor, data)
        }
//
//        chatInputView.setInputTextMaxSize(speakCount)
//
//
//        //贵族弹幕输入初始化
//        chatInputView.setFreeDanMu(data.hasFreeDanMu, data.danMuCard)
//        chatInputView.mystery = data.mystery
//        chatInputView.initRoyalDanmu(data.royalLevel)
        RongCloudManager.resetUserInfoData(ChatUtils.createRoomUserChat(viewModel.roomData, viewModel.baseData.value, false))
    }

    /**
     * 切换聊天模式
     * @param chatMode true 开启聊天模式  false 关闭聊天模式
     */
    fun switchChatMode(chatMode: Boolean) {
        if (SessionUtils.getUserType() == UserType.Anchor) {
            //主播身份不切换聊天模式
            return
        }
//        context.chatInputView.mChatMode = chatMode
        if (chatMode) {
            //修改聊天高度
            val oriHeight = context.publicMessageView.height
            if (oriHeight > ScreenUtils.getScreenHeight() * 0.7) {
                //已经是聊天模式，不需要重新设置
                return
            }
            val params = context.publicMessageView.layoutParams as? ConstraintLayout.LayoutParams
            val tempHeight = ScreenUtils.getScreenHeightHasVirtualKey() - DensityHelper.dp2px(140 + 90f)
            params?.height = tempHeight
            params?.bottomMargin = DensityHelper.dp2px(90f)
            params?.topMargin = 0
            context.publicMessageView?.requestLayout()

        } else {
            changeShowTypeLayout2()
        }
    }

    //    private var lifecycleObserver: GenericLifecycleObserver? = null
    @SuppressLint("RestrictedApi")
    fun showPaySuccessDialog() {
        //todo
//        val lifecycleObserver = object : GenericLifecycleObserver {
//            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                logger.info("context lifecycle:$event")
//                if (event == Lifecycle.Event.ON_RESUME) {
//                    if (GlobalDateManager.isPayShowed) {
//                        GlobalDateManager.isPayShowed = false
//                    } else {
//                        MyAlertDialog(context).showAlertWithOKNoTitle(context.getString(R.string.recharge_success), MyAlertDialog.MyDialogCallback(onCancel = {
////                            mFirstRechargeViewModel?.getFirstRecharge()
//                        }, onRight = {
////                            mFirstRechargeViewModel?.getFirstRecharge()
//                        }))
//                        if (isRefreshFirstRecharge) {
//                            mFirstRechargeViewModel?.getFirstRecharge()
//                            isRefreshFirstRecharge = false
//                        }
//                    }
//                    context.lifecycle.removeObserver(this)
//                }
//            }
//
//        }
//        context.lifecycle.addObserver(lifecycleObserver)
    }


    /**
     * 礼物面板数量变化
     */
//    fun markGiftFragmentNeedRefreshAll() {
//        if (giftFragment != null && giftFragment!!.isAdded) {
//            giftFragment?.dismiss()
//        }
//        giftFragment = null
//    }

    /**
     * 刷新用户的余额
     */
    fun refreshBalance() {
//        RechargeViewModel.synchronousBalance(context)
    }


    //显示或隐藏面包布局hide=true代表隐藏 needAnimator=true代表需要清屏动画
    fun showOrHideContentView(hide: Boolean, needAnimator: Boolean = true) {
        if (mOrientationViewModel.horizonState.value == true) {
            //横屏不执行隐藏
            return
        }
        if (needAnimator) {
            if (hide) {
                context.surface_view?.clearViews()
            } else {
                context.surface_view?.openViews()
            }
        } else {
            if (hide) {
                context.surface_view.hide()
                context.chat_layout.hide()
            } else {
                context.surface_view.show()
                context.chat_layout.show()
            }
        }
    }


    // 动画显示header头，隐藏软键盘和聊天框，显示底部组件
    fun showHeaderForAnimation(
        callbackStart: () -> Unit = {
//            val chatInputView = context.ll_input
            val publicMessageView = context.publicMessageView
            //todo
//        chatInputView.hideEmojiViewAndKeyboard()
//        chatInputView.hide()
            switchChatViewVisible(false)
//        vaBottomBar.displayedChild = BOTTOM_ACTIONVIEW_INDEX
            if (!noNeedPublicView) {
                publicMessageView.show()
            }
        }, callbackEnd: () -> Unit = {}
    ) {
        val surfaceView = context.surface_view
        val liveHeader = context.liveHeader

        surfaceView.show()
        val anim = ObjectAnimator.ofFloat(liveHeader, "translationY", HEADER_HEIGHT * -1f, 0f).setDuration(300)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                callbackStart()
//                if (viewModel.chatModeState.value == true) {
//                    switchChatMode(true)
//                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                onAnimationEnd(animation)
            }

            override fun onAnimationEnd(animation: Animator?) {
                callbackEnd()
                //
//                banner.show()
//                if (banner_RT == null) {
//                    initBanner(LiveHomeBannerLocation.RIGHT_TOP)
//                }
//                banner_RT?.show()
//                animationFragment.switchVisiable(true)
            }
        })
        anim.start()
    }

    // 动画隐藏header头，显示软键盘和聊天框，隐藏底部组件
    fun hideHeaderForAnimation(callback: () -> Unit = {}) {

        val surfaceView = context.surface_view
//        val chatInputView = context.ll_input
        val liveHeader = context.liveHeader
        //聊天的输入窗口弹起的时候,无论如何,先关掉跑道组件
//        liveRunwayView.hide()
//        littleBanner.hide()
//        animationFragment.switchVisiable(false)
        val anim = ObjectAnimator.ofFloat(liveHeader, "translationY", 0f, HEADER_HEIGHT * -1f).setDuration(300)
        anim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                //直接隐藏根布局 就不需要一个一个单独隐藏了
                surfaceView.hide()
//                chatInputView.show()
//                vaBottomBar.displayedChild = BOTTOM_CHATVIEW_INDEX
                switchChatViewVisible(true)
                //todo
//                chatInputView.showKeyboard()
            }

            override fun onAnimationEnd(animation: Animator?) {
                callback()
            }

        })
        anim.start()
    }

    /**
     * 显示主布局 隐藏聊天输入框
     */
    fun showHeaderAndHideChatView(): Boolean {

        val chatInputView = context.ll_input
        val publicMessageView = context.publicMessageView
        //如果跑道正在播放中,则再显示出来
//        if (liveRunwayView.isMessagePlay) {
//            liveRunwayView.show()
//        }
        if (chatInputView.isVisible()) {
            // emojiview显示的情况下，先隐藏整个聊天框，动画完了再显示底部actionview
            //todo
//            if (chatInputView.getEmojiVisible() == View.GONE) {
//                showHeaderForAnimation({
//                    chatInputView.hideAndReset()
//                    chatInputView.hide()
//                    switchChatViewVisible(false)
//                }, {
//                    //                    vaBottomBar.displayedChild = BOTTOM_ACTIONVIEW_INDEX
//                    if (!noNeedPublicView) {
//                        publicMessageView.show()
//                    }
//                })
//            } else {
//                showHeaderForAnimation()
//            }
            return true
        }
        return false
    }

    /**
     * 现在只有两种情况 界面显示输入框和不显示输入框
     * 有输入框就不要下边距 否则就要加下边距
     */
    fun switchChatViewVisible(isVisible: Boolean) {
        val publicFrameParams: ConstraintLayout.LayoutParams? =
            context.publicMessageView?.layoutParams as ConstraintLayout.LayoutParams?
                ?: return
        val marginHeight = if (viewModel.chatModeState.value == true) {
            DensityHelper.dp2px(90f)
        } else {
            ACTION_HEIGHT
        }
        val publicBottomMargin = if (isVisible) 0 else marginHeight
        publicFrameParams!!.bottomMargin = publicBottomMargin
        context.publicMessageView?.layoutParams = publicFrameParams
        if (viewModel.chatModeState.value == true) {
            switchChatMode(true)
        }
    }

    private var hideMenuDisPose: Disposable? = null
    fun delayHideMenu() {
        if (isAnchorSelf) {
            return
        }
        if (hideMenuDisPose?.isDisposed == false) {
            hideMenuDisPose?.dispose()
        }
        hideMenuDisPose = Observable.timer(6, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (!isMenuAniDoing) {
                hideMenuForAnimation()
            }

        }
    }

    /**
     * 切换竖屏时使用
     */
    fun showMenu() {
        hideMenuDisPose?.dispose()
        showMenuForAnimation(false)
    }

    /**
     * 显示或隐藏横屏的菜单
     */
    fun switchMenuShow() {
        if (isAnchorSelf) {
            return
        }
        if (hideMenuDisPose?.isDisposed == false) {
            hideMenuDisPose?.dispose()
        }
        if (isMenuAniDoing) {
            return
        }
        if (isMenuShow) {
            hideMenuForAnimation()
//            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            showMenuForAnimation(true)
//            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private var isMenuShow = true
    private var isMenuAniDoing = false

    /**
     *   动画显示header 显示底部组件
     *
     */

    private fun showMenuForAnimation(amoment: Boolean) {

//        val lv9BoxRunwayView = context.lv9BoxRunwayView
        val liveRunwayView = context.liveRunwayView
        val liveHeader = context.liveHeader
        val animHeader = ObjectAnimator.ofFloat(liveHeader, "translationY", HEADER_HEIGHT * -1f, 0f).setDuration(300)

//        val animRunwayLv9 = ObjectAnimator.ofFloat(lv9BoxRunwayView, "translationY", HEADER_HEIGHT * -1f, 0f).setDuration(300)
        val height = HEADER_HEIGHT + DensityHelper.dp2px(50)
        val animRunway = ObjectAnimator.ofFloat(liveRunwayView, "translationY", height * -1f, 0f).setDuration(300)
        val bHeight = context.resources.getDimensionPixelOffset(R.dimen.live_action_view_height)
        val bottomAnim = ObjectAnimator.ofFloat(context.actionView, "translationY", bHeight * 1f, 0f).setDuration(300)
        val aniSet = AnimatorSet()
        aniSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isMenuAniDoing = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isMenuAniDoing = false
                isMenuShow = true
                if (amoment) {
                    delayHideMenu()
                }
            }

        })
        aniSet.playTogether(animHeader, animRunway, bottomAnim)
        aniSet.start()
    }

    // 动画隐藏header头隐藏底部组件
    private fun hideMenuForAnimation(/*callback: () -> Unit = {}*/) {

//        val lv9BoxRunwayView = context.lv9BoxRunwayView
        val liveRunwayView = context.liveRunwayView
        val liveHeader = context.liveHeader
        //聊天的输入窗口弹起的时候,无论如何,先关掉跑道组件
//        liveRunwayView.hide()
//        littleBanner.hide()
//        animationFragment.switchVisiable(false)
        val animHeader = ObjectAnimator.ofFloat(liveHeader, "translationY", 0f, HEADER_HEIGHT * -1f).setDuration(300)

//        val animRunwayLv9 = ObjectAnimator.ofFloat(lv9BoxRunwayView, "translationY", 0f, HEADER_HEIGHT * -1f).setDuration(300)
        val height = HEADER_HEIGHT + DensityHelper.dp2px(50)
        val animRunway = ObjectAnimator.ofFloat(liveRunwayView, "translationY", 0f, height * -1f).setDuration(300)
        val bHeight = context.resources.getDimensionPixelOffset(R.dimen.live_action_view_height)
        val bottomAnim = ObjectAnimator.ofFloat(context.actionView, "translationY", 0f, bHeight * 1f).setDuration(300)
        val aniSet = AnimatorSet()
        aniSet.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                isMenuAniDoing = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isMenuAniDoing = false
                isMenuShow = false
            }

        })
        aniSet.playTogether(animHeader, animRunway, bottomAnim)
        aniSet.start()
    }

    /**
     * 打开公聊输入框
     */
    fun openChatInputBox(defaultContent: String, innerActionType: String) {
        //closeDialogIfExists()
        mDialogManager.hideAllDialog()
        context.ll_input.show()
        context.actionView.hide()
        val et = context.edit_text

        val deC = if (innerActionType == ClickType.SEND_MESSAGE) {
            //发言功能
            defaultContent
        } else {
            //@功能
            if (defaultContent.isNotEmpty()) {
                "@$defaultContent "
            } else {
                ""
            }
        }
        if (deC.isNotEmpty()) {
            et.setText(deC)
        }
        //
        et.setSelection(et.text.length)
        et.requestFocus()
//        logger.info("Player publicMessageView x = ${context.publicMessageView.x},y = ${context.publicMessageView.y},width = ${context.publicMessageView.width},height = ${context.publicMessageView.height}")
//
//            //引导发言取消还没显示的不再显示 已经显示的隐藏掉
//            viewModel.guideToSpeakDisposable?.dispose()
//            context.guideToSendMessageView.hide()

    }

    /**
     * 隐藏公聊的输入弹窗
     */
    fun hideInputFragment() {
//        mDialogManager.hideFragment(InputPlayerFragment::class.java)
    }


    /**
     * 显示私聊弹窗
     */
    fun openPrivateDialog() {
        mDialogManager.openDialog(PrivateFragment::class.java, reuse = true)
    }

    // 关注、取消关注后，修改直播间数据
    private fun modifySubscribe(isSubscribed: Boolean?) {
        context.subscribeAnchor.isEnabled = true
        if (isSubscribed == null) {
            return
        }
        if (isSubscribed) {
//            ToastUtils.show(context.getString(R.string.subscribe_success))
            //
            val publicMessageView = context.publicMessageView
            val baseData = viewModel.baseData.value ?: return
            publicMessageView.messageRecyclerView.changeItemLayout(
                ChatMessageBean(
                    ChatFollow(true, anchorUrl = baseData.headPic),
                    MessageRecyclerView.STYLE_FOLLOW
                )
            )

//            val notify = NotifyManager.checkNotifyPeriodical(context)
            //不提醒时才弹成功
//            if (!notify) {
            //关注成功提示
//                mDialogManager.openDialog(FollowSuccessFragment::class.java)
//            }

        } else {
//            ToastUtils.show(R.string.unsubscribe_success)
        }
        val roomData = viewModel.loginSuccessData.value
        if (roomData != null) {
            roomData.follow = isSubscribed
            // 修改headerView图标显示
            context.liveHeader.subscribeSuccess(isSubscribed)
        }
//        huanqueService.getService(IStatistics::class.java)?.onSubscribe("$programId", viewModel?.subscribeSource
//                ?: "", isSubscribed)

    }

    /**
     * 完全全屏的切换
     */
    fun changeShowTypeLayout2() {
        val surfaceView = context.surface_view
        val playerPanel = context.playerPanel
        val anchorNoOnline = context.anchor_no_online
        val isInPk = connectMicroViewModel.inPk.value == true
        logger.info("屏幕的高度：" + screenHeight + "屏幕宽度：" + screenWidth + "状态栏高度：" + STATUS_TOP)
        val ppLp = playerPanel.layoutParams as FrameLayout.LayoutParams
        val anoLp = anchorNoOnline.layoutParams as FrameLayout.LayoutParams
//        val trLp = mThemeRoomView?.layoutParams as? FrameLayout.LayoutParams
        val publicView = context.publicMessageView
        val shadowView = context.app_bottom_shadow
        //猜字谜气泡
//        val bubbleView = context.flPublicBubbleMsg.layoutParams as ConstraintLayout.LayoutParams
        //视频相关，宽度保持为屏幕宽度
        ppLp.width = ScreenUtils.getScreenWidth()
        val pbLp = publicView.layoutParams as ConstraintLayout.LayoutParams
        if (isHorizontal) {
//            surfaceView.topPadding = STATUS_TOP
            ppLp.topMargin = 0
            ppLp.height = screenHeight
            playerPanel.setPadding(0, 0, 0, 0)
            //设置全屏
            context.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
//            StatusBarUtil.setTransparent(context)
//            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            //如果时主播显示公聊宽高固定 如果是用户直接隐藏
            if (isAnchorSelf) {
                noNeedPublicView = false
                publicView.show()
                pbLp.height = SCREEN_WIDTH / 2
                pbLp.width = PUBLIC_CHAT_CLIENT_WIDTH
                pbLp.bottomMargin = DensityHelper.dp2px(4.5f)
            } else {
                noNeedPublicView = true
                publicView.hide()
            }
            shadowView.hide()
        } else {
            noNeedPublicView = false
            publicView.show()
            shadowView.show()
            pbLp.bottomMargin = ACTION_HEIGHT
//            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            //取消全屏
            context.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            //是否是主播端横屏
            val screenType = mOrientationViewModel.screenTypeData.value
            //添加是不是手机直播的的转换 主播模式同手机直播 黑色状态栏
            val verticalFullScreen = (screenType != ScreenType.HP) && isAppShow && videoPlayerViewModel.isSingleAnchor()
            if (verticalFullScreen) {
                ppLp.height = screenHeight /*- STATUS_TOP*/
                ppLp.topMargin = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    StatusBarUtil.setColor(context, ContextCompat.getColor(context, R.color.black))
                    StatusBarUtil.setTransparent(context)
                    surfaceView.topPadding = STATUS_TOP
                    anoLp.topMargin = HEADER_HEIGHT + STATUS_TOP
                } else {
                    surfaceView.topPadding = 0
                    anoLp.topMargin = HEADER_HEIGHT
                }
            } else {
                val realHeight = if (screenType == ScreenType.HP) {
                    LIVE_HEIGHT_NARROW
                } else {
                    LIVE_HEIGHT
                }
                ppLp.height = realHeight
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    StatusBarUtil.setColor(context, ContextCompat.getColor(context, R.color.black))
                    StatusBarUtil.setTransparent(context)
                    surfaceView.topPadding = STATUS_TOP
                    ppLp.topMargin = HEADER_HEIGHT + STATUS_TOP
                    anoLp.topMargin = HEADER_HEIGHT + STATUS_TOP
                } else {
                    surfaceView.topPadding = 0
                    ppLp.topMargin = HEADER_HEIGHT
                    anoLp.topMargin = HEADER_HEIGHT
                }
            }

            if (isInPk) {
                publicView.topPadding = PK_PROCESS_HEIGHT
            } else {
                publicView.topPadding = 0
            }
            //改变公聊高度
            changePublicChatHeight(true)
        }
        if (viewModel.chatModeState.value == true) {
            switchChatMode(true)
        }
        context.main_content.requestLayout()
    }

    //软键盘高度
//    private var keyboardHeight = 0
    val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener by lazy {
        ViewTreeObserver.OnGlobalLayoutListener {
            if (!isHorizontal) {
                changePublicChatHeight(false)
                changePagerPosition()
            }
        }
    }

    private var usableHeightPrevious: Int = 0
    private val appShowCut = 100//手机直播公聊高度的减少值

    /**
     * 对部分机型带虚拟物理键的机型进行适配 第一次就会调用 不用担心这两个控件布局没初始化
     * [needChange] 为true表示必须执行改变
     */

    private fun changePublicChatHeight(needChange: Boolean) {
        val publicMessageView = context.publicMessageView
        val rootContainer = context.rootContainer
        val appBottomShadow = context.app_bottom_shadow

        val isLiving = viewModel.isLiving
        var viewHeight = ScreenUtils.computeUsableHeight(rootContainer)
        if (viewHeight != usableHeightPrevious || needChange) {
            val first = if (usableHeightPrevious != 0) {
                Math.abs(usableHeightPrevious - viewHeight)
            } else 0
            //如果高度变化太大 可能是键盘切换 可以不重设高度
            if (first > viewHeight / 5) {
                if (!needChange) {
                    return
                } else {
                    //应对必须设置高度时 而软键盘在弹出状态 此时的可视总高度写死 防止后续公聊高度计算异常
                    viewHeight = screenHeight - STATUS_TOP
                }
            }

            var public_chat_height = viewHeight - LIVE_HEIGHT - HEADER_HEIGHT - ACTION_HEIGHT

            var previousHeight = usableHeightPrevious - LIVE_HEIGHT - HEADER_HEIGHT - ACTION_HEIGHT
            //手机直播时 或者主播推流时 公聊高度减低  2018/8/22 不要了
            //2019/1/23 再次加回来 改为正常的0.6倍
            if ((isAppShow && isLiving || isAnchorSelf) && videoPlayerViewModel.isSingleAnchor()) {
//                public_chat_height -= context.dip(appShowCut)
//                previousHeight -= context.dip(appShowCut)
                val screenType = mOrientationViewModel.screenTypeData.value
                if (screenType == ScreenType.HP) {
//                    val realHeight = SCREEN_WIDTH * 9 / 16
//                    val verticalPadding = (LIVE_HEIGHT - realHeight) / 2
                    public_chat_height = viewHeight - LIVE_HEIGHT_NARROW - HEADER_HEIGHT - ACTION_HEIGHT
                    previousHeight = usableHeightPrevious - LIVE_HEIGHT_NARROW - HEADER_HEIGHT - ACTION_HEIGHT
                } else {
                    public_chat_height = public_chat_height * 75 / 100
                    previousHeight = previousHeight * 75 / 100
                }
            }
            if (appBottomShadow.isVisible()) {
                val params = appBottomShadow.layoutParams
                params?.height = public_chat_height + ACTION_HEIGHT
                appBottomShadow.requestLayout()
            }
            logger.info("当前的公聊高度previousHeight:$previousHeight public_chat_height=$public_chat_height isLiving=$isLiving")
            //如果previousHeight值是负的 直接赋值0
            if (previousHeight < 0) {
                previousHeight = 0
            }
            if (public_chat_height < 0) {
                logger.info("高度不对直接返回 public_chat_height=$public_chat_height")
                return
            }
            val va1: ValueAnimator
            //显示view，高度从0变到height值
            val pmViewLp = publicMessageView.layoutParams as ConstraintLayout.LayoutParams
            pmViewLp.bottomMargin = ACTION_HEIGHT
            pmViewLp.width = PUBLIC_CHAT_CLIENT_WIDTH
            if (viewModel.chatModeState.value == true) {
                switchChatMode(true)
            } else {
                va1 = ValueAnimator.ofInt(previousHeight, public_chat_height)
                va1.addUpdateListener { valueAnimate ->
                    //获取当前的height值
                    //动态更新view的高度
                    pmViewLp.height = valueAnimate.animatedValue as Int
                    publicMessageView.requestLayout()
                }
                val animSet = AnimatorSet()
                animSet.duration = 500
                animSet.playTogether(va1/*, va2, va3*/)
                animSet.start()
            }
            //
            usableHeightPrevious = viewHeight
        }

    }

    private var lastRootHeight: Int = 0

    /** 改变上下页scroll位置*/
    private fun changePagerPosition() {
        val rootContainer = context.rootContainer

        val up_stair_bg = context.up_stair_bg
        val down_stair_bg = context.down_stair_bg
        var viewHeight = rootContainer.height
        if (viewHeight != lastRootHeight) {
            val first = if (lastRootHeight != 0) {
                Math.abs(lastRootHeight - viewHeight)
            } else 0
            //如果高度变化太大 可能是键盘切换 可以不重设高度
            if (first > SCREEN_HEIGHT / 5) {
                logger.info("如果高度变化太大 可能是键盘切换 :$first")
                return
            }
            //容错处理
            val heightError = viewHeight < SCREEN_HEIGHT * 0.8f
            if (heightError) {
                logger.info("当前高度不对  容错处理")
                viewHeight = SCREEN_HEIGHT - STATUS_TOP
            }
            if (isHorizontal) {
                logger.info("当前是横屏  容错处理")
                viewHeight = SCREEN_HEIGHT - STATUS_TOP
            }
            //新增 虚拟键变化时刷新上下页scrollY位置
            logger.info("cp getH=${rootContainer.height}  up_stair_bg.scrollY=${up_stair_bg.scrollY} down_stair_bg.scrollY=${down_stair_bg.scrollY}")
            if (up_stair_bg.scrollY == 0 && down_stair_bg.scrollY == 0 && viewHeight != 0) {
                logger.info("第一次设置")
                up_stair_bg.scrollY = viewHeight
                down_stair_bg.scrollY = -viewHeight
            } else {
                logger.info("再次设置 新增差值：${lastRootHeight - viewHeight}")
                up_stair_bg.scrollY = up_stair_bg.scrollY - lastRootHeight + viewHeight
                down_stair_bg.scrollY = down_stair_bg.scrollY + lastRootHeight - viewHeight

            }

            //
            lastRootHeight = viewHeight
        }
    }

    /**
     * 重置上下切换滑动位置和右抽屉位置
     */
    fun resetSlideViewLocation() {
        logger.info("resetSlideViewLocation")
        val up_stair_bg = context.up_stair_bg
        val down_stair_bg = context.down_stair_bg
        up_stair_bg.scrollY = 0
        down_stair_bg.scrollY = 0
        lastRootHeight = 0
        changePagerPosition()
        val main = context.main_content
        main.scrollTo(0, 0)

        //重置右抽屉
        val slideView = context.surface_view
        slideView.resetSelf()
        slideView.resetRightDrawer()

    }

    /**显示首充动画**/
//    fun showFirstChargeSVGA() {
//        val actionView = context.actionView
//        val svga_player = context.svga_player
//        val centerX = actionView.getGiftCenterX()
//        val leftX = centerX - context.dip(70) / 2
//        val params = svga_player.layoutParams as? ConstraintLayout.LayoutParams
//        params?.leftMargin = leftX
//        svga_player.show()
//        svga_player.startAnimation()
//        svga_player.requestLayout()
//        actionView.hideGiftView()
//    }

    /**
     * 登录状态变更导致的重置
     * 此方法代表切换直播间时或者登录后要做的关闭弹窗操作 根据需要关闭  有些特殊的场景弹窗是不能关闭的
     */
    fun closeDialogIfExists(loginStateChange: Boolean = false) {
        //因为每次切换房间分享地址都会变 在此制空分享fragment 到时重新创建
//        mDialogManager.closeUserMoreSettingDialog(true)
//        //在切换直播间时把萌新礼包关闭
//        mDialogManager.operateDialog(NewUserDialogFragment::class.java, Operators.CLOSE)
//        //关闭名片
//        mDialogManager.operateDialog(BaseCardFragment::class.java, Operators.CLOSE)
//
//        mDialogManager.operateDialog(NewUserDialogFragment::class.java, Operators.CLOSE)
//        //关闭宝箱
//        mDialogManager.operateDialog(ChestDialogFragment::class.java, Operators.CLOSE)
//        //关闭礼物弹窗
//        if (!loginStateChange) {
//            //登录状态变化 保留登录面板
//            mDialogManager.closeGiftDialog(true)
//        } else {
        mDialogManager.refreshGiftDialog()
//        }
//
//        //关闭一元气泡
//        closeGiftPop()
//        //关闭红包弹窗
//        mDialogManager.clearYearFragment()
//        //关闭贵族 or 普通用户列表弹窗
//        mDialogManager.operateDialog(NewOnlineDialogFragment::class.java, Operators.CLOSE)
//        //关闭pk猜猜
//        mDialogManager.operateDialog(PkGuessFragment::class.java, Operators.CLOSE)
//
//        //关闭星球弹窗
//        viewModel.closeDialog.value = PlanetFragment::class.java
    }

    //由于产品需要 只在切换时关闭页面
    fun closePassThroughDialog() {
        //关闭闯关弹窗
//        viewModel.closeDialog.value = PassThroughFragment::class.java
//        //关闭PK段位赛弹窗
//        viewModel.closeDialog.value = PkRankMainDialogFragment::class.java
//        //关闭饲料弹窗
//        viewModel.closeDialog.value = PrivateFragment::class.java

    }

    private fun goToWeb(bean: GoToUrl) {
        if (bean.needLogin) {
//            showNotLoginAlert {
            context.startActivity<WebActivity>(
                BusiConstant.WEB_URL to bean.url,
                IntentParamKey.EXTRA_FLAG_GO_HOME.name to false
            )
//            }
        } else {
            context.startActivity<WebActivity>(
                BusiConstant.WEB_URL to bean.url,
                IntentParamKey.EXTRA_FLAG_GO_HOME.name to true
            )
        }

    }

    fun setPublicMessageTopPadding(padding: Int) {
        val publicView = context.publicMessageView
        publicView.topPadding = padding
    }

//    /**
//     * 获取未读消息数量（私聊）
//     */
//    fun getUnReadMessageCount() {
//        mPlayerMessageViewModel.queryRongPrivateCount()
//    }

    /**
     * 请求获取关注列表 根据[isPull]判断是否下拉刷新
     */
    fun getFollowList(isPull: Boolean) {
//        liveFollowListViewModel.requestFollowLivingList(programId, isPull)
    }

    /**
     * 每次打开直播间页面请求一次就够了
     */
    fun requestFollowList() {
        getFollowList(true)
    }

    /**
     * 根据[dataList]展示数据
     */
//    private fun showFollowData(dataList: LiveFollowListData) {
//        logger.info("showFollowData${dataList.dataType}")
//        val followTitle = context.follow_title
//        if (dataList.list.isNotEmpty()) {
//            followTitle.show()
//        }
//        if (dataList.isPull) {
//            //
//            followAdapter.replaceData(dataList.list)
//        } else {
//            followAdapter.addData(dataList.list)
//        }
//        if (!dataList.hasMore) {
//            followAdapter.loadMoreModule.loadMoreEnd()
//        } else {
//            followAdapter.loadMoreModule.loadMoreComplete()
//        }
//        when (dataList.dataType) {
//            //关注列表
//            0 -> {
//                followTitle.text = "关注主播"
//                followTitle.backgroundResource = R.drawable.bg_live_follow_title
//            }
//            //推荐列表
//            1 -> {
//                followTitle.text = "推荐主播"
//                followTitle.backgroundResource = R.drawable.bg_live_recommend_title
//            }
//        }
//        if (dataList.isPull) {
//            showRightFollowView()
//        }
//    }

    //记录上下页的节目数据
    var previous: SwitchBean? = null
    var next: SwitchBean? = null

    /**
     *
     * 准备上下切换页的数据
     */
    fun preUpAndDownData() {
        logger.info("开始准备上下:$programId")
        val list: ArrayList<SwitchBean> = viewModel.switchList.value ?: return
        list.removeDuplicate()
        val up_stair_bg = context.up_stair_bg
        val down_stair_bg = context.down_stair_bg
        val surface = context.surface_view
        var curPosition = 0
        if (list.size < 2) return
        surface.canScrollPager = list.size > 1
        list.forEachIndexed { index, switchBean ->
            if (switchBean.programId == programId) {
                curPosition = index
            }
        }


        if (curPosition > 0 && curPosition < list.size - 1) {
            previous = list[curPosition - 1]
            next = list[curPosition + 1]
        } else if (curPosition == 0) {
            previous = list.last()
            next = if (list.size > curPosition + 1) {
                list[curPosition + 1]
            } else {
                list.last()
            }
        } else if (curPosition == list.size - 1) {
            previous = if ((curPosition - 1) >= 0) {
                list[curPosition - 1]
            } else {
                list[0]
            }
            next = list[0]
        }


        loadBlurImage(up_stair_bg, previous?.coverPic)
        loadBlurImage(down_stair_bg, next?.coverPic)
    }

    /**
     *  [sdw] [SimpleDraweeView]根据图片url[url]加载模糊图片
     *
     */
    fun loadBlurImage(sdw: SimpleDraweeView, url: String?) {
        ImageUtils.loadImageWithBlur(sdw, url ?: return, 3, 23/*,screenWidth/2,screenHeight/2*/, colors = intArrayOf(DARK_COLOR))
    }

    /**
     * 显示手势提示
     */
    fun showGestureGuideView() {
        val liveGestureGuideView = context.liveGestureGuideView ?: return
        if (StorageHelper.getLiveFirstGestureGuideStatus()) {
            liveGestureGuideView.inflate()
            context.flGestureGuideRoot?.show()
            context.flGestureGuideRoot?.onTouch { _, _ ->
                liveGestureGuideView.hide()
                false
            }
            StorageHelper.setLiveFirstGestureGuide(false)
        }
    }

    /**
     * 显示右侧关注列表
     */
    fun showRightFollowView() {
        val sv = context.surface_view
        if (StorageHelper.getLiveShowFollowStatus()) {
            sv.showRightDrawer()
            StorageHelper.setLiveShowFollow(false)
        }
    }


    //    private var aboveGiftBubble: BubbleDialog? = null
    private var giftBubbleDispose: Disposable? = null

    /**
     * 展示礼物上方气泡提示
     */
    fun showPopupOnGift(message: String, @ColorInt textColor: Int, @ColorInt bubbleColor: Int, duration: Long) {
        //todo
//        if (aboveGiftBubble?.isShowing == true) {
//            aboveGiftBubble?.dismiss()
//        }
//        val actionView = context.actionView
//        val clickView = actionView.getGiftView()
//        //展示提示
//        val title: TextView?
//        if (aboveGiftBubble == null) {
//            val bl = BubbleLayout(context)
//            bl.bubbleColor = bubbleColor
//            bl.shadowColor = Color.TRANSPARENT
//            bl.lookLength = DensityHelper.dp2px(10f)
//            bl.lookWidth = DensityHelper.dp2px(15f)
//            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_bubble2, null)
//            title = contentView.findViewById(R.id.tv_title)
//            aboveGiftBubble = BubbleDialog(context).addContentView(contentView)
//                    .setPosition(BubbleDialog.Position.TOP)
//                    .setTransParentBackground()
//                    .setBubbleLayout(bl)
//                    .setThroughEvent(false, true)
//                    .setOffsetY(8)
//                    .autoPosition(Auto.AROUND)
//            val window = aboveGiftBubble?.window
//            window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
//        } else {
//            title = aboveGiftBubble?.addContentView?.findViewById(R.id.tv_title)
//        }
//        title?.text = message
//        title?.textColor = textColor
//        aboveGiftBubble?.setClickedView(clickView)
//        aboveGiftBubble?.show()
//
//        giftBubbleDispose?.dispose()
//        giftBubbleDispose = Observable.timer(duration, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
//                .bindUntilEvent(context, ActivityEvent.DESTROY)
//                .subscribe {
//                    if (context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                        aboveGiftBubble?.dismiss()
//                    }
//                }
    }

    /**
     * 关闭礼物上方气泡
     */
    private fun closeGiftPop() {
        giftBubbleDispose?.dispose()
//        aboveGiftBubble?.dismiss()
    }

    /**
     * 横竖屏切换后，更新屏幕高度和宽度
     */
    fun refreshScreenDisplayMetrics(isHorizon: Boolean) {
        if (isHorizon) {
            screenHeight = ScreenUtils.getScreenHeight()
            screenWidth = ScreenUtils.getScreenHeightHasVirtualKey()
//            PUBCHAT_CLIENT_WIDTH = screenWidth / 2
        } else {
            screenHeight = ScreenUtils.getScreenHeightHasVirtualKey()
            screenWidth = ScreenUtils.getScreenWidth()
//            PUBCHAT_CLIENT_WIDTH = screenWidth - context.resources.getDimensionPixelSize(R.dimen.pk_width) - 5
        }


    }

    /**
     * 切换横竖屏高级礼物显示
     */
    fun switchHeightGiftAnimationView(isHorizon: Boolean) {
        val highAnim = context.highly_anim
        val haLp = highAnim.layoutParams as FrameLayout.LayoutParams
        if (isHorizon && viewModel.isAnchor) {
            haLp.width = SCREEN_WIDTH * 9 / 16
        } else {
            haLp.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        highAnim.requestLayout()
    }

    /**
     * 主播端切换横竖屏，用户端被动产生的变化
     */
    fun screenSwitch(screenType: String) {
        //用于接收到当前直播间的横竖屏切换消息
        if (mOrientationViewModel.horizonState?.value == true) {
            //用户处于横屏模式
            if (screenType == ScreenType.HP) {
                //主播切换到横屏
            } else {
                //主播切换到竖屏,用户端强制切换到竖屏
                viewModel.actionBeanData.value = BottomActionBean(ClickType.SWITCH_SCREEN, ScreenType.SP)
            }
        } else {
            //用户处于竖屏模式
            if (screenType == ScreenType.HP) {
                //主播切换到横屏
                //显示切换按钮
                val compareTo = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())
                val playePanelHeight = context.playerPanel.height
                logger.info("DXCP 修改布局 3")
                if (playePanelHeight == 0) {
                    //还没有实际高度，直接设置高度
                    changeShowTypeLayout2()
                    return
                }
                val playerCount = Math.max(context.playerPanel.width, playePanelHeight)
                logger.info("DXCP 修改布局 3")
                if (playerCount > compareTo) {
                    //需要修改布局
                    changeShowTypeLayout2()
                }
            } else {
                //用户处于可切换的状态下，将用户端切换到竖屏，全屏模式，并且隐藏切换按钮
                changeShowTypeLayout2()
            }
        }
    }

    /**
     * 切换到竖屏
     */
    fun switchToVertical() {
        if (mOrientationViewModel.horizonState.value == true) {
            //当前处于横屏状态，切换到竖屏
            viewModel.actionBeanData.value = BottomActionBean(ClickType.SWITCH_SCREEN, ScreenType.SP)
        }
    }

    var isRefreshFirstRecharge: Boolean = false


    //    private var mMoreBubble: BubbleDialog? = null
    private var mMoreBubbleDispose: Disposable? = null


    /**
     * 在登录后重置状态
     */
    fun resetStatus() {
        //登录成功刷新关注列表
//        getFollowList(true)
        refreshBalance()
        mDialogManager.resetStatus()
    }

    /**
     * 在界面销毁之前关闭的弹窗 全部遍历关闭
     */
    fun destroyDialog() {
        mDialogManager.clearAllDialog()
    }

    /**
     * intent附带的打开操作
     */
    fun openOperate(operate: OpenOperateBean) {
        when (operate.operate) {
            LiveOperaType.OPEN_GIFT -> {
                val gift = operate.params?.get(LiveOperaType.OPEN_GIFT) as? Int
                if (gift != null) {
                    if (gift != 0) {
                        viewModel.openGiftViewWithSelect.value = gift
                    }
                    context.surface_view?.post {
                        viewModel.actionBeanData.value = BottomActionBean(ClickType.GIFT)
                    }
                }
            }
            LiveOperaType.OPEN_SHARE -> {
                viewModel.shareView.value = true
            }
            LiveOperaType.STAR_GAME -> {

            }
            LiveOperaType.OPEN_FANS -> {
                //打开粉丝团

            }
            LiveOperaType.OPEN_FIRST_RECHARGE -> {
                //打开首充
            }
            LiveOperaType.OPEN_GUARD -> {
                //打开守护
//                viewModel.openOnlineDialog.value = DialogTypes.DIALOG_GUARD
            }
        }
    }


}