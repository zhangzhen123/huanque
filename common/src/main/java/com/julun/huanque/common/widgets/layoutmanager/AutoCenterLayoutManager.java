package com.julun.huanque.common.widgets.layoutmanager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 自动将选中的position居中显示的LinearLayoutManager
 */
public class AutoCenterLayoutManager extends LinearLayoutManager {
    public AutoCenterLayoutManager(Context context) {
        super(context);
    }

    public AutoCenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public AutoCenterLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

//    @Override
//    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
//        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
//        smoothScroller.setTargetPosition(position);
//        startSmoothScroll(smoothScroller);
//    }
    public void smoothScrollToPosition(RecyclerView recyclerView, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 880f / displayMetrics.densityDpi;
        }
    }
}
