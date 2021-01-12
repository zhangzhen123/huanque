package com.julun.huanque.core.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginTop
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.julun.huanque.common.interfaces.ScrollMarginListener
import com.julun.huanque.common.manager.GlobalDialogManager.logger
import java.util.logging.Logger


/**
 *@创建者   dong
 *@创建时间 2020/12/15 19:46
 *@描述 自定义滑动效果
 */
class CustomCoordinatorLayout(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout(context, attrs) {
    var mListener: ScrollMarginListener? = null
    private var mZoomView: View? = null
    private var mZoomViewWidth = 0
    private var mZoomViewHeight = 0

    //记录第一次按下的位置
    private var firstPosition = 0f

    //是否正在缩放
    private var isScrolling = false

    //是否下滑
    private var isScrollDown = false

    private val mScrollRate = 0.6f //缩放系数，缩放系数越大，变化的越大

    private val mReplyRate = 0.3f //回调系数，越大，回调越慢

    private var mMoveView: View? = null
    private var mMoveView2: View? = null
    private var mMoveView3: View? = null
    private var height1 = 0
    private var height2: Int = 0
    private var height3 = 0

    //是否处于缩放中
    fun isScrolling(): Boolean {
        val tempWidth = mZoomView?.measuredWidth ?: 0
        return tempWidth != mZoomViewWidth
    }

    fun setmZoomView(mZoomView: View?) {
        this.mZoomView = mZoomView
    }

    fun setmMoveView(mMoveView1: View?, mMoveView2: View, mMoveView3: View?) {
        mMoveView = mMoveView1
        this.mMoveView2 = mMoveView2
        this.mMoveView3 = mMoveView3
        height1 = mMoveView?.measuredHeight ?: 0
        height2 = mMoveView2.measuredHeight
        height3 = mMoveView3?.measuredHeight ?: 0
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val location = IntArray(2)
        mZoomView?.getLocationOnScreen(location)
        val y = location[1]
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            mZoomViewWidth = mZoomView?.measuredWidth ?: 0
            mZoomViewHeight = mZoomView?.measuredHeight ?: 0
        }

//        logger.info("y = $y")
        when (ev.action) {
            MotionEvent.ACTION_UP -> {
                if (isScrollDown) {
                    setZoom(0f)
                    return super.dispatchTouchEvent(ev)
                }
                //手指离开后恢复图片
                isScrolling = false
                val marginTop = mMoveView3?.marginTop ?: 0
                if (marginTop >= 150) {
                    (context as? Activity)?.finish()
                } else {
                    replyImage()
                }
            }
            MotionEvent.ACTION_MOVE -> {
//                logger.info("y = $y,mZoomView?.measuredWidth = ${mZoomView?.measuredWidth}")
                if (y != 0 && (mZoomView?.measuredWidth ?: 0) <= mZoomViewWidth) {
                    setZoom(0f)
                    return super.dispatchTouchEvent(ev)
                }
                isScrollDown = false
                if (!isScrolling) {
                    if (scrollY == 0) {
                        firstPosition = ev.y // 滚动到顶部时记录位置，否则正常返回
                    } else {
                        setZoom(0f)
                        return super.dispatchTouchEvent(ev)
                    }
                }
                val distance =
                    ((ev.y - firstPosition) * mScrollRate).toInt() // 滚动距离乘以一个系数
                if (distance < 0) { // 当前位置比记录位置要小，正常返回
                    isScrollDown = true
                    setZoom(0f)
                    return super.dispatchTouchEvent(ev)
                }
                // 处理放大
                isScrolling = true
                setZoom(distance.toFloat())
                return if (mMoveView3?.marginTop == 0) {
                    super.dispatchTouchEvent(ev)
                } else {
                    true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun scrollDown(zoom: Float) {
        mMoveView2?.setScrollY(-(height2 * ((height2 + zoom) / height2)) as Int)
    }

    //回弹动画
    private fun replyImage() {
//        setZoom(0f)
        val distance: Float = (mZoomView?.measuredWidth?.toFloat() ?: 0f) - mZoomViewWidth
//        logger.info("distance = $distance，zoomViewMeasuredWidth = ${mZoomView?.measuredWidth?.toFloat()}")
        val valueAnimator =
            ValueAnimator.ofFloat(distance, 0f).setDuration((distance * mReplyRate).toLong())
        valueAnimator.addUpdateListener { animation -> setZoom(animation.animatedValue as Float) }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                setZoom(0f)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        valueAnimator.start()
        mMoveView?.setScrollY(height)
        mMoveView2?.setScrollY(height2)
        scrollTo(0, 0)
//        mMoveView3?.setScrollY(height3)
    }

    fun setZoom(zoom: Float) {
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return
        }
        if (zoom == 0f &&mZoomView?.width == mZoomViewWidth) {
            mListener?.scroll(0)
            return
        }
        val lp: ViewGroup.LayoutParams = mZoomView?.layoutParams ?: return
        lp.width = (mZoomViewWidth * ((mZoomViewWidth + zoom) / mZoomViewWidth)).toInt()
        lp.height = (mZoomViewHeight * ((mZoomViewWidth + zoom) / mZoomViewWidth)).toInt()
        (lp as MarginLayoutParams).setMargins(-(lp.width - mZoomViewWidth) / 2, 0, 0, 0)
        mZoomView?.layoutParams = lp
        logger.info("zoom width = ${lp.width},height = ${lp.height},distance = $zoom")
        //给图片设置marginTop
        val marginTop = lp.height - mZoomViewHeight
        val picParams = mMoveView3?.layoutParams as? CollapsingToolbarLayout.LayoutParams ?: return
        picParams?.topMargin = marginTop
        mListener?.scroll(marginTop)
        mMoveView3?.layoutParams = picParams
        try {
            val parent = mMoveView?.parent as? CollapsingToolbarLayout ?: return
            val layoutParams = parent.layoutParams
            layoutParams.height = lp.height
            parent.layoutParams = layoutParams
        } catch (e: Exception) {
        }
    }

}