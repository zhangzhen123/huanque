package com.julun.huanque.common.widgets.recycler.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * Created by Android Studio.
 * User: xinchaodong
 * Date: 2019/3/11
 * Time: 10:53
 * 表情符合输入view的decoration
 */
class GridDecoration(val verticalPadding: Int,val horizontalPadding: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (outRect == null || view == null || parent == null) {
            return
        }
        outRect.top = verticalPadding/2
        outRect.bottom = verticalPadding/2
        outRect.left = horizontalPadding/2
        outRect.right = horizontalPadding/2
    }
}