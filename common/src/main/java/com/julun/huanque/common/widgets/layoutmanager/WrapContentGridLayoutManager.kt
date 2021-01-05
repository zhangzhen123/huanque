package com.julun.huanque.common.widgets.layoutmanager

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *@创建者   dong
 *@创建时间 2020/11/11 16:27
 *@描述
 */
class WrapContentGridLayoutManager(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}