package com.julun.huanque.common.widgets.recycler.decoration;

import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 *
 *@author zhangzhen
 *@data 2018/12/27
 *
 * GridLayoutManager的万能分隔
 *
 **/


public class GridLayoutSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public GridLayoutSpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
            //判断是否在第一排
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {//第一排的需要上面
                outRect.top = space;
            }
            outRect.bottom = space;
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (lp.getSpanSize() == spanCount) {//占满
                outRect.left = space;
                outRect.right = space;
            } else {
                outRect.left = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
                outRect.right = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.left);
            }
        } else {
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {//第一排的需要left
                outRect.left = space;
            }
            outRect.right = space;
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (lp.getSpanSize() == spanCount) {//占满
                outRect.top = space;
                outRect.bottom = space;
            } else {
                outRect.top = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
                outRect.bottom = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.top);
            }
        }

    }
}