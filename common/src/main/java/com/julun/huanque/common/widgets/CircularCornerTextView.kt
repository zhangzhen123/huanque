package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import com.julun.huanque.common.R
import org.jetbrains.anko.backgroundDrawable

/**
 * Created by dong on 2018/4/2.
 */
class CircularCornerTextView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    var radiusWrap = false
    var radius = 0F
    var leftTop = false
    var rightTop = false
    var leftBottom = false
    var rightBottom = false
    var startColor = 0
    var endColor = 0


    init {
        context?.let {
            val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.CircularCornerTextView, 0, 0)
            radius = ta.getDimension(R.styleable.CircularCornerTextView_radius, 0f)
            radiusWrap = ta.getBoolean(R.styleable.CircularCornerTextView_wrapradius, false)
            leftTop = ta.getBoolean(R.styleable.CircularCornerTextView_lefttop, false)
            rightTop = ta.getBoolean(R.styleable.CircularCornerTextView_righttop, false)
            leftBottom = ta.getBoolean(R.styleable.CircularCornerTextView_leftbottom, false)
            rightBottom = ta.getBoolean(R.styleable.CircularCornerTextView_rightbottom, false)

            startColor = ta.getColor(R.styleable.CircularCornerTextView_startcolor, Color.WHITE)
            endColor = ta.getColor(R.styleable.CircularCornerTextView_endcolor, Color.WHITE)

            ta.recycle()

        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        initBackground()
    }

    fun setTransitionalColor(@ColorInt tColor : Int){
        setTransitionalColor(tColor,tColor)
    }

    fun setTransitionalColor(@ColorInt startColor : Int,@ColorInt endColor : Int){
        this.startColor = startColor
        this.endColor = endColor
        invalidate()
    }

    /**
     * 初始化background
     */
    private fun initBackground() {
//        if (radius == 0F) {
//            return
//        }
        val colors = intArrayOf(startColor, endColor)
        val bg = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        if (radiusWrap) {
            radius = (measuredHeight / 2).toFloat()
        }
        val leftTopR = if (leftTop) {
            radius
        } else {
            0f
        }
        val rightTopR = if (rightTop) {
            radius
        } else {
            0f
        }
        val leftBottomR = if (leftBottom) {
            radius
        } else {
            0f
        }
        val rightBottomR = if (rightBottom) {
            radius
        } else {
            0f
        }
        bg.cornerRadii = floatArrayOf(leftTopR, leftTopR, rightTopR, rightTopR, rightBottomR, rightBottomR, leftBottomR, leftBottomR)
        backgroundDrawable = bg
    }

}