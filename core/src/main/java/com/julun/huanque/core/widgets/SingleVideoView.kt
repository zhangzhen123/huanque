package com.julun.huanque.core.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.View.OnClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.MicAnchor
import com.julun.huanque.common.interfaces.PlayStateListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliplayerManager
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

import kotlinx.android.synthetic.main.view_single_video.view.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2019/12/4 18:18
 *@描述  地址播放器使用的拉流view
 * @param useManager true 使用 AliplayerManager内的播放器    false  创建一个新的播放器
 */
class SingleVideoView(context: Context, attrs: AttributeSet?, val useManager: Boolean = false) : ConstraintLayout(context, attrs) {

    constructor(context: Context, useManager: Boolean = false) : this(context, null, useManager)
    constructor(context: Context, attrs: AttributeSet?) : this(context, null, false)

    //surfaceview是否已经创建
    private var created = false

    //播放地址
    private var mUrl = ""

    //阿里播放器
    private var mAliPlayer: AliPlayer? = null

    //是否处于使用中
    var isFree = true

    //播放状态监听
    var mPlayStateListener: PlayStateListener? = null

    var mOnVideoListener: OnVideoListener? = null

    //播放信息
    private var playerInfo: MicAnchor? = null
    private var mSetTime = 0L
    private var mPlayTime = 0L
    private var mAutoPlayTime = 0L

    //渲染成功时间
    private var mRenderTime = 0L


    private var mRenderListener = IPlayer.OnRenderingStartListener {
        mRenderTime = System.currentTimeMillis()
        //首帧渲染显示事件,隐藏封面
        if (mLogEnable) {
            ULog.i("${this} DXCPlayer 首帧渲染显示事件")
            ULog.i("${this} DXCPlayer  时间  started时间 = ${mPlayTime - mSetTime},renderTime = ${mRenderTime - mSetTime},mAutoPlayTime = ${mAutoPlayTime - mSetTime}")
        }
        posterImage.hide()
        mPlayStateListener?.onFirstRenderListener()
    }

    //log输出标识位
    private var mLogEnable = false


    //是否处于聊天模式(默认为false)
    var mChatMode = false

    init {
        context.let {
            LayoutInflater.from(it).inflate(R.layout.view_single_video, this)
        }
        anchor_info.onClickNew { }
        anchor_info.setOnClickListener(OnClickListener {
            if (playerInfo != null && playerInfo?.isAnchor != true) {
                mOnVideoListener?.onClickAuthorInfo(playerInfo ?: return@OnClickListener)
            }
        })
        initViewModel()
        if (useManager) {
            mAliPlayer = AliplayerManager.mAliPlayer
            AliplayerManager.mRenderListener = mRenderListener
            if (AliplayerManager.mRendered) {
                posterImage.hide()
            }

        } else {
            initPlayer()
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                created = true
                mAliPlayer?.setDisplay(holder)
                //防止黑屏
                mAliPlayer?.redraw()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mAliPlayer?.redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (!useManager) {
                    mAliPlayer?.setDisplay(null)
                }
            }
        })

//        if (BuildConfig.APP_TYPE.isEmpty()) {
//            //主包  显示水印
//            iv_watermark.show()
//        } else {
//            iv_watermark.hide()
//        }
    }

    private fun initViewModel() {
        val var10001 = this.context
        if (var10001 is BaseActivity) {
            //当前处于PlayerActivity
        }
    }

    /**
     * 播放
     * @param url 流地址
     * @param main 是否是主播放器
     */
    fun play(url: String, main: Boolean = false) {
        mSetTime = System.currentTimeMillis()
        if (mLogEnable) {
            ULog.i("DXCplayer  开始播放 时间 time = $mSetTime")
        }
//        if (!main) {
        //非主播放器，需要做校验
        if (url == mUrl) {
            return
        }
//        }
        isFree = false
        this.mUrl = url
        ULog.i("DXCplayer set 1")
        playStream()
    }

    /**
     * 释放流相关
     */
    fun release() {
        mUrl = ""
        if (!useManager) {
            mAliPlayer?.stop()
        }
        isFree = true
        playerInfo = null
        this.hide()
        anchor_info.visibility = View.GONE
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mAliPlayer?.pause()
    }

    /**
     * 恢复播放
     */
    fun resume() {
        ULog.i("DXCPlayer 恢复播放resume")
        if (mUrl.isNotEmpty()) {
            mAliPlayer?.start()
        }
    }

    /**
     * 关闭
     */
    fun stop() {
        if (!created) {
            return
        }
        mUrl = ""
        isFree = true
        mAliPlayer?.stop()
    }


    fun getStreamID(): String {
        return if (playerInfo != null) {
            playerInfo!!.streamID
        } else {
            ""
        }
    }

    fun getStreamUrl(): String {
        return if (useManager) {
            AliplayerManager.mUrl
        } else {
            mUrl
        }
    }

    /**
     * 显示封面
     */
    fun showCover(picUrl: String) {
        if (useManager && AliplayerManager.mRendered) {
            //播放器处于拉流状态，不显示封面
            return
        }
        posterImage.show()
//        ImageUtils.loadImage(posterImage, picUrl)
        ImageUtils.loadImageWithBlur(posterImage, picUrl, 3, 13)
    }

    /**
     * 初始化播放器
     */
    fun initPlayer() {
        ULog.i("PlayerLine 创建播放器")
        mAliPlayer = AliPlayerFactory.createAliPlayer(context.applicationContext)
        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
        ULog.i("PlayerLine 设置监听事件")
        mAliPlayer?.setOnCompletionListener {
            //播放完成事件
        }
        mAliPlayer?.setOnErrorListener {
            //出错事件
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 出错事件 =${it.code}")
            }
//            Observable.timer(2, TimeUnit.SECONDS)
//                    .bindToLifecycle(this)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({ playStream() }, {}, {})

        }
        mAliPlayer?.setOnPreparedListener {
            //准备成功事件
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 准备成功事件")
            }
        }
        mAliPlayer?.setOnVideoSizeChangedListener { width, height ->
            //视频分辨率变化回调
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 视频分辨率变化回调")
            }
            mAliPlayer?.redraw()
        }
        mAliPlayer?.setOnRenderingStartListener(mRenderListener)
        mAliPlayer?.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
            if (it.code == InfoCode.AutoPlayStart) {
                mAutoPlayTime = System.currentTimeMillis()
                if (mLogEnable) {
                    ULog.i("${this} DXCPlayer 自动播放回调")
                }
            }
//            if(it.code != InfoCode.CurrentPosition && it.code != InfoCode.BufferedPosition){
//                ULog.i("${this} DXCPlayer 其他信息的事件 code = ${it.code},msg = ${it.extraMsg},value = ${it.extraValue}")
//            }
        }
//        mAliPlayer?.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
//            override fun onLoadingEnd() {
//                ULog.i("${this} DXCPlayer onLoadingEnd")
//            }
//
//            override fun onLoadingBegin() {
//                ULog.i("${this} DXCPlayer onLoadingBegin")
//            }
//
//            override fun onLoadingProgress(p0: Int, p1: Float) {
//                ULog.i("${this} DXCPlayer onLoadingProgress")
//            }
//
//        })
        mAliPlayer?.setOnTrackChangedListener(object : IPlayer.OnTrackChangedListener {
            override fun onChangedSuccess(trackInfo: TrackInfo) {
                //切换音视频流或者清晰度成功
            }

            override fun onChangedFail(trackInfo: TrackInfo, errorInfo: ErrorInfo) {
                //切换音视频流或者清晰度失败
            }
        })
        mAliPlayer?.setOnStateChangedListener {
            //播放器状态改变事件
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 播放器状态改变事件 it = $it")
            }
            if (it == IPlayer.started) {
                mPlayTime = System.currentTimeMillis()
            }
            if (it == IPlayer.completion || it == IPlayer.error) {
                //CDN切换的时候会触发此回调，重新调用播放方法
                Observable.timer(2, TimeUnit.SECONDS)
                    .bindToLifecycle(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //                            ULog.i("DXCplayer set 2 it = $it")
                        playStream()
                    }, {}, {})
            }
            mPlayStateListener?.playState(it)
        }
        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL

        //禁用硬解码
//        mAliPlayer?.enableHardwareDecoder(false)
        val config = mAliPlayer?.config ?: return
        config.mNetworkTimeout = 2000;
        config.mNetworkRetryCount = 100;
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 3000;
// 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
//        config.mMaxBufferDuration = 50000;
//高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
//        config.mHighBufferDuration = 3000;
// 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
//        config.mStartBufferDuration = 500;
        mAliPlayer?.config = config
    }

    /**
     * 设置主播信息
     *
     * @param info
     */
    fun setPlayInfo(info: MicAnchor) {
        playerInfo = info
        if (!info.isAnchor) {
            if (info.isPK) {
                val visible = if (info.showInfo) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                anchor_info.visibility = visible

            } else {
                anchor_info.visibility = View.VISIBLE
            }
            if (!TextUtils.isEmpty(info.headPic) && anchor_header_image != null) {
                ImageUtils.loadImage(anchor_header_image, info.headPic, 24f, 24f)
            }
            if (anchor_name != null) {
                anchor_name?.text = info.programName
            }
        }
    }

    fun showPlayerInfo() {
        if (playerInfo != null && (playerInfo?.isAnchor != true)) {
            anchor_info.visibility = View.VISIBLE
        }
    }

    /**
     * 真正的播放方法
     */
    fun playStream() {
        //聊天模式 不需要播放视频   && NetUtils.isNetConnected()
        if (mUrl.isNotEmpty() && !mChatMode) {
            AliplayerManager.mRenderListener = mRenderListener
            val urlSource = UrlSource()
            urlSource.uri = mUrl
//            ULog.i("PlayerLine 创建播放源")
            mAliPlayer?.setDataSource(urlSource)
            mAliPlayer?.isAutoPlay = true
//            ULog.i("PlayerLine 准备播放器")
            if (useManager) {
                AliplayerManager.mUrl = mUrl
                AliplayerManager.mRendered = false
            }
            mAliPlayer?.prepare()
//            mAliPlayer?.start()
        }
//        ULog.i("DXCPlayer 开始播放playStream")
    }


    /**
     * 静音
     */
    fun soundOff() {
//        mAgoaraMediaPlayer?.adjustPlaybackSignalVolume(0)
        mAliPlayer?.isMute = true
    }

    /**
     * 开启声音
     */
    fun soundOn() {
//        mAgoaraMediaPlayer?.adjustPlaybackSignalVolume(400)
        mAliPlayer?.isMute = false
    }

    override fun toString(): String {
        return "SingleVideoView(mUrl='$mUrl StreamId=${playerInfo?.streamID}')"
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        surfaceView?.visibility = visibility
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
        //释放播放器
        if (!useManager) {
            mAliPlayer?.release();
        } else {
            AliplayerManager.mRenderListener = null
        }
        mAliPlayer = null
        ULog.i("PlayerLine 释放播放器")
//        Completable.complete()
//                .subscribeOn(Schedulers.io())
//                .subscribe({ mAliPlayer?.release() }, {})

    }

    fun getPlayerVideo(): AliPlayer? {
        return mAliPlayer
    }

    interface OnVideoListener {
        fun onClickAuthorInfo(authorInfo: MicAnchor)
    }
}