package com.julun.huanque.common.utils.bitmap

import android.graphics.*

/**
 * Created by dong on 2018/3/28.
 */
object DarkUtil {
    /**
     * 改变图片对比度,达到使图片明暗变化的效果
     *
     * @param srcBitmap source bitmap
     * @param contrast  图片亮度，0：全黑；小于1，比原图暗；1.0f原图；大于1比原图亮
     * @return bitmap
     */
    fun darkBitmap(srcBitmap: Bitmap, contrast: Float): Bitmap {

        val offset = 0.0.toFloat() //picture RGB offset

        val imgHeight: Int = srcBitmap.height
        val imgWidth: Int = srcBitmap.width

        val bmp = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.RGB_565)
        val cMatrix = ColorMatrix()
        cMatrix.set(floatArrayOf(contrast, 0f, 0f, 0f, offset, 0f, contrast, 0f, 0f, offset, 0f, 0f, contrast, 0f, offset, 0f, 0f, 0f, 1f, 0f))

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cMatrix)

        val canvas = Canvas(bmp)
        canvas.drawBitmap(srcBitmap, 0f, 0f, paint)

        return bmp
    }
}