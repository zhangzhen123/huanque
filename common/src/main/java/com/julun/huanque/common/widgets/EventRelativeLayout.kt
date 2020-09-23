package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.logger

/**
 *@创建者   dong
 *@创建时间 2020/9/23 15:25
 *@描述
 */
class EventRelativeLayout(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
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