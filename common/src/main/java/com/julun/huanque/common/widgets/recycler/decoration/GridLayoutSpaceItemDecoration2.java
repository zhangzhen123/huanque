package com.julun.huanque.common.widgets.recycler.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author zhangzhen
 * @data 2018/12/27
 * <p>
 * GridLayoutManager的万能分隔 边界处不带分割版
 **/


public class GridLayoutSpaceItemDecoration2 extends RecyclerView.ItemDecoration {
//    Logger logger = ULog.getLogger("GridLayoutSpaceItemDecoration2");

    private int space;

    public GridLayoutSpaceItemDecoration2(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        int total = parent.getAdapter().getItemCount();
        int spanGroupIndex = layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount);
        int lastGIndex = layoutManager.getSpanSizeLookup().getSpanGroupIndex(total - 1, spanCount);
        if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
//            int spanIndex=layoutManager.getSpanSizeLookup().getSpanIndex(childPosition, spanCount); //同lp.getSpanIndex()
            //判断是否在第一排
            if (spanGroupIndex == 0) {//第一排
                outRect.top = 0;
            }
//            logger.info("ChildCount:"+total+" lp.getSpanIndex()="+lp.getSpanIndex()+"spanIndex:"+spanIndex);
//            logger.info("spanGroupIndex:"+spanGroupIndex+" lastGIndex:"+lastGIndex+" spanCount="+spanCount+"lp.getSpanSize()="+lp.getSpanSize());
            if (spanGroupIndex == lastGIndex) {//最后一排
                outRect.bottom = 0;
            } else {
                outRect.bottom = space;
            }
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (lp.getSpanSize() == spanCount) {//占满
                outRect.left = 0;
                outRect.right = 0;
            } else {
                outRect.left = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
                outRect.right = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.left);
                if (lp.getSpanIndex() == 0) {//第一列
                    outRect.left = 0;
                } else if (lp.getSpanIndex() == spanCount - 1) {//最后一列
                    outRect.right = 0;
                }
            }
        } else {
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {//第一排的需要left
                outRect.left = 0;
            }
            if (spanGroupIndex == lastGIndex) {//最后一排的
                outRect.right = 0;
            } else {
                outRect.right = space;
            }
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (lp.getSpanSize() == spanCount) {//占满
                outRect.top = 0;
                outRect.bottom = 0;
            } else {
                outRect.top = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
                outRect.bottom = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.top);
                if (lp.getSpanIndex() == 0) {
                    outRect.top = 0;
                } else if (lp.getSpanIndex() == spanCount - 1) {
                    outRect.bottom = 0;
                }
            }
        }

    }
}