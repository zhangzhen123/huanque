package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.interfaces.EventListener

/**
 *@创建者   dong
 *@创建时间 2019/8/2 14:42
 *@描述  可以监听事件分发的RecyclerView
 */
class EventRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    var mEventListener: EventListener? = null
    //是否接收事件
    var mTouchEnable = true

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        mEventListener?.onDispatch(ev)
        return if (mTouchEnable) {
            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }

}