package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 *@创建者   dong
 *@创建时间 2019/8/2 14:42
 *@描述  不拦截事件的recyclerview
 */
open class NoTouchRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    constructor(context: Context) : this(context, null)

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

}