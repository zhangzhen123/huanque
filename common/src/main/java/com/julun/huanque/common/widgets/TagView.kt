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
            if (tagStr.startsWith(ProgramTagType.IMG)) {
                //图片标签   直接加载远程图片
                sdv.show()
                ImageUtils.loadImageWithHeight_2(sdv, tagStr.substring(ProgramTagType.IMG.length), sdvHeight)
            } else {
                sdv.hide()
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
            radiusWrap = true
            rightTop = true
            rightBottom = true

            tPaddingLeft = 5
            tPaddingTop = 3
            tPaddingRight = 10
            tPaddingBottom = 3
            ctv.setTextColor(Color.WHITE)
            ctv.textSize = 11f
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

    //设置推荐标签
    fun initReCommTagCTV() {
        rightTop = true
        rightBottom = true
        leftBottom = true
        leftTop = true

        tPaddingLeft = 5
        tPaddingTop = 2
        tPaddingRight = 5
        tPaddingBottom = 2
        ctv.textSize = 12f
        ctv.setTextColor(Color.WHITE)

        ctv.radius = dip(2).toFloat()
        ctv.leftTop = leftTop
        ctv.rightTop = rightTop
        ctv.leftBottom = leftBottom
        ctv.rightBottom = rightBottom
        ctv.startColor = startColor
        ctv.endColor = endColor

        ctv.setPadding(dip(tPaddingLeft), dip(tPaddingTop), dip(tPaddingRight), dip(tPaddingBottom))
//        invalidate()
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
            sdvHeight = dip(17)
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
            var endColorStr = ""
            if (strList.size >= 2) {
                ctv.text = strList[0]
                startColorStr = strList[1]
                endColorStr = strList[1]
            }
            if (strList.size >= 3) {
                endColorStr = strList[2]
            }

            try {
                val startColor = Color.parseColor("#$startColorStr")
                val endColor = Color.parseColor("#$endColorStr")
                ctv.setTransitionalColor(startColor, endColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

}