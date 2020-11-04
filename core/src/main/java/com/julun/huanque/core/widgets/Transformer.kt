package com.julun.huanque.core.widgets

import android.view.View
import github.hellocsl.layoutmanager.gallery.GalleryLayoutManager

/**
 *@创建者   dong
 *@创建时间 2020/10/29 12:00
 *@描述
 */
class Transformer : GalleryLayoutManager.ItemTransformer {
    override fun transformItem(layoutManager: GalleryLayoutManager?, item: View?, fraction: Float) {
        //以圆心进行缩放

        //以圆心进行缩放
        item!!.pivotX = item.width / 2.0f
        item.pivotY = item.height / 2.0f
        val scale = 1 - 0.3f * Math.abs(fraction)
        item.scaleX = scale
        item.scaleY = scale
    }
}