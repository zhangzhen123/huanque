package com.julun.huanque.core.ui.live

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.bean.forms.PKInfoForm
import com.julun.huanque.common.bean.forms.UserEnterRoomForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.common.viewmodel.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliplayerManager
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.core.ui.live.dialog.LiveSquareDialogFragment
import com.julun.huanque.core.ui.live.fragment.AnchorIsNotOnlineFragment
import com.julun.huanque.core.ui.live.fragment.AnimationFragment
import com.julun.huanque.core.ui.live.fragment.LivePlayerFragment
import com.julun.huanque.core.ui.live.manager.PlayerTransformManager
import com.julun.huanque.core.ui.live.manager.PlayerViewManager
import com.julun.huanque.core.viewmodel.*
import com.julun.huanque.core.widgets.live.slide.SlideViewContainer
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_live_room.*
import kotlinx.android.synthetic.main.frame_danmu.*
import kotlinx.android.synthetic.main.view_live_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@Route(path = ARouterConstant.PLAYER_ACTIVITY)
class PlayerActivity : BaseActivity() {

    private lateinit var liveViewManager: PlayerViewManager
    private lateinit var transformManager: PlayerTransformManager

    //viewModels----------------------------------------------------------------
    private val viewModel: PlayerViewModel by viewModels()
    private val mConfigViewModel: OrientationViewModel by viewModels()
    private val playerBannerViewModel: PlayerBannerViewModel by viewModels()
    private val videoPlayerViewModel: VideoChangeViewModel by viewModels()
    private val mVideoViewModel: VideoViewModel by viewModels()

    //消息使用的ViewModel
    private val playerMessageViewModel: PlayerMessageViewModel by viewModels()

    //    private var mBasePlayerViewModel: BasePlayerViewModel? = null
    //新版PK
    private val pKViewModel: PKViewModel by viewModels()

    //主播不在线ViewModel
    private val anchorNoLiveViewModel: AnchorNoLiveViewModel by viewModels()

    //连麦ViewModel
    private val connectMicroViewModel: ConnectMicroViewModel by viewModels()

    //道具相关ViewModel
    private val propViewModel: PropViewModel by viewModels()

    //会话相关viewmodel
//    private var conversationListViewModel: ConversationListViewModel? = null

    private val joinViewModel: JoinChatRoomViewModel by viewModels()
    //viewModels--------------------------------------------------------------

    private var programId: Long by Delegates.observable(0L) { _, _, newValue ->
        if (newValue != 0L) {
            liveViewManager.setProgramId(newValue)
            viewModel.programId = newValue
            return@observable
        }

//        val exception = Exception()
//        val stackTrace: Array<out StackTraceElement> = exception.stackTrace
//        val firstOrNull: StackTraceElement? =
//            stackTrace.firstOrNull { it.methodName == "exitLiveRoom" }
//        if (firstOrNull == null) {
//            reportCrash("programId被非正常的设置为 0 了", exception)
//        }
    }
    private var streamId: String? = null

    //来源
    private var mFrom = ""

    //分享用户Id
    private var mShareUSerId = ""

    //欢鹊领取倒计时
    private var mBirdAwardCountInfo: BirdLiveAward? = null
    override fun getLayoutId(): Int = R.layout.activity_live_room

    // 视频直播
    private var livePlayFragment: BaseFragment? = null

    //推流Fragment
    private var publishFragment: BaseFragment? = null

    //ZEGO推流Fragment
    private var mZegoPublishFragment: BaseFragment? = null

    //AGORA推流Fragment
    private var mAgoraPublishFragment: BaseFragment? = null

    // 未直播时主播推荐
    private var anchorIsNotOnlineFragment: AnchorIsNotOnlineFragment? = null

    private val animationFragment: AnimationFragment by lazy {
        AnimationFragment.newInstance(programId)
    }

    // 心跳间隔时间(单位：秒)（固定改为一分钟一次）
    private var heartSeconds: Long = 60 * 1000L

    //直播心跳间隔时间   2分钟（改为一分钟一次） 主播端需每1分钟调用一次，如果超过3分钟没有调用，直播自动停播
    private var delaySeconds = 30 * 1000L

    //当前的模糊背景封面地址
    private var currentLiveBgUrl: String? = null

//    private var donNotGoHome: Boolean = true
    /**
     * 代表是否是手机全屏直播还是半屏3/4直播
     */
    private var isAppShow: Boolean by Delegates.observable(false) { _, _, newValue ->
        liveViewManager.isAppShow = newValue
        viewModel.isAppShow = newValue
    }
    var isLiving: Boolean by Delegates.observable(false) { _, _, newValue ->
        viewModel.isLiving = newValue
    }

    private var isAnchor = false//是不是主播进入

    //是否被封禁了
    private var isBanned: Boolean = false

    companion object {
        //主播开播之前数据
        const val AnchorData = "AnchorData"
        private const val PERMISSIONALERT_WINDOW_CODE = 123

        /**
         * @param activity 源Activity
         */
        fun start(
            activity: Activity, programId: Long,
            prePic: String? = null,
            from: String = ""
        ) {
            val intent = Intent(activity, PlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putLong(IntentParamKey.PROGRAM_ID.name, programId)
            prePic?.let {
                bundle.putString(IntentParamKey.IMAGE.name, it)
            }
            bundle.putString(ParamConstant.FROM, from)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }

        /**
         * @param activity 源Activity
         * @param isAnchor 是不是主播进入
         */
        fun authorStart(
            activity: Activity, isAnchor: Boolean = false, programId: Long? = null,
            streamId: String? = null, prePic: String? = null, against: GuardAgainst? = null,
            from: String = ""
        ) {
            val intent = Intent(activity, PlayerActivity::class.java)
            val bundle = Bundle()
            intent.putExtra(UserType.Anchor, isAnchor)
            programId?.let {
                bundle.putLong(IntentParamKey.PROGRAM_ID.name, it)
            }
            streamId?.let {
                bundle.putString(IntentParamKey.STREAM_ID.name, it)
            }
            prePic?.let {
                bundle.putString(IntentParamKey.IMAGE.name, it)
            }
            against?.let {
                bundle.putSerializable(AnchorData, it)
            }
            bundle.putString(ParamConstant.FROM, from)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initViewModel()
        transformManager = PlayerTransformManager(this)
        liveViewManager = PlayerViewManager(this)
        //每次打开时关闭软键盘 防止后续获取视图高度异常
        if (ScreenUtils.isSoftInputShow(this)) {
            ScreenUtils.hideSoftInput(this)
        }

        //移除activity栈里面的私聊页面
        ActivitiesManager.INSTANCE.removeActivity("com.julun.huanque.message.activity.PrivateConversationActivity")

        if (savedInstanceState == null) {
            //重置token失效连接次数
            RongCloudManager.tokenIncorrectCount = 0
            programId = intent.getLongExtra(IntentParamKey.PROGRAM_ID.name, 0L)
            isAnchor = intent.getBooleanExtra(UserType.Anchor, false)
            streamId = intent.getStringExtra(IntentParamKey.STREAM_ID.name)
            mFrom = intent.getStringExtra(ParamConstant.FROM) ?: ""
            mShareUSerId = intent.getStringExtra(ParamConstant.ShareUserId) ?: ""
            mBirdAwardCountInfo = intent.getSerializableExtra(ParamConstant.BIRD_AWARD_INFO) as? BirdLiveAward
            if (mFrom != PlayerFrom.FloatWindow) {
                AliplayerManager.stop()
            }
//            isFromSquare = intent.getBooleanExtra(FromPager.FROM_SQUARE, false)
            //gift=-1代表无效
            val gift = intent.getIntExtra(IntentParamKey.OPEN_GIFT.name, -1)
            if (gift != -1) {
                if (gift != 0) {
                    viewModel.openGiftViewWithSelect.value = gift
                }
                viewModel.actionBeanData.value = BottomActionBean(ClickType.GIFT)
            }
            liveViewManager.isAnchorSelf = isAnchor
            viewModel.isAnchor = isAnchor
            //不传programId会触发后台随机返回programId
//            if (programId == 0L && !isAnchor) {
//                reportCrash("进入房间时 programId为0了")
//                ToastUtils.showErrorMessage("竟然出错了，再试一下吧！")
//                super.finish()
//                return
//            }
//            initViewModel()
//            donNotGoHome = intent.getBooleanExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
            initAnimationFragment()
            //拉取数据前布局处理  目的不让用户操作
            surface_view?.scrollEnable = false
            surface_view.visibility = View.INVISIBLE
            chat_layout.hide()
            if (isAnchor) {
                //获取开播传递过来的数据
                val extraData = intent?.getSerializableExtra(AnchorData)
                mVideoViewModel.guardAgainstData.value = extraData as? GuardAgainst

                liveHeader.isAnchor = true
                liveRunwayView.isAnchor = true

//                addPublishFragment()
                setPublishFullScreen()
                actionView.setIsAnchor(true)
            } else {
                currentLiveBgUrl = intent.getStringExtra(IntentParamKey.IMAGE.name)
                currentLiveBgUrl?.let {
                    cur_live_bg.show()
                    liveViewManager.loadBlurImage(cur_live_bg, it)
                }
                viewModel.getLivRoomBase(programId)
            }
        } else {
            // 跳转到贡献榜或在线，发生崩溃，回来直接finish掉，暂时这样吧，免得老是出现遮罩层
            super.finish()
            return
        }

    }

    override fun setHeader() {
        //设置状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.black))
            StatusBarUtil.setTransparent(this)
        }
    }

    /**
     * 初始化ViewModel 并且订阅数据
     */
    private fun initViewModel() {
        //获取余额
        viewModel.balance.observe(this, Observer { })

        mConfigViewModel.horizonState.observe(this, Observer {
            val horizon = it ?: return@Observer
            actionView.setHorizontal(horizon)
//            liveViewManager?.closeGuardView()
            liveViewManager.hideInputFragment()
            //处理横竖屏切换屏蔽特效逻辑(礼物特效和弹幕)
            if (!horizon) {
                //不是横屏恢复之前屏蔽的动效
                highly_anim.alpha = 1f
            } else {
                //玩家端直接隐藏礼物特效
                if (!isAnchor) {
                    highly_anim.alpha = 0f
                }
                //横屏状态获取屏蔽设置信息
//                val setting = viewModel.shieldSetting.value
//                if (setting == null) {
//                    //获取当前用户配置的屏蔽设置，如果有的话
//                    viewModel.getShieldSetting()
//                    return@Observer
//                }
//                setShieldState(setting)

            }
        })

        mConfigViewModel.screenTypeData.observe(this, Observer { })

        joinViewModel.joinData.observe(this, Observer {
            if (it == true) {
                //加入直播间成功
                hasJoinRoom = true
                joinChatCallback(it)
                playerMessageViewModel.getBlockedConversationList()
                //在获取消息前清空私聊红点
                actionView.togglePrivateRedPointView(0)
                playerMessageViewModel.queryRongPrivateCount()
            } else {
                viewModel.errorState.value = 2
            }
        })


        //4.30.0版本
        viewModel.gameRedPoint.observe(this, Observer {
            if (it == true) {
                //显示红点
                actionView.showGameRedPoint(true)
            } else {
                //隐藏红点
                actionView.showGameRedPoint(false)
            }
        })

        viewModel.finishState.observe(this, Observer {
            if (it == true) {
                finish()
            }
        })

        viewModel.alertViewMsg.observe(this, Observer {
            if (it?.isNotEmpty() == true) {
//                showAlertViewWithCallback(it) { finish() }
            }
        })
        viewModel.stopLiveData.observe(this, Observer {
//            val extra = Bundle()
//            extra.putString("LIVE_DURATION", "${it?.liveMinutes ?: 0}")
//            extra.putString("LIVE_USER_COUNT", "${getUserCount()}")
//            extra.putString("LIVE_COIN_COUNT", "${it?.presentBeans ?: 0}")
//todo
//            ARouter.getInstance().build(ARouterConstant.PUBLISH_OVER_ACTIVITY)
//                .with(extra)
//                .navigation()
        })

//        viewModel.masterView.observe(this, Observer { openMasterView() })


        viewModel.privateMessageView.observe(this, Observer {
            it?.let {
//                if (propViewModel?.mysteriousState?.value == true) {
//                    ToastUtils.show(R.string.no_private_in_mystery)
//                    return@Observer
//                }
                liveViewManager.openPrivateDialog()
//                mBasePlayerViewModel?.cancelPrivateExperienceDisposable()
            }
        })
        viewModel.loginSuccessData.observe(this, Observer { dto ->
            //用户进入直播间成功

            dto?.let { it ->
                viewModel.contributionSum.value = it.totalContribute//展示萌豆

                val mystery = it.user?.mystery
                propViewModel?.mysteriousState?.postValue(mystery)
                val speakTime = it.speakTtl
                propViewModel?.setColorfulTime(speakTime)
                propViewModel?.startMysteriousCountDown(it.mysteryTtl)
                it.anchor?.programId = programId
                loginSuccessAndInitView(it)
                //执行跳转携带的打开操作
                doWithOpenAction()
            }
        })


        joinViewModel.joinChatRoomFlag.observe(this, Observer {
            if (it == true) {
                if (isAnchor) {
                    joinChatRoomByAnchor()
                }
            }
        })
        viewModel.anchorProgramId.observe(this, Observer {
            setAnchorProgramId(it ?: return@Observer)
        })
        viewModel.jumpBean.observe(
            this,
            Observer { it?.let { jump(it.next, it.intentFlag, it.extra) } })


        viewModel.checkoutRoom.observe(this, Observer { checkoutRoom(it ?: return@Observer) })


        viewModel.baseData.observe(this, Observer {
            it ?: return@Observer
            //重置封禁开关
            isBanned = false
            //basic接口新增不传programId时 随机返回一个新的programId 这里更新programId
            programId = it.programId
            liveViewManager.resetSlideViewLocation()
            if (viewModel.needRefreshSwitchList) {
                viewModel.querySwitchList(programId)
            } else {
                liveViewManager.preUpAndDownData()
            }
            publicMessageView.clearMessages()
            currentLiveBgUrl = it.prePic
            if (it.isLiving) {
                cur_live_bg.hide()
            } else {
                cur_live_bg.show()
                liveViewManager.loadBlurImage(cur_live_bg, it.prePic)
            }

//            conversationListViewModel?.anchorData = it
            //每次进入直播间 请求数据后 首先判断直播类型
            isAppShow = !it.isLandscape
            mConfigViewModel.screenTypeData.value = it.screenType
            //当基础信息返回成功，同时开始拉流和融云相关操作
            anchorNoLiveViewModel.baseData.value = it
            addPlayFragment(it.isLiving, LiveBean().apply {
                programPoster = it.prePic
                programId = it.programId
                playinfo = it.playInfo
                isAppShow = this@PlayerActivity.isAppShow
            })
            initLoadingLiveRoom(false)
            liveHeader.initBaseData(it)
//            chatInputView.setAnchorIdAndProgramId(it.anchorId, programId)
        })

        viewModel.showNoOpenFragment.observe(this, Observer {
            if (it == true) {
                viewModel.showNoOpenFragment.value = false
                addPlayFragment(false)
            }
        })

        //错误返回码处理
        viewModel.errorState.observe(this, Observer {
            it?.let {
                live_room_back.show()
            }
        })


        viewModel.guideSendMessage.observe(this, Observer {
            if (it != null) {
//                chatInputView.sendTextByGuide(it)
//                viewModel.guideSendMessage.value = null
            }
        })
        viewModel.bgChange.observe(this, Observer {
            when (it) {
                LiveBgType.NORMAL -> {
                    //重新换成默认背景色
                    main_content.backgroundResource = R.color.live_bg_color
                }
                //todo
                LiveBgType.PK_TWO -> {
                    main_content.backgroundResource = R.mipmap.pk_two_bg
                }
                LiveBgType.PK_THREE -> {
                    main_content.backgroundResource = R.mipmap.pk_two_bg
                }
                LiveBgType.PK_LANDLORD -> {
                    main_content.backgroundResource = R.mipmap.pk_two_bg
                }
            }
        })


        mVideoViewModel.guardAgainstData.observe(this, Observer {
            if (it != null) {
                mVideoViewModel.agora = it.sdkProvider != GuardAgainst.Zego
                getPublishFragment(it.sdkProvider)
                addPublishFragment()
            }
        })
        mVideoViewModel.switchPublishData.observe(this, Observer {
            if (it != null) {
                var switch = false
                if (it.sdkProvider == GuardAgainst.Zego) {
                    //ZEGO
                    if (mVideoViewModel.agora) {
                        //原先是声网,需要切换SDK
                        if (mAgoraPublishFragment != null) {
                            mVideoViewModel?.agoraReleaseTag?.value = true
                        }
                        switch = true
                    }
                    mVideoViewModel.agora = false
                } else {
                    //AGORA
                    if (!mVideoViewModel.agora) {
                        //原先是ZEGO,需要切换SDK
                        if (mZegoPublishFragment != null) {
                            mVideoViewModel?.zegoReleaseTag?.value = true
                        }
                        switch = true
                    }
                    mVideoViewModel.agora = true
                }
                if (switch) {
                    getPublishFragment(it.sdkProvider)
                    addPublishFragment()
//                    Observable.timer(5, TimeUnit.SECONDS)
//                            .bindUntilEvent(this, ActivityEvent.DESTROY)
//                            .doOnSubscribe { _ -> getPublishFragment(it.sdkProvider) }
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe({ addPublishFragment() }, {}, {})
                }
            }
        })

        playerMessageViewModel.privateConversationData.observe(this, Observer {
            if (it != null) {
                //需要打开私聊页面,先判断悬浮窗权限是否存在
                if (checkOverlayPermissions(PermissionJumpType.PrivateChat)) {
                    openPrivateConversation()
                }

            }
        })

        playerMessageViewModel.contactsData.observe(this, Observer {
            if (it == true) {
                //打开联系人页面
                if (checkOverlayPermissions(PermissionJumpType.Contacts)) {
                    openContacts()
                }
            }
        })
        playerMessageViewModel.unreadCountInPlayer.observe(this, Observer {
            if (it != null) {
                actionView.togglePrivateRedPointView(it)
            }
        })

    }


    /**
     * 检测悬浮窗权限
     */
    private fun checkOverlayPermissions(type: String): Boolean {
        return if (PermissionUtils.checkFloatPermission(this)) {
            true
        } else {
            //没有悬浮窗权限,添加该权限
            viewModel.mPermissionJumpType = type
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "悬浮窗权限未开启，请到设置中授予欢鹊悬浮窗权限",
                MyAlertDialog.MyDialogCallback(onRight = {
                    val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, PERMISSIONALERT_WINDOW_CODE)
                }, onCancel = { viewModel.mPermissionJumpType = "" }), "设置提醒", "去设置"
            )
            false
        }
    }

    /**
     * 打开私聊页面
     */
    private fun openPrivateConversation() {
        lifecycleScope.launchWhenResumed {
            val userInfo =
                playerMessageViewModel.privateConversationData.value ?: return@launchWhenResumed
            val bundle = Bundle()
            bundle.putLong(ParamConstant.TARGET_USER_ID, userInfo.userId)
            bundle.putString(ParamConstant.NICKNAME, userInfo.nickname)
            bundle.putBoolean(ParamConstant.FROM, true)
            bundle.putString(ParamConstant.HeaderPic, userInfo.headPic)
            ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle)
                .navigation()
            val baseData = viewModel.baseData.value ?: return@launchWhenResumed

            FloatingManager.showFloatingView(
                GlobalUtils.getPlayUrl(baseData.playInfo ?: return@launchWhenResumed),
                viewModel.programId,
                baseData.prePic,
                !baseData.isLandscape
            )
        }
    }

    /**
     * 打开联系人页面
     */
    private fun openContacts() {
        lifecycleScope.launchWhenResumed {
            val baseData = viewModel.baseData.value ?: return@launchWhenResumed
            val bundler = Bundle()
            bundler.putString(ParamConstant.DEFAULT_TYPE, ContactsTabType.Intimate)
            ARouter.getInstance().build(ARouterConstant.ContactsActivity).with(bundler).navigation()
            FloatingManager.showFloatingView(
                GlobalUtils.getPlayUrl(baseData.playInfo ?: return@launchWhenResumed),
                viewModel.programId, baseData.prePic, !baseData.isLandscape
            )
        }
    }

    private var mHelper: PanelSwitchHelper? = null

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this) //可选
                .addKeyboardStateListener {
                    onKeyboardChange { visible, height ->
                        //可选实现，监听输入法变化
                        if (visible) {
                            emojiImage.imageResource = R.mipmap.chat_emoji_input
                        } else {
                            emojiImage.imageResource = R.mipmap.chat_key_input
                        }
                    }
                }
                .addEditTextFocusChangeListener {
                    onFocusChange { _, hasFocus ->
                        //可选实现，监听输入框焦点变化
                        if (hasFocus) {
//                            scrollToBottom()
                        }
                    }
                }
                .addPanelChangeListener {
                    onKeyboard {
                        //可选实现，输入法显示回调
                        logger.info("唤起系统输入法")
                        liveViewManager.hideHeaderForAnimation()
//                        iv_emoji.isSelected = false
//                        scrollToBottom()
                    }
                    onNone {
                        logger.info("隐藏所有面板")
                        ll_input.hide()
                        actionView.show()
                        //可选实现，默认状态回调
                        liveViewManager.showHeaderForAnimation()
                        //显示头部
//                        iv_emoji.isSelected = false
                    }
                    onPanel { view ->
                        liveViewManager.hideHeaderForAnimation()
                        //可选实现，面板显示回调
//                        if (view is PanelView) {
//                            iv_emoji.isSelected = view.id == R.id.panel_emotion
//                            scrollToBottom()
//                        }
                    }
                    onPanelSizeChange { panelView, _, _, _, width, height ->
                        //可选实现，输入法动态调整时引起的面板高度变化动态回调
                        if (panelView is PanelView) {
                            when (panelView.id) {
//                            R.id.panel_emotion -> {
//                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
//                                val viewPagerSize: Int = height - DensityHelper.dpToPx(30)
//                                pagerView.buildEmotionViews(
//                                    findViewById<PageIndicatorView>(R.id.pageIndicatorView),
//                                    edit_text,
//                                    Emotions.getEmotions(),
//                                    width,
//                                    viewPagerSize
//                                )
//                            }
                            }
                        }
                    }
                }
                .logTrack(false)
                .build()                     //可选，默认false，是否默认打开输入法
//
//            recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val layoutManager = recyclerView.layoutManager
//                    if (layoutManager is LinearLayoutManager) {
//                        val childCount = recyclerView.childCount
//                        if (childCount > 0) {
//                            val lastChildView = recyclerView.getChildAt(childCount - 1)
//                            val bottom = lastChildView.bottom
//                            val listHeight: Int = recyclerview.height - recyclerview.paddingBottom
//                            unfilledHeight = listHeight - bottom
//                        }
//                    }
//                }
//            })
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privatePoint(event: EventMessageBean) {
//        liveViewManager.getUnReadMessageCount()
        playerMessageViewModel.queryRongPrivateCount(event.targetId)
        playerMessageViewModel.needRefreshConversationFlag.value = event
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun blockChange(event: MessageBlockEvent) {
        //免打扰状态变更
        playerMessageViewModel.blockListData.value = null
        playerMessageViewModel.unreadList.clear()
        playerMessageViewModel.getBlockedConversationList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateShow(event: OpenPrivateChatRoomEvent) {
        //打开私信
        playerMessageViewModel.privateConversationData.value = event
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeChange(event: UserInfoChangeEvent) {
        val followBean = FollowResultBean(follow = event.follow, userId = event.userId)
        viewModel.followStatusData.value = followBean.convertRtData()
    }

    private fun joinChatCallback(joined: Boolean?) {
        if (joined != null && joined) {
            if (viewModel.loginSuccessData.value != null) {
                return
            }
            //加入聊天室成功
            if (isAnchor) {
                mVideoViewModel?.anchorLoginRoomSuccess?.value = true
            } else {
                //用户
                ULog.i("DXC  调用加入直播间方法")
                val form: UserEnterRoomForm
//                val fromType = GIODataPool.fromType
//                GIODataPool.fromType = null
//                val positionIndex = GIODataPool.positionIndex
//                GIODataPool.positionIndex = null
                form = UserEnterRoomForm(programId, fromType = mFrom, shareUserId = mShareUSerId)
                viewModel.enterLivRoom(form)
            }
        } else {
            //加入聊天室失败
            ToastUtils.show(resources.getString(R.string.join_chatroom_failed))

        }
    }


    private fun setAnchorProgramId(programId: Long) {
        this.programId = programId
        initChatByAnchor()
    }

    //设置播放区域全屏
    private fun setPublishFullScreen() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.black))
        }
//        liveViewManager.changePublicChatHeight(true)
        val params = playerPanel.layoutParams as FrameLayout.LayoutParams
        params.height = FrameLayout.LayoutParams.MATCH_PARENT
        params.topMargin = 0
        playerPanel.layoutParams = params
        logger.info("DXCP 修改布局 2")
    }


    //设置相关监听
    private fun initListener() {
        //todo
//        chatInputView.listener = object : ChatInputBoxListener {
//            override fun noEnoughBalance(bean: NotEnoughBalanceBean) {
//                viewModel.notEnoughBalance.value = bean
//            }
//
//            override fun colorfulTime(time: Long) {
//                propViewModel?.setColorfulTime(time)
//            }
//
//            override fun quickSpeak(wId: Int) {
//                viewModel.countLog(CountLogForm("QuickSpeak", "$wId", programId))
//            }
//        }

        //添加对布局变化的监听
        actionView?.viewTreeObserver?.addOnGlobalLayoutListener(liveViewManager.onGlobalLayoutListener)
        surface_view?.onHideOrShowListener = object : SlideViewContainer.OnHideOrShowListener {
            override fun onchange(isShow: Boolean) {
                if (isShow) {
                    surface_view.show()
                    chat_layout.show()
                } else {
                }

            }

        }
        surface_view?.onSwitchListener = object : SlideViewContainer.OnSwitchListener {
            override fun onSwitch(next: Boolean) {
                logger.info("上下切换：$next")
                viewModel.needRefreshSwitchList = false
                live_room_back.show()
                if (next) {
                    checkoutRoom(liveViewManager.next?.programId ?: return)
                } else {
                    checkoutRoom(liveViewManager.previous?.programId ?: return)
                }
            }

        }
        main_content.post {
            val height = rootContainer?.height ?: return@post
            logger.info("post getH=$height")
//            up_stair_bg.scrollY=height
//            down_stair_bg.scrollY=-height
            up_stair_bg.show()
            down_stair_bg.show()
        }
        //滑动监听
        surface_view?.onScrollListener = object : SlideViewContainer.OnScrollListener {
            override fun onScrollHorizontal(dx: Int) {
                highly_anim.scrollBy(dx, 0)
                chat_layout.scrollBy(dx, 0)
            }

            override fun onScrollVertical(dy: Int) {
//                logger.info("onScrollVertical=$dy")
                up_stair_bg.scrollBy(0, dy)
                down_stair_bg.scrollBy(0, dy)
            }

        }

        guideToSendMessageView.onClickNew {
            //点击公聊引导发言
            viewModel.actionBeanData.value = BottomActionBean().apply {
                type = ClickType.CHAT_INPUT_BOX
                actionValue = ""
            }
        }

    }

    private fun setAttentionAble() {
        subscribeAnchor.isEnabled = true
    }


    override fun isRegisterEventBus(): Boolean = true

    /**
     * 直播间开始的初始化，连接至融云，加入聊天室
     * 如果true是登录成功后的加载
     * @param connect true:basic接口返回新的Session，需要重新连接融云   false:当前保存得的session数据是可以正常使用的
     */
    private fun initLoadingLiveRoom(connect: Boolean) {
        liveViewManager.showHeaderAndHideChatView()
        if (!isAnchor) {

            //用户的逻辑
            if (connect) {
                //需要重新连接融云
                var timer = 1L
                if (!RongCloudUtils.RongCloudNeedConnectedManually()) {
                    //融云未处于Disconnect状态，需要先退出
                    RongCloudManager.logout()
                    timer = 10
                }
                Observable.timer(timer, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindUntilEvent(this, Lifecycle.Event.ON_DESTROY)
                    .subscribe({ connectRongAndEnterRoom(true) }, { it.printStackTrace() })
            } else {
                //之前的登录信息有效，不需要打断连接重连融云
                when {
                    RongCloudUtils.RongCloudNeedConnectedManually() -> {
                        // 可以手动重连融云
                        connectRongAndEnterRoom()
                    }
                    RongCloudUtils.RongCloudIsConnected() -> {
                        // 接入融云直播间
                        joinChatRoom()
                    }
                }
            }

        }
    }

    //主播身份初始化聊天   有两处需要调用，所以抽成一个方法
    private fun initChatByAnchor() {
        if (RongCloudUtils.RongCloudIsConnected()) {
            //融云连接成功
//            joinChatRoomByAnchor()
        } else if (RongCloudUtils.RongCloudNeedConnectedManually()) {
            // 可以手动重连融云
            connectRongAndEnterRoom()
        }
    }

    //统一使用这个广播接收作为连接成功的回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectSuccess(event: RongConnectEvent) {
        //融云连接成功通知
        if (hasJoinRoom) {
            return
        }
        if (RongCloudManager.RONG_CONNECTED == event.state) {
            logger.info("DXC 收到融云连接成功通知")
            if (RongCloudUtils.RongCloudIsConnected()) {
                //根据官方方法校验
                val baseData = viewModel.baseData.value
                //此时可能programId已经改变
                if (baseData != null && baseData.programId == programId) {
                    //basic数据已经返回  可以正常加入直播间
                    if (isAnchor) {
//                    joinChatRoomByAnchor()
                    } else {
                        joinChatRoom()
                    }
                }
            }
        }
    }

    // 用户接入融云
    private fun connectRongAndEnterRoom(isFirstConnect: Boolean = false) {
        RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = isFirstConnect)

    }

    /**
     *  有一种情况 直播间登录连接融云 立即就连上了 广播收到消息了立即加入直播间 而直播间初始化 走了直接接入融云 重复进入直播间
     *  加个标志hasJoinRoom 防止万一
     */
    private var hasJoinRoom: Boolean = false

    // 先进入融云的聊天室，然后再调用后台的进入直播间 用户
    private fun joinChatRoom() {
        if (hasJoinRoom) {
            return
        }
        joinViewModel.joinChatRoom(programId.toString())

    }

    /**
     * 登录成功之后重置页面，和数据不挂钩的页面
     */
    private fun resetView() {
        //重置缓存数据
        mFrom = ""
        mShareUSerId = ""
        //清空缓存的消息ID
        RongCloudManager.cacheList.clear()
        // 移除加载层
//        loadingLayout.hide()
        live_room_back.hide()

        surface_view.show()
        chat_layout.show()
        //开播后布局处理f
        if (isAnchor) {
            //主播
//            surface_view?.scrollEnable = true
            isAppShow = true
            initLiveHeart()
        } else {
//            surface_view?.scrollEnable = true
            // 心跳
            initChatAlive()
        }
        if (!liveViewManager.isHorizontal && !isAnchor) {
            //不管开不开播 都能滑动操作
            surface_view?.scrollEnable = true
        }
        // 清空公聊消息列表
        publicMessageView.clearMessages()
        //清空私聊红点
//        actionView.togglePrivateRedPointView(0)
        initInput()

        // 注册融云交互事件 因为有些消息回调需要roomData所以必须在成功回调后注册事件监听
        registerMessageEventProcessor()

    }

    //加入聊天室成功   初始化页面
    private fun loginSuccessAndInitView(data: UserEnterRoomRespDto) {
//        if (data.pking) {
//            val push = if (isAnchor) (BooleanType.TRUE) else {
//                BooleanType.TRUE
//            }
//            pKViewModel.getPkInfo(PKInfoForm().apply {
//                programId = this@PlayerActivity.programId
//                setIsPush(push)
//            })
//        } else {
//            pKViewModel.pkState.postValue(3)
//        }
        resetView()
        //重置过页面之后，调用刷新未读数方法
        privatePoint(EventMessageBean())
        //隐藏猜字谜气泡
        viewModel.isShowGuessWordsBubbleLayout.value = false
        //关闭已经在路上的倒计时
        viewModel.closeAllDelayTime()
        roomDataFetchedFromServer = true//做个标记,已经获取了房间的数据

        initComponentsAndData(data)

        if (!isAnchor) {

            //用户身份
            // 计算各个组件高度
            liveViewManager.calComponentHeight()
            if (animationFragment.isAdded)
                animationFragment.changeShowTypeLayout(isAppShow)
            //新增15/45秒后引导推荐
            if (data.user == null || !SessionUtils.getIsRegUser() || !data.follow) {
//                viewModel.guideToFollow()
            }
            //用户等级 《= 9 的用户  开播直播间  未显示过引导发言
            val guideSpeak = !isAnchor && isLiving && (data.user?.userLevel
                ?: 0) <= 9
            if (guideSpeak) {
//                viewModel.guideToSendMessage()
            }
        }
        val rescueData = data.salvationInfo
        if (rescueData != null) {
            //LiveData统一处理
            viewModel.fansRescueData.value = rescueData
        }
        if (mBirdAwardCountInfo != null) {
            bird_count_view.showCounting(mBirdAwardCountInfo!!)
            mBirdAwardCountInfo = null
        }
    }


    //加入聊天室   主播
    private fun joinChatRoomByAnchor() {
        if (programId <= 0) {
            return
        }
        if (RongCloudUtils.RongCloudIsConnected()) {
            //融云连接成功
            joinViewModel.joinChatRoom(programId.toString())

        } else if (RongCloudUtils.RongCloudNeedConnectedManually()) {
            //融云可以手动重连
            ToastUtils.show("正在连接聊天服务器，请稍后再试")
            joinViewModel.joinData.postValue(false)
            connectRongAndEnterRoom()
        }

    }

    // 网络回调后 初始化各个组件和数据
    private fun initComponentsAndData(data: UserEnterRoomRespDto) {
        startConsumerAfter()

        // 初始化headerView数据
        liveHeader.initData(data)
        //todo
//        chatInputView.setFreeDanMu(data.user?.hasFreeDanMu ?: false, data.user?.danMuCard ?: 0)
//        //贵族弹幕输入初始化
//        chatInputView.initRoyalDanmu(data.user?.royalLevel ?: 0)
//        val anchorData = viewModel.baseData.value
//        anchorData?.let {
//            chatInputView.setAnchorIdAndProgramId(it.anchorId, it.programId)
//        }
//
//        data.anchor?.let {
//            chatInputView.setAnchorIdAndProgramId(it.anchorId, it.programId)
//        }

        // 设置聊天框最大字数限制
//        val speakCount = if (data.user?.mystery == true) {
//            40
//        } else {
//            GlobalUtils.speakCount(isAnchor, data?.user)
//        }
//        chatInputView.setInputTextMaxSize(speakCount)
        if (publishFragment != null) {
            //todo 流名
//            publishFragment?.mPublishStramTitle = viewModel.baseData.value?.programName ?: ""
        }



        data.adList.let {
            //直播间banner数据
            playerBannerViewModel.roomBannerData.value = it
        }

    }


    private fun initAnimationFragment() {
        if (!supportFragmentManager.isStateSaved) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.animation_container, animationFragment)
                .commitNow()
        }
    }

    /**
     * 开始准备消费融云数据
     *
     */
    private fun startConsumerAfter() {
        // 准备好开始接收融云消息
        val temp =
            ChatUtils.createRoomUserChat(viewModel.roomData, viewModel.baseData.value, isAnchor)
        RongCloudManager.startMessageConsumerWithCurrentUserObj(temp)
    }


    /**
     * 初始化输入框相关
     */
    private fun initInput() {


        viewModel.mMessageSending.observe(this, Observer {
            judgeSendEnable()
        })
        ll_input.onClickNew {
            //屏蔽事件
        }
        panel_emotion.onClickNew {
            //屏蔽事件
        }
        panel_emotion.mListener = object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                val currentLength = edit_text.text.length
                val emojiLength = emotion.text.length
                if (currentLength + emojiLength > 30) {
                    Toast.makeText(this@PlayerActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                    return
                }
                val start: Int = edit_text.selectionStart
                val editable: Editable = edit_text.editableText
                val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                    this@PlayerActivity,
                    emotion.text
                )
                editable.insert(start, emotionSpannable)
            }

            override fun onLongClick(type: String, view: View, emotion: Emotion) {
                //长按,显示弹窗
                showEmojiSuspend(type, view, emotion)
            }

            override fun onActionUp() {
                mEmojiPopupWindow?.dismiss()
            }

            override fun onClickDelete() {
                //点击了删除事件
                deleteInputEmoji()
            }

            override fun showPrivilegeFragment(code: String) {
            }
        }

        sendBtn.onClickNew {
            //点击发送
            val message = edit_text.text.toString()
            edit_text.setText("")
            if (message.isEmpty() || message.trim().isEmpty()) {
                ToastUtils.show("不能发送空白消息哦")
                return@onClickNew
            }
            viewModel.sendMessage(message)
        }


        edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) {
                    sendBtn.isEnabled = false
                    return
                }
                if (s.toString().length > 30) {
                    edit_text.setText(s.toString().substring(0, 30))
                    edit_text.setSelection(30)
                    Toast.makeText(this@PlayerActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                    return
                }
                judgeSendEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        edit_text.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendBtn.performClick()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
    }

    // 删除光标所在前一位(不考虑切换到emoji时的光标位置，直接删除最后一位)
    private fun deleteInputEmoji() {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        edit_text.onKeyDown(keyCode, keyEventDown)
        edit_text.onKeyUp(keyCode, keyEventUp)
    }

    //悬浮表情
    private var mEmojiPopupWindow: PopupWindow? = null

    /**
     * 显示表情悬浮效果
     */
    private fun showEmojiSuspend(type: String, view: View, emotion: Emotion) {
        if (mEmojiPopupWindow?.isShowing == true) {
            return
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val content = emotion.text
        var dx = 0
        var dy = 0
        var rootView: View? = null
        when (type) {
            EmojiType.NORMAL -> {
                rootView =
                    LayoutInflater.from(this).inflate(R.layout.fragment_normal_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(50), dip(66))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_emoji_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(50)) / 2
                dy = location[1] - dip(66) + dip(13)
                rootView.findViewById<ImageView>(R.id.iv_emoji)?.imageResource = emotion.drawableRes
            }
            else -> {

            }
        }

        if (rootView == null) {
            return
        }


        val name = content.substring(content.indexOf("[") + 1, content.indexOf("]"))
        rootView.findViewById<TextView>(R.id.tv_emoji)?.text = name

        mEmojiPopupWindow?.isOutsideTouchable = false
        mEmojiPopupWindow?.showAtLocation(view, Gravity.TOP or Gravity.LEFT, dx, dy)
    }

    /**
     * 判断赠送按钮是否可用
     */
    private fun judgeSendEnable() {
        val sending = viewModel.mMessageSending.value ?: false
        //内容不为空，未处于发送状态(显示可用状态，其余显示不可用状态)
        sendBtn.isEnabled = edit_text.text.toString().isNotEmpty() && !sending
    }

    /**
     *
     * 网络回调后 添加播放界面
     * 新增参数 统一传值
     *
     */

    private fun addPlayFragment(isLive: Boolean = true, liveModel: LiveBean? = null) {

        if (isAnchor) {
            return
        }
        val baseData: UserEnterRoomRespBase = viewModel.baseData.value ?: return

        isLiving = isLive
        //每次添加前改变视频布局
        liveViewManager.changeShowTypeLayout2()
        if (isLiving) {
            anchorNoLiveViewModel.showRecommendProgram.value = null
            anchorNoLiveViewModel.recommendProgram.value = null
            // 正在直播中，加载视频直播组件
            if (livePlayFragment == null) {
                livePlayFragment = LivePlayerFragment()
            }

            mVideoViewModel.playerData.value = liveModel
            baseData.roomMicInfo?.joinList?.let { playerList ->
                if (playerList.isNotEmpty()) {
                    videoPlayerViewModel.checkExit()
                    playerList.forEach {
                        it.isAnchor = isAnchor
                    }
                    videoPlayerViewModel.addPlayer(playerList)
                }
            }

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                val fragmentTransaction = supportFragmentManager.beginTransaction()

                if (anchorIsNotOnlineFragment != null && anchorIsNotOnlineFragment!!.isAdded)
                    fragmentTransaction.hide(anchorIsNotOnlineFragment!!)

                if (livePlayFragment != null && livePlayFragment!!.isAdded) {
                    fragmentTransaction.show(livePlayFragment!!)
                } else
                    fragmentTransaction.replace(R.id.playerPanel, livePlayFragment!!)
                if (!supportFragmentManager.isStateSaved) {
                    fragmentTransaction.commitNow()
                }
                //获取PK数据
            }
        } else {
            //主播不在线，直接请求推荐列表
            closeVideoPlayer()
            addAnchorIsNotOnlineFragment()
            anchorNoLiveViewModel.showRecommendProgram.value = true
            anchorNoLiveViewModel.recommendProgram.value = null
            anchorNoLiveViewModel.findRecommendPrograms()
        }
    }

    /**
     * 获取对应推流Fragment
     */
    private fun getPublishFragment(type: String) {
        //todo
//        val publish=false/*BuildConfig.Publish*/
//        if (!publish) {
//            //不支持推流功能，直接返回
//            return
//        }
//        BeautyManager.pushType = type
//        if (type == GuardAgainst.Zego) {
//            //ZEGO
//            //初始化ZEGO
//            (ProviderPoolManager.getService(ARouterConstant.ZEGOSERVICE) as? ZegoService)?.initZegoSDK()
//            //声望释放，ZEGO初始化
//            mZegoPublishFragment = mZegoPublishFragment
//                ?: ARouter.getInstance().build(ARouterConstant.ZEGO_PUBLISH_FRAGMENT)
//                    .withInt(IntentParamKey.PROGRAM_ID.name, programId)
//                    .withString(
//                        IntentParamKey.STREAMID.name, streamId
//                            ?: return
//                    ).navigation() as? BaseFragment
//            publishFragment = mZegoPublishFragment
//        } else {
//            //默认使用声网播放器
//            //ZEGO释放，声望初始化
//            //初始化声网相关(获取)
//            (ProviderPoolManager.getService(ARouterConstant.AGORA_SERVICE) as? AgoraService)?.agoraInit(this.applicationContext)
//            mAgoraPublishFragment = mAgoraPublishFragment
//                ?: ARouter.getInstance().build(ARouterConstant.AGORA_PUBLISH_FRAGMENT)
//                    .withInt(IntentParamKey.PROGRAM_ID.name, programId)
//                    .withString(
//                        IntentParamKey.STREAMID.name, streamId
//                            ?: return
//                    ).navigation() as? BaseFragment
//            publishFragment = mAgoraPublishFragment
//        }
    }

    /**
     * 添加推流视图
     */
    private fun addPublishFragment() {
        //todo
//        if (BuildConfig.Publish) {
//            val fragmentTransaction = supportFragmentManager.beginTransaction()
//            //客户端防错 防止客户端执行推流相关而报错
//            fragmentTransaction.replace(R.id.playerPanel, publishFragment ?: return)
//            if (!supportFragmentManager.isStateSaved) {
//                fragmentTransaction.commitNow()
//            }
//        } else {
//            Toast.makeText(this@PlayerActivity, "该版本不支持开播，请联系所属公会或官方运营，提供最新的实名认证安装包。", Toast.LENGTH_SHORT).show()
//        }
    }

    /**
     * 如果收到关播指令 先关闭当前播放
     */
    private fun closeVideoPlayer() {
        mVideoViewModel?.stopAllStreamState?.value = true
    }

    private var roomDataFetchedFromServer: Boolean = false //房间信息的http请求是否已经正确返回

    private fun addAnchorIsNotOnlineFragment() {
        anchorIsNotOnlineFragment = AnchorIsNotOnlineFragment.newInstance()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            //每次展示前先隐藏播放Fragment
            if (livePlayFragment != null && livePlayFragment!!.isAdded)
                fragmentTransaction.hide(livePlayFragment!!)

            if (anchorIsNotOnlineFragment != null && anchorIsNotOnlineFragment!!.isAdded) {
                fragmentTransaction.show(anchorIsNotOnlineFragment!!)
            } else {
                val params = anchor_no_online.layoutParams
                params.height = PlayerViewManager.LIVE_HEIGHT
                anchor_no_online.requestLayout()
                fragmentTransaction.replace(R.id.anchor_no_online, anchorIsNotOnlineFragment!!)
            }
            if (!supportFragmentManager.isStateSaved) {
                fragmentTransaction.commitNow()
            }
        }
    }


    /**
     * 融云接收的直播间各种消息事件
     */
    private fun registerMessageEventProcessor() {
        MessageProcessor.clearProcessors(false)

        animationFragment.registerMessageEventProcessor()
        transformManager.registerMessage()
        // 公聊消息
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.TextMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                publicMessageView.addMessages(messageList)
                if (mConfigViewModel?.horizonState?.value == true && !isAnchor) {
                    //横屏同时不是主播身份,danmaView接受消息
//                    mDanmuFragment?.addDanmaList(messageList)
                }
            }
        })

        // 跑道消息
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.RunwayMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                val bean = messageList[0]
                liveViewManager.startRunwayAnimation(bean)
            }
        })

        //处理排行榜变更
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.RankMessageProcessor {
//            override fun process(data: RankingEvent) {
//                logger.info("收到榜单数据变化通知${JsonUtil.seriazileAsString(data)}")
//                liveHeader.handleTop2Change(data.resultList)
//            }
//        })

        //直播间在线用户变更....
        MessageProcessor.registerEventProcessor(object : MessageProcessor.RoomUserMessageProcessor {
            override fun process(data: RoomUserChangeEvent) {
                liveHeader.handleRoomUserChange(data)
            }
        })

        //开通守护
        MessageProcessor.registerEventProcessor(object : MessageProcessor.OpenGuardProcessor {
            override fun process(data: OpenGuardEvent) = openGuard(data)
        })

        //开通临时守护
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.OpenExperienceGuardProcessor {
            override fun process(data: OpenGuardEvent) = openGuard(data)
        })


        //幸运礼物
        MessageProcessor.registerEventProcessor(object : MessageProcessor.LuckGiftMessageProcessor {
            override fun process(data: LuckGiftEvent) = playAnimation(AnimModel().apply {
                logger.info("收到幸运动画 ${JsonUtil.serializeAsString(data)}")
                this.animType = AnimationTypes.LUCKY
                this.nickname = data.nickname
                this.extraObject.put("prize", data.luckTotalBeans)
                val localUserId = SessionUtils.getUserId()
                if (localUserId == data.userId) {
                    liveViewManager.refreshBalance()
                }
            })
        })

        //超级幸运礼物
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.SuperLuckGiftMessageProcessor {
            override fun process(data: SuperLuckGiftEvent) = playAnimation(AnimModel().apply {
                logger.info("收到幸运动画 ${JsonUtil.serializeAsString(data)}")
                this.animType = AnimationTypes.SUPER_LUCK_GIFT
//                this.nickname = data.nickname
                this.extraObject.put("prize", data.coin!!)
                this.extraObject.put("goodsName", data.goodsName!!)
                this.extraObject.put("nickname", data.nickname!!)
                val localUserId = SessionUtils.getUserId()
                if (localUserId == data.userId) {
                    liveViewManager.refreshBalance()
                }
            })
        })

        //动画礼物
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.AnimationMessageProcessor {
            override fun process(data: AnimEventBean) = playAnimation(AnimModel().apply {
                logger.info("收到GIF动画")
                this.animType = AnimationTypes.GIF
                extraObject.put("AnimEventBean", data)
            })
        })


        // 用户等级升级动画
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.UserLevelChangeMessageProcessor {
            override fun process(data: UserUpgradeEvent) {
                logger.info("收到升级动画 ${JsonUtil.serializeAsString(data)}")
                val localUserId = SessionUtils.getUserId()
                //特殊处理
//                if (data.grantInfo != null && data.grantInfo!!.isNotEmpty() && localUserId == data.userId) {
//                } else {
//                    playAnimation(AnimModel().apply {
//                        this.nickname = data.nickname
//                        this.animType = AnimationTypes.USER_UPGRADE
//                        this.levelValue = data.newLevel
//                    })
//                }
                // 如果升级的是自己，则刷新个人信息
                if (localUserId == data.userId) {
                    viewModel.refreshUserInfoData()
                }
            }
        })

        //主播等级升级动画
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorUpLevelProcessor {
            override fun process(data: AnchorUpgradeEvent) {
                //
//                playAnimation(AnimModel().apply {
//                    this.nickname = viewModel.roomBaseData?.programName?:""
//                    this.animType = AnimationTypes.ANCHOR_UPGRADE
//                    this.levelValue = data.newAnchorLevel
//                })

            }
        })
        // 开播
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.StartLivingMessageProcessor {
            override fun process(data: OpenShowEvent) {
//                logger.info("OpenShowEvent isPcLive:${data.isPcLive}")
                //获取直播的类型
                liveViewManager.mDialogManager.hideFragment(LiveSquareDialogFragment::class.java)
                isAppShow = !data.isLandscape
                viewModel.baseData.value?.isPcLive = data.isPcLive
                viewModel.baseData.value?.isLandscape = data.isLandscape
                mConfigViewModel.screenTypeData.value = ScreenType.SP
                if (!liveViewManager.isHorizontal && !isAnchor) {
                    surface_view?.scrollEnable = true
                }
                cur_live_bg.hide()
                if (data.programId == programId) {
                    val baseData: UserEnterRoomRespBase = viewModel.baseData.value ?: return
                    addPlayFragment(true, LiveBean().apply {
                        programPoster = baseData.prePic
                        programId = data.programId
                        playinfo = data.playInfo
                        isAppShow = this@PlayerActivity.isAppShow
                    })
                    if (viewModel.loginSuccessData.value?.pking == true) {
                        val push = if (isAnchor) (BooleanType.TRUE) else {
                            BooleanType.TRUE
                        }
                        pKViewModel.getPkInfo(PKInfoForm().apply {
                            programId = this@PlayerActivity.programId
                            setIsPush(push)
                        })
                    }
                }
            }
        })
        // 停播
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.StopLivingMessageProcessor {
            override fun process(data: CloseShowEvent) {
                logger.info("Player 接受到停播消息")
                viewModel.squareView.value = true
                liveViewManager.switchToVertical()
                //只有非NormalStop才关播
                if (isAnchor && data.stopType != StopType.NORMALSTOP) {
//                    anchorHeartDisposable?.dispose()
                    MyAlertDialog(this@PlayerActivity).showAlertWithOKAny(
                        "您已被停播，请联系官方人员~",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            forceStopPublish()
                        })
                    )
                } else {
                    //收到停播消息，设置上次开播时间为刚刚
                    AliplayerManager.stop()
                    showOriView()
                    mVideoViewModel.logout.postValue(true)
                    viewModel.baseData.value?.lastShowTimeDiffText = "刚刚"
//                    surface_view?.scrollEnable = false
                    currentLiveBgUrl?.let {
                        cur_live_bg.show()
                        liveViewManager.loadBlurImage(cur_live_bg, it)
                    }
                    viewModel.showNoOpenFragment.value = true
                }
            }
        })

        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKFinishMessageProcess {
            override fun process(data: PKFinishEvent) {
                logger.info("PKFinishMessage:${data.programIds.size}")
                animationFragment.closePk()
            }
        })

        MessageProcessor.registerEventProcessor(object : MessageProcessor.RoomHeatChangeProcessor {
            override fun process(data: RoomHeatChangeBean) {
                liveHeader.updateHeatValue(data.heatValue)
            }

        })

        // 周星通知
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.WeekStarMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                val bean = messageList[0]
                bean.beanType = WeekType.WEEK
                // 需要做排队
                liveRunwayView.weekStarAnimPlay(bean)

            }
        })
        // 头条榜消息
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.HeadlineMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                // 需要做排队
                if (ForceUtils.isIndexNotOutOfBounds(0, messageList)) {
                    val bean = messageList[0]
                    bean.beanType = WeekType.HEADLINE
                    liveRunwayView.weekStarAnimPlay(bean)
                }
            }
        })

        //红包通知
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.RedPacketMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                if (ForceUtils.isIndexNotOutOfBounds(0, messageList)) {
                    val bean = messageList[0]
                    bean.beanType = WeekType.REDPACKET
                    liveRunwayView.weekStarAnimPlay(bean)
                }
            }
        })


        /**
         * 额外公聊样式消息
         */
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.PublicCustomMsgProcessor {
//            override fun process(data: PublicCustomMsgBean) {
//                when (data.actionType) {
//                    PublicCustomType.TreasureBox -> {
//                        if (data.excludeRoomIds.contains(programId)) {
//                            return
//                        }
//                        val json = data.actionData as? JSONObject
//                        val obj = json?.toJavaObject(TreasureBoxBean::class.java)
//                        liveViewManager.addOtherPublicMessageItem(
//                            ChatMessageBean(
//                                obj
//                                    ?: return, MessageRecyclerView.STYLE_TREASURE_BOX
//                            )
//                        )
//                    }
//                    PublicCustomType.RoomLuckyPlanet -> {
//                        if (data.excludeRoomIds.contains(programId)) {
//                            return
//                        }
//                        val json = data.actionData as? JSONObject
//                        val obj = json?.toJavaObject(PlanetMessageBean::class.java)
//                        liveViewManager.addOtherPublicMessageItem(
//                            ChatMessageBean(
//                                obj
//                                    ?: return, MessageRecyclerView.STYLE_PLANET
//                            )
//                        )
//                    }
//                    //。。。
//                }
//            }
//
//        })

//        //禁言消息
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.MuteUserProcessor {
//            override fun process(data: OperatorMessageBean) {
//                ToastUtils.show("${data.nickname}给了${data.targetNickname}一个${data.time}禁言套餐")
//            }
//        })

    }


    private fun playAnimation(data: AnimModel) {
        if (data.animType == AnimationTypes.GIF) {
            //处理高级礼物动画
            highly_anim?.showGIFAnim(data)
        } else {
            animationFragment.playAnimation(data)
        }

    }

    // 关闭直播
    private fun exitLiveRoom(callback: () -> Unit = {}) {
        RongCloudManager.quitAllChatRoom(true)
        RongCloudManager.destroyMessageConsumer()
        // 退出直播间
        MessageProcessor.clearProcessors(false)
        animationFragment.destroyResource()
        programId = 0
        //每次退出统一关闭zego回调
//        (ProviderPoolManager.getService(ARouterConstant.ZEGOSERVICE) as? ZegoService)?.clearCallBack()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONALERT_WINDOW_CODE) {
            if (PermissionUtils.checkFloatPermission(this)) {
                when (viewModel.mPermissionJumpType) {
                    PermissionJumpType.Contacts -> {
                        openContacts()
                    }
                    PermissionJumpType.PrivateChat -> {
                        openPrivateConversation()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    // 心跳检查
    private fun handleAliveTimer() {
        logger.info("发送心跳：" + programId)
//        if (programId == 0) {
//            reportCrash("发送心跳时 programId为0了 roomData=" + viewModel.baseData.value?.programId + "当前的activity：" + isThisActivityDestroyed())
//            return
//        }
//        viewModel.alive(programId)
    }

    var heartDisposable: Disposable? = null

    // 初始化循环发送心跳
    private fun initChatAlive() {
//        // 游客进来，登录重新进入先停掉之前的心跳
//        heartDisposable?.dispose()
//        heartDisposable = Observable.interval(heartSeconds, heartSeconds, TimeUnit.MILLISECONDS)
//            .bindUntilEvent(this, ActivityEvent.DESTROY)
//            .subscribe({ handleAliveTimer() }, { it.printStackTrace() })

    }

//    var anchorHeartDisposable: Disposable? = null

    //初始化 直播心跳
    private fun initLiveHeart() {
//        viewModel.formerTime = System.currentTimeMillis()
//        anchorHeartDisposable?.dispose()
//        anchorHeartDisposable = Observable.interval(delaySeconds, delaySeconds, TimeUnit.MILLISECONDS)
//            .bindUntilEvent(this, ActivityEvent.DESTROY)
//            .subscribe({ handlerLiveHeart() }, { it.printStackTrace() })
    }

    //发送主播 直播心跳
    private fun handlerLiveHeart() {
//        if (programId == 0) {
//            return
//        }
//        viewModel.modPlayTimes(programId)
    }

    override fun initEvents(rootView: View) {
        initListener()
        chat_layout.onTouch { _, _ ->
            mHelper?.hookSystemBackByPanelSwitcher()
            ll_input.hide()
            false
        }
        publicMessageView.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_DOWN) {
                    mHelper?.hookSystemBackByPanelSwitcher()
                    ll_input.hide()
                }
            }
        }

        surface_view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (liveViewManager.isHorizontal) {
                    liveViewManager.switchMenuShow()
                }
            }
            false
        }
        live_room_back.setOnClickListener {
            finish()
        }

    }

    // 返回按键，聊天视图回到原点
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action === KeyEvent.ACTION_UP && event.keyCode === KeyEvent.KEYCODE_BACK) {
            if (liveViewManager.showHeaderAndHideChatView()) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onPause() {
        // 隐藏键盘还原视图
        showOriView()
        super.onPause()
    }


    private fun showOriView() {
        if (ll_input.isVisible()) {
            liveViewManager.showHeaderAndHideChatView()
        }
    }

    //针对不确定性支付 处理页面回来刷新支付的
    private var refreshPay = false

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        FloatingManager.hideFloatingView()
        AliplayerManager.soundOn()
        //从其他页面回来，获取气泡数据
        viewModel.requestBubble()
        //处理支付刷新
//        if (refreshPay) {
//            Observable.timer(200, TimeUnit.MILLISECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .bindUntilEvent(this, ActivityEvent.DESTROY)
//                .subscribe {
//                    ViewModelProviders.of(this).get(RechargeViewModel::class.java).refreshBalance()
//                    viewModel.closeDialog.value = RechargeDialogFragment::class.java
//                }
//            refreshPay = false
//        }
    }

    //当界面被销毁时关闭掉所有弹窗避免重建时弹窗恢复
    override fun onDestroy() {
        liveViewManager.destroyDialog()
        val baseData = viewModel.baseData.value
        if (!isBanned && SessionUtils.getSessionId()
                .isNotEmpty() && PermissionUtils.checkFloatPermission(this) && baseData != null && baseData.playInfo != null
        ) {
            FloatingManager.showFloatingView(
                GlobalUtils.getPlayUrl(baseData.playInfo ?: return),
                viewModel.programId,
                baseData.prePic,
                !baseData.isLandscape
            )
        } else {
            AliplayerManager.stop()
            viewModel.leave()
        }
        exitLiveRoom()
        super.onDestroy()
        logger.info("Player 执行OnDestroy方法")
    }

    private var closable = false

    /**
     * 关闭界面时增加判断是会主页还是直接关闭
     */
    override fun finish() {
        //封禁状态直接关闭直播间
        if (isBanned) {
            if (isAnchor) {
                stopPublish()
            } else {
//                if (goHome) {
//                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
//                }
                AliplayerManager.stop()
            }
            super.finish()
            return
        }
        if (isAnchor) {
            //主播
            if (!closable && joinViewModel.joinData.value == true) {
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "确认结束直播吗?",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        stopPublish()

                    }),
                    title = "提示"
                )
            } else {
                super.finish()
            }
        } else {
            //上下划手势引导
            if (StorageHelper.getLiveFirstGestureGuideStatus()) {
                liveViewManager.showGestureGuideView()
                return
            }
            if (!viewModel.finishCertain && viewModel.checkGuideFollow()) {
                return
            }
//            mDanmuFragment?.release()
            //用户
//            if (goHome) {
//                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
//            }
//            val baseData = viewModel.baseData.value
//            if (PermissionUtils.checkFloatPermission(this) && baseData != null && baseData.playInfo != null) {
//                FloatingManager.showFloatingView(
//                    GlobalUtils.getPlayUrl(baseData.playInfo ?: return),
//                    viewModel.programId,
//                    baseData.prePic,
//                    !baseData.isLandscape
//                )
//            } else {
//                AliplayerManager.stop()
//                viewModel.leaveProgram()
//            }
            super.finish()
        }
    }

    //主播关播
    fun stopPublish() {
        closable = true
        if (publishFragment != null && mVideoViewModel?.anchorIsPublishing) {
            //todo
//            viewModel.appStopLiving(programId.toLong())
            mVideoViewModel?.stopAllStreamState?.value = true
        } else {
            super.finish()
        }
    }

    //主播被后台封禁关播 不需要调用appStopLiving 直接todayStat
    fun forceStopPublish() {
        closable = true

        if (publishFragment != null && mVideoViewModel?.anchorIsPublishing) {
            //todo
//            viewModel.todayStat(programId)
            mVideoViewModel?.stopAllStreamState?.value = true
        } else {
            super.finish()
        }
    }

    /**
     * 登录成功的回调消息
     */
    private fun loginSuccess() {
        //登录成功重置状态
        liveViewManager.resetStatus()
        Observable.timer(10, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, Lifecycle.Event.ON_DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ resetRoom(true) }, { it.printStackTrace() })
    }

    override fun onNewIntent(intent: Intent) {
        logger.info("onNewIntent")
        super.onNewIntent(intent)
        setIntent(intent)
        //每次回来重置显隐 防止异常回来没有触发显示
        liveViewManager.showOrHideContentView(hide = false, needAnimator = false)
        //这段代码主要是处理 从推送过来的消息 如果正在看直播并且房间号 不一样点击时也可切换房间
        val program = intent.getLongExtra(IntentParamKey.PROGRAM_ID.name, 0L)
        mFrom = intent.getStringExtra(ParamConstant.FROM) ?: ""
        mBirdAwardCountInfo = intent.getSerializableExtra(ParamConstant.BIRD_AWARD_INFO) as? BirdLiveAward
        if (program != 0L) {
            //直播间切换
            checkoutRoom(program)
        } else {
            //传递数据
            //主播信息
            val info = intent.getSerializableExtra(AnchorSearch.CONNECT_MICRO) as? AnchorBasicInfo
            if (info != null) {
                //主播信息不为空   连麦请求返回
                connectMicroViewModel.anchorData.value = info
            }
        }
    }

    /**
     * 处理跳转事件
     */
    private fun doWithOpenAction() {


        val openOperation =
            intent?.getSerializableExtra(IntentParamKey.OPEN_OPERATE.name) as? OpenOperateBean
        intent?.removeExtra(IntentParamKey.OPEN_OPERATE.name)
        if (openOperation != null) {
            //关闭其他弹窗
            liveViewManager.mDialogManager.hideAllDialog()
            liveViewManager.openOperate(openOperation)
        }
    }

    /**
     * 切换房间
     */
    private fun checkoutRoom(program: Long) {
        //如果当前是主播推流 禁止一切切换房间
        if (!isAnchor && program != 0L && this@PlayerActivity.programId != program) {
            viewModel.anchorProgramId.value = program
            //通过viewModel设置ProgramId会有延迟，不能保证实时更新ProgramId
            this@PlayerActivity.programId = program
        } else {
            //处于当前直播间，直接执行打开操作
            if (this@PlayerActivity.programId == program) {
                if (viewModel.loginSuccessData.value != null) {
                    //就在当前直播间，直接打开对应功能
                    doWithOpenAction()
                }
            }
            return
        }
        liveViewManager.closePassThroughDialog()
        resetRoom()
    }

    /**
     * @param loginStateChange 登录状态变更触发重置的标识位
     */
    private fun resetRoom(loginStateChange: Boolean = false) {
        //在这里就先把注册的事件监听全部注销 因为到切换后请求base+连接融云+enter有时间间隔 期间会继续收到消息 导致一系列问题
        MessageProcessor.clearProcessors(false)
        //重新换成默认背景色
        main_content.backgroundResource = R.color.live_bg_color

        anchorNoLiveViewModel.recommendProgram.value = null
        //重置连麦数目
        videoPlayerViewModel.resetPlayerCount()
        //
        connectMicroViewModel.inPk.value = null
        //重置公聊上边距
        liveViewManager.setPublicMessageTopPadding(0)
        //重置加入聊天室标志
        hasJoinRoom = false
        viewModel.loginSuccessData.value = null
        //关闭上次开启的弹出框
        liveViewManager.closeDialogIfExists(loginStateChange)
        //每次关闭软键盘
        ScreenUtils.hideSoftInput(this)
//        removeOnlineFragment()
        //如果切换了房间 在此调用暂停播放高级动画
        animationFragment.resetView()
        //单独拎到activity的处理
        animationFragment.updateProgramId(programId)

        surface_view?.scrollEnable = false
        surface_view.visibility = View.INVISIBLE
//        chatInputView.resetView()
        bird_count_view?.resetView()
        closeVideoPlayer()
        //去除之前直播间的高级动画
        highly_anim.clearWebpResource()
        viewModel.getLivRoomBase(programId)
    }

    // activity完全跑起来后
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
//        chatInputView.setKeyboardConfig(this)
    }


    //增加返回键回到主页
    override fun onBackPressed() {
        //用户按下返回键的时候，如果显示面板，则需要隐藏
        if (mHelper != null && mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return
        }

        if (mConfigViewModel?.horizonState?.value == true) {
            //当前处于横屏状态，切换到竖屏
            viewModel.actionBeanData.value =
                BottomActionBean(ClickType.SWITCH_SCREEN, ScreenType.SP)
            return
        }
        finish()
    }


    //获取观看人数
//    private fun getUserCount() = liveHeader.tv_user_count.text

    /**
     * 刷新用户信息和弹幕等级[level]相关
     */
    fun refreshUserInfoAndRoyalDanmu(level: Int) {
        viewModel.refreshUserInfoData()
//        chatInputView.initRoyalDanmu(level)
    }


    /**
     * 横竖屏切换，回调的方法
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig?.orientation ?: return
        val state = orientation == Configuration.ORIENTATION_LANDSCAPE
        //过滤未发生变化的调用
        if (state == mConfigViewModel?.horizonState?.value) {
            //传递的值和之前的值一致，return
            return
        }

        liveViewManager.isHorizontal = state
        liveViewManager.refreshScreenDisplayMetrics(state)
        liveViewManager.switchHeightGiftAnimationView(state)
        if (!isAnchor) {
            surface_view.scrollEnable = !state
        }
        liveViewManager.changeShowTypeLayout2()
        if (state) {
            inflateDanmu()
            frame_danmu?.show()
            liveViewManager.delayHideMenu()
            surface_view.resetSelf()
            //竖屏打开已经待命的广告弹窗
            if (viewModel.openADFragment.value != null && viewModel.openADFragment.value!!.isNotEmpty()) {
                viewModel.openADFragment.value = viewModel.openADFragment.value
            }
        } else {
            frame_danmu?.hide()
            liveViewManager.showMenu()
//            if (ivFansBubbleRemind.isVisible()) {
//                //如果在切换横屏前显示了引导粉丝打卡气泡，那么在切换时就隐藏它
//                ivFansBubbleRemind.hide()
//            }
        }
        mConfigViewModel?.horizonState?.value = state
    }

    /**
     * 初始化弹幕相关
     */
    private fun inflateDanmu() {
//        if (viewStub == null || isAnchor) {
//            //ViewStub已经映射过，直接返回.主播身份直接返回
//            return
//        }
//        viewStub?.inflate()
//        mDanmuFragment = mDanmuFragment ?: DanmuFragment()
//        mDanmuFragment?.let {
//            val fragmentTransaction = supportFragmentManager.beginTransaction()
//            fragmentTransaction.replace(R.id.frame_danmu, it)
//            if (!supportFragmentManager.isStateSaved) {
//                fragmentTransaction.commitNow()
//            }
//        }
    }


    /**
     * 处理屏蔽状态
     */
//    private fun setShieldState(setting: ShieldSettingBean) {
//        if (isAnchor) {
//            if (setting.isShieldGift || setting.isShieldAll) {
//                highly_anim.alpha = 0f
//            } else if (!setting.isShieldGift && !setting.isShieldAll) {
//                highly_anim.alpha = 1f
//            }
//        } else {
//            val bean = BottomActionBean()
//            bean.type = ClickType.SHIELD
//            bean.actionValue = setting.isShieldAll
//            viewModel.actionBeanData.value = bean
//        }
//    }
//

    /**
     * 开通守护
     */
    private fun openGuard(data: OpenGuardEvent) {
        playAnimation(AnimModel().apply {
            logger.info("收到开通守护动画 ${JsonUtil.serializeAsString(data)}")
            this.animType = AnimationTypes.OPEN_GUARD
            this.nickname = data.nickname

            // 如果升级的是自己，则刷新个人信息
            val localUserId = SessionUtils.getUserId()
            if (localUserId == data.userId) {
                viewModel.refreshUserInfoData()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun queryUnreadMsgCount(event: QueryUnreadCountEvent) {
        if (event.player) {
            EventBus.getDefault().postSticky(
                UnreadCountEvent(
                    playerMessageViewModel.unreadCountInPlayer.value ?: 0,
                    true
                )
            )
        }
    }

    /**
     * 收到封禁通知关闭直播间
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bannedAndClosePlayer(event: BannedAndClosePlayer) {
        if (event.programId == null || event.programId == programId) {
            ToastUtils.show("您已被踢出直播间")
            isBanned = true
            finish()
        }
    }
    /**
     * 收到封禁通知关闭直播间
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideBirdAwardView(event: HideBirdAwardViewEvent) {
        bird_count_view.resetView()
    }
}