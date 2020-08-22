package com.julun.huanque.common.widgets.draweetext

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
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
class BeautyBubbleTextView : RelativeLayout {

    private val logger = ULog.getLogger("BubbleTextView")

    var svgaPlayerViewRt: SVGAPlayerView? = null
    var webpGifViewRt: SimpleDraweeView? = null
    var svgaPlayerViewRb: SVGAPlayerView? = null
    var webpGifViewRb: SimpleDraweeView? = null
    var svgaPlayerViewLt: SVGAPlayerView? = null
    var webpGifViewLt: SimpleDraweeView? = null
    var svgaPlayerViewLb: SVGAPlayerView? = null
    var webpGifViewLb: SimpleDraweeView? = null

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

    /**
     * 有些地方需要自定义文本边距的 这里调节处理
     */
    fun setTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        draweeSpanTv.setPadding(left, top, right, bottom)
    }

    fun render(tplBean: TplBean) {
        logger.info("chatBubble=${tplBean.userInfo?.chatBubble}")
        //渲染文字
        draweeSpanTv.render(tplBean)

        var chatBubble = tplBean.userInfo?.chatBubble
        //处理背景颜色
        try {
//            chatBubble = chatBubble ?: ChatBubble()
//            if (chatBubble.radius == -1) {
//                chatBubble.radius = MessageUtil.MESSAGE_BG_RADIUS
//            }
            val dlp = draweeSpanTv.layoutParams as LayoutParams
            if (chatBubble == null || chatBubble.bgc.isEmpty()) {
                logger.info("没有气泡配置")
                //默认填充背景色
//                chatBubble.bgc = "${MessageUtil.MESSAGE_BG}-${MessageUtil.MESSAGE_BG}"
                val gDrawable = GradientDrawable()
                gDrawable.cornerRadius = dp2pxf(MessageUtil.MESSAGE_BG_RADIUS)
                gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                val colorInt: Int = Color.parseColor(MessageUtil.MESSAGE_BG)
                gDrawable.setColor(colorInt)
                draweeSpanTv.backgroundDrawable = gDrawable
                dlp.topMargin = dp2px(3)
                dlp.bottomMargin = dp2px(3)
            } else {
                logger.info("有气泡配置=${chatBubble.bgc}")
                val colors = chatBubble.bgc.split("-") as AbstractList
                if (colors.isNotEmpty()) {

                    val colorInts = arrayListOf<Int>()
                    colors.forEach {
                        val color = GlobalUtils.formatColor(it.replace("#", "#66"), R.color.colorPrimary_lib)//默认加上40%透明度
                        colorInts.add(color)
                    }
                    logger.info("colors=$colorInts")
                    if (colorInts.size > 1) {
                        val gDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorInts.toIntArray())
                        val radius = if (chatBubble.radius == -1) MessageUtil.MESSAGE_BG_RADIUS else chatBubble.radius
                        gDrawable.cornerRadius = dp2pxf(radius)
                        //默认透明度
//                        gDrawable.alpha = (0.4 * 255).toInt()
                        if (chatBubble.bdc.isNotEmpty()) {
                            val colorBds = chatBubble.bdc.split("-") as AbstractList
                            val colorBdInts = arrayListOf<Int>()
                            colorBds.forEach {
                                val color = GlobalUtils.formatColor(it, R.color.colorPrimary_lib)
                                colorBdInts.add(color)
                            }
                            if (colorBdInts.isNotEmpty()) {
                                gDrawable.setStroke(dp2px(0.7f), colorBdInts[0])
                            }

                            gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                        }
                        draweeSpanTv.backgroundDrawable = gDrawable
                    }
                }
                if (chatBubble.lt.isNotEmpty() || chatBubble.rt.isNotEmpty()) {
                    dlp.topMargin = dp2px(7)
                } else {
                    dlp.topMargin = dp2px(3)
                }
                if (chatBubble.lb.isNotEmpty() || chatBubble.rb.isNotEmpty()) {
                    dlp.bottomMargin = dp2px(7)
                } else {
                    dlp.bottomMargin = dp2px(3)
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (chatBubble == null) {
            svgaPlayerViewRt?.hide()
            webpGifViewRt?.hide()
            svgaPlayerViewRb?.hide()
            webpGifViewRb?.hide()
            svgaPlayerViewLt?.hide()
            webpGifViewLt?.hide()
            svgaPlayerViewLb?.hide()
            webpGifViewLb?.hide()

            return
        }

        //处理图片
        //右上
        val rt = chatBubble.rt
        if (rt.isNotEmpty()) {
            val map = parseParams(rt)
            if (StringHelper.isSvgaUrl(rt)) {
//                var svgaPlayerView = findViewById<SVGAPlayerView>(R.id.bb_svga_rt_id)
                if (svgaPlayerViewRt == null) {
                    svgaPlayerViewRt = SVGAPlayerView(context)
                    val sParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))

                    sParams.alignEnd(R.id.draweeSpanTv)
                    sParams.sameTop(R.id.draweeSpanTv)
                    sParams.topMargin = -(dp2px(7))
                    sParams.rightMargin = -(dp2px(5))

                    svgaPlayerViewRt!!.scaleType = ImageView.ScaleType.FIT_CENTER
//                    svgaPlayerView.id = R.id.bb_svga_rt_id
                    addView(svgaPlayerViewRt, sParams)
                } else {
                    val sParams = svgaPlayerViewRt!!.layoutParams as LayoutParams
                    sParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    sParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    svgaPlayerViewRt!!.show()
                    svgaPlayerViewRt!!.requestLayout()
                }
                startPlaySvga(rt, svgaPlayerViewRt!!)
            } else {
//                var webpGifView = findViewById<SimpleDraweeView>(R.id.bb_sdv_rt_id)
                if (webpGifViewRt == null) {
                    webpGifViewRt = SimpleDraweeView(context)
                    val wParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))
                    wParams.alignEnd(R.id.draweeSpanTv)
                    wParams.sameTop(R.id.draweeSpanTv)
                    wParams.topMargin = -(dp2px(7))
                    wParams.rightMargin = -(dp2px(5))
//                    webpGifView.id = R.id.bb_sdv_rt_id
                    addView(webpGifViewRt, wParams)
                    webpGifViewRt!!.loadImageInPx(rt, wParams.width, wParams.height)
                } else {
                    val wParams = webpGifViewRt!!.layoutParams as LayoutParams
                    wParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    wParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    webpGifViewRt!!.show()
                    webpGifViewRt!!.requestLayout()
                    webpGifViewRt!!.loadImageInPx(rt, wParams.width, wParams.height)
                }

            }
        }
        //右下
        val rb = chatBubble.rb
        if (rb.isNotEmpty()) {
            val map = parseParams(rb)
            if (StringHelper.isSvgaUrl(rb)) {
//                var svgaPlayerView = findViewById<SVGAPlayerView>(R.id.bb_svga_rb_id)
                if (svgaPlayerViewRb == null) {
                    svgaPlayerViewRb = SVGAPlayerView(context)
                    val sParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))

                    sParams.alignEnd(R.id.draweeSpanTv)
                    sParams.sameBottom(R.id.draweeSpanTv)
                    sParams.bottomMargin = -(dp2px(7))
                    sParams.rightMargin = -(dp2px(5))
                    svgaPlayerViewRb!!.requestLayout()
                    svgaPlayerViewRb!!.scaleType = ImageView.ScaleType.FIT_CENTER
//                    svgaPlayerView.id = R.id.bb_svga_rb_id
                    addView(svgaPlayerViewRb, sParams)
                } else {
                    val sParams = svgaPlayerViewRb!!.layoutParams as LayoutParams
                    sParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    sParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    svgaPlayerViewRb!!.show()
                    svgaPlayerViewRb!!.requestLayout()
                }

                startPlaySvga(rb, svgaPlayerViewRb!!)
            } else {
//                var webpGifView = findViewById<SimpleDraweeView>(R.id.bb_sdv_rb_id)
                if (webpGifViewRb == null) {
                    webpGifViewRb = SimpleDraweeView(context)
                    val wParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))
                    wParams.alignEnd(R.id.draweeSpanTv)
                    wParams.sameBottom(R.id.draweeSpanTv)
                    wParams.bottomMargin = -(dp2px(7))
                    wParams.rightMargin = -(dp2px(5))
//                    webpGifView.id = R.id.bb_sdv_rb_id
                    addView(webpGifViewRb, wParams)
                    webpGifViewRb!!.loadImageInPx(rb, wParams.width, wParams.height)
                } else {
                    val wParams = webpGifViewRb!!.layoutParams as LayoutParams
                    wParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    wParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    webpGifViewRb!!.show()
                    webpGifViewRb!!.requestLayout()
                    webpGifViewRb!!.loadImageInPx(rt, wParams.width, wParams.height)
                }

            }
        }
        //左上
        val lt = chatBubble.lt
        if (lt.isNotEmpty()) {
            val map = parseParams(lt)
            if (StringHelper.isSvgaUrl(lt)) {
//                var svgaPlayerView = findViewById<SVGAPlayerView>(R.id.bb_svga_lt_id)
                if (svgaPlayerViewLt == null) {
                    svgaPlayerViewLt = SVGAPlayerView(context)
                    val sParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))
                    sParams.alignStart(R.id.draweeSpanTv)
                    sParams.sameTop(R.id.draweeSpanTv)
                    sParams.topMargin = -(dp2px(7))
                    sParams.leftMargin = -(dp2px(5))
                    svgaPlayerViewLt!!.requestLayout()
                    svgaPlayerViewLt!!.scaleType = ImageView.ScaleType.FIT_CENTER
//                    svgaPlayerViewLt.id = R.id.bb_svga_lt_id
                    addView(svgaPlayerViewLt, sParams)
                } else {
                    val sParams = svgaPlayerViewLt!!.layoutParams as LayoutParams
                    sParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    sParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    svgaPlayerViewLt?.show()
                    svgaPlayerViewLt?.requestLayout()
                }

                startPlaySvga(lt, svgaPlayerViewLt!!)
            } else {
//                var webpGifView = findViewById<SimpleDraweeView>(R.id.bb_sdv_lt_id)
                if (webpGifViewLt == null) {
                    webpGifViewLt = SimpleDraweeView(context)
                    val wParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))
                    wParams.alignStart(R.id.draweeSpanTv)
                    wParams.sameTop(R.id.draweeSpanTv)
                    wParams.topMargin = -(dp2px(7))
                    wParams.leftMargin = -(dp2px(5))
//                    webpGifViewLt.id = R.id.bb_sdv_lt_id
                    addView(webpGifViewLt, wParams)
                    webpGifViewLt!!.loadImageInPx(lt, wParams.width, wParams.height)
                } else {
                    val wParams = webpGifViewLt!!.layoutParams as LayoutParams
                    wParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    wParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    webpGifViewLt!!.show()
                    webpGifViewLt!!.requestLayout()
                    webpGifViewLt!!.loadImageInPx(rt, wParams.width, wParams.height)
                }

            }
        }
        //左下
        val lb = chatBubble.lb
        if (lb.isNotEmpty()) {
            val map = parseParams(lb)
            if (StringHelper.isSvgaUrl(lb)) {
//                var svgaPlayerView = findViewById<SVGAPlayerView>(R.id.bb_svga_lb_id)
                if (svgaPlayerViewLb == null) {
                    svgaPlayerViewLb = SVGAPlayerView(context)
                    val sParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))

                    sParams.alignStart(R.id.draweeSpanTv)
                    sParams.sameBottom(R.id.draweeSpanTv)
                    sParams.bottomMargin = -(dp2px(7))
                    sParams.leftMargin = -(dp2px(5))
                    svgaPlayerViewLb!!.requestLayout()
                    svgaPlayerViewLb!!.scaleType = ImageView.ScaleType.FIT_CENTER
//                    svgaPlayerViewLb.id = R.id.bb_svga_lb_id
                    addView(svgaPlayerViewLb, sParams)
                } else {
                    val sParams = svgaPlayerViewLb!!.layoutParams as LayoutParams
                    sParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    sParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    svgaPlayerViewLb!!.show()
                    svgaPlayerViewLb!!.requestLayout()
                }

                startPlaySvga(lb, svgaPlayerViewLb!!)
            } else {
//                var webpGifView = findViewById<SimpleDraweeView>(R.id.bb_sdv_lb_id)
                if (webpGifViewLb == null) {
                    webpGifViewLb = SimpleDraweeView(context)
                    val wParams = LayoutParams(dp2px(map["w"]?.toIntOrNull() ?: 12), dp2px(map["h"]?.toIntOrNull() ?: 12))
                    wParams.alignStart(R.id.draweeSpanTv)
                    wParams.sameBottom(R.id.draweeSpanTv)
                    wParams.bottomMargin = -(dp2px(7))
                    wParams.leftMargin = -(dp2px(5))
//                    webpGifView.id = R.id.bb_sdv_lb_id
                    addView(webpGifViewLb, wParams)
                    webpGifViewLb!!.loadImageInPx(lb, wParams.width, wParams.height)
                } else {
                    val wParams = webpGifViewLb!!.layoutParams as LayoutParams
                    wParams.width = dp2px(map["w"]?.toIntOrNull() ?: 12)
                    wParams.height = dp2px(map["h"]?.toIntOrNull() ?: 12)
                    webpGifViewLb!!.show()
                    webpGifViewLb!!.requestLayout()
                    webpGifViewLb!!.loadImageInPx(rt, wParams.width, wParams.height)
                }

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
        SVGAHelper.startParse(StringHelper.getOssImgUrl(url), callback)
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
