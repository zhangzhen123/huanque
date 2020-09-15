package com.julun.huanque.common.utils.fresco

import android.graphics.Bitmap
import android.graphics.Canvas
import com.facebook.common.references.CloseableReference
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory
import com.facebook.imagepipeline.request.BasePostprocessor

class MyTilePostprocessor : BasePostprocessor() {
    override fun process(
        bitmap: Bitmap,
        bitmapFactory: PlatformBitmapFactory
    ): CloseableReference<Bitmap> {

        val width: Int = bitmap.getWidth() //获取位图的宽

        val height: Int = bitmap.getHeight() //获取位图的高

        val pixels = IntArray(width * height) //通过位图的大小创建像素点数组

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val alpha = 0xFF shl 24
        for (i in 0 until height) {
            for (j in 0 until width) {
                var grey = pixels[width * i + j]
//                val red = grey and 0x00FF0000 shr 16
//                val green = grey and 0x0000FF00 shr 8
//                val blue = grey and 0x000000FF
//                grey = (red.toFloat() * 0.3 + green.toFloat() * 0.59 + blue.toFloat() * 0.11).toInt()
//                grey = alpha or (grey shl 16) or (grey shl 8) or grey
//                pixels[width * i + j] = grey
                if(grey!=0x00000000){
                    pixels[width * i + j] = 0x80000000.toInt()
                }
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmapFactory.createBitmap(bitmap)

    }
}
