package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.julun.huanque.common.interfaces.EventListener

/**
 *@创建者   dong
 *@创建时间 2020/8/14 14:54
 *@描述
 */
class EventViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    var mEventListener: EventListener? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        mEventListener?.onDispatch(ev)
        return super.dispatchTouchEvent(ev)
    }
}