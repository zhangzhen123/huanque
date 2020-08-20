package com.julun.huanque.common.widgets.draweetext

import android.graphics.*
import android.text.style.ReplacementSpan
import com.julun.huanque.common.suger.logger

class RainbowSpan: ReplacementSpan() {

    private val colors: IntArray= intArrayOf(Color.BLUE,Color.DKGRAY)
    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        logger("text=${text} start=$start end=$end fm=${fm}")
        return end
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.style = Paint.Style.FILL
        val shader: Shader = LinearGradient(
            0f, 0f, 0f, paint.textSize * colors.size, colors, null,
            Shader.TileMode.MIRROR
        )
        val matrix = Matrix()
        matrix.setRotate(90f)
        shader.setLocalMatrix(matrix)
        paint.shader = shader
    }
}