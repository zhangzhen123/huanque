package com.julun.huanque.common.widgets.recycler.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.julun.huanque.common.utils.ULog;

import java.util.logging.Logger;

/**
 * @author zhangzhen
 * @data 2018/12/27
 * <p>
 * GridLayoutManager的万能分隔 边界处不带分割版
 *
 * 于2020/11/23 改进算法 修复spanCount>2时分割不均异常的bug
 **/


public class GridLayoutSpaceItemDecoration2 extends RecyclerView.ItemDecoration {
    Logger logger = ULog.Companion.getLogger("GridLayoutSpaceItemDecoration2");

    private int space;

    public GridLayoutSpaceItemDecoration2(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        if(layoutManager==null){
            return;
        }
//        final GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        int total = parent.getAdapter().getItemCount();
        int spanSize = layoutManager.getSpanSizeLookup().getSpanSize(childPosition);
        int spanIndex=layoutManager.getSpanSizeLookup().getSpanIndex(childPosition,spanCount);
        int spanGroupIndex = layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount);
        ; // view 所在的列
        int lastGIndex = layoutManager.getSpanSizeLookup().getSpanGroupIndex(total - 1, spanCount);
        if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
//            int spanIndex=layoutManager.getSpanSizeLookup().getSpanIndex(childPosition, spanCount); //同lp.getSpanIndex()
            //判断是否在第一排
            if (spanGroupIndex == 0) {//第一排
                outRect.top = 0;
            }
//            logger.info("childPosition=" + childPosition + " spanGroupIndex:" + spanGroupIndex + " spanCount=" + spanCount +
//                    " spanSize=" + spanSize + "spanIndex=" + spanIndex);
            if (spanGroupIndex == lastGIndex) {//最后一排
                outRect.bottom = 0;
            } else {
                outRect.bottom = space;
            }
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (spanSize == spanCount) {//占满
                outRect.left = 0;
                outRect.right = 0;
            } else {
//                outRect.left = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
//                outRect.right = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.left);
//                if (lp.getSpanIndex() == 0) {//第一列
//                    outRect.left = 0;
//                } else if (lp.getSpanIndex() == spanCount - 1) {//最后一列
//                    outRect.right = 0;
//                }

                outRect.left = spanIndex * space / spanCount; // column * (列间距 * (1f / 列数))
                outRect.right = space - (spanIndex + 1) * space / spanCount; // 列间距 - (column + 1) * (列间距 * (1f /列数))
                logger.info("childPosition=" + childPosition + " outRect.left" + outRect.left + " outRect.right" + outRect.right);
            }
        } else {
            if (spanGroupIndex == 0) {//第一排的需要left
                outRect.left = 0;
            }
            if (spanGroupIndex == lastGIndex) {//最后一排的
                outRect.right = 0;
            } else {
                outRect.right = space;
            }
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (spanSize == spanCount) {//占满
                outRect.top = 0;
                outRect.bottom = 0;
            } else {
//                outRect.top = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * space);
//                outRect.bottom = (int) (((float) space * (spanCount + 1) / spanCount) - outRect.top);
//                if (lp.getSpanIndex() == 0) {
//                    outRect.top = 0;
//                } else if (lp.getSpanIndex() == spanCount - 1) {
//                    outRect.bottom = 0;
//                }

                outRect.top = spanIndex * space / spanCount; // column * (列间距 * (1f / 列数))
                outRect.bottom = space - (spanIndex + 1) * space / spanCount; // 列间距 - (column + 1) * (列间距 * (1f /列数))
            }
        }

    }
}