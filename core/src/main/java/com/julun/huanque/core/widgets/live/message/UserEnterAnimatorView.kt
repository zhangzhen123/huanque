package com.julun.huanque.core.widgets.live.message

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.EnterTypeBean
import com.julun.huanque.common.bean.beans.RoyalChangeEventBean
import com.julun.huanque.common.constant.EnterType
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.widgets.live.WebpGifView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.manager.PlayerViewManager
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.view_room_user_enter.view.*
import org.jetbrains.anko.backgroundDrawable
import java.util.*

/**
 *
 *@author zhangzhen
 *@data 2017/3/6
 *
 **/
class UserEnterAnimatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val logger = ULog.getLogger("UserEnterAnimatorView")

    //    private var viewModel: EntranceViewModel? = null
    val queueList: MutableList<EnterTypeBean> = Collections.synchronizedList(mutableListOf())

    //屏幕宽度
    private val screenWidth = ScreenUtils.screenWidthFloat

    init {
        LayoutInflater.from(context).inflate(R.layout.view_room_user_enter, this)
        luxury_enter.setCallBack(object : WebpGifView.GiftViewPlayCallBack {
            override fun onStart() {

                if (!isRoyalAni && !isLuxuryPlay) {
                    luxuryAnimatorSet?.start()
                    mAnimatorSet?.cancel()
                    mAnimatorSet?.start()
//                    entranceViewModel.animatorState.value = true
                }
            }

            override fun onError() {
                if (!isRoyalAni) {
                    if (!isLuxuryPlay) {
                        mAnimatorSet?.cancel()
                        mAnimatorSet?.start()
                    }
                } else if (isRoyalAni) {
                    mRoyalHideAni?.start()
                }

            }

            override fun onEnd() {
                logger.info("luxury_enter webp end")
                if (isRoyalAni) {
                    mRoyalHideAni?.start()
                }
            }

            override fun onRelease() {
            }

        }
        )
        val olp = open_royal_layout.layoutParams as FrameLayout.LayoutParams
        olp.bottomMargin = PlayerViewManager.SCREEN_WIDTH / 2 - dp2px(24)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //todo test
//        repeat(2) {
//            playEnterAnimator(
//                EnterTypeBean(
//                    EnterTypeBean.OPEN_ROYAL, RoyalChangeEventBean(
//                        headPic = "user/cover/466133f8310147de80185d55ba97e723.jpg",
//                        headFrame = "config/frame/RH4-74.png?w=74&h=74",
//                        nickname = "名字很长还带彩虹屁也",
//                        royalChangeTypeText = "开通",
//                        royalLevelName = "帝皇",
//                        bgColor = "#AC12FF-#D47CFF",
//                        webUrl = "config/anim/5ca07bda52eb44a89b950207e9625a81.webp?playCount=1&playSeconds=4",
//                        royalPic = "level/royal/R4.png"
//                    )
//                )
//            )
//
//        }

    }

    private var isPlaying = false
    private var mAnimatorSet: AnimatorSet? = null

    /**
     * 方法名不变 但作用是执行所有的动画 包括坐骑 贵族 守护入场
     * 新增开通贵族动画
     */
    fun playEnterAnimator(bean: EnterTypeBean) {
        queueList.add(bean)
        logger.info("开始动画playEnterAnimator")
        if (isPlaying || queueList.size <= 0) return
        checkAnimationBean()
    }

    /**
     * 开始检票
     */
    private fun checkAnimationBean() {

        if (queueList.size > 0) {
            isPlaying = true
            val bean: EnterTypeBean = queueList.removeAt(0)
            try {
                when (bean.type) {
                    EnterTypeBean.COMMON_ENTER -> {
                        setAnimationBean(bean.content as TplBean)
                    }
                    EnterTypeBean.OPEN_ROYAL -> {
                        playRoyalAnimator(bean.content as RoyalChangeEventBean)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                isPlaying = false
            }
        }
    }

    private fun setAnimationBean(bean: TplBean) {

        if (bean.context == null) {
            isPlaying = false
            return
        }
        //判断这个属性是不是空 不是空代表时新的入场动画
        val carType = bean.context!!.type

        val svga = bean.context!!.svga

        val webp = bean.context!!.webp

        val theme = bean.context!!.theme

//        val evlp = this.layoutParams as FrameLayout.LayoutParams
//        val etvlp = enter_text_view.layoutParams as RelativeLayout.LayoutParams

        try {
            val colors = theme.split("-") as AbstractList
            if (colors.isNotEmpty()) {

                var alpha: Float? = null

                val ls = colors[colors.size - 1]
                if (!ls.contains("#"))
                    alpha = ls.toFloat()

                if (alpha != null) {
                    logger.info("透明度：" + (alpha * 255).toInt())
                    colors.removeAt(colors.size - 1)
                }
                val colorInts = arrayListOf<Int>()
                colors.forEach {
                    //                    var color: Int?
//                    try {
//                        color = Color.parseColor(it)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                    if (color != null)
                    val color = GlobalUtils.formatColor(it, R.color.colorPrimary_lib)
                    colorInts.add(color)
                }
                if (colorInts.size > 1) {
                    val gDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorInts.toIntArray())
                    gDrawable.cornerRadius = DensityHelper.dp2pxf(14)
                    gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    if (alpha != null) {
                        gDrawable.alpha = (alpha * 255).toInt()
                    }
                    enter_text_view.backgroundDrawable = gDrawable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //普通特效与高级特效切换时改变高度
        val thisLp = enter_mini_container.layoutParams
        when (carType) {
            EnterType.MINI -> {
                thisLp.height = DensityHelper.dp2px(150f)
                val str1s = svga.split("?")
                var svgaUrl = ""
                if (str1s.isNotEmpty()) {
                    svgaUrl = str1s[0]
                }
                val svgaMap = MixedHelper.getParseMap(svga)
                startPlaySVGA(svgaUrl)
                setAnimator(svgaMap, false)
                mAnimatorSet?.start()
            }
            EnterType.SKILL -> {
                val str2s = webp.split("?")
                var webpUrl = ""
                if (str2s.isNotEmpty()) {
                    webpUrl = str2s[0]
                }
                val webpMap = MixedHelper.getParseMap(webp)

                //优先使用webp
                if (webpUrl.isNotEmpty() && webpMap.isNotEmpty()) {
                    thisLp.height = DensityHelper.dp2px(100f)
                    enter_bg_svga.hide()
                    setAnimator(webpMap, true)
//                    viewModel?.messageState?.value = webp
                    playLuxuryAnimator(webp)
                } else {
                    thisLp.height = DensityHelper.dp2px(150f)
                    val str1s = svga.split("?")
                    var svgaUrl = ""
                    if (str1s.isNotEmpty()) {
                        svgaUrl = str1s[0]
                    }
                    val svgaMap = MixedHelper.getParseMap(svga)
                    startPlaySVGA(svgaUrl)
                    setAnimator(svgaMap, false)
                    mAnimatorSet?.start()
                }
            }
        }
        enter_text_view.render(bean, finalColor = "#ffffff")

    }

    /**
     * 显示入场动画
     *
     * isLux 是不是豪华动画 如果是延迟完成
     */
    private fun setAnimator(map: HashMap<String, Float>, isLux: Boolean) {

        val start: Float = map["in"] ?: 1.260f
        val stop = map["stop"] ?: 6.510f
        val out = map["out"] ?: 1.540f


        //View宽度
//        val viewWidth = ScreenUtils.getViewRealWidth(enter_text_view).toFloat()
        val animatorEnter = ObjectAnimator.ofFloat(enter_mini_container, "translationX", screenWidth, 0f)
        animatorEnter.duration = (start * 1000).toLong()
        animatorEnter.interpolator = LinearInterpolator()

        val animatorExit = ObjectAnimator.ofFloat(enter_mini_container, "translationX", 0f, -screenWidth)
        animatorExit.duration = (out * 1000).toLong()
        animatorExit.startDelay = (stop * 1000).toLong()
        animatorExit.interpolator = LinearInterpolator()
        mAnimatorSet?.cancel()
        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.play(animatorExit)?.after(animatorEnter)
        mAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
//                this@UserEnterAnimatorView.show()
                enter_mini_container.show()
                logger.info("startFortyAnimator${queueList.size}")
            }

            override fun onAnimationEnd(animation: Animator) {
                var delay = 0L
                if (isLux) {
                    delay = 100L
                }
                postDelayed({
                    logger.info("endFortyAnimator")
                    isPlaying = false
                    if (!ViewCompat.isAttachedToWindow(this@UserEnterAnimatorView)) {
                        return@postDelayed
                    }
                    if (queueList.size <= 0) {
//                        this@UserEnterAnimatorView?.hide()
                        enter_mini_container?.hide()
                        return@postDelayed
                    }
//                    setAnimationBean()
                    checkAnimationBean()
                }, delay)
            }
        })
    }

    //播放SVGA动画
    private fun startPlaySVGA(url: String?) {
        if (url == null || url.isEmpty()) {
            return
        }
        enter_bg_svga.show()
        val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                enter_bg_svga.setVideoItem(videoItem)
                enter_bg_svga.startAnimation()
            }

            override fun onError() {}
        }
        //记得带上域名
        SVGAHelper.startParse(StringHelper.getOssImgUrl(url), callback)
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
        luxury_enter.alpha = 1f
        luxury_enter?.show()
        luxury_enter.setURI(StringHelper.getOssImgUrl(webpUrl), 1)
    }

    /**
     * 显示开通贵族相关动画
     *
     */
    private var isRoyalAni: Boolean = false
    private var mRoyalHideAni: AnimatorSet? = null
    private fun playRoyalAnimator(bean: RoyalChangeEventBean) {
        val headPic: String = bean.headPic
        val headFrame: String = bean.headFrame
        val text: String = "恭喜${bean.nickname}${bean.royalChangeTypeText}了${bean.royalLevelName}"
        val bgColor: String = bean.bgColor
        val webpUrl: String = bean.webUrl
        mPhotoHeadView.setImageCustomByOneFrameSide(headUrl = headPic, frameUrl = headFrame, headSize = 30, frameHeight = 48)
        tv_open_royal.text = text
        miniRoyal.loadImage(bean.royalPic, 16f, 16f)
        try {
            val colors = bgColor.split("-") as AbstractList
            if (colors.isNotEmpty()) {

                var alpha: Float? = null

                val ls = colors[colors.size - 1]
                if (!ls.contains("#"))
                    alpha = ls.toFloat()

                if (alpha != null) {
                    logger.info("透明度：" + (alpha * 255).toInt())
                    colors.removeAt(colors.size - 1)
                }
                val colorInts = arrayListOf<Int>()
                colors.forEach {
                    val color = GlobalUtils.formatColor(it, R.color.colorPrimary_lib)
                    colorInts.add(color)
                }
                if (colorInts.size > 1) {
                    val gDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorInts.toIntArray())
                    gDrawable.cornerRadius = DensityHelper.dp2pxf(15)
                    gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    gDrawable.setStroke(dp2px(1f), GlobalUtils.formatColor("#B3FFFFFF"))
                    if (alpha != null) {
                        gDrawable.alpha = (alpha * 255).toInt()
                    }
                    tv_open_royal.backgroundDrawable = gDrawable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //View宽度
//        val viewWidth = ScreenUtils.getViewRealWidth(enter_text_view).toFloat()
        val animatorEnter = ObjectAnimator.ofFloat(open_royal_layout, "translationX", screenWidth, 0f)
        animatorEnter.duration = 750
        animatorEnter.interpolator = LinearInterpolator()
        animatorEnter.start()
        open_royal_layout.alpha = 1f
        luxury_enter.alpha = 1f
        luxury_enter.translationX = 0f
        luxury_enter?.show()
        open_royal_layout.show()

        luxury_enter.setURI(StringHelper.getOssImgUrl(webpUrl), 1)
        isRoyalAni = true
        val animatorExit1 = ObjectAnimator.ofFloat(open_royal_layout, View.ALPHA, 1f, 0f)
        val animatorExit2 = ObjectAnimator.ofFloat(luxury_enter, View.ALPHA, 1f, 0f)
        mRoyalHideAni = AnimatorSet().apply {
            this.playTogether(animatorExit1, animatorExit2)
            this.duration = 500
            this.addListener(onStart = {
            }, onEnd = {
                logger.info("mRoyalHideAni 结束动画")
                isPlaying = false
                isRoyalAni = false
                checkAnimationBean()
            })
        }

    }

    fun clearQueue() {
        queueList.clear()
        mAnimatorSet?.cancel()
        isPlaying = false
        isRoyalAni = false
    }

    fun resetView() {
        enter_mini_container.hide()
        queueList.clear()
        mAnimatorSet?.cancel()
        isPlaying = false
        isRoyalAni = false

        luxury_enter?.hide()
        luxuryAnimatorSet?.cancel()
        luxury_enter?.resetView()
    }

    fun cancelLuxuryEnter() {
        luxuryAnimatorSet?.cancel()
    }

    fun setUserEnterMiniLayout(isHorizontal: Boolean) {
        val ueLp = (enter_mini_container?.layoutParams as? FrameLayout.LayoutParams) ?: return
        if (!isHorizontal) {
            ueLp.topMargin = PlayerViewManager.HEADER_HEIGHT + PlayerViewManager.LIVE_HEIGHT + DensityHelper.dp2px(15f)
        } else {
            ueLp.topMargin = DensityHelper.dp2px(150f)
        }

    }
}