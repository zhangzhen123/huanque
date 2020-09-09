package com.julun.huanque.common.widgets.draweetext

import android.graphics.*
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ScreenUtils
import kotlin.math.sin

class AnimatedRainbowSpan(private var colors: IntArray = intArrayOf(Color.BLUE, Color.DKGRAY, Color.GREEN)) :
    CharacterStyle(), UpdateAppearance {
    private var shader: Shader? = null
    private val matrix = Matrix()
    var translateX = 0f
    var width = 0f
    override fun updateDrawState(paint: TextPaint) {
//        logger("updateDrawState $translateX")

        paint.style = Paint.Style.FILL
//        val width=paint.measureText(text)
//        val width = paint.textSize * colors.size
        if (width <= 0f) {
            width = paint.textSize * colors.size
        }
        if (shader == null) {
            shader = LinearGradient(
                0f, 0f, width, 0f, colors, null,
                Shader.TileMode.REPEAT
            )
        }
        matrix.reset()
//        matrix.setRotate(90f)
        matrix.postTranslate(translateX, 0f)
        shader!!.setLocalMatrix(matrix)
        paint.shader = shader

        translateX += 3.5f
        if (translateX >= width * 1) {
            translateX = -width
        }
    }


}