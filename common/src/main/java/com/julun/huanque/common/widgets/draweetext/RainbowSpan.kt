package com.julun.huanque.common.widgets.draweetext

import android.graphics.*
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import com.julun.huanque.common.suger.logger
import kotlin.math.sin

class AnimatedRainbowSpan(private var colors: IntArray= intArrayOf(Color.BLUE, Color.DKGRAY, Color.GREEN)) : CharacterStyle(), UpdateAppearance {
    private var shader: Shader? = null
    private val matrix = Matrix()
    var translateXPercentage = 1f

    override fun updateDrawState(paint: TextPaint) {
        logger("updateDrawState $translateXPercentage")
        translateXPercentage += 0.03f
        if(translateXPercentage>=4.3f){
            translateXPercentage=1.9f
        }
        paint.style = Paint.Style.FILL
//        val width=paint.measureText(text)
        val width = paint.textSize * colors.size
        if (shader == null) {
            shader = LinearGradient(
                0f, 0f, 0f, width, colors, null,
                Shader.TileMode.CLAMP
            )
        }
        matrix.reset()
        matrix.setRotate(90f)
        matrix.postTranslate(width * translateXPercentage, 0f)
        shader!!.setLocalMatrix(matrix)
        paint.shader = shader
    }


}