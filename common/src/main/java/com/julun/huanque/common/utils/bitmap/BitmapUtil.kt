package com.julun.huanque.common.utils.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.text.TextUtils
import android.util.Base64
import android.view.View
import androidx.annotation.ColorInt
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
    fun viewConversionBitmap(v: View,bgColor:Int=Color.WHITE): Bitmap {
        val w: Int = v.width
        val h: Int = v.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(bgColor)
        /** 如果不设置canvas画布为白色，则生成透明  */
        v.layout(0, 0, w, h)
        v.draw(c)
        return bmp
    }

    /**
     * bitmap转base64字符串
     */
    open fun bitmapToBase64(bitmap: Bitmap?): String? {
        var var1: String? = null
        var var2: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                var2 = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, var2)
                var2.flush()
                var2.close()
                val var3 = var2.toByteArray()
                var1 = Base64.encodeToString(var3, 0)
            }
        } catch (var11: IOException) {
            var11.printStackTrace()
        } finally {
            try {
                if (var2 != null) {
                    var2.flush()
                    var2.close()
                }
            } catch (var10: java.lang.Exception) {
            }
        }
        return var1
    }

    /**
     * base64字符串转bitmap
     */
    fun base64ToBitmap(baseStr: String): Bitmap? {
        var var0 = baseStr
        return if (TextUtils.isEmpty(var0)) {
            null
        } else {
            var0 = var0.replace(' ', '+')
            try {
                val var1 = Base64.decode(var0, 0)
                BitmapFactory.decodeByteArray(var1, 0, var1.size)
            } catch (var2: Exception) {
                var2.printStackTrace()
                null
            }
        }
    }
}