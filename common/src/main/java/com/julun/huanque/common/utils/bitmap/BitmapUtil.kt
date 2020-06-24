package com.julun.huanque.common.utils

import android.graphics.Bitmap
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
}