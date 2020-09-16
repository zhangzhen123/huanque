package com.julun.huanque.common.utils.fresco

import android.graphics.Bitmap
import com.facebook.common.references.CloseableReference
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory
import com.facebook.imagepipeline.request.BasePostprocessor
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.bitmap.BitmapUtil

/**
 * 对bitmap的像素的颜色进行修改
 * 由于bitmap是又复用池的 所以不要对同一个bitmap处理多次 不然在设置带透明度的颜色时会越来越透明 最后看不见
 */
class ColorPostprocessor(var colors: IntArray? = null) : BasePostprocessor() {
    companion object {
        val bitmapHashSet: HashSet<Int> = hashSetOf()
    }

    override fun process(
        bitmap: Bitmap,
        bitmapFactory: PlatformBitmapFactory
    ): CloseableReference<Bitmap> {

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
        val rb: Bitmap?
        if (bitmapHashSet.contains(bitmap.hashCode())) {
            rb = bitmap
        } else {
            rb = BitmapUtil.addGradient(bitmap, colors)
            bitmapHashSet.add(bitmap.hashCode())
        }
//        logger("bitmap=${bitmap.hashCode()} rb=${rb.hashCode()}")
        return bitmapFactory.createBitmap(rb)

    }
}
