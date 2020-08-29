package com.julun.huanque.core.ui.live.fragment

import android.animation.*
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.message_dispatch.EventMessageType
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.isVisible
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.viewmodel.ConnectMicroViewModel
import com.julun.huanque.common.widgets.live.WebpGifView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.manager.PlayerViewManager
import com.julun.huanque.core.viewmodel.*
import com.julun.huanque.core.widgets.live.HighlyAnimationView
import com.julun.huanque.core.widgets.live.banner.BannerListener
import com.julun.huanque.core.widgets.live.message.UserEnterAnimatorView
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_live_animation.*
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2017/4/10
 *
 * 直播间[PlayerActivity]的大部分动画特效都在此页面
 *
 **/
class AnimationFragment : BaseFragment() {


    private val playerViewModel: PlayerViewModel by activityViewModels()

    //星球霸主消息
//    private var mPlanetViewModel: PlanetViewModel = null
    //首充相关viewModel
//    private var mFirstRechargeViewModel: FirstRechargeViewModel = null
    private val svgaViewModel: SVGAViewModel by activityViewModels()
    private val playerBannerViewModel: PlayerBannerViewModel by activityViewModels()

    //    private var pkResultViewModel: PkResultViewModel = null
    private val connectMicroViewModel: ConnectMicroViewModel by activityViewModels()
    private val entranceViewModel: EntranceViewModel by activityViewModels()

    //    private var pkGuessViewModel: PkGuessViewModel by activityViewModels()
    //新版PK
    private val pKViewModel: PKViewModel by activityViewModels()

    //道具类的viewModel
    private val propViewModel: PropViewModel by activityViewModels()

    //PK道具使用提醒弹窗
//    private var mPKPropNotifyDialog: PKPropNotifyDialog? = null


    private var mHighlyAnimation: HighlyAnimationView? = null

    private var enterAnimatorView: UserEnterAnimatorView? = null


    //节目id
    private var programId: Long = 0L

    // 屏幕高度
    private val SCREEN_HEIGHT: Int by lazy {
        ScreenUtils.getScreenHeightHasVirtualKey() - if (context != null) {
            StatusBarUtil.getStatusBarHeight(context)
        } else {
            0
        }
    }

    //视频以下的高度
    private val bottomLiveHeight: Int by lazy { SCREEN_HEIGHT - PlayerViewManager.HEADER_HEIGHT - PlayerViewManager.LIVE_HEIGHT }
    private val container001Top: Int by lazy {
        (PlayerViewManager.HEADER_HEIGHT + PlayerViewManager.LIVE_HEIGHT - DensityHelper.dp2px(43f))
    }

    private val ANCHOR_ANI_WIDTH: Int = PlayerViewManager.SCREEN_WIDTH * 9 / 16

    //直播答题倒计时动画
    private var questionAnimator: ValueAnimator? = null

    //隐藏入场消息标识位
    var mHideEnter = false

    companion object {
        val PROGRAM_ID = "programId"
        fun newInstance(programId: Long): AnimationFragment {
            val fragment = AnimationFragment()
            val bundle = Bundle()
            bundle.putLong(PROGRAM_ID, programId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_live_animation
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        logger.info("我加载了几次++++++$activity")

        programId = arguments?.getLong(PROGRAM_ID, 0L) ?: 0L
        bannerWebView.programId = "$programId"
        mHighlyAnimation = rootView.findViewById(R.id.highlyAnimation)
        enterAnimatorView = rootView.findViewById(R.id.user_enter_view)

        initViewHeight()
        simpleGift?.clearQueueData()

        initViewModel()
        initListeners()
    }


    /**初始化监听**/
    private fun initListeners() {
        tv_mysterious_time.onClickNew {
            ToastUtils.show(R.string.mystery_count_down)
        }
        tv_exit_chat_mode.onClickNew {
            //退出聊天模式按钮
            showExitChatModeDialog()
        }
        svga.callback = object : SVGACallback {
            override fun onFinished() {
                svgaViewModel.animationFinish.value = true
            }

            override fun onPause() {

            }

            override fun onRepeat() {

            }

            override fun onStep(frame: Int, percentage: Double) {

            }

        }
        luxury_enter.setCallBack(object : WebpGifView.GiftViewPlayCallBack {
            override fun onStart() {

                if (!isLuxuryPlay) {
                    luxuryAnimatorSet?.start()
                    entranceViewModel.animatorState.value = true
                }
            }

            override fun onError() {
                entranceViewModel.animatorState.value = true

            }

            override fun onEnd() {
                logger.info("luxury_enter webp end")
            }

            override fun onRelease() {
            }

        }
        )
        bannerWebView.bannerListener = object : BannerListener {
            override fun doAction(roomBean: RoomBanner) {
                when (roomBean.touchType) {
                    BannerTouchType.Url -> {
                        //跳转h5页面
                        var url = roomBean?.touchValue

                        if (url != null) {
                            val extra = Bundle()
                            extra.putString(BusiConstant.WEB_URL, url)
                            extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
                            if (roomBean?.canShare == BooleanType.TRUE) {
                                extra.putSerializable(BusiConstant.ROOM_AD, roomBean)
                            }
                            context?.let {
                                (context as BaseActivity).jump(WebActivity::class.java, extra = extra)
                            }
                        }
                    }
                    BannerTouchType.Home -> {
                        //返回首页
                        ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
                    }
                    BannerTouchType.Recharge -> {
                        //充值页面
                        context?.let {
                            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
                        }
                    }
                    BannerTouchType.Room -> {
                        //切换房间
                        context?.let {
                            if (it is PlayerActivity) {
                                try {
                                    val roomId = roomBean.touchValue.toLongOrNull()
                                    //房间切换
                                    ViewModelProvider(it).get(PlayerViewModel::class.java).checkoutRoom.postValue(roomId)
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    BannerTouchType.Native -> {
                        when (roomBean.touchValue) {
                            //todo
                        }
                    }
                    BannerTouchType.MINI -> {
                        //打开小程序
//                        huanqueService.getService(IShareService::class.java)
//                                ?.onAwakenMini((context as? Activity)
//                                        ?: return, roomBean.miniUserName, roomBean.touchValue)
                    }
                }
//                huanqueService.getService(IStatistics::class.java)?.onAdClick(roomBean?.adCode, "${roomBean?.position + 1}", "直播间广告")
            }

            override fun doCheck(bannerCodes: String) {
                playerBannerViewModel.checkBanner(bannerCodes, programId)
            }
        }
    }


    /**
     * 显示退出聊天模式确认弹窗
     */
    private fun showExitChatModeDialog() {
        MyAlertDialog(
            activity
                ?: return, true
        ).showAlertWithOKAndCancelAny(
            GlobalUtils.getString(R.string.exit_chat_mode_attention),
            MyAlertDialog.MyDialogCallback(onRight = {
                playerViewModel.chatModeState.value = false
            }), "提示", "确定"
        )
    }

    //横屏标记
    var isHorizontal: Boolean = false

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        logger.info("newConfig=${newConfig.orientation}")
        isHorizontal = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        setUserEnterLayout()
        showGiftEffectNormal()
//        val leLp = luxury_enter.layoutParams as FrameLayout.LayoutParams
        val uecLp = user_enter_container?.layoutParams ?: return
        if (isHorizontal) {
            vertical_container.hide()
            horizontal_container.show()
            //横屏主播端处理
            if (playerViewModel.isAnchor == true) {
                anchor_horizontal_container.show()
                val ahcLP = anchor_horizontal_container.layoutParams
                ahcLP.width = PlayerViewManager.SCREEN_WIDTH
                uecLp.width = PlayerViewManager.SCREEN_WIDTH
            } else {
                user_enter_container.hide()
                anchor_horizontal_container.hide()
            }
        } else {
            vertical_container.show()
            horizontal_container.hide()
            anchor_horizontal_container.hide()
            uecLp.width = ViewGroup.LayoutParams.MATCH_PARENT
            user_enter_container.show()
        }
//        setAnchorShieldViews(isHorizontal, playerViewModel.shieldSetting.value ?: return)
    }

    fun updateProgramId(pId: Long) {
        programId = pId
        bannerWebView.programId = "$programId"
    }

    private var luxuryAnimatorSet: AnimatorSet? = null
    private var isLuxuryPlay = false
    private fun playLuxuryAnimator(webp: String) {
        val str2s = webp.split("?")
        var webpUrl = ""
        if (str2s.isNotEmpty()) {
            webpUrl = str2s[0]
        }
        val webpMap = MixedHelper.getParseMap(webp)
        val start: Float = webpMap["in"] ?: 1.260f
        val stop = webpMap["stop"] ?: 6.510f
        val out = webpMap["out"] ?: 1.540f

        //屏幕宽度
        val screenWidth = ScreenUtils.screenWidthFloat
        val animatorEnter = ObjectAnimator.ofFloat(luxury_enter, "translationX", screenWidth, 0f)
        animatorEnter.duration = (start * 1000).toLong()
        animatorEnter.interpolator = LinearInterpolator()

        val animatorExit = ObjectAnimator.ofFloat(luxury_enter, "translationX", 0f, -screenWidth)
        animatorExit.duration = (out * 1000).toLong()
        animatorExit.startDelay = (stop * 1000).toLong()
        animatorExit.interpolator = LinearInterpolator()
        luxuryAnimatorSet = AnimatorSet()
        luxuryAnimatorSet?.play(animatorExit)?.after(animatorEnter)
        luxuryAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                isLuxuryPlay = true
                logger.info("startluxury_enter")
            }

            override fun onAnimationEnd(animation: Animator) {
                logger.info("endluxury_enter")
                isLuxuryPlay = false
                luxury_enter?.hide()
            }
        })
        luxury_enter?.show()
        luxury_enter.setURI(StringHelper.getOssImgUrl(webpUrl), 1)
    }

    private fun initViewModel() {

        playerViewModel.loginSuccessData.observe(this, Observer {
            if (it != null) {

            }
        })



        playerViewModel.baseData.observe(this, Observer {
            if (it != null) {
                barrage_view.anchorId = it.programId
            }
        })

        playerViewModel.chatModeState.observe(this, Observer {
            if (it == true) {
                tv_exit_chat_mode.show()
            } else {
                tv_exit_chat_mode.hide()
            }
        })
        playerViewModel.chatModeExitState.observe(this, Observer {
            if (it == true) {
                showExitChatModeDialog()
                playerViewModel.chatModeExitState.value = false
            }
        })


        svgaViewModel.startPlay?.removeObservers(this)
        svgaViewModel.startPlay.observe(this, Observer {
            if (it != null) {
                startPlay(svgaViewModel.resource.value)
                svgaViewModel.startPlay.value = null
            }
        })

        playerBannerViewModel.roomBannerData.observe(this, Observer {
            if (it != null) {
                bannerWebView.showAds(it)
//                playerBannerViewModel.roomBannerData.value = null
            } else {
//                bannerView?.clearData()
            }
        })

        entranceViewModel.messageState.observe(this, Observer {
            if (it != null) {
                logger.info("收到豪华入场$it")
                playLuxuryAnimator(it)
            }
        })


        playerBannerViewModel.popCode.observe(this, Observer {
            //校验结果返回
            bannerWebView.checkResult(it)
        })

        pKViewModel.pkState.observe(this, Observer {
            if (playerViewModel.isAnchor == true) {
                return@Observer
            }
            when (it) {
                1 -> {
                    //开始
//                    gdavGameBanner.setBanner(BusiConstant.GameType.TYPE_GAME_GUESS)
//                    pkGuessViewModel.regreshState.value = true
                }
                2 -> {
                    //惩罚
//                    gdavGameBanner.setBanner(BusiConstant.GameType.TYPE_GAME_GUESS)
                    pKViewModel.openPropWindowData.value = null
                    pKViewModel.pkState.value = null
                }
                3 -> {
                    //结束
//                    gdavGameBanner.removeBanner(BusiConstant.GameType.TYPE_GAME_GUESS)
                    pKViewModel.openPropWindowData.value = null
                    pKViewModel.pkState.value = null
                }
            }
        })
//        pKViewModel.pkNum.observe(this, Observer {
//            pkGuessViewModel.pkNum.value = it
//        })
        pKViewModel.openPropWindowData.observe(this, Observer {
//            if (it == true) {
//                //显示PK道具入口
//                gdavGameBanner.setBanner(BusiConstant.GameType.TYPE_PK_PROP)
//            } else {
//                //隐藏PK道具入口
//                gdavGameBanner.removeBanner(BusiConstant.GameType.TYPE_PK_PROP)
//            }
        })
        propViewModel.mysteriousTime.observe(this, Observer {
            if (it in 1..30) {
                if (!tv_mysterious_time.isVisible()) {
                    tv_mysterious_time.show()
                }
                tv_mysterious_time.text = TimeUtils.formatTime(it * 1000)
            } else {
                if (tv_mysterious_time.isVisible()) {
                    tv_mysterious_time.hide()
                }
            }
        })
        propViewModel.mysteriousState.observe(this, Observer {
            if (it == true) {

            } else {
                tv_mysterious_time.hide()
            }
            propViewModel.resetColorfulTime()
        })
        //监听礼物面板是否展开
        playerViewModel.sendGiftHigh.observe(this, Observer {
            if (it ?: 0 > 0) {
                logger.info("礼物面板展开$it")
                showGiftEffectInSend(it)
            } else if (it == -1) {
                logger.info("礼物面板收起")
                showGiftEffectNormal()
            }
        })
//        playerViewModel.shieldSetting.observe(this, Observer {
//            it ?: return@Observer
//            //屏蔽流光和入场动画 -> 区分主播和用户端
//            setAnchorShieldViews(isHorizontal, it)
//        })
    }

    /**
     * 设置屏蔽功能(礼物流光特效和入场动画特效)
     */
//    private fun setAnchorShieldViews(isHorizontal: Boolean, bean: ShieldSettingBean) {
//        if (!isHorizontal) {
//            simpleGift.show()
//            user_enter_container.show()
//            return
//        }
//        if (playerViewModel.isAnchor == true) {
//            if (bean.isShieldAll) {
//                simpleGift.hide()
//                user_enter_container.hide()
//            } else {
//                if (bean.isShieldStreamer) {
//                    simpleGift.hide()
//                } else {
//                    simpleGift.show()
//                }
//                if (bean.isShieldEnter) {
//                    user_enter_container.hide()
//                } else {
//                    user_enter_container.show()
//                }
//            }
//        } else {
//            if (bean.isShieldAll) {
//                simpleGift.hide()
//            } else {
//                simpleGift.show()
//            }
//        }
//    }

    private fun initViewHeight() {
        val coLp = container_001.layoutParams as FrameLayout.LayoutParams
        coLp.topMargin = container001Top
        logger.info("CONTAINER_001_TOP:" + container001Top)
        setUserEnterLayout()
        //送礼特效高度
        showGiftEffectNormal()
        //用户升级进度
//        val alLp = author_progress_up?.layoutParams as FrameLayout.LayoutParams
//        alLp.topMargin = (HEADER_HEIGHT + DensityHelper.dp2px(34)).toInt()


        val extendHigh = resources.getDimensionPixelSize(R.dimen.pk_extend_high)
        pkMicView.layoutParams.height = PlayerViewManager.LIVE_HEIGHT + extendHigh
        logger.info("pkMicView h=${PlayerViewManager.LIVE_HEIGHT + extendHigh}")

        val pkProcessHigh = resources.getDimensionPixelSize(R.dimen.pk_process_high)

        //改为距离视频底部下方1dp
//        (gdavGameBanner.layoutParams as? FrameLayout.LayoutParams)?.topMargin = LIVE_HEIGHT + pkProcessHigh + HEADER_HEIGHT + DensityHelper.dp2px(1f)

        //退出聊天模式布局
        (tv_exit_chat_mode.layoutParams as? FrameLayout.LayoutParams)?.topMargin =
            PlayerViewManager.LIVE_HEIGHT + pkProcessHigh + PlayerViewManager.HEADER_HEIGHT + DensityHelper.dp2px(1f) - DensityHelper.dp2px(40f)

    }

    private fun setUserEnterLayout() {
        val ueLp = (user_enter_view?.layoutParams as? FrameLayout.LayoutParams) ?: return
        if (!isHorizontal) {
            ueLp.topMargin = container001Top + DensityHelper.dp2px(56f)
            ueLp.width = PlayerViewManager.SCREEN_WIDTH
        } else {
            ueLp.topMargin = DensityHelper.dp2px(150f)
            ueLp.width = PlayerViewManager.SCREEN_WIDTH
        }

    }


    //正常显示的礼物流光
    private fun showGiftEffectNormal() {
        if (simpleGift == null) {
            return
        }
        val sgLp = simpleGift.layoutParams as FrameLayout.LayoutParams
        //BOTTOM_LIVE-24dp（视频下边距24dp）-120dp(流光中心距其底部边距的距离)
        if (isHorizontal) {
            sgLp.bottomMargin = 0
        } else {
            val bottom = bottomLiveHeight - DensityHelper.dp2px(40f) - DensityHelper.dp2px(120f)
            sgLp.bottomMargin = bottom
        }
        simpleGift.requestLayout()
    }

    //打开礼物面板的流光显示
    private fun showGiftEffectInSend(giftHigh: Int) {
        val sgLp = simpleGift.layoutParams as FrameLayout.LayoutParams
        if (isHorizontal) {
            //横屏时去掉头部的tips高度
            val bottom = giftHigh + DensityHelper.dp2px(5f) - DensityHelper.dp2px(40f) - DensityHelper.dp2px(120f)
            if (bottom > 0)
                sgLp.bottomMargin = bottom
        } else {
            if (bottomLiveHeight < giftHigh + DensityHelper.dp2px(40f)) {
                val bottom = giftHigh + DensityHelper.dp2px(5f) - DensityHelper.dp2px(120f)
                sgLp.bottomMargin = bottom
            }
        }
        simpleGift.requestLayout()
    }

    /**
     * 如果是手机直播 改变布局
     */
    fun changeShowTypeLayout(isAppShow: Boolean) {
        val coLp = container_001.layoutParams as FrameLayout.LayoutParams
        if (isAppShow) {
            coLp.topMargin = container001Top + DensityHelper.dp2px(10f)
        } else {
            coLp.topMargin = container001Top
        }
    }


    @SuppressLint("CheckResult")
    fun registerMessageEventProcessor() {
        //主播升级进度
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorLevelProgressProcessor {
            override fun process(data: AnchorLevelProgressEvent) {
//                if (!data.show) {//此时表示已经升级了......
//                    author_progress_up?.upSuccessAndHide()
//                } else {
//                    author_progress_up?.setProgressData(data)
//                }

            }
        })
        //送礼消息的处理 普通礼物
        MessageProcessor.registerEventProcessor(object : MessageProcessor.GiftMessageProcessor {
            override fun process(data: SendGiftEvent) {
                data.isMyself = if (data.userId == SessionUtils.getUserId()) 1 else 0
                //只有界面展示时才接收消息
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    simpleGift?.handleSendGiftNotification(data)
                }
                //刷新余额

                if (data.luckBeans > 0 && data.userId == SessionUtils.getUserId()) {
                    logger.info("有中奖 刷新余额${data.luckBeans}")
                    //todo
//                    RechargeViewModel.synchronousBalance(activity ?: return)
                }
            }
        })
        // 清除高级动画事件
        MessageProcessor.registerEventProcessor(object : MessageProcessor.ClearAnimationMessageProcessor {
            override fun process(data: VoidResult) {
                clearAllAnim()
            }
        })
        //入场特效
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.WelcomeMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                if (mHideEnter) {
                    return
                }
                val bean = messageList[0]
                if (bean != null) {
                    val beanType = TplTypeBean(TplTypeBean.GUARD, bean)
                    if (isHorizontal && playerViewModel.isAnchor) {
                        //主播端横屏时不使用豪华入场
                        bean.context!!.webp = ""
                    }
                    enterAnimatorView?.playWelcomeAnimator(beanType)

                }
            }
        })
        MessageProcessor.registerEventProcessor(object : MessageProcessor.DanMuProcessor {

            override fun process(data: BarrageEvent) {
                data.eventCode = EventMessageType.SendDanmu.name
                barrage_view?.playBarrageAnimator(data)
            }
        })
        MessageProcessor.registerEventProcessor(object : MessageProcessor.BRDanMuProcessor {

            override fun process(data: BarrageEvent) {
                data.eventCode = EventMessageType.BROADCAST.name
                logger.info("收到广播弹幕")
                barrage_view?.playBarrageAnimator(data)
            }
        })


        //PK比分变化
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKScoreChangeMessageProcess {
            override fun process(data: PKScoreChangeEvent) {
                if (data.pkInfo != null) {
                    connectMicroViewModel.inPk.value = true
//                    pKViewModel.pkData.value = data.pkInfo
                    data.pkInfo!!.needAnim = true
                    pkMicView?.justSetPk(data.pkInfo!!)
                    connectMicroViewModel.inMicro.value = null
                }
            }
        })
        //PK结果
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKResultMessageProcess {
            @SuppressLint("CheckResult")
            override fun process(data: PKResultEvent) {
//                mPKPropNotifyDialog?.dismiss()
                Observable.timer(1500, TimeUnit.MILLISECONDS)
                    .bindUntilEvent(this@AnimationFragment, FragmentEvent.DESTROY)
                    .subscribe({
//                            pkGuessViewModel.regreshState?.postValue(true)
                    }, { it.printStackTrace() })
                pKViewModel.pkState.value = 2
                if (data?.detailList != null) {
                    showPKResult(data)
                }
//                playerViewModel.resetDialog.value = PkRankMainDialogFragment::class.java
            }

        })
        //PK道具
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkPropMessageProcess {
            override fun process(data: PkPropEvent) {

                if (data.programIds.contains(programId)) {
                    logger.info("开始道具 PkPropEvent=$data")
                    pkMicView?.setPkPropsData(true, data.propInfo ?: return)
                }
            }
        })
        //PK道具使用提醒
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkPropUseWarnMessageProcess {
//            override fun process(data: PkPropUseWarnEvent) {
//                data?.propUseWarnInfo?.let { info ->
//                    if (mPKPropNotifyDialog?.isAdded == true) {
//                        mPKPropNotifyDialog?.dismiss()
//                    }
//                    mPKPropNotifyDialog = PKPropNotifyDialog.newInstance(info, programId)
//                    mPKPropNotifyDialog?.show(childFragmentManager, "PKPropNotifyDialog")
//                }
//
//            }
//        })
        //PK礼物任务
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkTaskMessageProcess {
            override fun process(data: PkTaskEvent) {

                if (programId == data.programId) {
                    logger.info("开始 礼物任务PkTaskEvent=$data")
                    pkMicView?.setPkGiftTaskData(true, data.taskInfo ?: return)
                }
            }
        })
        //PK积分任务
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkScoreTaskMessageProcess {
            override fun process(data: PkScoreEvent) {

                if (programId == data.programId) {
                    logger.info("开始 礼物任务PkTaskEvent=$data")
                    pkMicView?.setPkScoreTaskData(true, data.taskInfo ?: return)
                }
            }
        })
        /**
         * banner消息相关
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.RoomBannerMessageProcessor {
            override fun process(data: String) {
                bannerWebView.showMessage(data)
//                if (data.contains(BusiConstant.PopupShow_RoomHeadLine)) {
//                    //头条消息，同时通知顶部
//                    playerViewModel.headerInfoAction(data)
//                }
//                if (data.contains(BusiConstant.PopupShow_RoomPlanet)) {
//                    //星球霸主消息,刷新血量
//                    mPlanetViewModel.bloodCount(data)
//                }
            }
        })
        /**
         * PK竞猜赔率发生变化
         */
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkGuessOddsChangeMessageProcessor {
//            override fun process(data: VoidResult) {
//                pkGuessViewModel.regreshState.value = true
//            }
//        })
        /**
         * PK竞猜结果
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkGuessEndMessageProcessor {
            override fun process(data: PkGuessResult) {
                ToastUtils.show(data.pkGuessResult)
            }
        })

    }

//    private var pkDialog: PKResultDialog? = null

    fun closePk() {
        pkMicView?.resetPkView()
    }

    private fun showPKResult(data: PKResultEvent) {
        //pk结束后将额外属性去除 防止影响下次的pk的界面设置
        pkMicView?.showPkResult(data)
    }

//    private var lastAnim: AnimModel? = null

    fun playAnimation(data: AnimModel): Unit {
        logger.info("收到动画事件：${data.animType}")
        mHighlyAnimation?.handleEventNotify(data)
    }

    fun clearAllAnim() {
        mHighlyAnimation?.handleAnimationClearNotify()
    }


    fun resetView() {
//        bannerView?.clearData()
//        gdavGameBanner?.removeAll()
//        rp_grouper_view?.clearAll()
//        bgv_grouper_view?.clearAll()
        playerBannerViewModel.roomBannerData.value = null
        playerBannerViewModel.pkData.value = null
        mHighlyAnimation?.handleAnimationClearNotify()
//        runOnMain(VoidResult()) {
//            hideGifViews()
//        }
        luxury_enter?.hide()
        luxuryAnimatorSet?.cancel()
        luxury_enter?.resetView()

        enterAnimatorView?.resetView()
        simpleGift?.clearQueueData()
//        duanwu_view?.reset()
//        author_progress_up?.upSuccessAndHide()//目的时为了在切换时重置状态
        //清空弹幕缓存数据和内容
        barrage_view?.clear()
//        pkview?.gonePKView()

        closePk()
//        rescueAnchorView?.resetView()
    }

    fun destoryResource() {
        simpleGift?.destoryResource()
        mHighlyAnimation?.destoryResources()
        user_enter_view?.clearQueue()
        barrage_view?.clearQueue()
    }

    //播放SVGA动画
    private fun startPlay(url: String?) {
        if (url == null) {
            return
        }
//        val parser = SVGAParser(context)
//        Thread({
        val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
//                svga?.handler?.post {
                svga.setVideoItem(videoItem)
                svgaViewModel.animationFinish.value = null
                svga.startAnimation()
//                }
            }

            override fun onError() {}
        }
        SVGAHelper.startParse(url, callback)
//        }).start()
    }


    override fun onStop() {
        super.onStop()
        questionAnimator?.cancel()
        luxuryAnimatorSet?.cancel()
    }

    override fun onDestroyView() {
//        if (bannerView != null) {
//            bannerView.clearData()
//        }
        bannerWebView.hide()
        super.onDestroyView()
    }

}
