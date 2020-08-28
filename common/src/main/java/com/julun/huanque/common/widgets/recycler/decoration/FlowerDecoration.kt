package com.julun.huanque.common.widgets.recycler.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.dp2px


class FlowerDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (outRect == null || view == null || parent == null) {
            return
        }
        val index = parent.getChildLayoutPosition(view)
        // 有一个头部
        if (index == 1) {
            outRect.top = dp2px(5)
        }
    }
}