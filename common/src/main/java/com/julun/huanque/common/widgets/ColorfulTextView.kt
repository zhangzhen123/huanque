package com.julun.huanque.common.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.julun.huanque.common.suger.logger


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/18 14:25
 *
 *@Description: 彩虹色的文字 还带自定义描边
 *
 */
class ColorfulTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {


    private var borderText: AppCompatTextView = AppCompatTextView(context, attrs)///用于描边的TextView

    //渲染范围
    private var mGradientMatrix: Matrix? = null

    //描边颜色和宽度
    var strokeColor: Int = Color.WHITE
    var strokeWidth: Float = 2f


    init {
        mGradientMatrix = Matrix()
        val tp1: TextPaint = borderText.paint
        tp1.style = Paint.Style.STROKE //对文字只描边
        tp1.strokeWidth = strokeWidth //设置描边宽度
        borderText.setTextColor(strokeColor) //设置描边颜色

        borderText.gravity = gravity
    }

    private var mPaint: Paint = paint

    fun setStrokeColorAndWidth(color: Int, width: Float) {
        strokeColor = color
        strokeWidth = width

        val tp1: TextPaint = borderText.paint
        tp1.strokeWidth = strokeWidth //设置描边宽度
        borderText.setTextColor(strokeColor) //设置描边颜色
        requestLayout()
    }

    var colors: IntArray = intArrayOf(Color.BLUE, Color.DKGRAY, Color.GREEN)
        set(value) {
            field = value
            requestLayout()
        }
    private var mViewWidth = 0
    private var mViewHeight = 0
    var isColorsVertical = false
        set(value) {
            field = value
            requestLayout()
        }


    private var mLinearGradient: LinearGradient? = null
    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        borderText.layoutParams = params
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measuredWidth
        mViewHeight = measuredHeight
        logger("mViewWidth=$mViewWidth mViewHeight=$mViewHeight")
//        if (mLinearGradient == null) {
        mLinearGradient = if (isColorsVertical) {
            LinearGradient(
                0f, 0f, 0f, mViewHeight.toFloat(),
                colors, null, Shader.TileMode.REPEAT
            )
        } else {
            LinearGradient(
                0f, 0f, mViewWidth.toFloat(), 0f,
                colors, null, Shader.TileMode.REPEAT
            )
        }

//        }

        val tt = borderText.text
        //两个TextView上的文字必须一致
        if (tt == null || tt != this.text) {
            borderText.text = text
            this.postInvalidate()
        }
        borderText.measure(widthMeasureSpec, heightMeasureSpec)


        mPaint.shader = mLinearGradient
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        borderText.layout(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        borderText.draw(canvas)
        mGradientMatrix?.reset()
        mLinearGradient?.setLocalMatrix(mGradientMatrix) //在指定矩阵上渲染
        super.onDraw(canvas)

    }
}