package com.julun.huanque.common.widgets.recycler.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * 针对LinearLayoutManager线性垂直布局
 * 添加上下间距[space]px
 *
 */
class VerticalItemDecoration(val space : Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)

        if (position != 0 ) {
            outRect.top = space
        }

    }
}