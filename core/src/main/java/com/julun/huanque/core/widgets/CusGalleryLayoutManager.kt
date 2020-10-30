package com.julun.huanque.core.widgets

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import github.hellocsl.layoutmanager.gallery.GalleryLayoutManager

/**
 *@创建者   dong
 *@创建时间 2020/10/30 11:51
 *@描述 修改速度的画廊 layoutmanager
 */
class CusGalleryLayoutManager(orientation: Int) : GalleryLayoutManager(orientation) {

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        if(recyclerView != null){
            val linearSmoothScroller = CusGallerySmoothScroller(recyclerView.context)
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }
    }


    private class CusGallerySmoothScroller(context: Context) : LinearSmoothScroller(context){
        /**
         * Calculates the horizontal scroll amount necessary to make the given view in center of the RecycleView
         *
         * @param view The view which we want to make in center of the RecycleView
         * @return The horizontal scroll amount necessary to make the view in center of the RecycleView
         */
        fun calculateDxToMakeCentral(view: View): Int {
            val layoutManager: RecyclerView.LayoutManager? = layoutManager
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0
            }
            val params: RecyclerView.LayoutParams = view.layoutParams as RecyclerView.LayoutParams
            val left: Int = layoutManager.getDecoratedLeft(view) - params.leftMargin
            val right: Int = layoutManager.getDecoratedRight(view) + params.rightMargin
            val start: Int = layoutManager.getPaddingLeft()
            val end: Int = layoutManager.getWidth() - layoutManager.getPaddingRight()
            val childCenter = left + ((right - left) / 2.0f).toInt()
            val containerCenter = ((end - start) / 2f).toInt()
            return containerCenter - childCenter
        }

        /**
         * Calculates the vertical scroll amount necessary to make the given view in center of the RecycleView
         *
         * @param view The view which we want to make in center of the RecycleView
         * @return The vertical scroll amount necessary to make the view in center of the RecycleView
         */
        fun calculateDyToMakeCentral(view: View): Int {
            val layoutManager: RecyclerView.LayoutManager? = layoutManager
            if (layoutManager == null || !layoutManager.canScrollVertically()) {
                return 0
            }
            val params: RecyclerView.LayoutParams = view.layoutParams as RecyclerView.LayoutParams
            val top: Int = layoutManager.getDecoratedTop(view) - params.topMargin
            val bottom: Int = layoutManager.getDecoratedBottom(view) + params.bottomMargin
            val start: Int = layoutManager.getPaddingTop()
            val end: Int = layoutManager.getHeight() - layoutManager.getPaddingBottom()
            val childCenter = top + ((bottom - top) / 2.0f).toInt()
            val containerCenter = ((end - start) / 2f).toInt()
            return containerCenter - childCenter
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return 0.0001f
        }

//        override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
////            val dx = calculateDxToMakeCentral(targetView)
////            val dy = calculateDyToMakeCentral(targetView)
////            val distance = Math.sqrt(dx * dx + dy * dy.toDouble()).toInt()
////            val time = 10
////            if (time > 0) {
////                action.update(-dx, -dy, time, mDecelerateInterpolator)
////            }
//        }
    }
}