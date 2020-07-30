package com.julun.huanque.core.widgets.live.slide

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.RelativeLayout
import android.widget.Scroller
import androidx.core.view.ViewCompat
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R


/**
 *
 *@作者: zhangzhen
 *
 *@Date 2019/7/16 15:23
 *
 *@Description 侧滑的右抽屉(可平滑移动) 配合[SlideViewContainer]使用
 *
 */
class SlideViewRightDrawer : RelativeLayout {
    private val logger= ULog.getLogger("SlideViewRightDrawer")
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    //右抽屉的宽度
    val RIGHTDRAWERWIDTH: Int by lazy { resources.getDimensionPixelOffset(R.dimen.right_drawer_width) }
    companion object{
        //动画执行时间
        private const val PAGE_ANI_DURATION = 300 // ms
//        //回调延迟执行时间 一定要大于动画时间
//        const val DELAY_CALLBACK_TIME= PAGE_ANI_DURATION +50L//
    }

    var onScrollListener: OnScrollListener?=null
    //正在执行动作时
    var isSetting=false

    private  var mScroller: Scroller
    private val sInterpolator = Interpolator { t ->
        var t = t
        t -= 1.0f
        t * t * t * t * t + 1.0f
    }
    init {
        mScroller = Scroller(context, sInterpolator)
    }
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
//            logger.info("computeScroll=${mScroller.currY}")
            isSetting=true
            onScrollListener?.onScroll( mScroller.currX - scrollX, mScroller.currY - scrollY)
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
        val sx: Int
        val wasScrolling = mScroller != null && !mScroller.isFinished
        if (wasScrolling) {
            sx = mScroller.currX
            // And abort the current scrolling.
//            mScroller.abortAnimation()
        } else {
            sx = scrollX
        }
        val sy = scrollY
        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            return
        }
        val duration: Int =
            PAGE_ANI_DURATION
        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
        mScroller.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun setScrollXY(x: Int,y: Int){
        mScroller.currY
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
        logger.info("completeScroll x=$x y=$y")
        val notEnd=(oldX!=0&&oldX!=-RIGHTDRAWERWIDTH)||(x!=0&&x!=-RIGHTDRAWERWIDTH)
        if ((dx != 0 || dy != 0)&&notEnd) {
            scrollTo(x, y)
            onScrollListener?.onScroll(dx, dy)
        }
    }
    interface OnScrollListener {
        fun onScroll( dx: Int,dy:Int)
    }
}
