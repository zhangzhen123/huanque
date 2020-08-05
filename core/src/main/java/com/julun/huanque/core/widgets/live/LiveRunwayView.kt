package com.julun.huanque.core.widgets.live

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.TplBeanExtraContext
import com.julun.huanque.common.constant.RunWaySVGAType
import com.julun.huanque.common.constant.WeekType
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.DefaultAnimatorListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.SVGAViewModel
import kotlinx.android.synthetic.main.view_runway_simple.view.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import java.util.*
import kotlin.properties.Delegates


/**
 *
 *@author zhangzhen
 *@data 2018/4/9
 *
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/08/29
 * @iterativeVersion 4.18
 * @iterativeDetail 开放主播端守护列表入口
 * @iterativeDate 2019/11/08
 * @iterativeVersion 4.20
 * @iterativeDetail 迭代详情：增加热度体系
 **/
class LiveRunwayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val logger = ULog.getLogger("LiveRunwayView")
    var isAnchor: Boolean by Delegates.observable(false) { _, _, newValue ->
        //        if (newValue) {
//            tv_guard.hide()
//        } else {
//            tv_guard.show()
//        }
    }
    private var playerViewModel: PlayerViewModel? = null
    private lateinit var svgaViewModel: SVGAViewModel


    companion object {
        const val SVG_DURATION: Long = 2750L //svg动画播放时间
        const val SLIDE_IN_DURATION: Long = 700L //滑入的动画时间
        const val SLIDE_OUT_DURATION: Long = 625L //滑出的动画时间
        const val PLANE_FADE_DURATION: Long = 1250L //飞机消失总时间
        const val PLANE_FADE_DELAY_DURATION: Long = 600L //飞机消失延时时间

        const val PLANE_FADE_DISTANCE: Int = 8 //飞机消失滑动距离

        const val MIN_PLAY_TIMES = 3 //播放次数


        const val REAL_CONTENT_MARGIN = 140f //实际显示内容的多余边距
    }

    private val myHandler: Handler by lazy { Handler() }
    private var activity: PlayerActivity? = null

    private fun initViewModel() {
        activity = context as? PlayerActivity
        activity?.let { act ->
            playerViewModel = ViewModelProvider(act).get(PlayerViewModel::class.java)
            svgaViewModel = ViewModelProvider(act).get(SVGAViewModel::class.java)

            svgaViewModel.animationFinish.observe(act, androidx.lifecycle.Observer {
                if (it == true) {
                    slideIn()
                }
            })
            playerViewModel?.contributionSum?.observe(act, androidx.lifecycle.Observer {
                if (it != null) {
                    totalScoreText.text = StringHelper.formatCoinsCount(it)
                }
            })
        }
    }


    init {
        LayoutInflater.from(context).inflate(R.layout.view_runway_simple, this)
        initViewModel()
        this.isFocusable = true
        this.isClickable = true
        runwayMessageText.onClickNew {
            logger.info("click runwayMessageText")
            checkoutRoom()
        }
        runway_layout.setOnTouchListener { _, event ->
            logger.info("touch runway_layout")
            if (event.action == MotionEvent.ACTION_DOWN) {
                checkoutRoom()
            }
            true
        }
        score_layout.onClickNew {
            playerViewModel?.scoreView?.value = true
//            weekStarAnimPlay()
        }

        tv_live_square.onClickNew {
            playerViewModel?.squareView?.value=true

        }

        week_star_layout.setOnTouchListener { _, event ->
            logger.info("touch week_star_layout")
            if (event.action == MotionEvent.ACTION_DOWN) {
                weekStarCheck()
            }
            true
        }

        week_star_text.onClickNew {
            weekStarCheck()
        }
    }

    private fun weekStarCheck() {
        //拯救点击跳转时上报操作
        when (currentWeekStar?.beanType) {
//            WeekType.RESCUE -> {
//                val context = currentWeekStar?.context ?: return
//                playerViewModel?.reportRescueClick(context.programId, context.salvationId ?: return)
//            }

        }
        //跳转到通知的另一个直播间
        currentWeekStar?.context?.programId?.let {
            playerViewModel?.checkoutRoom?.value = it
        }
    }

    private fun checkoutRoom() {
        if (!isAnchor) {
            val messageContext = currentRunwayMessage?.context       //此时当前的消息不能为空
            if (messageContext != null) {
                playerViewModel?.checkoutRoom?.value = messageContext.programId
            }
        }

    }

    private var currentRunwayMessage: TplBean? = null
    private var cachedRunwayMessage: TplBean? = null
    private val messagesQueue: LinkedList<TplBean> = LinkedList()    //等待播放的队列
//    private var changeLiveRoomListener: AnchorIsNotOnlineFragment.ChangeLiveRoomListener? = null

    //    private val MEASURED_HEIGHT by lazy { this.measuredHeight }      //组件自身的高度
    //    private val MEASURED_WIDTH by lazy { this.measuredWidth }      //组件自身的宽度
    private val realContentLength by lazy { ScreenUtils.getScreenWidth() - DensityHelper.dp2px(REAL_CONTENT_MARGIN) }//实际显示内容的区域宽度 100+10

    /**
     * 当前跑道消息是否已经过期 true代表还有效 false代表失效了
     */
    private fun messageIsInvalid(tplBean: TplBean?): Boolean {
        val messageContext: TplBeanExtraContext = tplBean?.context ?: return false//此时一定不能为空
        val playIsOverForThisMessage = messageContext.cacheIt &&
                (messageContext.cacheStartTime + (1000 * messageContext.seconds) - System.currentTimeMillis() >= 0)
//        return messageContext.cacheStartTime + (messageContext.seconds * 1000) - System.currentTimeMillis() <= 0
        return playIsOverForThisMessage
    }

    /**
     * 缓存数据发生变化
     */
    interface OnCachedMessageChangesListener {
        fun onCachedMessageChanges(messageContext: TplBeanExtraContext): Unit
    }

    private var cachedMessageChangesListener: OnCachedMessageChangesListener? = null

    /**
     * 从消息队列里找出第一条记录渲染并且播放动画
     */
    private fun fetchMessageAndRender(callback: () -> Unit = {}): Unit {
        currentPlayTimes = 0
        val fetchedFromQueue: TplBean? = messagesQueue.poll()
        if (fetchedFromQueue != null) {              //如果队列里有,从队列里取
//            ULog.i("当前有队列消息 开始播放新消息")
            justRenderMessageAndPlay(fetchedFromQueue, 0)
        } else {            //如果当前队列里已经没有数据了，从缓存里取
            //如果缓存的记录存在，并且没有过期，播放这个缓存的消息
            if (cachedRunwayMessage != null && messageIsInvalid(cachedRunwayMessage!!)) {
//                justRenderMessageAndPlay(cachedRunwayMessage!!, 0)
                callback()
                return
            }
            //如果连缓存消息都没有 直接结束
            cleanMessageAll()
        }
    }

    private fun cleanMessageAll() {
        ULog.i("清空所有跑道消息")
        slideOut()
        currentRunwayMessage = null
        cachedRunwayMessage = null
        currentPlayTimes = 0
        cachedMessageChangesListener?.onCachedMessageChanges(TplBeanExtraContext(seconds = 0, cacheValue = 0))
        isMessagePlay = false
        isFirstRun = true
    }

    private var isFirstRun = true //是不是第一次播放

    /**
     * 消息开始处理
     */
    private fun justRenderMessageAndPlay(tplBean: TplBean, delaySeconds: Int = 0, isCache: Boolean = false) {
        currentRunwayMessage?.context?.isOld = true//每次播放前都将上一个消息变成老消息 如果有的话
        isMessagePlay = true
//        val bean = tplBean.preProcess()
        currentRunwayMessage = tplBean

        myHandler.postDelayed({
            val level = getLevel(tplBean)
            var needEffects = false   //预留后台返回的是否需要打榜特效

            var canOnlyPlayOneTime: Boolean = false
            val runwayMessageContext = tplBean.context
            if (runwayMessageContext != null) {
                needEffects = runwayMessageContext.cacheIt
                canOnlyPlayOneTime = runwayMessageContext.canOnlyPlayOneTime
            }


            if (!isFirstRun) {
                slideOut(start = {
                    //缓存消息和第三档不需要播放svg特效
                    if (!isCache && level >= 1 && needEffects && !canOnlyPlayOneTime) {
//                        logger.info("下一条不是缓存 且等级达到特效 而且有特效 且不是直播间数据附带的跑道")
                        playSvgAnimation(level)
                    }
                }, end = {
                    if (isCache || level <= 0 || !needEffects || canOnlyPlayOneTime) {
//                        logger.info("下一条是缓存或等级不够或无特效")
                        slideIn()
                    }
                })
            } else {
                isFirstRun = false

                if (canOnlyPlayOneTime) {
                    slideIn()
//                    logger.info("第一次播放$level 但是属于附带跑道")
                } else {
//                    logger.info("第一次播放$level 一定有打榜特效???")
                    playSvgAnimation(level)
                }
            }

        }, delaySeconds * 1000L)

    }

    /**
     * 获取跑道等级  500元一下没有特效 500-1000飞艇特效 1000以上飞机特效
     */
    private fun getLevel(tplBean: TplBean): Int {
        var level = -1
        val messageContext: TplBeanExtraContext? = tplBean.context
        if (messageContext != null) {
            val giftFeeValue: Long = messageContext.cacheValue
            level = when {
                giftFeeValue < 500000 -> {
                    0
                }
                giftFeeValue < 1000000 -> {
                    1
                }
                else -> {
                    2
                }
            }

        }
        return level
    }

    /**
     * 根据礼物价钱设置不同的背景
     */
    private fun processDataAndRender(tplBean: TplBean) {
        runwayMessageText.render(tplBean)
//        runwayMessageText.gravity = Gravity.CENTER_VERTICAL

        //给图文内容也添加点击事件
//        runwayMessageText.setOnClickListener {
//            val messageContext = currentRunwayMessage?.context       //此时当前的消息不能为空
//            if (messageContext != null) {
////                changeLiveRoomListener?.onChangeLiveRoom(messageContext.programId)
////                EventDispatcherCenter.postData(ChangeRoomEvent(messageContext.programId))
//                EventBus.getDefault().post(ChangeRoomEvent(messageContext.programId))
//            }
//        }
        val messageContext: TplBeanExtraContext? = tplBean.context
        if (messageContext != null) {
            val giftFeeValue: Long = messageContext.cacheValue
            when {
                giftFeeValue < 500000 -> {
                    runway_container.backgroundDrawable = resources.getDrawable(R.drawable.shape_runway_1)
//                    runway_icon.setImageResource(R.mipmap.runway_img_002)
                    runway_icon.hide()
                }
                giftFeeValue < 1000000 -> {
                    runway_container.backgroundDrawable = resources.getDrawable(R.drawable.shape_runway_2)
                    runway_icon.setImageResource(R.mipmap.runway_img_001)
                    runway_icon.show()
                }
                else -> {
                    runway_container.backgroundDrawable = resources.getDrawable(R.drawable.shape_runway_3)
                    runway_icon.setImageResource(R.mipmap.runway_img_002)
                    runway_icon.show()
                }
            }

        }
    }

    /**
     *
     */
    private fun slideOut(start: () -> Unit = {}, end: () -> Unit = {}) {
        val width = ScreenUtils.screenWidthFloat - dip(100)
        val translationXOut = ObjectAnimator.ofFloat(runway_container, View.TRANSLATION_X, 0f, -width)
        translationXOut?.addListener(object : DefaultAnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                runway_container.hide()
                end()
            }
        })
        start()
        translationXOut.setDuration(SLIDE_OUT_DURATION).start()
    }

    /**
     *    1.播放svga动画  只是播放svg 没有其他逻辑
     *
     */

    private fun playSvgAnimation(level: Int) {
//        logger.info("开始播放svg动画")
        when (level) {
            0 -> {
                slideIn()
            }
            1 -> {
                svgaViewModel.resource.value = RunWaySVGAType.AIRSHIP
                svgaViewModel.startPlay.value = true
//                slideIn()
            }
            2 -> {
                svgaViewModel.resource.value = RunWaySVGAType.AIRPLANE
                svgaViewModel.startPlay.value = true
//                slideIn()
            }
        }
    }

    var enterAllset: AnimatorSet? = null

    /**
     *  2.橫幅进场
     */

    private fun slideIn() {
//        logger.info("跑道飞机一起走")
        //跑道飞机一起走
        if (enterAllset == null) {
            enterAllset = AnimatorSet()
            val translationXIn101 = ObjectAnimator.ofFloat(runway_icon, View.TRANSLATION_X, ScreenUtils.screenWidthFloat, 0f)
            val translationXIn102 = ObjectAnimator.ofFloat(runway_container, View.TRANSLATION_X, ScreenUtils.screenWidthFloat, 0f)
            //飞机再飞一会同时隐藏
            val translationXIn201 =
                ObjectAnimator.ofFloat(runway_icon, View.TRANSLATION_X, 0f, -dip(PLANE_FADE_DISTANCE).toFloat())
            val alpha = ObjectAnimator.ofFloat(runway_icon, View.ALPHA, 1f, 0f)
            alpha.startDelay =
                PLANE_FADE_DELAY_DURATION
            val enter = AnimatorSet()
            enter.duration = SLIDE_IN_DURATION
            enter.interpolator = AccelerateDecelerateInterpolator()
            enter.playTogether(translationXIn101, translationXIn102)
            enter.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (ViewCompat.isAttachedToWindow(this@LiveRunwayView)) {
                        playMessage()
                    }
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (currentRunwayMessage != null)
                        processDataAndRender(currentRunwayMessage!!)
                    runway_container.show()
                }

            })
            val disappear = AnimatorSet()
            disappear.duration =
                PLANE_FADE_DURATION
            disappear.interpolator = LinearInterpolator()
            disappear.playTogether(translationXIn201, alpha)
            disappear.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    runway_icon.alpha = 1.0f
                    runway_icon.hide()
                }
            })

            enterAllset?.playSequentially(enter, disappear)
        }
        enterAllset?.start()
    }

    /**
     * 渲染最后一条跑道消息如果存在  真正的消息跑道唤醒入口
     * @param runwayMessage 传入的message
     * @param onCachedRunwayMessageChangesListener 跑道消息的事件监听器
     */
    fun loadLastRunwayMessage(
        runwayMessage: TplBean,
        delaySeconds: Int, onCachedRunwayMessageChangesListener: OnCachedMessageChangesListener
    ) {
        cachedMessageChangesListener = onCachedRunwayMessageChangesListener
        //如果缓存的跑道与新消息randomGeneratedId一致就不再添加
        if (runwayMessage.context?.msgId == cachedRunwayMessage?.context?.msgId) {
            logger.info("过滤掉内容重复的跑道：${runwayMessage.realTxt}")
            return
        }
        val runwayMessageContext = runwayMessage.context
        if (runwayMessageContext != null) {// context 为空的 消息直接忽略
            if (runwayMessageContext.cacheIt) {
                cachedRunwayMessage = runwayMessage
                //缓存发生变化
                cachedMessageChangesListener?.onCachedMessageChanges(cachedRunwayMessage!!.context!!)
            }

            if (!isMessagePlay) {
                justRenderMessageAndPlay(runwayMessage, delaySeconds)
            } else {
                //向队列里添加元素
                messagesQueue.offer(runwayMessage)
                //增加排序 高价的放在前面
                messagesQueue.sortWith(Comparator { arg1, arg2 ->
                    val messageContext1: TplBeanExtraContext? = arg1.context
                    val messageContext2: TplBeanExtraContext? = arg2.context
                    val giftFeeValue1: Long = messageContext1?.cacheValue ?: 0
                    val giftFeeValue2: Long = messageContext2?.cacheValue ?: 0

                    giftFeeValue2.compareTo(giftFeeValue1)
                })
            }

        }

    }

    var isClosedNow = false//当view被移除时 关闭
    override fun onAttachedToWindow() {
        isClosedNow = false
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        isClosedNow = true
        isMessagePlay = false
        hide()
        //立即取消还是有必要的 避免内存泄漏 其会立即调用onAnimationEnd 所以还是需要根据状态isClosedNow结束动画
        playMessageAnimator?.cancel()
        playWeekText04?.cancel()

        //取消周星特效
        queueListOfWeekStar.clear()
        weekStarDisappearAni?.cancel()
        weekStarStartAni?.cancel()
        myHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }


    var currentPlayTimes = 0

    //    var messageDuration = 6 * 1000L//消息滚动时间
    private var isMessagePlay = false//当前是不是正在播放
    private var playMessageAnimator: Animator? = null

    /**
     * 3.播放跑道消息
     */
    fun playMessage() {
        val x = ScreenUtils.getViewRealWidth(runwayMessageText)
//        val start = ((x + a) / 2).toFloat()
//        val end = -((x + a) / 2).toFloat()//公式又不对了 2017/3/30
        val runwayWidth = runway_layout.width - DensityHelper.dp2px(10f)
        val start = if (runwayWidth > 0) {
            runwayWidth
        } else {
            realContentLength
        }.toFloat()
        val end = -x.toFloat()//这么简单的动画竟然折腾了一大圈 也是醉了
//        logger.info("可见区宽度：" + realContentLength +" runwayWidth=$runwayWidth 实际=$start"+ "内容长度：" + x)
        playMessageAnimator = ObjectAnimator.ofFloat(runwayMessageText, "translationX", start, end)
        playMessageAnimator!!.interpolator = LinearInterpolator()
        playMessageAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                runwayMessageText.show()
                currentPlayTimes++
            }

            override fun onAnimationEnd(animation: Animator?) {
                runwayMessageText.hide()
                if (isClosedNow) return//当界面关闭时不再循环播放
                val canOnlyPlayOneTime: Boolean? = currentRunwayMessage?.context?.canOnlyPlayOneTime
                val oneTime = canOnlyPlayOneTime ?: false

                var isOld = currentRunwayMessage?.context?.isOld
                isOld = isOld ?: false
                val playOver = isOld || (currentPlayTimes >= MIN_PLAY_TIMES)
                        || (oneTime)

//                ULog.i("跑道信息 currentPlayTimes $currentPlayTimes currentCachePlayTimes:" +currentRunwayMessage?.context?.seconds
//                        +"isOLd:"+currentRunwayMessage?.context?.isOld + "oneTime:" + oneTime)
                if (playOver) {
                    fetchMessageAndRender {
                        if (currentRunwayMessage?.randomGeneratedId.equals(cachedRunwayMessage?.randomGeneratedId)
                            && cachedRunwayMessage?.context!!.isOld
                        ) {
                            //                            ULog.i("缓存的消息就是当前消息 而且已经老了 继续播")
                            animation?.startDelay = 10L
                            animation?.start()
                        } else {
                            //                            ULog.i("继续播放当前缓存消息")
                            if (cachedRunwayMessage != null)
                                justRenderMessageAndPlay(cachedRunwayMessage!!, 0, true)
                        }
                    }
                } else {
                    //继续播放
                    animation?.startDelay = 10L
                    animation?.start()
                }
            }
        })
        playMessageAnimator!!.startDelay = 500L
        playMessageAnimator!!.duration = calculateDuration(x.toFloat())
        playMessageAnimator!!.start()
    }

    private fun calculateDuration(length: Float): Long {
        val width = DensityHelper.px2dp(length)
        //文本长度分解成固定每秒播放的dp宽度计算最终这次的文本需要的播放时间
        val duration = (width / 50f + 0.5f) * 1000
        logger.info("当前的view的长度：" + width + "当前的播放时长：" + duration.toLong())
        return duration.toLong()
    }


    //周星特效
    val queueListOfWeekStar: LinkedList<TplBean> = LinkedList()
    private var weekStarStartAni: AnimatorSet? = null

    private var playWeekText04: ObjectAnimator? = null

    private var weekStarDisappearAni: AnimatorSet? = null

    var isWeekStarPlay = false
    private var currentWeekStar: TplBean? = null

    private fun setWeekStarContent(bean: TplBean) {

        currentWeekStar = bean
        week_star_text.render(bean)
        when (bean.beanType) {
            WeekType.REDPACKET -> {
//                //红包UI
//                week_star_container.backgroundResource = R.drawable.shape_redpacket
//                weekStar_bg.backgroundResource = R.drawable.shape_redpacketstar_gift_bg
//                week_star_icon.hide()
            }
//           WeekType.HOTRANGE -> {
//                //热度UI
//                week_star_text.textColor = GlobalUtils.getColor(R.color.white)
//                week_star_container.backgroundResource = R.drawable.shape_hot_range
//                weekStar_bg.backgroundResource = R.drawable.shape_hot_range_gift_bg
//                week_star_icon.hide()
//            }
//           WeekType.RESCUE -> {
//                //拯救
//                week_star_container.backgroundResource = R.drawable.shape_rescue_runway
//                weekStar_bg.backgroundResource = R.drawable.shape_rescue_head_bg
//                week_star_icon.hide()
//            }
//           WeekType.HEADLINE -> {
//                //主播头条榜
//                week_star_text.textColor = GlobalUtils.formatColor("#FEFEFE")
//                week_star_container.backgroundResource = R.drawable.shape_header_info
//                weekStar_bg.backgroundResource = R.drawable.shape_header_info_icon_bg
//                week_star_icon.hide()
//            }
//           WeekType.DrawAct -> {
//                //抽奖
//                week_star_text.textColor = GlobalUtils.formatColor("#FEFEFE")
//                week_star_container.backgroundResource = R.drawable.shape_header_info
//                weekStar_bg.backgroundResource = R.drawable.shape_header_info_icon_bg
//                week_star_icon.hide()
//            }
            else -> {
                //默认周星UI
                week_star_container.backgroundResource = R.drawable.shape_weekstar
                weekStar_bg.backgroundResource = R.drawable.shape_weekstar_gift_bg
                week_star_icon.show()
            }
        }
        when {
            bean.beanType == WeekType.REDPACKET -> //红包图片地址
                bean.context?.rbIcon?.let {
                    ImageUtils.loadImage(week_gift_icon, it, 16f, 20f)
                }
//            bean.beanType ==WeekType.HOTRANGE -> {//热度
//                ImageUtils.loadImageLocal(week_gift_icon, R.mipmap.lm_core_icon_fire)
//            }
//            bean.beanType ==WeekType.HEADLINE -> {
//                //主播头条榜
//                ImageUtils.loadImageLocal(week_gift_icon, R.mipmap.icon_crown)
//            }
//            bean.beanType ==WeekType.DrawAct -> {
//                //抽奖
//                ImageUtils.loadImageLocal(week_gift_icon, R.mipmap.lm_core_icon_lotto)
//            }
            else -> //周星礼物地址
                bean.context?.giftPic?.let {
                    ImageUtils.loadImage(week_gift_icon, it, 16f, 20f)
                }
        }

//        week_gift_icon.setImageResource(R.mipmap.week_star_gift01)

    }

    /**
     * 从队列循环取消息并且检查消息是否在有效时间内
     */
    private fun fetchAndCheckWeekBean(): TplBean? {

        var isValid = false
        var bean: TplBean? = null
        while (!isValid) {
            if (queueListOfWeekStar.size <= 0) {
                logger.info("最后一条周星播放完毕了")
                return null
            }
            bean = queueListOfWeekStar.poll()
            logger.info("取消息中 ${bean.textTpl}")
            val remind = bean?.context?.remindEndMills
            val currentTime = System.currentTimeMillis()
            if (bean.beanType == WeekType.RESCUE && remind != null) {
                isValid = remind > currentTime
            } else {
                isValid = true
            }
        }
        return bean

    }

    /**
     * 周星播放动画
     */
    fun weekStarAnimPlay(bean: TplBean) {
        queueListOfWeekStar.add(bean)
        if (isWeekStarPlay || queueListOfWeekStar.size <= 0) {
            return
        }
//        logger.info("周星特效开始展示")
        setWeekStarContent(fetchAndCheckWeekBean() ?: return)

        if (weekStarStartAni == null) {
            weekStarStartAni = AnimatorSet()
            //1.周星icon的尺寸从0%到100%  0.33秒 缩放动画
            val scaleX01 = ObjectAnimator.ofFloat(weekStar_bg, View.SCALE_X, 0.0f, 1f)
            val scaleY01 = ObjectAnimator.ofFloat(weekStar_bg, View.SCALE_Y, 0.0f, 1f)
            val enter01 = AnimatorSet()
            enter01.duration = 330L
            enter01.interpolator = LinearInterpolator()
            enter01.playTogether(scaleX01, scaleY01)


            //周星icon以z轴为中心旋转180度  0.42秒
            val rotationY0201 = ObjectAnimator.ofFloat(weekStar_bg, View.ROTATION_Y, -360f, -90f)
            val rotationY0202 = ObjectAnimator.ofFloat(weekStar_bg, View.ROTATION_Y, -90f, 0f)
            rotationY0201.duration = 420L
            rotationY0202.duration = 210L
            //在旋转至90度的时候（0.21秒时）替换成周星礼物图标
            rotationY0201.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    //开始显示礼物图标
//                    logger.info("开始显示礼物图标")
                    week_star_icon.hide()
                    week_gift_icon.show()
                }
            })
            //目的是为了文字控件位置复原
            val resetText0301 = ObjectAnimator.ofFloat(week_star_text, View.TRANSLATION_X, 0f, 0f)
            //左边公告区域从icon后划出，左到右用时0.33秒
            val translationXIn03 = ObjectAnimator.ofFloat(week_star_container, View.TRANSLATION_X, -dip(138).toFloat(), 0f)
            translationXIn03.duration = 330L
            translationXIn03.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    week_star_container.show()
                }
            })
            val translationIn03 = AnimatorSet()
            translationIn03.playTogether(resetText0301, translationXIn03)



            weekStarStartAni?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
//                    logger.info("周星播放开始了")
                    isWeekStarPlay = true
                    week_star_container.hide()
                    week_star_layout.show()
                    if (currentWeekStar?.beanType == WeekType.REDPACKET
                        || currentWeekStar?.beanType == WeekType.HOTRANGE
                        || currentWeekStar?.beanType == WeekType.RESCUE
                        || currentWeekStar?.beanType == WeekType.HEADLINE
                    ) {
                        week_gift_icon.show()
                        week_star_icon.hide()
                    } else {
                        week_star_icon.show()
                        week_gift_icon.hide()
                    }
//                    playWeekText04?.start()
                    playWeekMessageAni()
                }
            })
            weekStarStartAni?.playSequentially(enter01, rotationY0201, rotationY0202, translationIn03)
        }

        if (weekStarDisappearAni == null) {
            weekStarDisappearAni = AnimatorSet()
            //公告栏内信息滚动完成之后停留0.5秒
            //公告栏收回icon后方消失，用时0.5秒
            val translationXOut05 = ObjectAnimator.ofFloat(week_star_container, View.TRANSLATION_X, 0f, -dip(138).toFloat())
            translationXOut05.duration = 500L
            translationXOut05.startDelay = 500L

            //icon大小从100%缩放到0%，透明度从100%到0%，
            //消失用时0.5秒（透明和缩放同步进行）
            val scaleX0601 = ObjectAnimator.ofFloat(weekStar_bg, View.SCALE_X, 1f, 0.0f)
            val scaleY0602 = ObjectAnimator.ofFloat(weekStar_bg, View.SCALE_Y, 1f, 0.0f)
            val alpha0603 = ObjectAnimator.ofFloat(weekStar_bg, View.ALPHA, 1f, 0.0f)
            scaleX0601.duration = 500L
            scaleY0602.duration = 500L
            alpha0603.duration = 500L

            weekStarDisappearAni?.interpolator = LinearInterpolator()
//            weekStarDisappearAni.playTogether(scaleX0601, scaleY0602, alpha0603)
            weekStarDisappearAni!!.play(scaleX0601).with(scaleY0602).with(alpha0603).after(translationXOut05)
            weekStarDisappearAni?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    isWeekStarPlay = false
                    weekStar_bg.alpha = 1.0f
                    week_star_layout.hide()

                    //
                    val newBean = fetchAndCheckWeekBean()
                    if (newBean != null) {
                        setWeekStarContent(newBean)
                        weekStarStartAni?.startDelay = 100
                        weekStarStartAni?.start()
                    }
                }
            })
        }

        weekStarStartAni?.start()
    }

    //文字播放动画特殊处理 因为文字长度可变 每次要重新计算
    //公告栏停止之后停留2秒
    //公告栏内信息滚动，速度2字/秒
    fun playWeekMessageAni() {
        val weekTextWidth = ScreenUtils.getViewRealWidth(week_star_text)
        val canSeeWidth = dip(110).toFloat()
        val end = if (weekTextWidth > canSeeWidth) {
            weekTextWidth.toFloat() - canSeeWidth
        } else {
            0f
        }
        playWeekText04 = ObjectAnimator.ofFloat(week_star_text, View.TRANSLATION_X, 0f, -end)
        playWeekText04?.duration = calculateDuration(end)
        playWeekText04?.startDelay = 1000L
        playWeekText04?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                logger.info("文字播完以后重置位置")
                if (currentWeekStar?.beanType == WeekType.RESCUE) {
                    weekStarDisappearAni?.startDelay = 3000
                    weekStarDisappearAni?.start()
                } else {
                    weekStarDisappearAni?.startDelay = 1000
                    weekStarDisappearAni?.start()
                }

            }
        })
        playWeekText04?.start()
    }

}
