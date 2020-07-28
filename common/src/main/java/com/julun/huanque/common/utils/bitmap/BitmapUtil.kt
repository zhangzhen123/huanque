package com.julun.huanque.common.utils.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created by djp on 2016/11/24.
 */

object BitmapUtil {

    fun bitmap2Bytes(bitmap: Bitmap): ByteArray {
        var byteArray: ByteArray? = null
        var baos: ByteArrayOutputStream? = null
        try {
            baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
            byteArray = baos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (baos != null) {
                baos.close()
            }
        }
        return byteArray!!
    }
    /**
     * bitmap转化成指定大小的byte[]
     */
    fun bitmap2AssignBytes(bitmap: Bitmap, size: Int): ByteArray {
        var byteArray: ByteArray? = null
        var baos: ByteArrayOutputStream? = null
        try {
            var options = 100
            baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            //图片原始大小
            val originalSize = baos.toByteArray()?.size ?: 0
            //是否继续执行压缩
            var isStartCompress = originalSize / 1024 > size
            while (isStartCompress && (baos.toByteArray()?.size ?: 0) / 1024 > size) {
                baos.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
                if (options <= 0) {
                    //避免死循环
                    isStartCompress = false
                } else {
                    options -= 10
                    if (options <= 0) {
                        options = 0
                    }
                }
            }
            byteArray = baos.toByteArray()
//            logger.info("atLastSize -> ${byteArray.size}")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            baos?.close()
        }
        return byteArray!!
    }
    /**
     * Bitmap转换成byte[]并且进行压缩,压缩到不大于maxkb
     *
     * @param bitmap
     * @param maxKb
     * @return
     */
    fun bmpToByteArray(bitmap: Bitmap, maxKb: Int): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        var options = 100
        while (output.toByteArray().size > maxKb && options != 10) {
            output.reset() //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output)//这里压缩options%，把压缩后的数据存放到output中
            options -= 10
        }
        return output.toByteArray()
    }


    /**
     * view转bitmap
     */
    fun viewConversionBitmap(v: View): Bitmap {
        val w: Int = v.width
        val h: Int = v.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        /** 如果不设置canvas画布为白色，则生成透明  */
        v.layout(0, 0, w, h)
        v.draw(c)
        return bmp
    }
}