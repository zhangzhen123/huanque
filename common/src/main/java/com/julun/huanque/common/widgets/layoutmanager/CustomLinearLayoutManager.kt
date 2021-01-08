package com.julun.huanque.common.widgets.layoutmanager

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 *@创建者   dong
 *@创建时间 2020/3/11 16:20
 *@描述
 */
class CustomLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        if (recyclerView == null) {
            super.smoothScrollToPosition(recyclerView, state, position)
        } else {
            val linearSmoothScroller =
                CustomLinearSmoothScroller(recyclerView.context)
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }
    }
}

class CustomLinearSmoothScroller(context: Context?) : LinearSmoothScroller(context) {
    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        //每px需要的时间
        return 1f
    }
}
