package com.julun.huanque.common.widgets.rappleView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.logger
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/1/6 9:31
 *
 *@Description: WaterRippleView
 *
 */
class WaterRippleView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    companion object {
        /**
         * 更新界面
         */
        private const val UPDATE_VIEW = 100
    }

    private var mCircleColor = Color.BLACK

    /**
     * 画圆的画笔
     */
    private val mPaint = Paint()

    /**
     * 圆圈描边的画笔
     */
    private val mStrokePaint = Paint()

    init {
        initView()
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
        mPaint.color = mCircleColor

        mStrokePaint.isAntiAlias = true
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.strokeWidth = 1f
    }

    /**
     * 控件的宽
     */
    private var mWidth = 0

    /**
     * 控件的高
     */
    private var mHeight = 0


    /**
     * 圆的集合
     */
    private val circles: MutableList<Circle> = ArrayList()

    /**
     * 动画间隔时间(ms)
     */
    var intervalTime = 25

    /**
     * 每次增加的距离(px)
     */
    var distance = 2

    /**
     * 最小的半径(dp)
     */
    var minRadius = 18

    /**
     * 间隔的距离(dp)
     */
    var intervalDistance = 18

    /**
     * 画圆角的半径
     */
    private var angle = 0

    /**
     * 最大的半径
     */
    private var maxRadius = 0

    /**
     *
     */
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE_VIEW ->                     //更新界面
                    if (isMoving) {
                        updateCircles()
                        sendEmptyMessageDelayed(UPDATE_VIEW, intervalTime.toLong())
                    }
            }
        }
    }

    /**
     * 是否需要动画
     */
    private var isMoving = false


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        logger("onSizeChanged w=$w h=$h")
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth
        mHeight = measuredHeight
        maxRadius = Math.min(mWidth / 2, mHeight / 2)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in circles.indices) {
            val circle = circles[i]
            mPaint.alpha = circle.alpha
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, mPaint)
            mStrokePaint.alpha = circle.strokeAlpha
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, mStrokePaint)
        }

        canvas.drawCircle(mWidth / 2.toFloat(), mHeight / 2.toFloat(), dp2pxf(minRadius), mPaint)
        canvas.drawCircle(mWidth / 2.toFloat(), mHeight / 2.toFloat(), dp2pxf(minRadius), mStrokePaint)

        canvas.save()

        canvas.restore()

    }

    /**
     * 开始动画
     */
    fun startMoving() {
        isMoving = true
        mHandler.removeMessages(UPDATE_VIEW)
        mHandler.sendEmptyMessage(UPDATE_VIEW)
    }

    /**
     * 结束动画
     */
    fun stopMoving() {
        isMoving = false
        mHandler.removeMessages(UPDATE_VIEW)
    }

    override fun onDetachedFromWindow() {
        stopMoving()
        super.onDetachedFromWindow()
    }

    /**
     * 更新界面
     */
    private fun updateCircles() {
        if (mWidth == 0 && mHeight == 0) {
            return
        }
        angle += distance
        //更新圆
        for (i in circles.indices.reversed()) {
            val circle = circles[i]
            circle.radius += distance
            circle.alpha = updateCircleAlpha(circle.radius)
            circle.strokeAlpha = updateCircleStrokeAlpha(circle.radius)
            if (circle.radius >= maxRadius) {
                circles.remove(circle)
            }
        }
        if (circles.size == 0 || circles[circles.size - 1].radius - dp2px(minRadius) >= dp2px(intervalDistance)) {
            circles.add(
                Circle(
                    mWidth.toFloat() / 2,
                    mHeight.toFloat() / 2,
                    dp2pxf(minRadius),
                    updateCircleAlpha(dp2pxf(minRadius))
                )
            )
        }
        invalidate()
    }

    /**
     * 更新圆的颜色
     *
     * @param radius
     * @return
     */
    private fun updateCircleAlpha(radius: Float): Int {
        var alpha = ((maxRadius - radius) * 1.0 * 255 / maxRadius).toInt()
        if (alpha < 0) {
            alpha = 0
        }
        return alpha
    }

    private fun updateCircleStrokeAlpha(radius: Float): Int {
        var alpha = ((maxRadius - radius * 0.1) * 255 / maxRadius).toInt()
        if (alpha < 0) {
            alpha = 0
        }
        return alpha
    }

    /**
     * 设置波形的颜色
     *
     * @param color
     */
    fun setCircleColor(@ColorInt color: Int) {
        mCircleColor = color
        mPaint.color = mCircleColor
        mStrokePaint.color = mCircleColor
    }

    /**
     * 设置波形的描边宽度
     *
     */
    fun setCircleStrokeWidth(width: Float) {
        mStrokePaint.strokeWidth = width
    }


}