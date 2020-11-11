package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ProgramTagType
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import kotlinx.android.synthetic.main.view_tag.view.*
import org.jetbrains.anko.dip
import kotlin.properties.Delegates

/**
 * Created by dong on 2018/4/10.
 */
class TagView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    companion object {
        const val LEFT_TOP = 1
        const val RIGHT_TOP = 2
        const val LEFT_BOTTOM = 3
        const val RIGHT_BOTTOM = 4
    }

    constructor(context: Context) : this(context, null)

    //是否是节目标签
    var isProgramTag: Boolean by Delegates.observable(false, { _, _, _ ->
        initProgramTagCTV()
        initSimpleDraweeView()
    })

    //是否是礼物标签
    var isGiftTag: Boolean by Delegates.observable(false, { _, _, _ ->
        initProgramTagCTV()
        initSimpleDraweeView()
    })

    //CircularCornerTextView相关属性
    private var radiusWrap = false
    private var radius = 0F
    private var leftTop = false
    private var rightTop = false
    private var leftBottom = false
    private var rightBottom = false
    private var startColor = 0
    private var endColor = 0

    //CircularCornerTextView padding 属性(单位dp)
    private var tPaddingLeft = 0
    private var tPaddingTop = 0
    private var tPaddingRight = 0
    private var tPaddingBottom = 0

    private var sdvWidth = 0
    private var sdvHeight = 0

    init {
        context?.let {
            LayoutInflater.from(it).inflate(R.layout.view_tag, this)
        }
    }


    fun setData(tagStr: String?) {
        if (tagStr == null) {
            hide()
            return
        } else {
            show()
        }
        if (tagStr.startsWith(ProgramTagType.TEXT)) {
            //文本标签，需要解析
            ctv.show()
            sdv.hide()
            analysisTag(tagStr, ctv)
        } else {
            ctv.hide()
            when {
                tagStr.startsWith(ProgramTagType.IMG) -> {
                    //图片标签   直接加载远程图片
                    sdv.show()
                    ImageUtils.loadImageWithHeight_2(sdv, tagStr.substring(ProgramTagType.IMG.length), sdvHeight)
                }
                tagStr.isNotEmpty() -> {
                    sdv.show()
                    ImageUtils.loadImageWithHeight_2(sdv, tagStr, sdvHeight)
                }
                else -> {
                    sdv.hide()
                }
            }
        }
    }

    /**
     * 设置右对齐
     */
    fun setAlignRight() {
        (ctv.layoutParams as? LayoutParams)?.gravity = Gravity.RIGHT
        (sdv.layoutParams as? LayoutParams)?.gravity = Gravity.RIGHT
    }


    /**初始化CircularCornerTextView**/
    private fun initProgramTagCTV() {
        if (isProgramTag) {
            rightTop = false
            rightBottom = true
            leftBottom = false
            leftTop = true

            tPaddingLeft = 5
            tPaddingTop = 2
            tPaddingRight = 5
            tPaddingBottom = 2
            ctv.textSize = 12f
            ctv.setTextColor(Color.WHITE)
            radius = 6f
        } else if (isGiftTag) {
            radiusWrap = true
            rightTop = true
            rightBottom = true
            leftTop = true
            leftBottom = false

            tPaddingLeft = 4
//            tPaddingTop = 2
            tPaddingRight = 5
//            tPaddingBottom = 2
            val params = ctv.layoutParams
            params.height = dp2px(12f)

            ctv.setTextColor(Color.WHITE)
            ctv.textSize = 8f
            ctv.gravity = Gravity.CENTER
        }

        ctv.radiusWrap = radiusWrap
        ctv.radius = dip(radius).toFloat()
        ctv.leftTop = leftTop
        ctv.rightTop = rightTop
        ctv.leftBottom = leftBottom
        ctv.rightBottom = rightBottom
        ctv.startColor = startColor
        ctv.endColor = endColor

        ctv.setPadding(dip(tPaddingLeft), dip(tPaddingTop), dip(tPaddingRight), dip(tPaddingBottom))
//        invalidate()
    }

    /**
     * 设置节目单角标[type]代表四个角的位置
     */
    fun initProgramTag(type: Int) {
        isProgramTag = true
        val params = sdv.layoutParams
        when (type) {
            LEFT_TOP -> {
                rightTop = false
                rightBottom = true
                leftBottom = false
                leftTop = true
                sdvHeight = dip(20)
                params.height = sdvHeight
            }
            RIGHT_TOP -> {
                rightTop = true
                rightBottom = false
                leftBottom = true
                leftTop = false
                sdvHeight = dip(16)
            }
            LEFT_BOTTOM -> {
                rightTop = true
                rightBottom = false
                leftBottom = true
                leftTop = false
                sdvHeight = dip(20)
                params.height = sdvHeight
            }
            RIGHT_BOTTOM -> {
                rightTop = false
                rightBottom = true
                leftBottom = false
                leftTop = true
                sdvHeight = dip(20)
                params.height = sdvHeight
            }
        }


        tPaddingLeft = 5
        tPaddingTop = 2
        tPaddingRight = 5
        tPaddingBottom = 2
        ctv.textSize = 12f
        ctv.setTextColor(Color.WHITE)

        ctv.radius = dip(6).toFloat()
        ctv.leftTop = leftTop
        ctv.rightTop = rightTop
        ctv.leftBottom = leftBottom
        ctv.rightBottom = rightBottom
        ctv.startColor = startColor
        ctv.endColor = endColor

        ctv.setPadding(dip(tPaddingLeft), dip(tPaddingTop), dip(tPaddingRight), dip(tPaddingBottom))
//        invalidate()
        sdv.requestLayout()
    }

    //给文字添加固定的左图片
    fun addTextImg(add: Boolean) {
        if (add) {
            val dr = context?.resources?.getDrawable(R.mipmap.recommend_first)
            dr?.setBounds(0, 0, dip(16), dip(16))
            ctv.compoundDrawablePadding = dip(2)
            ctv.setCompoundDrawables(dr, null, null, null)
        } else {
            ctv.setCompoundDrawables(null, null, null, null)
        }
    }

    fun setTextSize(size: Float) {
        ctv.textSize = size
    }

    private fun initSimpleDraweeView() {
        val params = sdv.layoutParams
        if (isProgramTag) {
            //节目标签
//            sdvWidth = dip(50)
            sdvHeight = dip(20)
            params.height = sdvHeight
        } else if (isGiftTag) {
            //礼物标签
            sdvWidth = dip(12)
            sdvHeight = dip(12)
            params.width = sdvWidth
            params.height = sdvHeight
        }

        sdv.requestLayout()
    }

    /**解析文本标签**/
    private fun analysisTag(tagStr: String?, ctv: CircularCornerTextView) {
        if (tagStr != null && tagStr.startsWith(ProgramTagType.TEXT)) {
            val firstStr = tagStr.substring(ProgramTagType.TEXT.length)
            val strList = firstStr.split("-")
            var startColorStr = ""
            var middleColor = ""
            var endColorStr = ""
            if (strList.size >= 2) {
                ctv.text = strList[0]
                startColorStr = strList[1]
                endColorStr = strList[1]
            }
            if (strList.size >= 3) {
                startColorStr = strList[1]
                endColorStr = strList[2]
            }

            if (strList.size >= 4) {
                //3色渐变
                startColorStr = strList[1]
                middleColor = strList[2]
                endColorStr = strList[3]
            }
            try {
                val startColor = Color.parseColor("#$startColorStr")
                val endColor = Color.parseColor("#$endColorStr")
                if (middleColor.isEmpty()) {
                    ctv.setTransitionalColor(startColor, endColor)
                } else {
                    val middleColor = Color.parseColor("#$middleColor")
                    ctv.setTransitionalColor(startColor, middleColor, endColor)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

}