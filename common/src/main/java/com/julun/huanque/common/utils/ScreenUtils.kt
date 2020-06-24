package com.julun.huanque.common.utils


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.julun.huanque.common.init.CommonInit


/**
 * 获得屏幕相关的辅助类
 */
class ScreenUtils private constructor() {

    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }

    companion object {

        private val application by lazy { CommonInit.getInstance().getApp() }

        /**
         * 显示软键盘
         */
        fun showSoftInput(context: Context, view: View) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                view,
                0
            )
        }

        /**
         * 显示软键盘
         */
        fun showSoftInput(view: View) {
            (application.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                view,
                0
            )
        }

        /**
         * 关闭软键盘
         */
        fun hideSoftInput(view: View) {
            val inputMethodManager =
                (application.baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * 关闭软键盘

         * @param activity activity
         */
        fun hideSoftInput(activity: Activity) {
            var view = activity.currentFocus
            if (view == null) view = View(activity)
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                ?: return
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * 获得屏幕宽度

         * @return
         */
        fun getScreenWidth(): Int {
            return application.resources.displayMetrics.widthPixels
        }

        /**
         * 获得屏幕宽度

         * @return
         */
        val screenWidthFloat: Float
            get() = getScreenWidth().toFloat()

        /**
         * 获得屏幕高度

         * @return
         */
        fun getScreenHeight(): Int {
            return CommonInit.getInstance().getApp().resources.displayMetrics.heightPixels
        }

        /**
         * 通过反射，获取包含虚拟键的整体屏幕高度

         * @return
         */
        fun getScreenHeightHasVirtualKey(): Int {
            var dpi = 0
            val wmManager = CommonInit.getInstance().getApp()
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wmManager.defaultDisplay
            val dm = DisplayMetrics()
            val c: Class<*>
            try {
                c = Class.forName("android.view.Display")
                val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
                method.invoke(display, dm)
                dpi = dm.heightPixels
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return dpi
        }

        /**
         * 获得屏幕高度

         * @return
         */
        val screenHeightFloat: Float
            get() = getScreenHeight().toFloat()

        /**
         * 获得状态栏的高度

         * @return
         */
        val statusHeight: Int
            get() {
                var result = 0
                val resourceId = CommonInit.getInstance().getApp().resources.getIdentifier(
                    "status_bar_height",
                    "dimen",
                    "android"
                )
                if (resourceId > 0) {
                    result = CommonInit.getInstance().getApp().resources.getDimensionPixelSize(
                        resourceId
                    )
                }
                return result
            }

        /**
         * 用到反射,会有效率问题.舍弃.
         * @return
         */
        val statusHeight2: Int
            @Deprecated("")
            get() {
                var statusHeight = -1
                try {
                    val clazz = Class.forName("com.android.internal.R\$dimen")
                    val `object` = clazz.newInstance()
                    val height = Integer.parseInt(
                        clazz.getField("status_bar_height").get(`object`).toString()
                    )

                    statusHeight = application.resources.getDimensionPixelSize(height)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return statusHeight
            }

        /**
         * 获取当前屏幕截图，包含状态栏

         * @param activity
         * *
         * @return
         */
        fun snapShotWithStatusBar(activity: Activity): Bitmap {
            val view = activity.window.decorView
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val bmp = view.drawingCache
            val width = getScreenWidth()
            val height = getScreenHeight()
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(bmp, 0, 0, width, height)
            bmp.recycle()
            view.destroyDrawingCache()
            return bp

        }

        /**
         * 获取当前屏幕截图，不包含状态栏

         * @param activity
         * *
         * @return
         */
        fun snapShotWithoutStatusBar(activity: Activity): Bitmap {
            val view = activity.window.decorView
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val bmp = view.drawingCache
            val frame = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(frame)
            val statusBarHeight = frame.top

            val width = getScreenWidth()
            val height = getScreenHeight()
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight)
            view.destroyDrawingCache()
            bmp.recycle()
            return bp

        }

        /**
         * 计算视图可视高度

         * @return
         */
        fun computeUsableHeight(mViewObserved: View?): Int {
            val r = Rect()
            mViewObserved?.getWindowVisibleDisplayFrame(r)
            return r.bottom - r.top
        }

        /**
         * 获取一个view的真实宽度
         */
        fun getViewRealWidth(view: View): Int {

            var width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

            val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

            view.measure(width, height)

            width = view.measuredWidth
            return width
        }

        /**
         * 获取一个view的真实高度
         */
        fun getViewRealHeight(view: View): Int {

            var width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

            var height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

            view.measure(width, height)

            height = view.measuredHeight
            return height
        }
    }


}
