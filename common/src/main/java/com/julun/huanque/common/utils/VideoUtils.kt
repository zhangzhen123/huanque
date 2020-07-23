package com.julun.huanque.common.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.util.Log
import com.julun.huanque.common.suger.logger
import java.io.File


/**
 * @author zhangzhen
 * @data 2019/2/16
 */
object VideoUtils {
    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     * 其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    fun getVideoThumbnail(videoPath: String, width: Int = 0, height: Int = 0, kind: Int = MediaStore.Images.Thumbnails.MINI_KIND): Bitmap? {
        var bitmap: Bitmap? = null
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind)
        if (bitmap != null && width != 0 && height != 0) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        }
        return bitmap
    }

    fun getVideoWH(videoPath: String): Array<Int>? {
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(videoPath)

//            val duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)
            val swidth = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
            val sheight = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
            val width = swidth.toFloat()
            val height = sheight.toFloat()
            return arrayOf(width.toInt(), height.toInt())
        } catch (ex: Exception) {
            Log.e("TAG", "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release()
        }
        return null
    }

    /**
     * 获取视频首帧预览图
     * @param videoFile
     * @return
     */
    fun getVideoFirstFrame(videoFile: File): Bitmap? {
        if (!videoFile.exists()) {
            logger("视频文件不存在")
            return null
        }
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFile.absolutePath)
        return retriever.frameAtTime
    }
    /**
     * 返回视频播放总时长
     * @param videoFile
     * @return
     */
    fun getVideoTotalTime(videoFile: File): Long? {
        if (!videoFile.exists()) {
            logger("视频文件不存在")
            return null
        }
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFile.absolutePath)
        val timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return java.lang.Long.valueOf(timeString)
    }
}
