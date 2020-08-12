package com.julun.huanque.common.widgets.draweetext

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.MessageUtil
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.loadImageInPx
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.widgets.svgaView.SVGAPlayerView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.layout_bubble_drawee_span_text.view.*
import org.jetbrains.anko.*
import java.util.*


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/11 16:16
 *
 *@Description: BubbleTextView
 *
 * 漂亮的气泡文字组件 在[DraweeSpanTextView]的基础上进行的扩展
 *
 */
class BubbleTextView : RelativeLayout {

    private val logger = ULog.getLogger("HeaderPageView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_bubble_drawee_span_text, this)
    }

    fun render(tplBean: TplBean) {
        logger.info("tpl=$tplBean")
        //渲染文字
        draweeSpanTv.render(tplBean)

        var chatBubble = tplBean.userInfo?.chatBubble
        //处理背景颜色
        try {
            chatBubble = chatBubble ?: ChatBubble()
            if (chatBubble.bgc.isEmpty()) {
                chatBubble.bgc = MessageUtil.MESSAGE_BG
            }
            if (chatBubble.radius == -1) {
                chatBubble.radius = MessageUtil.MESSAGE_BG_RADIUS
            }
            val colors = chatBubble.bgc.split("-") as AbstractList
            if (colors.isNotEmpty()) {

                val colorInts = arrayListOf<Int>()
                colors.forEach {
                    val color = GlobalUtils.formatColor(it, R.color.colorPrimary_lib)
                    colorInts.add(color)
                }
                if (colorInts.size > 1) {
                    val gDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorInts.toIntArray())
                    gDrawable.cornerRadius = dp2pxf(chatBubble.radius)
                    if (chatBubble.bdc.isNotEmpty()) {
                        gDrawable.setStroke(dp2px(1), GlobalUtils.formatColor(chatBubble.bdc, R.color.colorPrimary_lib))
                        gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                    }
                    this.backgroundDrawable = gDrawable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        chatBubble ?: return
        //处理图片
        //右上
        val rt = chatBubble.rt
        if (rt.isNotEmpty()) {
            val map = parseParams(rt)
            if (StringHelper.isSvgaUrl(rt)) {
                val svgaPlayerView = SVGAPlayerView(context)
                val sParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))

                sParams.alignEnd(R.id.draweeSpanTv)
                sParams.sameTop(R.id.draweeSpanTv)
                svgaPlayerView.requestLayout()
                svgaPlayerView.scaleType = ImageView.ScaleType.FIT_CENTER
                addView(svgaPlayerView, 0, sParams)
                startPlaySvga(rt, svgaPlayerView)
            } else {
                val webpGifView = SimpleDraweeView(context)
                val wParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))
                wParams.alignEnd(R.id.draweeSpanTv)
                wParams.sameTop(R.id.draweeSpanTv)
                addView(webpGifView, 0, wParams)
                webpGifView.loadImageInPx(rt, wParams.width, wParams.height)
            }
        }
        //右下
        val rb = chatBubble.rb
        if (rb.isNotEmpty()) {
            val map = parseParams(rb)
            if (StringHelper.isSvgaUrl(rb)) {
                val svgaPlayerView = SVGAPlayerView(context)
                val sParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))

                sParams.alignEnd(R.id.draweeSpanTv)
                sParams.sameBottom(R.id.draweeSpanTv)
                svgaPlayerView.requestLayout()
                svgaPlayerView.scaleType = ImageView.ScaleType.FIT_CENTER
                addView(svgaPlayerView, 0, sParams)
                startPlaySvga(rb, svgaPlayerView)
            } else {
                val webpGifView = SimpleDraweeView(context)
                val wParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))
                wParams.alignEnd(R.id.draweeSpanTv)
                wParams.sameBottom(R.id.draweeSpanTv)
                addView(webpGifView, 0, wParams)
                webpGifView.loadImageInPx(rb, wParams.width, wParams.height)
            }
        }
        //左上
        val lt = chatBubble.lt
        if (lt.isNotEmpty()) {
            val map = parseParams(lt)
            if (StringHelper.isSvgaUrl(lt)) {
                val svgaPlayerView = SVGAPlayerView(context)
                val sParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))

                sParams.alignStart(R.id.draweeSpanTv)
                sParams.sameTop(R.id.draweeSpanTv)
                svgaPlayerView.requestLayout()
                svgaPlayerView.scaleType = ImageView.ScaleType.FIT_CENTER
                addView(svgaPlayerView, 0, sParams)
                startPlaySvga(lt, svgaPlayerView)
            } else {
                val webpGifView = SimpleDraweeView(context)
                val wParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))
                wParams.alignStart(R.id.draweeSpanTv)
                wParams.sameTop(R.id.draweeSpanTv)
                addView(webpGifView, 0, wParams)
                webpGifView.loadImageInPx(lt, wParams.width, wParams.height)
            }
        }
        //左下
        val lb = chatBubble.lb
        if (lb.isNotEmpty()) {
            val map = parseParams(lb)
            if (StringHelper.isSvgaUrl(lb)) {
                val svgaPlayerView = SVGAPlayerView(context)
                val sParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))

                sParams.alignEnd(R.id.draweeSpanTv)
                sParams.sameBottom(R.id.draweeSpanTv)
                svgaPlayerView.requestLayout()
                svgaPlayerView.scaleType = ImageView.ScaleType.FIT_CENTER
                addView(svgaPlayerView, 0, sParams)
                startPlaySvga(lb, svgaPlayerView)
            } else {
                val webpGifView = SimpleDraweeView(context)
                val wParams = LayoutParams(map["w"]?.toIntOrNull() ?: dp2px(10), map["h"]?.toIntOrNull() ?: dp2px(10))
                wParams.alignEnd(R.id.draweeSpanTv)
                wParams.sameBottom(R.id.draweeSpanTv)
                addView(webpGifView, 0, wParams)
                webpGifView.loadImageInPx(lb, wParams.width, wParams.height)
            }
        }

    }

    private fun startPlaySvga(url: String?, svgaPlayerView: SVGAPlayerView) {
        if (url == null) {
            return
        }

        val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                svgaPlayerView.setVideoItem(videoItem)
//                svgaViewModel.animationFinish.value = null
                svgaPlayerView.startAnimation()
            }

            override fun onError() {
            }
        }
        SVGAHelper.startParse(url, callback)
    }

    private fun parseParams(paramString: String): Map<String, String> {
        // 注意：按后台传过来的是dp处理

//        val str = "animation/959133086d8b4265be3181e835c6439a.svga?playCount=1&width=600&height=432"
        val map = hashMapOf<String, String>()
        val split = paramString.split("?")
        if (split.size > 1) {
            val pairList: List<Pair<String, String>> = split[1].split('&')//此时应该是 key->value 对
                .filter { StringHelper.isNotEmpty(it) }    //过滤掉空字符串
                .map { it.split("=") }.filter { it.size == 2 }//直接拆分成数组，并且过滤掉有空值的属性
                .map { it[0] to it[1] }
            map.putAll(pairList)
        }
        return map

    }

}
