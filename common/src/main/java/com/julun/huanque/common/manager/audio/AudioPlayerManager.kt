package com.julun.huanque.common.manager.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import java.io.File
import java.io.IOException

/**
 * 播放音频文件工具类
 */
class AudioPlayerManager {
    /**
     * 获取当前播放资源类型
     * @return 当前资源类型
     */
    var musicType = -1 //判断
        private set
    private var duration = 0

    /**
     * 是否在播放中
     * @return true 正在播放，false 停止播放
     */
    var isPlaying = false
        private set

    //播放文件的路径
    private var targetFile: File? = null

    //播放raw媒体源
    private var rawId = 0
    private var mContext: Context? = null

    //播放assets媒体源
    private var assetsName: String? = null

    //播放网络资源
    private var netPath: String? = null

    /**
     * 获取MediaPlayer对象
     * @return MediaPlayer
     */
    var mediaPlayer: MediaPlayer? = null
        private set
    private var mediaPlayFunctionListener: MediaPlayFunctionListener? = null
    private var mMediaPlayInfoListener: MediaPlayInfoListener? = null

    //多久获取一次进度 默认500毫秒
    private var sleep = 500
    private val mHandler: Handler by lazy {
        Handler(Handler.Callback { msg -> //停止前发送进度.
            if (mMediaPlayInfoListener != null && mediaPlayer != null && isPlaying) mMediaPlayInfoListener!!.onSeekBarProgress(
                mediaPlayer!!.currentPosition
            )
            if (msg.what == IS_PLAY_STOP) {
                mHandler.removeCallbacks(mRunnable)
            }
            false
        })
    }
    private val mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mMediaPlayInfoListener != null && mediaPlayer != null && isPlaying) mMediaPlayInfoListener!!.onSeekBarProgress(
                mediaPlayer!!.currentPosition
            )
            mHandler.postDelayed(this, sleep.toLong())
        }
    }

    constructor() {}
    constructor(context: Context?) {
        mContext = context
    }

    /**
     * 设置文件路径
     * @param file 需要播放文件的路径
     */
    fun setFilePlay(file: File?) {
        targetFile = file
        musicType = PLAY_STATE0
        stop()
    }

    /**
     * 设置Raw播放
     * @param rawId R.raw.music3
     */
    fun setRawPlay(context: Context?, rawId: Int) {
        mContext = context
        this.rawId = rawId
        musicType = PLAY_STATE1
        stop()
    }

    /**
     * 设置Raw播放
     * 调用此方法必须在初始化时传入Context
     * @param rawId R.raw.music3
     */
    fun setRawPlay(rawId: Int) {
        if (mContext == null) {
            throw NullPointerException("Context Null")
        }
        this.rawId = rawId
        musicType = PLAY_STATE1
        stop()
    }

    /**
     * 设置Assets播放
     * @param assetsName assets文件名
     */
    fun setAssetsName(context: Context?, assetsName: String?) {
        mContext = context
        this.assetsName = assetsName
        musicType = PLAY_STATE2
        stop()
    }

    /**
     * 设置Assets播放
     * 调用此方法必须在初始化时传入Context
     * @param assetsName assets文件名
     */
    fun setAssetsName(assetsName: String?) {
        if (mContext == null) {
            throw NullPointerException("Context Null")
        }
        this.assetsName = assetsName
        musicType = PLAY_STATE2
        stop()
    }

    /**
     * 设置网络资源播放
     * @param netPath 网络音乐地址
     */
    fun setNetPath(netPath: String?) {
        this.netPath = netPath
        musicType = PLAY_STATE3
        stop()
    }

    /**
     * 开始播放
     * @return true 开始播放， false 播放错误
     */
    fun start(): Boolean {
        mediaPlayer = if (musicType == PLAY_STATE1) {
            MediaPlayer.create(mContext, rawId)
        } else {
            MediaPlayer()
        }
        try {
            when (musicType) {
                PLAY_STATE0 -> {
                    mediaPlayer!!.setDataSource(targetFile!!.absolutePath)
                    mediaPlayer!!.prepare()
                }
                PLAY_STATE1 -> {
                }
                PLAY_STATE2 -> {
                    val fileDescriptor = mContext!!.assets.openFd(assetsName!!)
                    mediaPlayer!!.setDataSource(
                        fileDescriptor.fileDescriptor,
                        fileDescriptor.startOffset,
                        fileDescriptor.length
                    )
                    mediaPlayer!!.prepare()
                }
                PLAY_STATE3 -> {
                    mediaPlayer!!.setDataSource(netPath)
                    mediaPlayer!!.prepareAsync()
                }
            }
            //播放完成自动停止
            mediaPlayer!!.setOnCompletionListener { mediaPlayer ->
                if (mMediaPlayInfoListener != null) mMediaPlayInfoListener!!.onCompletion(mediaPlayer)
                stop()
            }
            //准备完毕 自动播放
            mediaPlayer!!.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
                mHandler.postDelayed(mRunnable, sleep.toLong())
                duration = mediaPlayer.duration
                if (mediaPlayFunctionListener != null) {
                    mediaPlayFunctionListener!!.prepared()
                    mediaPlayFunctionListener!!.start()
                }
            }
            //播放错误监听
            mediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                    if (mMediaPlayInfoListener != null) {
                        mMediaPlayInfoListener!!.onError(mp, what, extra)
                    }
                    if (mediaPlayer != null) stop()
                    mHandler.removeCallbacks(mRunnable)
                    return false
                }
            })
            //网络缓冲监听
            mediaPlayer!!.setOnBufferingUpdateListener { mp, percent ->
                if (mMediaPlayInfoListener != null) mMediaPlayInfoListener!!.onBufferingUpdate(
                    mp,
                    percent
                )
            }
            //调整进度监听
            mediaPlayer!!.setOnSeekCompleteListener { mp ->
                if (mMediaPlayInfoListener != null) mMediaPlayInfoListener!!.onSeekComplete(
                    mp
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            stop()
        }
        val result = mediaPlayer != null
        isPlaying = result
        return result
    }

    /**
     * 停止播放
     */
    fun stop() {
        isPlaying = false
        duration = 0
        if (mediaPlayer != null) {
            val message = Message()
            message.what = IS_PLAY_STOP
            mHandler.sendMessage(message)
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayFunctionListener?.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    /**
     * resume调用 继续播放也调用此方法即可
     */
    fun resume() {
        if (mediaPlayer != null) {
            isPlaying = true
            mHandler.postDelayed(mRunnable, sleep.toLong())
            mediaPlayer!!.start()
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        isPlaying = false
        mHandler.removeCallbacks(mRunnable)
        if (mediaPlayer != null) {
            mediaPlayer!!.pause()
            mediaPlayFunctionListener?.pause()
        }
    }

    /**
     * 是否正在运行
     * @return true 正在运行，false停止运行
     */
    val isRunning: Boolean
        get() = mediaPlayer != null

    /**
     * 播放文件的时长
     * @return 文件时长
     */
    fun getDuration(): Int {
        return if (mediaPlayer == null) {
            duration
        } else mediaPlayer!!.duration
    }

    /**
     * 获取当前播放位置
     * @return 当前播放位置值
     */
    val currentPosition: Int
        get() = if (mediaPlayer == null) {
            0
        } else mediaPlayer!!.currentPosition

    /**
     * 左右声道大小
     * @param leftVolume 左声道大小 0 - 1
     * @param rightVolume 右声道大小 0 - 1
     */
    fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (mediaPlayer != null) {
            mediaPlayer!!.setVolume(leftVolume, rightVolume)
        }
    }

    /**
     * 设置唤醒方式 需要在清单文件AndroidManifest.xml中添加权限 <uses-permission android:name="android.permission.WAKE_LOCK"></uses-permission>
     * @param context 上下文
     * @param mode 唤醒模式
     */
    fun setWakeMode(context: Context?, mode: Int) {
        if (mediaPlayer != null) mediaPlayer!!.setWakeMode(context, mode)
    }

    /**
     * 播放时不熄屏
     * @param screenOn true 不息屏，false 息屏
     */
    fun setScreenOnWhilePlaying(screenOn: Boolean) {
        if (mediaPlayer != null) mediaPlayer!!.setScreenOnWhilePlaying(screenOn)
    }

    /**
     * 指定播放位置 毫秒
     * @param msec 要播放的值
     */
    fun seekTo(msec: Int) {
        if (mediaPlayer != null) mediaPlayer!!.seekTo(msec)
    }

    /**
     * 是否循环播放
     * @param looping true 循环播放，false 不循环
     */
    fun setLooping(looping: Boolean) {
        if (mediaPlayer != null) mediaPlayer!!.isLooping = looping
    }

    /**
     * 必须调用此方法 销毁，释放
     */
    fun destroy() {
        stop()
    }

    /**
     * 多久获取一次进度 毫秒
     * @param sleep 默认500
     */
    fun setSleep(sleep: Int) {
        this.sleep = sleep
    }

    /**
     * 功能监听
     */
    fun setMediaPlayFunctionListener(mediaPlayFunctionListener: MediaPlayFunctionListener?) {
        this.mediaPlayFunctionListener = mediaPlayFunctionListener
    }

    /**
     * 播放信息监听
     */
    fun setMediaPlayInfoListener(mediaPlayInfoListener: MediaPlayInfoListener?) {
        mMediaPlayInfoListener = mediaPlayInfoListener
    }

    companion object {
        const val PLAY_STATE0 = 1 //文件
        const val PLAY_STATE1 = 2 //raw
        const val PLAY_STATE2 = 3 //assets
        const val PLAY_STATE3 = 4 //网络
        const val IS_PLAY_STOP = 100 //是否停止播放
    }
}