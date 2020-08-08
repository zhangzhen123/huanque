package com.julun.huanque.core.widgets.live.simpleffects

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.julun.huanque.common.bean.beans.SendGiftEvent
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.widgets.live.WebpGifView
import com.julun.huanque.common.widgets.svgaView.SVGAPlayerView
import com.julun.huanque.core.R
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.view_simple_effects.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor
import java.lang.ref.WeakReference
import java.util.*

/**
 *
 *@author zhangzhen
 *@data 2019/3/7
 * 礼物连击特效
 *
 **/


class SimpleGiftEffectsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val logger = ULog.getLogger("SimpleGiftEffectsView")

    companion object {
        //连送背景：lm/room/stream/bg1.png  (1-5)
        //连送动画：lm/room/stream/liansong2.webp (2-5)
        //升级动画：lm/room/stream/shengji2.webp (2-5
        val LIANSONG_WEBP_URL = com.julun.huanque.common.BuildConfig.IMAGE_SERVER_URL + "lm/room/stream/liansong"
        val SHENGJI_WEBP_URL = com.julun.huanque.common.BuildConfig.IMAGE_SERVER_URL + "lm/room/stream/shengji"
        // 当前view动画状态
        private const val STATE_ENABLE: Int = 0
        private const val STATE_PLAYING: Int = 1
        private const val STATE_PLAY_PAUSING: Int = 2
        private const val STATE_PLAY_ENDING: Int = 3

        // view显示3秒后移除
        private const val SEND_GIFT_PAUSE_DURATION_MAX: Long =2400
        private const val SEND_GIFT_PAUSE_DURATION_LUCKY_MIN: Long =1000
        private const val SEND_GIFT_PAUSE_DURATION_MIN: Long = 500

        // 显示、隐藏动画时长
        private const val SEND_GIFT_SHOW_DURATION: Long = 300
        // 数字放大动画时长
        private const val SEND_GIFT_SCALE_DURATION: Long = 600

        private const val LUCKY_AWARD_SMALL_SHOW: Long = 800
    }

    // 动画显示初始状态
    private var animViewSate: Int =
        STATE_ENABLE
    // 最新礼物显示数据
    private var lastGift: SendGiftEvent? = null
    // 另外一个互相引用的view
    private var otherWeakView: WeakReference<SimpleGiftEffectsView>? = null
    // 用来调度停止动画
    private var endHandler: Handler? = null
    private var endRunnable: Runnable? = null

    private var parentWeakView: WeakReference<SimpleGiftEffectsContainerView>? = null
    fun setOtherAnimationView(otherAnimationView: SimpleGiftEffectsView, parentView: SimpleGiftEffectsContainerView) {
        this.otherWeakView = WeakReference<SimpleGiftEffectsView>(otherAnimationView)
        this.parentWeakView = WeakReference<SimpleGiftEffectsContainerView>(parentView)
        lastGift = SendGiftEvent(giftId = -100)//giftId = -100为了防止后台不返回giftId而导致与默认初始值相同 导致无法判断该事件唯一性
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_simple_effects, this)

        if(!isInEditMode){
            endHandler = Handler()
            endRunnable = Runnable {
                endPlaying()
            }
            text_send.setTFFugazOne()
            text_x.setTFFugazOne()
            giftCountText.setTFFugazOne()
        }

        send_gift_effects.setCallBack(object : WebpGifView.GiftViewPlayCallBack {
            //如果当前view被hide了 就会直接执行onRelease 而不走onEnd
            //为了防止执行多次 hide后就会执行onRelease 这里统一到onRelease执行
            override fun onStart() {
            }

            override fun onError() {
//                logger.info("播放错误--------------------")
                onEnd()
            }

            override fun onEnd() {
//                logger.info("播放结束--------------------")
                send_gift_effects?.hide()
//                send_gift_effects.alpha=0f
                isGiftEffectPlay = false
                playGiftEffects()
            }

            override fun onRelease() {
//                logger.info("回收--------------------visiable:${send_gift_effects.visibility}")
                //只有当view不可见时被回收才代表完成（当view不可见时fresco会立即执行onRelease,而当每次设置新的uri时 show也会执行onRelease）
                if (send_gift_effects.visibility != View.VISIBLE) {
                    onEnd()
                }
            }

        })
        send_gift_update.setCallBack(object : WebpGifView.GiftViewPlayCallBack {
            override fun onStart() {
            }

            override fun onError() {
                onEnd()
            }

            override fun onEnd() {
                send_gift_update?.hide()
            }

            override fun onRelease() {
            }

        })


    }

    // 根据状态判断是否能播放动画，
    // 如果是暂停状态，只能播放同用户同礼物的动画
    fun isCanPlay(newGift: SendGiftEvent): Boolean {
        when (animViewSate) {
            STATE_ENABLE -> return true
            STATE_PLAYING, STATE_PLAY_PAUSING -> return SimpleGiftEffectsContainerView.getUserOrderKey(newGift) == SimpleGiftEffectsContainerView.getUserOrderKey(lastGift)
            else -> return false
        }
    }

    fun isPlaySame(newGift: SendGiftEvent): Boolean {
        return SimpleGiftEffectsContainerView.getUserOrderKey(newGift) == SimpleGiftEffectsContainerView.getUserOrderKey(lastGift)
    }

    // 开始播放动画，传入一个送礼的实体类
    fun startPlaying(newGift: SendGiftEvent) {
        logger.info("startPlaying $newGift")
        if(endHandler==null)return
        if(animViewSate== STATE_PLAYING){
//          logger.info("此时正在播放着 直接刷新数字：$newGift")
            replaceAndRefreshView(newGift)
            playLuckyShow(newGift)
        }else if (animViewSate == STATE_PLAY_PAUSING) {
            animViewSate =
                STATE_PLAYING
//            logger.info("有新礼物过来，取消结束动画")
            // 暂停状态，有新礼物过来，取消结束动画
            endHandler?.removeCallbacks(endRunnable)
//            checkAndIncrementCount(newGift)
            replaceAndRefreshView(newGift)
            playLuckyShow(newGift)
            startScaleCounting()
        } else {
//            checkAndIncrementCount(newGift)
            replaceAndRefreshView(newGift)
            this.visibility = View.VISIBLE
            animViewSate =
                STATE_PLAYING
            val anim = GiftAnimationHelper.createFlyFromLtoR(this, (-width).toFloat(), 0f,
                SEND_GIFT_SHOW_DURATION
            )
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator?) {
                    onAnimationEnd(animation)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    playLuckyShow(newGift)
                    startScaleCounting()
                }
            })
            anim.start()
        }
    }

    private fun replaceAndRefreshView(newGift: SendGiftEvent) {
        this.lastGift = newGift
        sendNicknameText.text = lastGift!!.nickname
        giftNameText.text = "送出 ${lastGift!!.giftName}"
        ImageUtils.loadImage(giftImage, lastGift!!.giftPic, 46f, 46f)
        giftCountText.text = "${lastGift!!.times} "

//        ImageUtils.loadImage(sdv_sender, newGift.headPic, 40f, 40f)
        sdv_sender.setImage(headUrl = newGift.headPic,headSize = 40,frameUrl = newGift.headFrame,frameWidth = 52,frameHeight = 66)
        //设置文字颜色
        when (newGift.level) {
            1 -> {
                setTextColor(R.color.simple_gift_font_01, R.color.simple_gift_font_stroke_01)
            }
            2 -> {
                setTextColor(R.color.simple_gift_font_02, R.color.simple_gift_font_stroke_02)
            }
            3 -> {
                setTextColor(R.color.simple_gift_font_03, R.color.simple_gift_font_stroke_03)
            }
            4 -> {
                setTextColor(R.color.simple_gift_font_04, R.color.simple_gift_font_stroke_04)
            }
            5 -> {
                setTextColor(R.color.simple_gift_font_05, R.color.simple_gift_font_stroke_05)
            }
        }
    }

    fun setTextColor(@ColorRes textColor: Int, @ColorRes strokeColor: Int) {
        text_send.strokeColor = ContextCompat.getColor(context, strokeColor)
        text_send.textColor = ContextCompat.getColor(context, textColor)
        text_x.strokeColor = ContextCompat.getColor(context, strokeColor)
        text_x.textColor = ContextCompat.getColor(context, textColor)
        giftCountText.strokeColor = ContextCompat.getColor(context, strokeColor)
        giftCountText.textColor = ContextCompat.getColor(context, textColor)
    }

    // 数量放大动画
    fun startScaleCounting() {
//        logger.info("startScaleCounting")
        setEffectBg()
        giftCountText.show()
        val anim = GiftAnimationHelper.scaleGiftNumNew(giftCountText)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                onAnimationEnd(animation)
            }

            override fun onAnimationEnd(animation: Animator?) {
                checkNextPlay()
//                playGiftEffects()
            }
        })
        anim.start()
        //独立的特效 有就播
        if (lastGift != null && lastGift!!.level > 1) {
            queueEffects.add(lastGift!!)
        }
        playGiftEffects()
    }

    private fun setEffectBg() {
        //展示连送升级特效 与数字跳动一起做
        if (lastGift?.upgrade == true) {
            send_gift_update.show()
            val url = "$SHENGJI_WEBP_URL${lastGift?.level}.webp"
            send_gift_update.setURI(url, 1)
        }
        val effectLevel = lastGift?.level
        //设置连送背景
        when (effectLevel) {
            1 -> {
                leftLayout.setBackgroundResource(R.mipmap.simple_gift_bg_01)
            }
            2 -> {
                leftLayout.setBackgroundResource(R.mipmap.simple_gift_bg_02)
            }
            3 -> {
                leftLayout.setBackgroundResource(R.mipmap.simple_gift_bg_03)
            }
            4 -> {
                leftLayout.setBackgroundResource(R.mipmap.simple_gift_bg_04)
            }
            5 -> {
                leftLayout.setBackgroundResource(R.mipmap.simple_gift_bg_05)
            }
        }
    }

    var isGiftEffectPlay = false
    private val queueEffects = LinkedList<SendGiftEvent>()
    private fun playGiftEffects() {
        if (isGiftEffectPlay || queueEffects.isEmpty()) {
            return
        }
        isGiftEffectPlay = true
        val effects = queueEffects.poll() ?: return
        val effectLevel = effects.level
        val sgelp = send_gift_effects.layoutParams

        //展示连击特效 2级以上才有
//        logger.info("playGiftEffects")
        var url = "$LIANSONG_WEBP_URL$effectLevel.webp"
//        url = "http://cdn.51lm.tv/lm/room/stream/liansong5.webp"
        //2级大小 105*105 3级140 4级230 5级260
        when (effectLevel) {
            2 -> {
                sgelp.width = dip(105)
                sgelp.height = dip(105)
            }
            3 -> {
                sgelp.width = dip(140)
                sgelp.height = dip(140)
            }
            4 -> {
                sgelp.width = dip(230)
                sgelp.height = dip(230)
            }
            5 -> {
                sgelp.width = dip(250)
                sgelp.height = dip(250)
            }
        }
        send_gift_effects.show()
        send_gift_effects.setURI(url, 1)

    }

    /**
     * 检测下一条
     */
    fun checkNextPlay() {
        if(endHandler==null){
            logger.info("说明view被销毁了")
            return
        }
        animViewSate =
            STATE_PLAY_PAUSING

        val result = parentWeakView?.get()?.checkAndPick(this, otherWeakView?.get())
        if (result != null) {
            logger.info("存在相同的礼物")
        } else {
            queueEffects.clear()
            endHandler?.removeCallbacks(endRunnable)
            //先判断队列高价值
            val hasHighGift = parentWeakView?.get()?.haveHighlyGift(lastGift,otherWeakView?.get())
            if (hasHighGift == true) {
                if (lastGift != null && lastGift!!.luckBeans > 0) {
//                    logger.info("存在更高价值礼物并且是幸运礼物 1s后隐藏")
                    endHandler?.postDelayed(endRunnable,
                        SEND_GIFT_PAUSE_DURATION_LUCKY_MIN
                    )
                } else {
//                    logger.info("存在更高价值礼物 0.5秒后隐藏：$lastGift")
                    endHandler?.postDelayed(endRunnable,
                        SEND_GIFT_PAUSE_DURATION_MIN
                    )
                }
            } else {
//                logger.info("普通流光 3秒后隐藏$lastGift")
                endHandler?.postDelayed(endRunnable,
                    SEND_GIFT_PAUSE_DURATION_MAX
                )
            }
//            lastGift = null
//            endHandler?.postDelayed(endRunnable, SEND_GIFT_PAUSE_DURATION)
        }
    }

    // 结束播放
    private fun endPlaying() {
        isGiftEffectPlay = false
        // 动画暂停中，过3秒消失
        animViewSate =
            STATE_PLAY_ENDING
        lastGift = null
        val anim = GiftAnimationHelper.createFadeAnimator(this, 0f, (-width).toFloat(),
            SEND_GIFT_SHOW_DURATION
        )
        anim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator?) {
                onAnimationEnd(animation)
            }

            override fun onAnimationEnd(animation: Animator?) {
                this@SimpleGiftEffectsView.visibility = View.GONE
                lucky_small_tips.hide()
                lucky_big_tips.hide()
                giftCountText.inVisible()
                animViewSate =
                    STATE_ENABLE
                // 队列中还有数据，继续动画
                val result = parentWeakView?.get()?.checkAndPick(this@SimpleGiftEffectsView, otherWeakView?.get())
                if(result==null){
                    logger.info("队列中没有合适消息了 不再执行")
                }else{
                    logger.info("队列中找到合适消息继续执行：$result")
                }
            }
        })
        anim.start()
    }

    private fun playLuckyShow(newGift: SendGiftEvent) {
        if (newGift.luckBeans > 0) {
//            logger.info("有中奖 ${newGift.luckBeans}")
            if (newGift.luckAnima == "T") {
                startPlayLuckySvga(lucky_big_tips, "svga/lucky_tip.svga", "${newGift.luckBeans}")
            } else {
                startPlayLuckyView("${newGift.luckBeans}")
            }
        }
    }

    private fun startPlayLuckyView(message: String) {
        lucky_big_tips.hide()
        lucky_small_tips.show()
        lucky_tips_award.setTFDinCdc2()
        lucky_tips_award.text = message
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable,
            LUCKY_AWARD_SMALL_SHOW
        )
    }

    private val hideHandler: Handler by lazy {
        Handler()
    }
    private val hideRunnable: Runnable by lazy {
        Runnable {
//            logger.info("小中奖开始隐藏")
            lucky_small_tips?.hide()
        }
    }

    //播放SVGA动画
    private fun startPlayLuckySvga(svgaPlayer: SVGAPlayerView, url: String?, message: String) {
        if (url == null) {
            return
        }
        lucky_small_tips.hide()
        val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
//                svga.setVideoItem(videoItem)
                //这里一个坑 千万别使用库的该类SVGADrawable
                val drawable = com.julun.huanque.common.widgets.svgaView.SVGADrawable(videoItem, requestDynamicItem(message))
                svgaPlayer.setImageDrawable(drawable)
                svgaPlayer.startAnimation()
            }

            override fun onError() {
                logger.info("onError")
            }
        }
        svgaPlayer.show()
        SVGAHelper.startParse(url, callback)
    }

    /**
     * 进行简单的文本替换
     * @return
     */
    private fun requestDynamicItem(message: String): SVGADynamicEntity {
        val dynamicEntity = SVGADynamicEntity()
        val textPaint = TextPaint()
        textPaint.color = Color.WHITE
        val tf = Typeface.createFromAsset(context.assets, "fonts/DINCondensedC-2.ttf")
        textPaint.typeface = tf
        textPaint.textSize = dip(9).toFloat()
        dynamicEntity.setDynamicText(message, textPaint, "img_28")
        return dynamicEntity
    }

    fun resetLastGiftObj() {
        lastGift = SendGiftEvent(giftId = -100)//giftId = -100为了防止后台不返回giftId而导致与默认初始值相同 导致无法判断该事件唯一性
    }

    fun destoryResource() {
        this.clearAnimation()
        otherWeakView?.clear()
        endHandler?.removeCallbacks(endRunnable)
        endHandler?.removeCallbacksAndMessages(null)
        endHandler = null

        hideHandler.removeCallbacksAndMessages(null)
    }
}
