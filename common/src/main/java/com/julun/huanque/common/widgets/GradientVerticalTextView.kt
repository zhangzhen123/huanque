package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 *@创建者   dong
 *@创建时间 2020/4/9 10:08
 *@描述
 */
class GradientVerticalTextView(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    //颜色列表
    var mColorArray: IntArray? = null
    //位置坐标
    var mPositionArray: FloatArray? = null

    constructor(context: Context) : this(context, null)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val colorArray = mColorArray
        val positionArray = mPositionArray
        if (changed && colorArray != null ) {
            paint.shader = LinearGradient(
                    0f, height.toFloat(), 0f, 0f,
                    colorArray,
                    positionArray, Shader.TileMode.CLAMP)
        }
    }

}