package com.julun.huanque.common.widgets.recycler.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.julun.huanque.common.helper.DensityHelper


class StaggeredDecoration(val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (outRect == null || view == null || parent == null) {
            return
        }
        val params = view.layoutParams as androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams
        // 获取item在span中的下标
        val spanIndex = params.spanIndex
        // 中间间隔
        if (spanIndex % 2 == 0) {
            outRect.left = space.toInt()
            outRect.right = space.toInt() / 2
        } else {
            // item为奇数位，设置其左间隔为5dp
            outRect.left = space.toInt() / 2
            outRect.right = space.toInt()
        }
        // 上方间隔
        outRect.top = space.toInt()
    }
}