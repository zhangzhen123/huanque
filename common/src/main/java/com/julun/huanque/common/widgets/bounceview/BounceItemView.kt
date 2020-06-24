package com.julun.huanque.common.widgets.bounceview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.julun.huanque.common.R

/**
 * Created by dong on 2018/5/9.
 */
class BounceItemView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context?) : this(context,null)
    private val animationT : ValueAnimator = ValueAnimator.ofFloat(1f,0f)
    private var totalY = 0
    private var tempY = 0f
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val radius = 12f
    var repeatListener : RepeatListener? = null

    init {
        animationT.duration = 240
        animationT.interpolator = LinearInterpolator()
        animationT.addUpdateListener {
            val tempValue = it.animatedValue as Float
            tempY = (totalY - 2 * radius) * tempValue + radius
            this.alpha = 0.2f + 0.8f * tempValue
            postInvalidate()
        }

        animationT.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
                if(ViewCompat.isAttachedToWindow(this@BounceItemView)){
                    repeatListener?.onRepeat()
                }
            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })

        animationT.repeatMode = ValueAnimator.REVERSE
        animationT.repeatCount = 1
        mPaint.style = Paint.Style.FILL_AND_STROKE
        context?.let {
            mPaint.color = ContextCompat.getColor(it, R.color.primary_color)
        }
    }

    fun startPlay(){
        if(ViewCompat.isAttachedToWindow(this@BounceItemView)){
            animationT?.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationT.removeAllListeners()
        animationT.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        totalY = measuredHeight
        tempY = totalY - radius
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null){
            return
        }
        canvas.drawCircle(measuredWidth/2.toFloat(),tempY,radius,mPaint)
    }

    interface RepeatListener{
        fun onRepeat()
    }

}