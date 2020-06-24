package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import kotlin.math.abs

/**
 * Created by WanZhiYuan on 2019/02/28 0028.
 * 用于优化图片多的列表，滚动时不再加载图片，在滚动停止再去加载图片
 * 在性能较差的手机上 图片绘制到控件上是一个会卡顿的情况，这样会导致滚动不流畅问题
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/08/30
 * @iterativeVersion 4.17
 * @iterativeDetail 再次优化图片列表，现在判断滚动的速率，滚动快就暂停加载图片，慢速滚动就继续加载图片
 */
class ImageRecyclerView(context: Context, attributeSet: AttributeSet?) : RecyclerView(context, attributeSet) {

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    Fresco.getImagePipeline().resume()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //根据纵向一次滚动的Y轴偏移量来判断这一次滚动的速度是多大，定一个中间值来判断这一次的速度是快还是慢
                if (abs(dy) < 80) {
                    Fresco.getImagePipeline().resume()
                } else {
                    Fresco.getImagePipeline().pause()
                }
            }
        })
    }
}