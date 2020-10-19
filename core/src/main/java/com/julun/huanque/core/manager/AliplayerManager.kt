package com.julun.huanque.core.manager

import android.view.SurfaceHolder
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.nativeclass.TrackInfo
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.BuildConfig
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/26 14:33
 *@描述 单例的阿里播放器，直播间主流和悬浮窗使用
 */
object AliPlayerManager {
    //阿里播放器
    val mAliPlayer: AliPlayer by lazy { AliPlayerFactory.createAliPlayer(CommonInit.getInstance().getContext()) }

    //log输出标识位
    private var mLogEnable = true

    private val logger = ULog.getLogger("AliPlayerManager")

//    var mRenderListener: IPlayer.OnRenderingStartListener? = null

    //SurfaceHolder.Callback唯一
//    private val mHolderCallback: SurfaceHolder.Callback by lazy {
//        object : SurfaceHolder.Callback {
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                logger.info("surfaceCreated=${holder.hashCode()}")
//                mAliPlayer.setDisplay(holder)
//                //防止黑屏
//                mAliPlayer.redraw()
//            }
//
//            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//                mAliPlayer.redraw()
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                logger.info("surfaceDestroyed=${holder.hashCode()}")
////                mAliPlayer.setDisplay(null)
//            }
//        }
//    }
    //当前播放的流地址
//    var mUrl = ""

    //调用了stop方法
    var stoped = false

    //是否渲染了首帧
    var mRendered = false

    init {
        logger.info("AliPlayerManager init")
        initPlayer()
    }

    /**
     * 初始化播放器
     */
    private fun initPlayer() {
//        mAliPlayer.enableLog(false)
        mAliPlayer.setOnCompletionListener {
            //播放完成事件
        }
        mAliPlayer.setOnErrorListener {
            //出错事件
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 出错事件 =${it.code}")
            }
//            Observable.timer(2, TimeUnit.SECONDS)
//                    .bindToLifecycle(this)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({ playStream() }, {}, {})

        }
        mAliPlayer.setOnPreparedListener {
            //准备成功事件
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 准备成功事件")
            }
        }
        mAliPlayer.setOnVideoSizeChangedListener { width, height ->
            //视频分辨率变化回调
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 视频分辨率变化回调")
            }
            mAliPlayer.redraw()
        }
//        mAliPlayer.setOnRenderingStartListener {
//            //首帧渲染显示事件,隐藏封面
//            mRendered = true
//            mRenderListener?.onRenderingStart()
//        }
        mAliPlayer.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
//            if (it.code == InfoCode.AutoPlayStart) {
            if (mLogEnable) {
//                 logger.info("${this} DXCPlayer code = ${it.code},msg = ${it.extraMsg}")
            }
//            }
        }
        mAliPlayer.setOnTrackChangedListener(object : IPlayer.OnTrackChangedListener {
            override fun onChangedSuccess(trackInfo: TrackInfo) {
                //切换音视频流或者清晰度成功
            }

            override fun onChangedFail(trackInfo: TrackInfo, errorInfo: ErrorInfo) {
                //切换音视频流或者清晰度失败
            }
        })
        mAliPlayer.setOnStateChangedListener {
            //播放器状态改变事件
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 播放器状态改变事件 it = $it")
            }
            if (it == IPlayer.completion || it == IPlayer.error) {
                //CDN切换的时候会触发此回调，重新调用播放方法
                if (stoped) {
                    return@setOnStateChangedListener
                }
//                if (BuildConfig.DEBUG) {
//                    return@setOnStateChangedListener
//                }
                Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (!stoped) {
                            mAliPlayer.prepare()
                        }
                    }, {}, {})
            }
        }
        mAliPlayer.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL

//        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                created = true
//                mAliPlayer.setDisplay(holder)
//                //防止黑屏
//                mAliPlayer.redraw()
//            }
//
//            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//                mAliPlayer.redraw()
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                mAliPlayer.setDisplay(null)
//            }
//        })
        //禁用硬解码
//        mAliPlayer.enableHardwareDecoder(false)
        val config = mAliPlayer.config ?: return
        config.mNetworkTimeout = 2000;
        config.mNetworkRetryCount = 100;
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 5000;
// 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
        config.mMaxBufferDuration = 50000;
//高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
        config.mHighBufferDuration = 3000;
// 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
        config.mStartBufferDuration = 500;
        mAliPlayer.config = config
    }

    fun stop() {
//        mUrl = ""
        logger.info("stop")
        mRendered = false
        mAliPlayer.stop()
        stoped = true
    }

    //彻底关闭播放器 并且断开播放器与视图的一切关联 当直播间和悬浮窗都关闭时调用
    fun destroy() {
        logger.info("destroy")
        mRendered = false
        mAliPlayer.stop()
        stoped = true
        mAliPlayer.setDisplay(null)
        mAliPlayer.setOnRenderingStartListener(null)
    }

    /**
     * 静音
     */
    fun soundOff() {
//        mAgoaraMediaPlayer?.adjustPlaybackSignalVolume(0)
        mAliPlayer.isMute = true
    }

    /**
     * 开启声音
     */
    fun soundOn() {
//        mAgoaraMediaPlayer?.adjustPlaybackSignalVolume(400)
        mAliPlayer.isMute = false
    }
}