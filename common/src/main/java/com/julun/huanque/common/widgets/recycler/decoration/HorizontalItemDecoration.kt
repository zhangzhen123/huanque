package com.julun.huanque.common.widgets.recycler.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * 针对LinearLayoutManager线性水平布局
 * 横向添加间距[space]px，上下无间距
 */
class HorizontalItemDecoration(val space : Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if(outRect == null || parent == null || view == null){
            return
        }
        val index = parent.getChildLayoutPosition(view)
        if(index != 0){
            outRect.left = space
        }

    }

}