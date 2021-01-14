package com.julun.huanque.common.utils


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
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
         * 判断当前软键盘是否打开
         *
         * @param activity
         * @return
         */
        fun isSoftInputShow(activity: Activity): Boolean {

            // 虚拟键盘隐藏 判断view是否为空
            val view = activity.window.peekDecorView()
            if (view != null) {
                // 隐藏虚拟键盘
                val inputmanger = activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                return inputmanger!!.isActive() && activity.window.currentFocus != null
            }
            return false
        }

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

         *   "目前最低版本就是17 不再需要反射" 使用[getRealScreenHeight]代替
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
         *
         * 由于现在最低版本是17 所以可以直接使用getRealMetrics方法获取真实的屏幕高度了
         *
         */
        fun getRealScreenHeight(): Int {
            val wmManager = CommonInit.getInstance().getApp()
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wmManager.defaultDisplay

            val dm = DisplayMetrics()
            display.getRealMetrics(dm)
            return dm.heightPixels
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


        /**
         * 获取虚拟按键的高度
         * 1. 全面屏下
         * 1.1 开启全面屏开关-返回0
         * 1.2 关闭全面屏开关-执行非全面屏下处理方式
         * 2. 非全面屏下
         * 2.1 没有虚拟键-返回0
         * 2.1 虚拟键隐藏-返回0
         * 2.2 虚拟键存在且未隐藏-返回虚拟键实际高度
         */
        fun getNavigationBarHeightIfRoom(context: Context): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (navigationGestureEnabled(context)) {
                    0
                } else getCurrentNavigationBarHeight(context as Activity)
            } else {
                0
            }

        }

        /**
         * 全面屏（是否开启全面屏开关 0 关闭  1 开启）
         *
         * @param context
         * @return
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        fun navigationGestureEnabled(context: Context): Boolean {
            val enable = Settings.Global.getInt(context.contentResolver, getDeviceInfo(), 0)
            return enable != 0
        }

        /**
         * 获取设备信息（目前支持几大主流的全面屏手机，亲测华为、小米、oppo、魅族、vivo都可以）
         *
         * @return
         */
        fun getDeviceInfo(): String {
            val brand = Build.BRAND
            if (TextUtils.isEmpty(brand)) return "navigationbar_is_min"

            return if (brand.equals("HUAWEI", ignoreCase = true)) {
                "navigationbar_is_min"
            } else if (brand.equals("XIAOMI", ignoreCase = true)) {
                "force_fsg_nav_bar"
            } else if (brand.equals("VIVO", ignoreCase = true)) {
                "navigation_gesture_on"
            } else if (brand.equals("OPPO", ignoreCase = true)) {
                "navigation_gesture_on"
            } else {
                "navigationbar_is_min"
            }
        }

        /**
         * 非全面屏下 虚拟键实际高度(隐藏后高度为0)
         * @param activity
         * @return
         */
        fun getCurrentNavigationBarHeight(activity: Activity): Int {
            return if (isNavigationBarShown(activity)) {
                getNavigationBarHeight(activity)
            } else {
                0
            }
        }

        /**
         * 非全面屏下 虚拟按键是否打开
         * @param activity
         * @return
         */
        fun isNavigationBarShown(activity: Activity): Boolean {
            //虚拟键的view,为空或者不可见时是隐藏状态
            val view = activity.findViewById<View>(android.R.id.navigationBarBackground)
                ?: return false
            val visible = view.visibility
            return !(visible == View.GONE || visible == View.INVISIBLE)
        }

        /**
         * 非全面屏下 虚拟键高度(无论是否隐藏)
         * @param context
         * @return
         */
        fun getNavigationBarHeight(context: Context): Int {
            var result = 0
            val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    }


}
