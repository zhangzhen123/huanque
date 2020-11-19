package com.julun.huanque.common.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.SPUtils

/**
 *@创建者   dong
 *@创建时间 2020/9/5 17:18
 *@描述 本地音效管理类
 */
object VoiceManager {
    private var mAudioManager: AudioManager? = null

    private var mPlaybackAttributes: AudioAttributes? = null

    private var mFocusRequest: AudioFocusRequest? = null

    private var mListenerHandler: Handler? = null

    //音频播放器
    private var mPlayer: MediaPlayer? = null

    private val mAudioListener = AudioManager.OnAudioFocusChangeListener {
        logger("Voice focusChange = $it")
    }


    /**
     * 播放响铃
     */
    fun playRing() {
        playerAudio("ring.mp3")
    }

    /**
     * 播放结束音效
     */
    fun playFinish() {
        playerAudio("finish.mp3", false)
    }

    /**
     * 播放匹配音效
     */
    fun playMatch() {
        playerAudio("anonymous_match.mp3")

    }

    /**
     * 播放缘分来了音效
     */
    fun playYuanFen() {
        if (SPUtils.getBoolean(SPParamKey.Fate_Voice_Open, true)) {
            playerAudio("yuanfen.mp3", false, false)
        }
    }


    /**
     * 停止音效
     */
    fun stop() {
        checkAudioManageAndService()
        mPlayer?.stop()
    }

    /**
     * 页面销毁  释放MediaPlayer
     */
    fun destroy() {
        mPlayer?.stop()
        mPlayer?.release();
        mPlayer = null
        releaseVideoFocus()
    }


    /**
     * 释放焦点
     */
    fun releaseVideoFocus() {
        mAudioManager?.isSpeakerphoneOn = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFocusRequest?.let {
                mAudioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            mAudioManager?.abandonAudioFocus(
                mAudioListener
            )
        }
        //回复音效
        unmute()
    }


    /**
     * 播放音效
     */
    private fun playerAudio(audioName: String, loop: Boolean = true, quietOther: Boolean = true) {
        try {
            checkAudioManageAndService()
            if (quietOther) {
                basicSetting()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPlaybackAttributes = AudioAttributes.Builder()
                    .apply {
                        if (quietOther) {
                            setUsage(AudioAttributes.USAGE_ALARM)
                        } else {
                            setUsage(AudioAttributes.USAGE_MEDIA)
                        }
                    }
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                mPlayer?.setAudioAttributes(mPlaybackAttributes)
            } else {
                if (quietOther) {
                    mPlayer?.setAudioStreamType(AudioManager.STREAM_ALARM);//音量跟随闹钟音量
                } else {
                    mPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC);//音量跟随闹钟音量
                }
            }

            val afd = CommonInit.getInstance().getApp().assets.openFd(audioName)
            mPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mPlayer?.prepare()
            mPlayer?.isLooping = loop
            val mFocusLock = Any()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mListenerHandler = Handler()
                mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(mPlaybackAttributes!!)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(mAudioListener, mListenerHandler!!)
                    .build()

                // requesting audio focus
                val res = mAudioManager?.requestAudioFocus(mFocusRequest!!)
                synchronized(mFocusLock) {
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mPlayer?.start()
                    }
                }
            } else {
                val res = mAudioManager?.requestAudioFocus(
                    mAudioListener,  // Use the music stream.
                    AudioManager.STREAM_SYSTEM,  // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )

                synchronized(mFocusLock) {
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        mPlayer?.start()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检查参数是否没有初始化
     * 未初始化则直接初始化
     */
    private fun checkAudioManageAndService() {
        mAudioManager = mAudioManager ?: CommonInit.getInstance().getApp().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        mPlayer = mPlayer ?: MediaPlayer()
        mPlayer?.reset()
    }

    private fun basicSetting() {
        //设置扬声器播放
        mAudioManager?.mode = AudioManager.MODE_NORMAL
        mAudioManager?.isSpeakerphoneOn = true

        //静音其他音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND)
        } else {
            mAudioManager?.setStreamMute(AudioManager.STREAM_MUSIC, true)
        }
    }

    /**
     * 取消静音
     */
    fun unmute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //静音其他音效
            mAudioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND)
        } else {
            mAudioManager?.setStreamMute(AudioManager.STREAM_MUSIC, false)
        }
    }
}