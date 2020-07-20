package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.julun.huanque.common.interfaces.EventListener

/**
 *@创建者   dong
 *@创建时间 2020/7/20 15:59
 *@描述
 */
class EventConstraintLayout(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
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