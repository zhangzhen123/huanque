package com.julun.huanque.common.utils

import android.util.DisplayMetrics
import android.util.TypedValue
import com.julun.huanque.common.init.CommonInit

/**
 * 常用单位转换的辅助类
 */
class DensityUtils private constructor() {
    companion object {
        private val RESOURCESDisplayMetrics: DisplayMetrics =
            CommonInit.getInstance().getApp().resources.displayMetrics

        /**
         * dp转px
         *
         * @param dpVal
         * @return
         */
        fun dp2px(dpVal: Int): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal.toFloat(),
                RESOURCESDisplayMetrics
            )
        }

        /**
         * dp转px
         *
         * @param dpVal
         * @return
         */
        @JvmStatic
        fun dp2px(dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal,
                RESOURCESDisplayMetrics
            ).toInt()
        }

        /**
         * sp转px
         *
         * @param spVal
         * @return
         */
        fun sp2px(spVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal,
                RESOURCESDisplayMetrics
            ).toInt()
        }

        /**
         * px转dp
         *
         * @param pxVal
         * @return
         */
        fun px2dp(pxVal: Float): Int {
            val scale = RESOURCESDisplayMetrics.density
            return (pxVal / scale + 0.5f).toInt()
        }

        /**
         * px转sp
         *
         * @param pxVal
         * @return
         */
        fun px2sp(pxVal: Float): Float {
            return pxVal / RESOURCESDisplayMetrics.scaledDensity
        }
    }

    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}