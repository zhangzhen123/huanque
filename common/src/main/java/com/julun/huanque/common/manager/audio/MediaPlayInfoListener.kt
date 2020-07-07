package com.julun.huanque.common.manager.audio

import android.media.MediaPlayer

/**
 * 获取当前信息的回调
 */
interface MediaPlayInfoListener {
    //播放错误监听
    fun onError(mp: MediaPlayer?, what: Int, extra: Int)

    //播放完成监听
    fun onCompletion(mediaPlayer: MediaPlayer?)

    //网络缓冲监听
    fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int)

    //进度调整监听
    fun onSeekComplete(mediaPlayer: MediaPlayer?)

    //时实播放进度
    fun onSeekBarProgress(progress: Int)
}