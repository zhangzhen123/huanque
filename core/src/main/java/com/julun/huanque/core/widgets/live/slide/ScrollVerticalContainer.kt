package com.julun.huanque.core.widgets.live.slide

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.core.view.ViewCompat
import com.julun.huanque.common.utils.ULog


/**
 *
 *@作者: zhangzhen
 *
 *@Date 2019/7/15 17:20
 *
 *@Description 该容器可以平滑的滑动 配合[SlideViewContainer]使用
 *
 */
class ScrollVerticalContainer : FrameLayout {
    private val logger = ULog.getLogger("ScrollVerticalContainer")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    companion object {
        //动画执行时间
        private const val PAGE_ANI_DURATION = 300 // ms
        //回调延迟执行时间 一定要大于动画时间
        const val DELAY_CALLBACK_TIME = PAGE_ANI_DURATION + 50L//
    }

    var onScrollListener: OnScrollListener? = null

//    var mIsScrollStarted: Boolean = false

    //正在执行动作时
    var isSetting=false
    private var mScroller: Scroller
    private val sInterpolator = Interpolator { t ->
        var t = t
        t -= 1.0f
        t * t * t * t * t + 1.0f
    }

    init {
        mScroller = Scroller(context, sInterpolator)
    }

    override fun computeScroll() {
//        mIsScrollStarted = true
        if (mScroller.computeScrollOffset()) {
            isSetting=true
//            logger.info("computeScroll=${mScroller.currY}")
            onScrollListener?.onScroll(mScroller.currX - scrollX, mScroller.currY - scrollY)
            scrollTo(mScroller.currX, mScroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
        }else{
            isSetting=false
        }
    }

    override fun onDetachedFromWindow() {
        // To be on the safe side, abort the scroller
        if (mScroller != null && !mScroller.isFinished) {
            mScroller.abortAnimation()
        }
        super.onDetachedFromWindow()
    }

    fun abortAnimation() {
        mScroller.abortAnimation()
    }

    /**
     * 容器滑动
     */
    fun smoothScrollTo(x: Int, y: Int) {
        val sx: Int = scrollX
        val sy: Int
        val wasScrolling = !mScroller.isFinished
        if (wasScrolling) {
            sy = mScroller.currY
//            sy = if (mIsScrollStarted) mScroller.currY else mScroller.startY
            // And abort the current scrolling.
            mScroller.abortAnimation()
        } else {
            sy = scrollY
        }

        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            return
        }
        val duration: Int =
            PAGE_ANI_DURATION
        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
//        mIsScrollStarted = false
        mScroller.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * 完成未完成的滑动到最后
     */
    fun finishScroll() {
        val oldX = scrollX
        val oldY = scrollY
        val x = mScroller.currX
        val y = mScroller.currY
        val dx = x - oldX
        val dy = y - oldY
        val needScroll=oldX!=0||oldY!=0
        logger.info("completeScroll x=$x y=$y")
        if (needScroll&&(dx != 0 || dy != 0)) {
            scrollTo(x, y)
            onScrollListener?.onScroll(dx, dy)
        }
    }

    interface OnScrollListener {
        fun onScroll(dx: Int, dy: Int)
    }
}
