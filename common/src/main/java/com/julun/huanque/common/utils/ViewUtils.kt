package com.julun.huanque.common.utils

import android.view.View

/**
 *@创建者   dong
 *@创建时间 2020/8/27 15:58
 *@描述 view相关的工具类
 */
object ViewUtils {
    /**
     * 更新View的宽高
     */
    fun updateViewBorder(view: View, width: Int, height: Int) {
        val params = view.layoutParams
        params.width = width
        params.height = height
        view.layoutParams = params
    }

    /**
     * 更新view的宽度
     */
    fun updateViewWidth(view: View, width: Int){
        val params = view.layoutParams
        params.width = width
        view.layoutParams = params
    }
}