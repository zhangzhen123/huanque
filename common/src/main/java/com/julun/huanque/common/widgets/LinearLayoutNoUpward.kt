package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 *@创建者   dong
 *@创建时间 2019/8/28 11:24
 *@描述 不向上传递的LinearLayout
 */
class LinearLayoutNoUpward(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
}