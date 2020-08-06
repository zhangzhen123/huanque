package com.julun.huanque.core.widgets

import android.graphics.Outline
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi

/**
 *@创建者   dong
 *@创建时间 2020/8/6 9:39
 *@描述
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SurfaceVideoViewOutlineProvider(var radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        val selfRect = Rect(
            0, 0,
            view.width, view.height
        )
        outline.setRoundRect(selfRect, radius)
    }
}