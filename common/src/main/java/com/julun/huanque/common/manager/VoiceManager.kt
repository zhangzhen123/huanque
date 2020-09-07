package com.julun.huanque.common.manager

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.R
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.FileUtils
import java.io.File

/**
 *@创建者   dong
 *@创建时间 2020/9/5 17:18
 *@描述 本地音效管理类
 */
object VoiceManager {
    //阿里云播放器
    private val mVoicePlayer: AliPlayer = AliPlayerFactory.createAliPlayer(CommonInit.getInstance().getContext())

    //响铃地址
    private val RingUrl = "config/bgm/ring.mp3"

    //停止音效地址
    private val FinishUrl = "config/bgm/finish.mp3"

    //匹配音效的地址
    private val MatchUrl = "config/bgm/anonymous_match.mp3"

    init {

        val config = mVoicePlayer.config
        config.mNetworkTimeout = 2000;
        config.mNetworkRetryCount = 100;
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 3000;

// 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
        config.mMaxBufferDuration = 1000;
//高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
//        config.mHighBufferDuration = 3000;
// 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
        config.mStartBufferDuration = 50;
        mVoicePlayer?.config = config


        //设置缓存相关
        val cacheConfig = CacheConfig()
        //开启缓存功能
        cacheConfig.mEnable = true
        //能够缓存的单个文件最大时长。超过此长度则不缓存
        cacheConfig.mMaxDurationS = 2000
//        //缓存目录的位置
        cacheConfig.mDir = "${FileUtils.getCachePath(CommonInit.getInstance().getApp())}/chat/voice"
        //缓存目录的最大大小。超过此大小，将会删除最旧的缓存文件
        cacheConfig.mMaxSizeMB = 500
//        //设置缓存配置给到播放器
        mVoicePlayer.setCacheConfig(cacheConfig)

        mVoicePlayer.setOnPreparedListener {
            logger("voice preparedTime = ${System.currentTimeMillis()}")
        }
        mVoicePlayer.setOnRenderingStartListener {
            logger("voice RenderingTime = ${System.currentTimeMillis()}")
        }

    }


    /**
     * 播放响铃音效
     */
    fun startRing(autoPlayer: Boolean = true) {
        mVoicePlayer.isLoop = true
        val dataSource = UrlSource()
        dataSource.uri = StringHelper.getOssAudioUrl(RingUrl)
        mVoicePlayer.isAutoPlay = autoPlayer
        if (!autoPlayer) {
            //缓冲，不播放
            mVoicePlayer.setOnPreparedListener {
                startFinish(false)
            }
        } else {
            mVoicePlayer.setOnPreparedListener(null)
        }
        mVoicePlayer.setDataSource(dataSource)

        mVoicePlayer.prepare()
        logger("voice StartTime = ${System.currentTimeMillis()}")
    }

    /**
     * 播放结束音效
     */
    fun startFinish(autoPlayer: Boolean = true) {
        mVoicePlayer.isLoop = false
        val dataSource = UrlSource()
        dataSource.uri = StringHelper.getOssAudioUrl(FinishUrl)
        mVoicePlayer.isAutoPlay = autoPlayer
        if (!autoPlayer) {
            //缓冲，不播放
            mVoicePlayer.setOnPreparedListener {
                startMatch(false)
            }
        } else {
            mVoicePlayer.setOnPreparedListener(null)
        }
        mVoicePlayer.setDataSource(dataSource)
        mVoicePlayer.prepare()
    }

    /**
     * 播放匹配音效
     */
    fun startMatch(autoPlayer: Boolean = true) {
        mVoicePlayer.isLoop = true
        val dataSource = UrlSource()
        dataSource.uri = StringHelper.getOssAudioUrl(MatchUrl)
        mVoicePlayer.isAutoPlay = autoPlayer
        mVoicePlayer.setOnPreparedListener(null)
        mVoicePlayer.setDataSource(dataSource)
        mVoicePlayer.prepare()
    }

    /**
     * 停止所有音效
     */
    fun stopAllVoice() {
        mVoicePlayer.stop()
    }

}