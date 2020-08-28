package com.julun.huanque.core.widgets.live.message

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.bean.beans.TplTypeBean
import com.julun.huanque.common.constant.EnterType
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.viewmodel.EntranceViewModel
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
class UserEnterAnimatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    val logger = ULog.getLogger("UserEnterAnimatorView")
    private var viewModel: EntranceViewModel? = null
    val queueList: MutableList<TplTypeBean> = Collections.synchronizedList(mutableListOf())
    //屏幕宽度
    val screenWidth = ScreenUtils.screenWidthFloat

    init {
        LayoutInflater.from(context).inflate(R.layout.view_room_user_enter, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val activity = context as? PlayerActivity
        activity?.let {
            viewModel = ViewModelProvider(it).get(EntranceViewModel::class.java)
            viewModel?.animatorState?.observe(it, androidx.lifecycle.Observer {
                logger.info("animatorState动画开始播放$it")
                if (it == true) {
                    mAnimatorSet?.cancel()
                    mAnimatorSet?.start()
                    viewModel?.animatorState?.value = null
                }
            })
        }
    }

    var isPlay2 = false
    private var mAnimatorSet: AnimatorSet? = null
    /**
     * 方法名不变 但作用是执行所有的动画 包括坐骑 贵族 守护
     */
    fun playWelcomeAnimator(bean: TplTypeBean) {
        queueList.add(bean)
        logger.info("开始动画playEnterAnimator")
        if (isPlay2 || queueList.size <= 0) return
        setAnimationBean()
    }

    fun setAnimationBean() {
        isPlay2 = true
        val bean: TplTypeBean = queueList[0]
        if (bean.content == null) return
        if (bean.content!!.context == null) return
        if (queueList.size > 0) {
            queueList.removeAt(0)
        }
        //判断这个属性是不是空 不是空代表时新的入场动画
        val carType = bean.content!!.context!!.type

        val svga = bean.content!!.context!!.svga

        val webp = bean.content!!.context!!.webp

        val theme = bean.content!!.context!!.theme

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
        val thisLp = layoutParams
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
                    viewModel?.messageState?.value = webp
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
        enter_text_view.render(bean.content!!, finalColor = "#ffffff")

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
        val animatorEnter = ObjectAnimator.ofFloat(this, "translationX", screenWidth, 0f)
        animatorEnter.duration = (start * 1000).toLong()
        animatorEnter.interpolator = LinearInterpolator()

        val animatorExit = ObjectAnimator.ofFloat(this, "translationX", 0f, -screenWidth)
        animatorExit.duration = (out * 1000).toLong()
        animatorExit.startDelay = (stop * 1000).toLong()
        animatorExit.interpolator = LinearInterpolator()
        mAnimatorSet?.cancel()
        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.play(animatorExit)?.after(animatorEnter)
        mAnimatorSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                this@UserEnterAnimatorView.show()
                logger.info("startFortyAnimator${queueList.size}")
            }

            override fun onAnimationEnd(animation: Animator) {
                var delay = 0L
                if (isLux) {
                    delay = 100L
                }
                postDelayed({
                    logger.info("endFortyAnimator")
                    isPlay2 = false
                    if (!ViewCompat.isAttachedToWindow(this@UserEnterAnimatorView)) {
                        return@postDelayed
                    }
                    if (queueList.size <= 0) {
                        this@UserEnterAnimatorView?.hide()
                        return@postDelayed
                    }
                    setAnimationBean()
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

    fun clearQueue() {
        queueList.clear()
        mAnimatorSet?.cancel()
        isPlay2 = false
    }

    fun resetView() {
        this.hide()
        queueList.clear()
        mAnimatorSet?.cancel()
        isPlay2 = false
    }
}