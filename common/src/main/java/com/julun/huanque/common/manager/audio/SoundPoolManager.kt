package com.julun.huanque.common.manager.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import com.julun.huanque.common.suger.logger

class SoundPoolManager private constructor() {

    companion object {
        private val TAG = SoundPoolManager::class.java.simpleName
        private var mSound: SoundPoolManager? = null
        val instance: SoundPoolManager
            get() {
                synchronized(SoundPoolManager::class.java) {
                    if (mSound == null) {
                        mSound = SoundPoolManager()
                    }
                }
                return mSound!!
            }
    }

    private var mSoundPool: SoundPool
    private var isLoadC = false
    private val idCache: MutableMap<String, Int> = mutableMapOf()
    private val sidCache: MutableList<Int> = mutableListOf()

    private inner class MyOnLoadCompleteListener : SoundPool.OnLoadCompleteListener {
        override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
            isLoadC = true
        }
    }

    /**
     * 加载指定资源
     *
     * @param name
     * @param path
     */
    fun loadR(name: String, path: String?) {
        if (checkSoundPool()) {
            if (!idCache.containsKey(name)) {
                idCache[name] = mSoundPool.load(path, 1)
            }
        }
    }

    /**
     * 加载指定路径列表的资源
     *
     * @param map
     */
    fun loadR(map: Map<String, String>) {
        val entries = map.entries
        for ((key, value) in entries) {
            if (checkSoundPool()) {
                if (!idCache.containsKey(key)) {
                    idCache[key] = mSoundPool.load(value, 1)
                }
            }
        }
    }

    /**
     * 加载指定AssetFileDescriptor的资源
     *
     * @param name
     * @param afd
     */
    fun loadRF(name: String, afd: AssetFileDescriptor) {
        if (checkSoundPool()) {
            if (!idCache.containsKey(name)) {
                idCache[name] = mSoundPool.load(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), 1)
            }
        }
    }

    /**
     * 加载指定AssetFileDescriptor列表的资源
     *
     * @param map
     */
    fun loadRF(map: Map<String, AssetFileDescriptor>) {
        val entries: Set<Map.Entry<String, AssetFileDescriptor>> = map.entries
        for ((key, value) in entries) {
            if (checkSoundPool()) {
                if (!idCache.containsKey(key)) {
                    idCache[key] =
                        mSoundPool.load(value.getFileDescriptor(), value.getStartOffset(), value.getLength(), 1)
                }
            }
        }
    }

    /**
     * 加载指定列表资源
     *
     * @param context
     * @param map
     */
    fun loadR(context: Context?, map: Map<String, Int>) {
        val entries = map.entries
        for ((key, value) in entries) {
            if (checkSoundPool()) {
                if (!idCache.containsKey(key)) {
                    idCache[key] = mSoundPool.load(context, value, 1)
                    logger("loadR: $key")
                    logger("loadR------------" + idCache.size)
                }
            }
        }
    }

    /**
     * 加载单个音频
     *
     * @param context
     * @param name
     * @param res
     */
    fun loadR(context: Context?, name: String, res: Int) {
        if (checkSoundPool()) {
            if (!idCache.containsKey(name)) {
                idCache[name] = mSoundPool.load(context, res, 1)
            }
        }
    }

    /**
     * 播放指定音频，并返用于停止、暂停、恢复的StreamId
     *
     * @param name
     * @param times
     * @return
     */
    fun play(name: String, times: Int = 0): Int {
//        logger("play: ------------------------：$name")
        return this.play(name, 1f, 1f, 1, times, 1f)
    }

    /**
     * 播放指定音频，并指定播放次数和频率
     *
     * @param name
     * @param times
     * @param rate
     * @return
     */
    fun play(name: String, times: Int, rate: Float): Int {
        return this.play(name, 1f, 1f, 1, times, rate)
    }

    /**
     * 播放指定音频，并指定优先级和播放频率
     *
     * @param name
     * @param property
     * @param times
     * @param rate
     * @return
     */
    fun play(name: String, times: Int, property: Int, rate: Float): Int {
        return this.play(name, 1f, 1f, property, times, rate)
    }

    /**
     * 播放指定音频，并指定左右声道、优先级、播放次数、播放频率
     *
     * @param name
     * @param leftVolume
     * @param rightVolume
     * @param property
     * @param times
     * @param rate
     * @return
     */
    fun play(
        name: String,
        leftVolume: Float,
        rightVolume: Float,
        property: Int,
        times: Int,
        rate: Float
    ): Int {
        var streamId = -1
        if (checkSoundPool()) {
//           logger( "play: " + name);
            if (idCache.containsKey(name) && isLoadC) {
//               logger( "name:" + idCache.get(name));
                streamId = mSoundPool.play(idCache[name]!!, leftVolume, rightVolume, property, times, rate)
//                logger("streadmId:" + streamId);
                sidCache.add(streamId)
            }
        }
        return streamId
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表
     *
     * @param names
     * @param times
     * @return
     */
    fun play(names: List<String>, times: Int): List<Int> {
        return this.play(names, 1f, 1f, 1, times, 1f)
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定次数和频率
     *
     * @param names
     * @param times
     * @param rate
     * @return
     */
    fun play(names: List<String>, times: Int, rate: Float): List<Int> {
        return this.play(names, 1f, 1f, 1, times, rate)
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定所有参数
     *
     * @param names
     * @param leftVolume
     * @param rightVolume
     * @param property
     * @param times
     * @param rate
     * @return
     */
    fun play(
        names: List<String>,
        leftVolume: Float,
        rightVolume: Float,
        property: Int,
        times: Int,
        rate: Float
    ): List<Int> {
        val streamIds: MutableList<Int> = ArrayList()
        if (checkSoundPool()) {
            for (name in names) {
                if (idCache.containsKey(name) && isLoadC) {
                    val a: Int = mSoundPool.play(idCache[name]!!, leftVolume, rightVolume, property, times, rate)
                    streamIds.add(a)
                    sidCache.add(a)
                }
            }
        }
        return streamIds
    }

    /**
     * 停止指定id音频
     */
    fun stop(r: Int) {
        if (checkSoundPool()) {
            mSoundPool.stop(r)
        }
    }

    /**
     * 停止指定列表音频
     */
    fun stopAll() {
        if (checkSoundPool()) {
            for (r in sidCache) {
                mSoundPool.stop(r)
            }
        }
    }

    /**
     * 暂停指定音效
     *
     * @param r
     */
    fun pause(r: Int) {
        if (checkSoundPool()) {
            mSoundPool.pause(r)
        }
    }

    /**
     * 暂停指定列表音频
     *
     * @param list
     */
    fun pause(list: List<Int>) {
        if (checkSoundPool()) {
            for (r in list) {
                mSoundPool.pause(r)
            }
        }
    }

    /**
     * 暂停所有音效
     */
    fun pauseAll() {
        mSoundPool.autoPause()
    }

    /**
     * 恢复指定音频播放
     *
     * @param r
     */
    fun resume(r: Int) {
        if (checkSoundPool()) {
            mSoundPool.resume(r)
        }
    }

    /**
     * 恢复指定列表的音频
     *
     * @param list
     */
    fun resume(list: List<Int>) {
        if (checkSoundPool()) {
            for (r in list) {
                mSoundPool.resume(r)
            }
        }
    }

    /**
     * 恢复所有暂停的音频
     */
    fun resumeAll() {
        if (checkSoundPool()) {
            mSoundPool.autoResume()
        }
    }

    /**
     * 卸载指定音频
     *
     * @param name
     */
    fun unLoad(name: String) {
        if (checkSoundPool()) {
            if (idCache.containsKey(name)) {
                mSoundPool.unload(idCache.get(name) ?: return)
                idCache.remove(name)
            }
        }
    }

    /**
     * 卸载指定列表的音频
     *
     * @param names
     */
    fun unLoad(names: List<String>) {
        if (checkSoundPool()) {
            for (name in names) {
                if (idCache.containsKey(name)) {
                    mSoundPool.unload(idCache[name] ?: return)
                    idCache.remove(name)
                }
            }
        }
    }

    /**
     * 释放所有资源，如果想继续播放，需要重新加载资源
     */
    fun release() {
        if (checkSoundPool()) {
            mSoundPool.release()
            idCache.clear()
        }
    }

    private fun checkSoundPool(): Boolean {
        return mSoundPool != null
    }


    init {
        mSoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val aab = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();
            SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(aab)
                .build();
        } else {
            SoundPool(5, AudioManager.STREAM_MUSIC, 8);
        }
        mSoundPool.setOnLoadCompleteListener(MyOnLoadCompleteListener())
    }
}