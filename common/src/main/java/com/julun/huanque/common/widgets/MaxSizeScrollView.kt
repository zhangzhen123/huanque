package com.julun.huanque.common.widgets

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Display
import android.widget.ScrollView
import com.julun.huanque.common.suger.dp2px


/**
 *@创建者   dong
 *@创建时间 2020/8/7 11:20
 *@描述
 */
class MaxSizeScrollView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ScrollView(context, attrs, defStyleAttr) {
    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        //最大高度为249dp
        val maxHeight = dp2px(290)
        //此处是关键，设置控件高度不能超过屏幕高度一半（d.heightPixels / 2）（在此替换成自己需要的高度）
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        //重新计算控件高、宽
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


}