package com.julun.huanque.core.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/18 10:04
 *
 *@Description: 可以控制分发事件的RecycleView
 *
 */
class DispatchRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    var mDispatchListener: DispatchListener? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (mDispatchListener != null) {
            return mDispatchListener!!.dispatch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    interface DispatchListener {
        fun dispatch(event: MotionEvent?): Boolean
    }
}