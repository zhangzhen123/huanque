package com.julun.huanque.common.utils

import android.content.res.Resources
import android.util.TypedValue
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout
import java.lang.reflect.Field

/**
 *
 *@author zhangzhen
 *@data 2017/5/8
 *
 **/
class WidgetsUtils {
    companion object {
        /**
         * 给tablayout的底部指示器设置左右边距
         */
        fun setIndicator(tabs: TabLayout, leftDip: Int, rightDip: Int) {
            val tabLayout = tabs.javaClass
            var tabStrip: Field? = null
            try {
                tabStrip = tabLayout.getDeclaredField("mTabStrip")
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }

            tabStrip!!.isAccessible = true
            var llTab: LinearLayout? = null
            try {
                llTab = tabStrip.get(tabs) as LinearLayout
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

            for (i in 0 until llTab!!.childCount) {
                val child = llTab.getChildAt(i)
                child.setPadding(0, 0, 0, 0)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                params.leftMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip.toFloat(), Resources.getSystem().displayMetrics).toInt()
                params.rightMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip.toFloat(), Resources.getSystem().displayMetrics).toInt()
                child.layoutParams = params
                child.invalidate()
            }
        }

    }
}