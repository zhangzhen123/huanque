package com.julun.huanque.common.widgets.viewpager

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import com.julun.huanque.common.widgets.bgabanner.BGABannerScroller


/**
 * Created by dong on 2018/4/12.
 */
class DViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    constructor(context: Context) : this(context, null)
    var scroller : BGABannerScroller? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var tHeight = 0
        //下面遍历所有child的高度
        (0 until childCount)
                .forEach {
                    val child = getChildAt(it)
                    child.measure(widthMeasureSpec,
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                    val h = child.measuredHeight
                    tHeight = Math.max(tHeight, h)
                }
        var mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(tHeight,
                MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec)
    }

    /**
     * 设置调用setCurrentItem(int item, boolean smoothScroll)方法时，page切换的时间长度
     *
     * @param duration page切换的时间长度
     */
    fun setPageChangeDuration(duration: Int) {
        try {
            scroller = BGABannerScroller(context, duration)
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            scrollerField.set(this, scroller)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, true)
    }

    override fun setCurrentItem(item: Int,smooth : Boolean) {
        val current = currentItem
        //如果页面相隔大于1,就设置页面切换的动画的时间为0
        if (Math.abs(current - item) > 1) {
            scroller?.noDuration = true
            super.setCurrentItem(item,smooth)
            scroller?.noDuration = false
        } else {
            scroller?.noDuration = false
            super.setCurrentItem(item,smooth)
        }

    }
}