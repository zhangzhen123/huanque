package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by WanZhiYuan on 2018/09/16 0016.
 * 不被任何外界事物影响的跑马灯
 */
class MarqueeTextView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {

    override fun isFocused(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
//        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
//        super.onWindowFocusChanged(hasWindowFocus)
    }

}