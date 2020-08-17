package com.julun.huanque.common.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.GlobalUtils
import org.jetbrains.anko.dip
import kotlin.math.cos
import kotlin.math.sin

/**
 *@创建者   dong
 *@创建时间 2020/8/15 14:35
 *@描述 流光TextView
 */
class ShimmerTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    //渲染器
    private var mLinearGradient: LinearGradient? = null

    //渲染范围
    private var mGradientMatrix: Matrix? = null

    //渲染的起始位置
    private var mViewWidth = 0

    //渲染的终止距离
    private var mTranslate = 0

    //是否启动动画
    private var mAnimating = true
    fun setAnimation(animation: Boolean) {
        mAnimating = animation
        postInvalidate()
    }

    //多少毫秒刷新一次
    private val speed = 50
    private var mPaint: Paint? = null

    init {
        mPaint = paint
        mGradientMatrix = Matrix()
    }


    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        logger("炫彩昵称  onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measuredWidth
        val gradientWidth = dp2pxf(20)
        mLinearGradient = mLinearGradient ?: LinearGradient(
            0f, 0f, gradientWidth * cos(20f / 180), gradientWidth * sin(20f / 180),
            intArrayOf(GlobalUtils.formatColor("#FF5757"), GlobalUtils.formatColor("#FFFC7B"), GlobalUtils.formatColor("#FF5757")),
            null, Shader.TileMode.CLAMP
        )
        if (mAnimating) {
            mPaint?.shader = mLinearGradient
        } else {
            mPaint?.shader = null
        }
    }


//    override fun setText(text: CharSequence?, type: BufferType?) {
////        logger("昵称 text = ${text.toString()},oriText = ${this.text.toString()}")
//        if (text?.toString() == this.text.toString()) {
//            //两次内容一致
//            return
//        }
//        super.setText(text, type)
//    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        logger("炫彩昵称 mViewWidth = $mViewWidth,mTranslate = $mTranslate,mGradientMatrix = $mGradientMatrix")
        if (mAnimating && mGradientMatrix != null) {
            mTranslate += 8 //每次移动屏幕的10px
            if (mTranslate > (mViewWidth + 50)) {
                mTranslate = 0
            }
            mGradientMatrix?.setTranslate(mTranslate.toFloat(), 0f)
            mLinearGradient?.setLocalMatrix(mGradientMatrix) //在指定矩阵上渲染
            postInvalidateDelayed(speed.toLong())
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

}