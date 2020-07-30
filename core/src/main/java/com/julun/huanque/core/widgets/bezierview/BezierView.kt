package com.julun.huanque.core.widgets.bezierview

import android.animation.*
import android.content.Context
import android.graphics.PointF
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.view.animation.*
import android.widget.RelativeLayout
import android.widget.TextView
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor
import java.util.*

/**
 *
 *@author zhangzhen
 *@data 2018/11/19
 *
 **/


class BezierView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {
    private val logger = ULog.getLogger("BezierView")
    private val line = LinearInterpolator()//线性
    private val acc = AccelerateInterpolator()//加速
    private val dce = DecelerateInterpolator()//减速
    private val accdec = AccelerateDecelerateInterpolator()//先加速后减速
    // 初始化插补器
//    private val interpolators: Array<Interpolator> = arrayOf(line, acc, dce, accdec)

    private val random = Random()

    private var mHeight: Int = 0
    private var mWidth: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
    }

    private var dWidth: Int = 0
    private val dHeight: Int by lazy { dip(18) }
    fun addNumberView(number: Long, @ColorInt color: Int) {
        val textView = TextView(context)
        val lp: LayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    addRule(CENTER_HORIZONTAL, TRUE)//这里的TRUE 要注意 不是true
                    addRule(ALIGN_PARENT_BOTTOM, TRUE)
                }
        textView.layoutParams = lp
        textView.text = "+$number"
        textView.textColor = color
        textView.textSize = 18f
        textView.paint.isFakeBoldText = true
        addView(textView)
        dWidth = ScreenUtils.getViewRealWidth(textView)
        logger.info("当前的textView宽度=$dWidth 总宽度$mWidth")

        val set = getAnimator(textView)
        set.addListener(AnimEndListener(textView))
        set.start()

    }

    private fun getAnimator(target: View): Animator {
        val set = getEnterAnimtor(target)

        val bezierValueAnimator = getBezierValueAnimator(target)

        val finalSet = AnimatorSet()
//        finalSet.playSequentially(set)
        finalSet.playSequentially(set, bezierValueAnimator)
//        bezierValueAnimator.interpolator = interpolators[random.nextInt(4)]
        bezierValueAnimator.interpolator = line
        finalSet.setTarget(target)
        return finalSet
    }

    private fun getEnterAnimtor(target: View): AnimatorSet {

        val alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.2f, 1f)
        val scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.2f, 1f)
        val enter = AnimatorSet()
        enter.duration = 500
        enter.interpolator = LinearInterpolator()
        enter.playTogether(alpha, scaleX, scaleY)
        enter.setTarget(target)
        return enter
    }

    private fun getBezierValueAnimator(target: View): ValueAnimator {

        //初始化一个贝塞尔计算器- - 传入
        val evaluator = BezierEvaluator(getPointF(1), getPointF(2))

        //这里最好画个图 理解一下 传入了起点 和 终点
        var wid = if (mWidth == 0) 50 else mWidth
        wid = if (wid == 0) 50 else wid
        //改成从右下角开始
        val animator = ValueAnimator.ofObject(evaluator,
                //                                PointF(((mWidth - dWidth)).toFloat(), (mHeight - dHeight).toFloat()),//原来的样子,如果多加上这一行,会有比较有趣的现象
                //再减bottomMargin 是因为上面设置layout的时候,bottomMargin 设置为 bottomMargin
                PointF(((mWidth - dWidth) / 2).toFloat(), (mHeight - dHeight).toFloat()),
                PointF(random.nextInt(wid).toFloat(), 0f))
        animator.addUpdateListener(BezierListenr(target))
        animator.setTarget(target)
        animator.duration = 3000
        return animator
    }

    /**
     * 获取中间的两个 点

     * @param scale
     */
    private fun getPointF(scale: Int): PointF {
        val pointF = PointF()
//        pointF.x = random.nextInt(Math.abs(mWidth - dWidth) + 1).toFloat()//减去100 是为了控制 x轴活动范围,看效果 随意~~
//        //再Y轴上 为了确保第二个点 在第一个点之上,我把Y分成了上下两半 这样动画效果好一些  也可以用其他方法
//        pointF.y = (random.nextInt(Math.abs(mHeight - dHeight) + 1) / scale).toFloat()

        pointF.x = random.nextInt(Math.abs(mWidth - dWidth) + 1).toFloat()//减去100 是为了控制 x轴活动范围,看效果 随意~~
        pointF.y = ((Math.abs(mHeight - dHeight * 3) + 1) / scale).toFloat()
        return pointF
    }

    private inner class BezierListenr(private val target: View) : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            //这里获取到贝塞尔曲线计算出来的的x y值 赋值给view 这样就能让爱心随着曲线走啦
            val pointF = animation.animatedValue as PointF
            target.x = pointF.x
            target.y = pointF.y
            // 这里顺便做一个alpha动画
            target.alpha = 1 - animation.animatedFraction
        }
    }


    private inner class AnimEndListener(private val target: View) : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator) {
            //因为不停的add 导致子view数量只增不减,所以在view动画结束后remove掉
            if (!isDetach)
                removeView(target)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isDetach = false
    }

    var isDetach = false
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isDetach = true
    }
}