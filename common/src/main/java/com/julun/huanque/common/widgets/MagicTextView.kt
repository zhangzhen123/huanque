package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.TextView
import com.julun.huanque.common.R


class MagicTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : TextView(context, attrs, defStyle) {
    @ColorInt
    var strokeColor:Int= ContextCompat.getColor(context, R.color.white)
//     var fontMetricsInt: Paint.FontMetricsInt? = null
    override fun onDraw(canvas: Canvas) {
//        if(noPadding){
//            if (fontMetricsInt == null) {
//                fontMetricsInt = Paint.FontMetricsInt()
//                paint.getFontMetricsInt(fontMetricsInt)
//            }
//            fontMetricsInt?.let {
//                 canvas.translate(0f, (it.bottom - it.descent).toFloat())
//            }
//        }
        super.onDraw(canvas)

        val restoreColor = currentTextColor

        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 2f
        setTextColor(strokeColor)

        super.onDraw(canvas)

        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = true
        setTextColor(restoreColor)
    }

}
