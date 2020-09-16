package com.julun.huanque.common.utils.fresco

import android.graphics.Bitmap
import com.facebook.cache.common.CacheKey
import com.facebook.cache.common.SimpleCacheKey
import com.facebook.imagepipeline.request.BasePostprocessor
import com.julun.huanque.common.utils.bitmap.BitmapUtil

/**
 * 对bitmap的像素的颜色进行修改
 */
class ColorPostprocessor(var colors: IntArray? = null) : BasePostprocessor() {

    private var mCacheKey: CacheKey? = null
//    override fun process(
//        bitmap: Bitmap,
//        bitmapFactory: PlatformBitmapFactory
//    ): CloseableReference<Bitmap> {
//        logger("process1=s=${bitmap.hashCode()}")
//        return super.process(bitmap, bitmapFactory)
    //这个是直接修改像素点为指定颜色值 简单暴力 但是效果不好会有毛边锯齿
//        val width: Int = bitmap.width //获取位图的宽
//
//        val height: Int = bitmap.height //获取位图的高
//
//        val pixels = IntArray(width * height) //通过位图的大小创建像素点数组
//
//        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                val color = pixels[width * i + j]
//                val colorAlpha: Int = color ushr 24//获取颜色值的alpha值
//                if(colorAlpha>=30){
//                    pixels[width * i + j] = 0x80000000.toInt()
//                }
//            }
//        }
//        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    //使用着色器实现
//        val rb: Bitmap? = BitmapUtil.addGradient(bitmap.copy(Bitmap.Config.ARGB_8888,true), colors)
//        logger("bitmap=${bitmap.byteCount} rb=${rb?.byteCount}")
//        return bitmapFactory.createBitmap(rb)

//    }

//    override fun process(destBitmap: Bitmap, sourceBitmap: Bitmap?) {
//        logger("process2=d=${destBitmap.hashCode()} s=${sourceBitmap.hashCode()}")
//        super.process(destBitmap, sourceBitmap)
//    }

    override fun process(bitmap: Bitmap) {
//        logger("process3=${bitmap.hashCode()}")
        BitmapUtil.addGradient(bitmap, colors)
    }

    override fun getPostprocessorCacheKey(): CacheKey? {
        if (mCacheKey == null) {
            var colorStr = "c"
            colors?.forEach {
                colorStr += it
            }
            val key: String = "ColorPostprocessor;${colorStr};"
            mCacheKey = SimpleCacheKey(key)
        }
//        logger("mCacheKey=${mCacheKey}")
        return mCacheKey
    }
}
