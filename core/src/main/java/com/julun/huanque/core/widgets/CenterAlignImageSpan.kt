package com.julun.huanque.core.widgets

import android.R.attr.bottom
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ImageSpan
import androidx.annotation.NonNull


class CenterAlignImageSpan : ImageSpan {

    constructor(drawable: Drawable,  verticalAlignment:Int) : super(drawable,verticalAlignment)

    override fun draw(@NonNull canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int,
                      @NonNull paint: Paint) {

        val b = drawable
        val fm = paint.getFontMetricsInt()
        val transY = (y + fm.descent + y + fm.ascent) / 2 - b.bounds.bottom / 2//计算y方向的位移
        canvas.save()
        canvas.translate(x, transY.toFloat())//绘制图片位移一段距离
        b.draw(canvas)
        canvas.restore()
    }
}