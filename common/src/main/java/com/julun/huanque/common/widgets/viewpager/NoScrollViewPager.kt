package com.julun.huanque.common.widgets.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 *@创建者   dong
 *@创建时间 2019/10/14 10:13
 *@描述 不允许滚动的ViewPager
 */
class NoScrollViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    var noScroll: Boolean = true
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (noScroll) {
            false
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (noScroll) {
            false
        } else {
            super.onTouchEvent(ev)
        }
    }
}