package com.julun.huanque.message.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.nativeclass.CacheConfig
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ImageUtils


/**
 *@创建者   dong
 *@创建时间 2020/9/2 20:32
 *@描述 私信播放动画使用的ViewModel
 */
class PrivateAnimationViewModel : BaseViewModel() {
    //动画播放成功标识位
    val animationEndFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //需要播放的礼物列表
    val giftList = mutableListOf<ChatGift>()

    //音效准备成功标识位
    var voicePreparedFlag = false

    //动画下载成功标记位
    var bitmapPreparedFlag = false

    //音效播放完成标识位
    val voiceCompleteFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //弹窗隐藏标识位
    val dismissState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //需要播放动画的礼物对象
    val giftData: MutableLiveData<ChatGift> by lazy { MutableLiveData<ChatGift>() }

    //资源准备成功的标识位
    val preparedFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //播放器
    private var mAliPlayer = AliPlayerFactory.createAliPlayer(CommonInit.getInstance().getContext())

    init {
        val config = mAliPlayer.config
        config.mNetworkTimeout = 2000;
        config.mNetworkRetryCount = 100;
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 3000;

// 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
        config.mMaxBufferDuration = 50000;
//高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
//        config.mHighBufferDuration = 3000;
// 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
        config.mStartBufferDuration = 50000;
        mAliPlayer?.config = config


        //设置缓存相关
        val cacheConfig = CacheConfig()
        //开启缓存功能
        cacheConfig.mEnable = true
        //能够缓存的单个文件最大时长。超过此长度则不缓存
        cacheConfig.mMaxDurationS = 1000
//        //缓存目录的位置
        cacheConfig.mDir = "${FileUtils.getCachePath(CommonInit.getInstance().getApp())}/gift/voice"
        //缓存目录的最大大小。超过此大小，将会删除最旧的缓存文件
        cacheConfig.mMaxSizeMB = 500
//        //设置缓存配置给到播放器
        mAliPlayer.setCacheConfig(cacheConfig)


        mAliPlayer.setOnCompletionListener {
            //播放完成事件
            logger("Message 音效播放完成 duration = ${System.currentTimeMillis() - voiceStartTime} time = ${System.currentTimeMillis()}")
            voiceCompleteFlag.postValue(true)
        }

        mAliPlayer.setOnPreparedListener {
            //准备成功事件
            logger("Message 音效准备成功事件 time = ${System.currentTimeMillis()}")
            voicePreparedFlag = true
            preparedFlag.value = judgePrepared()
            logger("Private  voice dur = ${mAliPlayer.duration}")
        }
    }

    /**
     * 准备资源
     */
    fun prepareResource(gift: ChatGift) {
        logger("Message 开始准备资源")
        voicePreparedFlag = false
        bitmapPreparedFlag = false
        val specialParams = gift.specialParams
        when (gift.specialType) {
//            ChatGift.Sound -> {
//                //音效动画
//                prepareVoice(StringHelper.getOssAudioUrl(specialParams.bgm))
//            }
            ChatGift.Screen -> {
                //飘屏动画
                preparedFlag.value = true
            }
            ChatGift.Sound, ChatGift.Animation -> {
                //动画类型
                prepareVoice(StringHelper.getOssAudioUrl(specialParams.bgm))
                prepareAnimation(StringHelper.getOssImgUrl(specialParams.webpUrl))
            }
            else -> {
                //普通礼物动画
                prepareAnimation(StringHelper.getOssImgUrl(specialParams.webpUrl))
            }
        }
    }

    /**
     * 判断资源是否准备成功
     */
    private fun judgePrepared(): Boolean {
        val gift = giftData.value ?: return false
        when (gift.specialType) {
//            ChatGift.Sound -> {
//                return voicePreparedFlag
//            }
            ChatGift.Sound, ChatGift.Animation -> {
                val params = gift.specialParams
                if (params.bgm.isNotEmpty() && params.webpUrl.isNotEmpty()) {
                    return voicePreparedFlag && bitmapPreparedFlag
                }
                if (params.bgm.isEmpty()) {
                    return bitmapPreparedFlag
                }
                if (params.webpUrl.isEmpty()) {
                    return voicePreparedFlag
                }
                return false
            }
            ChatGift.Normal -> {
                //普通礼物，准备动画资源
                return bitmapPreparedFlag
            }
            else -> {
                return true
            }
        }
    }

    /**
     * 准备图片资源
     */
    private fun prepareAnimation(picUrl: String) {
        ImageUtils.requestImageForBitmap(picUrl, {
            //图片下载成功
            logger("Message 图片下载成功")
            bitmapPreparedFlag = true
            preparedFlag.postValue(judgePrepared())
        })
    }

    /**
     * 开始消费动画礼物
     */
    fun startConsumeGift(fragment: Fragment? = null) {
        logger("Message state = ${fragment?.lifecycle?.currentState},atLeast = ${fragment?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.CREATED)}")
        if (fragment?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.CREATED) == true || giftData.value != null) {
            //弹窗处于显示当中或者正在消费礼物动画
            return
        } else {
            //弹窗未处于显示当中,并且没有处于消费礼物流程当中，开始消费礼物动画
            if (ForceUtils.isIndexNotOutOfBounds(0, giftList)) {
                val tempData = giftList.removeAt(0)
                giftData.value = tempData
            }
        }

    }

    /**
     * 开始播放音效
     */
    fun prepareVoice(url: String) {
        val urlSource = UrlSource()
        urlSource.uri = url
        mAliPlayer?.isAutoPlay = false
        mAliPlayer?.setDataSource(urlSource)
        mAliPlayer?.prepare()
    }

    var voiceStartTime = 0L
    fun startPlayer() {
        mAliPlayer.start()
        voiceStartTime = System.currentTimeMillis()
        logger("Message 音效开始播放 voiceStartTime = $voiceStartTime")
    }

    /**
     * 停止播放
     */
    fun stopPlayer() {
        mAliPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        mAliPlayer.release()
    }

}