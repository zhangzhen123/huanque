package com.julun.huanque.core.widgets.live

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.julun.huanque.common.suger.hide

import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.widgets.svgaView.SVGAPlayerView
import com.julun.huanque.common.bean.beans.AnimEventBean
import com.julun.huanque.common.bean.beans.AnimModel
import com.julun.huanque.common.bean.events.AnimatorEvent
import com.julun.huanque.common.constant.AnimEventItemTypes
import com.julun.huanque.common.constant.AnimationTypes
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.core.widgets.live.message.HighlyWebpView
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.dip

/**
 * Created by dong on 2018/5/18.
 * 整个动画播放View(只播放svga、webp、gif)
 */
class SuperAnimationView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    // 显示动画时长1秒
    private val SHOW_DURATION = 1000L

    // 隐藏动画时长0.6秒
    private val HIDE_DURATION = 600L

    //    // 屏幕宽度
    private val SCREEN_WIDTH: Int by lazy { ScreenUtils.getScreenWidth() }
    var logger = ULog.getLogger("SuperAnimationView")

    private var highlyWebpView = HighlyWebpView(context)

    private var svgaPlayerView = SVGAPlayerView(context)

    init {
        addView(highlyWebpView)
        addView(svgaPlayerView)
        val hParams = highlyWebpView.layoutParams
        hParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        hParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        highlyWebpView.requestLayout()


        val sParams = svgaPlayerView.layoutParams
        sParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        sParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        svgaPlayerView.requestLayout()
        svgaPlayerView.scaleType = ImageView.ScaleType.FIT_END

        svgaPlayerView.callback = object : SVGACallback {
            override fun onFinished() {
                EventBus.getDefault().post(AnimatorEvent())
            }

            override fun onPause() {

            }

            override fun onRepeat() {

            }

            override fun onStep(frame: Int, percentage: Double) {

            }

        }

    }

    /**
     * 清空webp资源
     */
    fun clearWebpResource() {
        highlyWebpView.controller?.animatable?.stop()
        highlyWebpView.setImageURI("")
    }


    //播放gif动画
    fun showGIFAnim(lastAnim: AnimModel) {
        // 暂时只处理GIF动画(忽略height属性，高度自适应，只处理width)
        if (lastAnim == null) {   //如果 lastAnim 为空,直接返回
            reportCrash("播放动画的时候,动画对象无法获取 lastAnim ==null ", NullPointerException(" lastAnim == null "))
            return
        }
//        val anim = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
//        anim.duration = SHOW_DURATION
//        anim.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                this@SuperAnimationView.visibility = View.VISIBLE
//            }
//        })
//        anim.start()
        if (lastAnim?.animType == AnimationTypes.GIF) {
            try {
//                viewWithGIF(lastAnim!!)
                playAnimation(animModel = lastAnim)
            } catch (e: Exception) {
                e.printStackTrace()
                //抓住异常
                reportCrash("播放动画的时候出错,这个错误虽然不会崩溃,但是需要重视!!!!!!!需要重视!!!!!!!重视!!!!!!!", e)
//                throw e
            }

        } else {
            logger.info("未能解析的动画类型")
            reportCrash("未能解析的动画类型", Exception("接收到的动画类型是 <${lastAnim?.animType}> ,未能识别"))
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //预览编辑模式跳过
//        if (!isInEditMode) {
//            val lp = this.layoutParams as ViewGroup.LayoutParams
////            val frameTop = HEADER_HEIGHT + SCREEN_WIDTH * 3 / 4 / 2
////            lp.height = ScreenUtils.getScreenHeight() - frameTop - ScreenUtils.statusHeight
//            //现在把高级礼物的显示高度最大设置为屏宽的3/2
//            lp.height = SCREEN_WIDTH * 3 / 2
//        }
    }

    /**
     *  新的动画消息格式
     */
    private fun playAnimation(animModel: AnimModel) {
        val animEventBean = animModel.extraObject["AnimEventBean"] as AnimEventBean

        val gifBean = parseParams(animEventBean.gifUrl, AnimEventItemTypes.GIF)

        val webpBean = parseParams(animEventBean.webUrl, AnimEventItemTypes.WEBP)
        val svgaBean = parseParams(animEventBean.svgaUrl, AnimEventItemTypes.SVGA)

        //                var duration = others.map["playSeconds"]?.toInt()
        //优先播放webp
        when {
            webpBean.filed1.isNotBlank() -> {
                var count = webpBean.map["playCount"]?.toIntOrNull()
                highlyWebpView.show()
                svgaPlayerView.hide()
                logger.info("webp动图${webpBean.filed1}")
                count = count ?: 1
                highlyWebpView.setURISpecial(StringHelper.getOssImgUrl(webpBean.filed1), count)
            }
            gifBean.filed1.isNotBlank() -> {
                logger.info("gif动图")
                highlyWebpView.show()
                svgaPlayerView.hide()
                var count = gifBean.map["playCount"]?.toIntOrNull()
                count = count ?: 1
                highlyWebpView.setURISpecial(StringHelper.getOssImgUrl(gifBean.filed1), count)
            }
            svgaBean.filed1.isNotBlank() -> {
                logger.info("svga动图")
                highlyWebpView.hide()
                svgaPlayerView.show()
                var count = svgaBean.map["playCount"]?.toIntOrNull()
                count = count ?: 1
                svgaPlayerView.loops = count
                startPlay(StringHelper.getOssImgUrl(svgaBean.filed1))
            }
        }

    }

    private fun parseParams(paramString: String, type: String): OthersParam {
        // 注意：按后台传过来的是dp处理

//        val str = "animation/959133086d8b4265be3181e835c6439a.svga?playCount=1&width=600&height=432"

        val split = paramString.split("?")
        val result = OthersParam(split[0])
        if (split.size > 1) {
            val pairList: List<Pair<String, String>> = split[1].split('&')//此时应该是 key->value 对
                .filter { StringHelper.isNotEmpty(it) }    //过滤掉空字符串
                .map { it.split("=") }.filter { it.size == 2 }//直接拆分成数组，并且过滤掉有空值的属性
                .map { it[0] to it[1] }
            result.map.putAll(pairList)
        }
        result.type = type
        return result
    }

    /**
     * 根据others提供的额外属性 优先使用webp的地址
     *
     * 如果没有使用others提供的gif 如果没有再使用老接口的GIF属性
     */
    private fun viewWithGIF(animModel: AnimModel) {

        val gifUrl = StringHelper.getOssImgUrl(animModel.extraObject["GIF"].toString())
        val width = if (animModel.extraObject["width"] != null)
            context.dip(animModel.extraObject["width"].toString().toInt()) else 0
        val lp = this.layoutParams as ViewGroup.LayoutParams
        // 超过屏幕就整个宽度了
        if (width > 0 && width < ScreenUtils.getScreenWidth()) {
            lp.width = width
        } else {
            lp.width = FrameLayout.LayoutParams.MATCH_PARENT
        }
        this.layoutParams = lp

        val other = animModel.extraObject["OTHERS"].toString()
        val others = getOthersParams(other)

        if (others != null && "svga" == others.type) {
            highlyWebpView.hide()
            svgaPlayerView.show()
            logger.info("svga动图")
            var count = others.map["playCount"]?.toInt()
            count = if (count == null) 1 else count
            svgaPlayerView.loops = count
            startPlay(StringHelper.getOssImgUrl(others.filed1))
        } else {
            highlyWebpView.show()
            svgaPlayerView.hide()
            if (others != null && "webp" == others.type) {
                logger.info("webp动图")
                var count = others.map["playCount"]?.toInt()
                count = count ?: 1
                highlyWebpView.setURISpecial(StringHelper.getOssImgUrl(others.filed1), count)
            } else if (others != null && "gif" == others!!.type) {
                logger.info("gif动图")
//                var duration = others.map["playSeconds"]?.toInt()
                var count = others.map["playCount"]?.toInt()
                count = count ?: 1
                highlyWebpView.setURISpecial(StringHelper.getOssImgUrl(others.filed1), count)
            } else {
                logger.info("老接口gif动图")
//                val dur = animModel.extraObject["playSeconds"].toString().toInt()
                highlyWebpView.setURISpecial(gifUrl, 0)
            }
        }
    }

    private fun startPlay(url: String?) {
        if (url == null) {
            return
        }
//        val parser = SVGAParser(context)
//        Thread({
        val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
//                svga?.handler?.post {
                svgaPlayerView?.setVideoItem(videoItem)
//                svgaViewModel.animationFinish.value = null
                svgaPlayerView?.startAnimation()
//                }
            }

            override fun onError() {
                EventBus.getDefault().post(AnimatorEvent())
            }
        }
        SVGAHelper.startParse(url, callback)
//        }).start()
    }

    /**
     * 获取额外的属性 动图的url以及属性++
     */
    private fun getOthersParams(other: String): OthersParam? {
        // 注意：按后台传过来的是dp处理

//        val str = "animation/hongzaji.gif?way=gif&playSeconds=10\"animation/hongzaji.webp?way=webp&playCount=2";
        val resultList: List<OthersParam> = other.split("\"").map {
            val split = it.split("?")
            val result = OthersParam(split[0])
            if (split.size > 1) {
                val pairList: List<Pair<String, String>> = split[1].split('&')//此时应该是 key->value 对
                    .filter { StringHelper.isNotEmpty(it) }    //过滤掉空字符串
                    .map { it.split("=") }.filter { it.size == 2 }//直接拆分成数组，并且过滤掉有空值的属性
                    .map { it[0] to it[1] }
                result.map.putAll(pairList)
            }
            result
        }
        // 暂时关闭svga动画的处理，测试过后再上线
//        val filterSvga = resultList.filter { it.map["way"].equals("svga") }
//        if (!filterSvga.isEmpty()) {
//            filterSvga[0].type = "svga"
//            return filterSvga[0]
//        }

        val filter = resultList.filter { it.map["way"].equals("webp") }
        if (!filter.isEmpty()) {
            filter[0].type = "webp"
            return filter[0]
        }
        val filter2 = resultList.filter { it.map["way"].equals("gif") }
        if (!filter2.isEmpty()) {
            filter2[0].type = "gif"
            return filter2[0]
        }
        return null
    }

    class OthersParam(val filed1: String) {
        //        var pairs:MutableList<Pair<String,String>> = arrayListOf()
        var map: HashMap<String, String> = hashMapOf()
        var type: String = ""
    }

}