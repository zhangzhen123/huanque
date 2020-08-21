package com.julun.huanque.common.widgets.draweetext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.AttributeSet
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.setImageSpan
import com.julun.huanque.common.bean.beans.BaseTextBean
import com.julun.huanque.common.bean.MessageUtil
import com.julun.huanque.common.bean.StyleParam
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.constant.MessageDisplayType
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.setCircleImageSpan
import com.julun.huanque.common.utils.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.dip

/**
 *
 *@author zhangzhen
 *@data 2017/3/21
 *
 **/
class DraweeSpanTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SimpleDraweeSpanTextView(context, attrs, defStyleAttr) {
    val logger = ULog.getLogger("DraweeSpanTextView")

    private var data: TplBean? = null
    private var hasRainbow:Boolean =false
    companion object {
        private const val DEFAULT_TEXT_BG_COLOR = "#FFFFFF"
        const val BOLD = "bold" //加粗标识
        const val NORMAL = "normal"
    }

    init {
        includeFontPadding = false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        logger.info("onDraw")
        if(hasRainbow){
            //有彩虹屁就不停的更新
            postInvalidateDelayed(16)
        }
    }

    override fun onDetachedFromWindow() {
        invalidate()
        super.onDetachedFromWindow()
    }

    /**
     * 是否需要彩虹屁
     */
    fun setNeedRainbow(need:Boolean){
        hasRainbow=need
    }
    fun render(item: TplBean, specifiedColor: String = DEFAULT_TEXT_BG_COLOR, finalColor: String? = null) {//原来的颜色   21ad79
        data = item
        //添加聊天模式标识
        //聊条模式标记位添加的标识 (消息由标识位，并且是主播身份)
        val chatModeFlag =false
//            item.userInfo?.displayType?.contains(MessageDisplayType.CHATMODE) == true/* && SessionUtils.getUserType() == BusiConstant.UserType.Anchor*/

        val realText = if (chatModeFlag) {
            "${item.realTxt}  "
        } else {
            item.realTxt
        }

        val builder = DraweeSpanStringBuilder(realText)

        setTextColor(Color.parseColor(specifiedColor))
        //不再需要字体阴影
//        this.setShadowLayer(shadow_layer_size, shadow_layer_size, shadow_layer_size, Color.parseColor("#cc000000"))
        item.styleParamMap.forEach {

            val paramKey: String = it.key
            val styleParam: StyleParam = it.value

            //先处理全局的
            if (paramKey == MessageUtil.KEY_ALL) {
                if (MessageUtil.KEY_BASIC == styleParam.styleType) {
                    if (StringHelper.isNotEmpty(styleParam.color)) {
                        setTextColor(Color.parseColor(styleParam.color))
                    }

                    if (BOLD == styleParam.fontWeight) {
                        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    } else if (NORMAL == styleParam.fontWeight) {
                        typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    }

                    if (StringHelper.isNotEmpty(styleParam.underLineColor)) {
                        //全局显示下划线
                        //下划线
                        paint.flags = Paint.UNDERLINE_TEXT_FLAG
                        //抗锯齿
                        paint.isAntiAlias = true
                    }
                    //useBg代表需要设置bg  //notShowBackColor如果特殊地方不需要背景直接不处理
//                    if (item.useBg && !item.notShowBackColor) {
//
//                        //不是任何炫彩发言
//                        //处理背景颜色圆角 如果没有该参数 本地设置默认颜色 圆角
//                        if (styleParam.bgColor.isBlank()) {
//                            styleParam.bgColor = MessageUtil.MESSAGE_BG
//                        }
//                        if (styleParam.radius == -1) {
//                            styleParam.radius = MessageUtil.MESSAGE_BG_RADIUS
//                        }
//                        val gDrawable = GradientDrawable()
//                        gDrawable.cornerRadius = dp2pxf(styleParam.radius)
//                        gDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
//                        val colorInt: Int = Color.parseColor(styleParam.bgColor)
//                        gDrawable.setColor(colorInt)
//                        backgroundDrawable = gDrawable
//                    }
                }
                return@forEach
            }

            //处理图片
            val indexes: MutableList<Int>? = item.paramIndexInRealText[paramKey]
            val paramValue = item.textParams[paramKey]
            when (styleParam.styleType) {
                MessageUtil.KEY_IMG -> {
                    indexes?.forEach { index ->
                        run {

                            //获取Drawable资源
//                            val imageMargin = sp(5f)
                            // 这里有一个特例 ,原定计划是所有的图片(其实是指各个等级的图标,不能为0,但是emoji是可以有 0 开始的...所以需要特殊处理)
                            if ((MessageUtil.KEY_LOCAL == styleParam.source && StringHelper.isNotEmpty(paramValue) && paramValue!!.toInt() >= 0)
                                || styleParam.preffix == MessageUtil.PREFIX_EMOJI
                            ) {
                                val resId: Int = ImageHelper.getLocalImageResId(paramValue, styleParam)
                                //改用固定高度
                                var specifiedWidth = dip(16)
                                var specifiedHeight = specifiedWidth

                                if (styleParam.preffix == MessageUtil.PREFIX_USER_LEVEL) {
                                    specifiedHeight = dip(16)
                                    specifiedWidth = dip(29)
                                }

                                builder.setImageSpan(context, resId, index, index, specifiedWidth, specifiedHeight)

                            } else if (MessageUtil.KEY_REMOTE == styleParam.source) {
                                val specifiedHeight = dip(16)
                                builder.setImageSpan(context, StringHelper.getOssImgUrl(paramValue!!), index, specifiedHeight, specifiedHeight)
                            }

                        }
                    }
                }
                MessageUtil.KEY_BASIC -> {
                    indexes?.forEach { index -> forEachBasicSpan(index, styleParam, builder) }
                }
                else -> {
                    indexes?.forEach { index -> forEachBasicSpan(index, styleParam, builder) }
                }
            }

        }
//        if (chatModeFlag) {
//            builder.setImageSpan(context, "res://${context?.packageName}/${R.mipmap.icon_chat_flag}", realText.length - 1, 48, 48)
//        }
        if (finalColor != null)
            setTextColor(Color.parseColor(finalColor))


        builder.setDraweeSpanChangedListener { bd -> this.setDraweeSpanStringBuilder(bd) }
        this.setDraweeSpanStringBuilder(builder)

//        this.movementMethod = LinkMovementMethod.getInstance() //开始响应点击事件

    }

    /**
     * 渲染每一块 文本区域
     */
    private fun forEachBasicSpan(index: Int, styleParam: StyleParam, source: SpannableStringBuilder) {
        val text=data?.textParams?.get(styleParam.el)
        val subTexLength: Int = text?.length ?: return
        val allColor: String? = data?.styleParamMap?.get(MessageUtil.KEY_ALL)?.color
        //                        source.setSpan(UnderlineSpan(), index, (index + subTexLength), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (StringHelper.isNotEmpty(styleParam.underLineColor)) {
            //下划线
            source.setSpan(
                UnderlineSpan(), index, (index + subTexLength),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (styleParam.el.indexOf("\${extra.user.nickName}") > -1) {
            logger.info("nickName render")
            val color = if (StringHelper.isEmpty(styleParam.color)) {
                ContextCompat.getColor(context, R.color.app_main)
            } else {
                Color.parseColor(styleParam.color)
            }
            source.setSpan(
                ForegroundColorSpan(color), index, (index + subTexLength),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            ) //setSpan时需要指定的 flag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括).
            if(styleParam.lightColor.isNotEmpty()){
                val lightColor = if (StringHelper.isEmpty(styleParam.lightColor)) {
                    ContextCompat.getColor(context, R.color.app_main)
                } else {
                    Color.parseColor(styleParam.lightColor)
                }
                source.setSpan(
                    AnimatedRainbowSpan(intArrayOf(color,lightColor,color)), index, (index + subTexLength),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE  )
                setNeedRainbow(true)
                postInvalidateDelayed(1)
            }else{
                setNeedRainbow(false)
            }
            //删除线
//            source.setSpan(StrikethroughSpan(), index, (index + subTexLength),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) //setSpan时需要指定的 flag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括).
            //下划线
//            source.setSpan(UnderlineSpan(), index, (index + subTexLength),  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) //setSpan时需要指定的 flag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括).
            //加点击

            /*source.setSpan(object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.color = Color.RED
                    ds.isUnderlineText = true
                }

                override fun onClick(widget: View?) {
                    println("点击了: widget $widget ")
//                    huanqueApp.getApp().longToast("被 点击了 呵呵 ")
                }
            }, index, (index + subTexLength), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)*/

        } else {

            //前景色
            when {
                StringHelper.isNotEmpty(styleParam.color) -> source.setSpan(
                    ForegroundColorSpan(Color.parseColor(styleParam.color)), index, (index + subTexLength),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                ) //setSpan时需要指定的 flag,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括).
                //如果有全局颜色使用全局颜色
                StringHelper.isNotEmpty(allColor) -> source.setSpan(
                    ForegroundColorSpan(Color.parseColor(allColor)), index, (index + subTexLength),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                else -> source.setSpan(
                    ForegroundColorSpan(Color.WHITE), index, (index + subTexLength),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            //背景色
            if (StringHelper.isNotEmpty(styleParam.bgColor)) {
                source.setSpan(
                    BackgroundColorSpan(Color.parseColor(styleParam.bgColor)), index, (index + subTexLength),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        if (BOLD == styleParam.fontWeight) {
            source.setSpan(
                StyleSpan(Typeface.BOLD), index, (index + subTexLength),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }


    /**
     * 手动组装的富文本
     */
    fun renderBaseText(textBean: BaseTextBean) {
        val builder = DraweeSpanStringBuilder(textBean.realText)
        textBean.imgParams.forEach {
            if (it.imgRes == 0) {
                if (it.isCircle) {
                    builder.setCircleImageSpan(context, it.url, it.borderRedId, it.borderWidth, it.index, it.width, it.height)
                } else {
                    builder.setImageSpan(context, it.url, it.index, it.width, it.height)
                }
            } else {
                builder.setImageSpan(context, it.imgRes, it.index, it.index, it.width, it.height)
            }
        }
        textBean.textParams.forEach {
            val indexEnd = it.indexStart + it.text.length

            //设置颜色 优先使用[TextParam.textColorInt]
            if (it.textColorInt != 0) {
                builder.setSpan(
                    ForegroundColorSpan(it.textColorInt), it.indexStart, indexEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else if (!StringHelper.isEmpty(it.textColor)) {
                val color = Color.parseColor(it.textColor)
                builder.setSpan(
                    ForegroundColorSpan(color), it.indexStart, indexEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            //设置字体大小
            if (it.textSize != 0) {
                val span_2 = AbsoluteSizeSpan(it.textSize)//字体大小
                builder.setSpan(span_2, it.indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            //设置文字风格
            val span_3 = StyleSpan(it.styleSpan)
            builder.setSpan(span_3, it.indexStart, indexEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        builder.setDraweeSpanChangedListener { build -> this.setDraweeSpanStringBuilder(build) }
        this.setDraweeSpanStringBuilder(builder)
    }
}