package com.julun.huanque.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 *@创建者   dong
 *@创建时间 2020/12/17 15:09
 *@描述 自动滚动RecyclerView
 */
class AutoScrollRecyclerView(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs) {

    //触摸停止滚动标记位
    var needTouchStop = true

    var noTouchAble = false

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (needTouchStop) {
            animator?.cancel()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (noTouchAble) {
            return false
        }
        return super.onTouchEvent(e)
    }

    private var animator: ValueAnimator? = null

    /**
     * 开始滚动
     */
    fun startScroll() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 100_100_100f).apply {
            duration = 1000_1000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        animator?.addUpdateListener {
            scrollBy(0, 2)
        }

        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

}