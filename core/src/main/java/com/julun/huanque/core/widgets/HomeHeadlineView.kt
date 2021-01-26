package com.julun.huanque.core.widgets

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
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.TplBeanExtraContext
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.view_home_headline_container.view.*
import org.jetbrains.anko.dip
import java.util.*


class HomeHeadlineView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    private val logger = ULog.getLogger("HomeHeadlineView")

    companion object {
        const val SVG_DURATION: Long = 2750L //svg动画播放时间
        const val SLIDE_IN_DURATION: Long = 700L //滑入的动画时间
        const val SLIDE_OUT_DURATION: Long = 625L //滑出的动画时间

        const val MIN_PLAY_TIMES = 3 //播放次数


    }

    private val myHandler: Handler by lazy { Handler() }


    init {
        LayoutInflater.from(context).inflate(R.layout.view_home_headline_container, this)
        this.isFocusable = true
        this.isClickable = true
        runway_headLine.onClickNew {
            logger.info("click runwayMessageText")
            currentRunwayMessage?.let { tpl ->
                AppHelper.openTouch(tpl.textTouch ?: return@let, tpl.context?.touchValue ?: "")
            }

        }
        runway_container.setOnTouchListener { _, event ->
            logger.info("touch runway_layout")
            if (event.action == MotionEvent.ACTION_DOWN) {
                runway_headLine.performClick()
            }
            true
        }
    }


    private var currentRunwayMessage: TplBean? = null
    private var cachedRunwayMessage: TplBean? = null
    private val messagesQueue: LinkedList<TplBean> = LinkedList()    //等待播放的队列

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
        logger.info("清空所有跑道消息")
        slideOut()
        currentRunwayMessage = null
        cachedRunwayMessage = null
        currentPlayTimes = 0
        isMessagePlay = false
        isFirstRun = true
    }

    private var isFirstRun = true //是不是第一次播放

    /**
     * 消息开始处理
     */
    private fun justRenderMessageAndPlay(
        tplBean: TplBean,
        delaySeconds: Int = 0
    ) {
        currentRunwayMessage?.context?.isOld = true//每次播放前都将上一个消息变成老消息 如果有的话
        isMessagePlay = true
//        val bean = tplBean.preProcess()
        currentRunwayMessage = tplBean

        myHandler.postDelayed({

            if (!isFirstRun) {
                slideOut(end = {
                    slideIn()
                })
            } else {
                isFirstRun = false
                slideIn()
            }

        }, delaySeconds * 1000L)

    }

    private fun processDataAndRender2(tplBean: TplBean) {
        runway_headLine.render(tplBean)
//        val messageContext: TplBeanExtraContext? = tplBean.context
//        if (messageContext != null) {
//            try {
//                val bgColor = messageContext.bgColor
//                val colors = bgColor.split("-") as AbstractList
//                if (colors.isNotEmpty()) {
//
//                    var alpha: Float? = null
//
//                    val ls = colors[colors.size - 1]
//                    if (!ls.contains("#"))
//                        alpha = ls.toFloat()
//
//                    if (alpha != null) {
//                        logger.info("透明度：" + (alpha * 255).toInt())
//                        colors.removeAt(colors.size - 1)
//                    }
//                    val colorInts = arrayListOf<Int>()
//                    colors.forEach {
//                        val color = GlobalUtils.formatColor(it, R.color.colorPrimary_lib)
//                        colorInts.add(color)
//                    }
//                    if (colorInts.size > 1) {
//                        val gDrawable = GradientDrawable(
//                            GradientDrawable.Orientation.LEFT_RIGHT,
//                            colorInts.toIntArray()
//                        )
//                        gDrawable.cornerRadius = DensityHelper.dp2pxf(52)
//                        gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
//                        if (alpha != null) {
//                            gDrawable.alpha = (alpha * 255).toInt()
//                        }
//                        runway_container.backgroundDrawable = gDrawable
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//        }
    }

    /**
     *
     */
    private fun slideOut(start: () -> Unit = {}, end: () -> Unit = {}) {
        val width = ScreenUtils.screenWidthFloat - dip(100)
        val translationXOut01 = ObjectAnimator.ofFloat(runway_container, View.TRANSLATION_X, 0f, -width)
//        val translationXOut02 = ObjectAnimator.ofFloat(
//            runway_icon,
//            View.TRANSLATION_X,
//            0f, -width
//        )
        translationXOut01?.addListener(object : DefaultAnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                runway_container.hide()
                end()
            }
        })
        start()
        translationXOut01.setDuration(SLIDE_OUT_DURATION).start()
//        translationXOut02.setDuration(SLIDE_OUT_DURATION).start()
    }

    private var enterAllSet: AnimatorSet? = null

    /**
     *  2.橫幅进场
     */

    private fun slideIn() {
//        logger.info("slideIn")
        //跑道飞机一起走
        if (enterAllSet == null) {
            enterAllSet = AnimatorSet()
            val translationXIn102 = ObjectAnimator.ofFloat(
                runway_container,
                View.TRANSLATION_X,
                ScreenUtils.screenWidthFloat,
                0f
            )
            translationXIn102.duration = SLIDE_IN_DURATION
            translationXIn102.interpolator = AnticipateOvershootInterpolator(1f)

            translationXIn102.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (ViewCompat.isAttachedToWindow(this@HomeHeadlineView)) {
                        playMessage()
                    }
                }

                override fun onAnimationStart(animation: Animator?) {
                    if (currentRunwayMessage != null) {
                        //processDataAndRender(currentRunwayMessage!!)
                        processDataAndRender2(currentRunwayMessage!!)
                    }
                    runway_container.show()
                }

            })
            enterAllSet?.playSequentially(translationXIn102/*, disappear*/)
        }
        enterAllSet?.start()
    }

    /**
     * 渲染最后一条跑道消息如果存在  真正的消息跑道唤醒入口
     * @param runwayMessage 传入的message
     */
    fun loadLastRunwayMessage(
        runwayMessage: TplBean,
        delaySeconds: Int = 0
    ) {
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
            }

            if (!isMessagePlay) {
                justRenderMessageAndPlay(runwayMessage, delaySeconds)
            } else {
                //向队列里添加元素
                messagesQueue.offer(runwayMessage)
                //增加排序 高价的放在前面
//                messagesQueue.sortWith(Comparator { arg1, arg2 ->
//                    val messageContext1: TplBeanExtraContext? = arg1.context
//                    val messageContext2: TplBeanExtraContext? = arg2.context
//                    val giftFeeValue1: Long = messageContext1?.cacheValue ?: 0
//                    val giftFeeValue2: Long = messageContext2?.cacheValue ?: 0
//
//                    giftFeeValue2.compareTo(giftFeeValue1)
//                })
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
        val x = ScreenUtils.getViewRealWidth(runway_headLine)
//        val start = ((x + a) / 2).toFloat()
//        val end = -((x + a) / 2).toFloat()//公式又不对了 2017/3/30
        val runwayWidth = runway_container.width - DensityHelper.dp2px(10f)
        val start = if (runwayWidth > 0) {
            runwayWidth
        } else {
            ScreenUtils.getScreenWidth() - dp2px(100)
        }.toFloat()
        val end = -x.toFloat()//这么简单的动画竟然折腾了一大圈 也是醉了
//        logger.info("可见区宽度：" + realContentLength +" runwayWidth=$runwayWidth 实际=$start"+ "内容长度：" + x)
        playMessageAnimator = ObjectAnimator.ofFloat(runway_headLine, "translationX", start, end)
        playMessageAnimator!!.interpolator = LinearInterpolator()
        playMessageAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                runway_headLine.show()
                currentPlayTimes++
            }

            override fun onAnimationEnd(animation: Animator?) {
                runway_headLine.hide()
                if (isClosedNow) return//当界面关闭时不再循环播放
                val canOnlyPlayOneTime: Boolean? = currentRunwayMessage?.context?.canOnlyPlayOneTime
                val oneTime = canOnlyPlayOneTime ?: false
                val runwayPlayTimes =
                    currentRunwayMessage?.context?.playTimes ?: MIN_PLAY_TIMES
                val isOld = currentRunwayMessage?.context?.isOld ?: false
                val playOver = isOld || (currentPlayTimes >= runwayPlayTimes)
                        || (oneTime)

//                ULog.i("跑道信息 currentPlayTimes $currentPlayTimes currentCachePlayTimes:" +currentRunwayMessage?.context?.seconds
//                        +"isOLd:"+currentRunwayMessage?.context?.isOld + "oneTime:" + oneTime)
                if (playOver) {
                    fetchMessageAndRender {
                        if (currentRunwayMessage?.randomGeneratedId.equals(cachedRunwayMessage?.randomGeneratedId)
                            && cachedRunwayMessage?.context!!.isOld
                        ) {
//                            logger.info("缓存的消息就是当前消息 而且已经老了 继续播")
                            animation?.startDelay = 10L
                            animation?.start()
                        } else {
//                            logger.info("继续播放当前缓存消息")
                            if (cachedRunwayMessage != null)
                                justRenderMessageAndPlay(cachedRunwayMessage!!, 0)
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

}
