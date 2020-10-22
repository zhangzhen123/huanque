package com.julun.huanque.core.widgets

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.bean.beans.MicAnchor
import com.julun.huanque.common.interfaces.PlayStateListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.BuildConfig
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliPlayerManager
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
class SingleVideoView(context: Context, attrs: AttributeSet?, var useManager: Boolean = false) :
    ConstraintLayout(context, attrs) {

    constructor(context: Context, useManager: Boolean = false) : this(context, null, useManager)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, false)

    //surfaceHolder
    private var mSurfaceHolder: SurfaceHolder? = null

    //surfaceview是否已经创建
    private var created = false

    private val logger = ULog.getLogger("SingleVideoView")

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
        if (useManager) {
            AliPlayerManager.mRendered = true
        }
        //首帧渲染显示事件,隐藏封面
        if (mLogEnable) {
            logger.info("${this} DXCPlayer 首帧渲染显示事件")
            logger.info("${this} DXCPlayer  时间  started时间 = ${mPlayTime - mSetTime},renderTime = ${mRenderTime - mSetTime},mAutoPlayTime = ${mAutoPlayTime - mSetTime}")
        }
        posterImage.hide()
        mPlayStateListener?.onFirstRenderListener()
    }

    //log输出标识位
    private var mLogEnable = true


    //是否处于聊天模式(默认为false)
    var mChatMode = false

    init {
        context.let {
            LayoutInflater.from(it).inflate(R.layout.view_single_video, this)
        }
        if (!useManager) {
            val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SingleVideoView)
            useManager = ta.getBoolean(R.styleable.SingleVideoView_useManager, false)
            ta.recycle()
        }


        anchor_info.onClickNew { }
        anchor_info.setOnClickListener(OnClickListener {
            if (playerInfo != null && playerInfo?.isAnchor != true) {
                mOnVideoListener?.onClickAuthorInfo(playerInfo ?: return@OnClickListener)
            }
        })
        initPlayer()
    }

    /**
     * 播放
     * @param url 流地址
     * @param main 是否是主播放器
     */
    fun play(url: String, main: Boolean = false) {
        mSetTime = System.currentTimeMillis()
        if (mLogEnable) {
            logger.info("DXCplayer  开始播放 时间 time = $mSetTime")
        }
//        if (!main) {
        //非主播放器，需要做校验
        if (url == mUrl) {
            return
        }
//        }
        isFree = false
        this.mUrl = url
        logger.info("DXCplayer set 1")
        playStream()
    }

    /**
     * 是否播放器相关 用于播放器视图销毁时 如果当前使用的单例播放器则不做处理
     */
    private fun release() {
        logger.info("release")
        mUrl = ""
        if (!useManager) {
            mAliPlayer?.stop()
            mAliPlayer?.release()
            this.hide()
        }
        isFree = true
        playerInfo = null
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
        if (mLogEnable)
            logger.info("DXCPlayer 恢复播放resume")
        if (mUrl.isNotEmpty()) {
            mAliPlayer?.start()
        }
    }

    /**
     * 停止播放器[stopAll]停止所有播放器 无论单例还是常规 基本上代表该播放要关闭 并且重置视图内容 隐藏视图
     */
    fun stop(stopAll: Boolean = false) {
        logger.info("stop stopAll=$stopAll")
        if (!created) {
            return
        }
        if (useManager) {
            if (stopAll)
                AliPlayerManager.stop()
        } else {
            mAliPlayer?.stop()
        }
        anchor_info.visibility = View.GONE
        if(stopAll){
            this.hide()
        }
        playerInfo = null
        mUrl = ""
        isFree = true
    }


    /**
     * 用于彻底销毁播放器 无论是单例播放器还是常规的播放器
     * 单例播放器停止并且初始化一些监听器和断开渲染视图
     * 常规播放器停止并收回
     */
    fun destroy() {
        if (useManager) {
            AliPlayerManager.destroy()
        } else {
            mAliPlayer?.stop()
            mAliPlayer?.release()

        }
        isFree = true
        mUrl = ""
        playerInfo = null
    }

    fun getStreamID(): String {
        return if (playerInfo != null) {
            playerInfo!!.streamID
        } else {
            ""
        }
    }

    fun getStreamUrl(): String {
        return mUrl
    }

    /**
     * 显示封面
     * @param blur 是否需要模糊效果
     */
    fun showCover(picUrl: String, blur: Boolean = true) {
        logger("showCover mRendered=${AliPlayerManager.mRendered}")
        if (useManager && AliPlayerManager.mRendered) {
            //播放器处于拉流状态，不显示封面
            return
        }
        posterImage.show()
        if (blur) {
            ImageUtils.loadImageWithBlur(posterImage, picUrl, 3, 13)
        } else {
            ImageUtils.loadImage(posterImage, picUrl)
        }
    }

    /**
     * 初始化播放器
     */
    fun initPlayer() {
        logger.info("PlayerLine 创建播放器")
        if (useManager) {
            initSingleModePlayer()
            return
        } else {
            mAliPlayer = AliPlayerFactory.createAliPlayer(context.applicationContext)
        }

//        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
        logger.info("PlayerLine 设置监听事件")
//        mAliPlayer?.setOnCompletionListener {
//            //播放完成事件
//        }
        mAliPlayer?.setOnErrorListener {
            //出错事件
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 出错事件 =${it.code}")
            }
        }
        mAliPlayer?.setOnPreparedListener {
            //准备成功事件
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 准备成功事件")
            }
        }
        mAliPlayer?.setOnVideoSizeChangedListener { width, height ->
            //视频分辨率变化回调
            if (mLogEnable) {
                logger.info("${this} DXCPlayer 视频分辨率变化回调")
            }
            mAliPlayer?.redraw()
        }
        mAliPlayer?.setOnRenderingStartListener(mRenderListener)
        mAliPlayer?.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
            if (it.code == InfoCode.AutoPlayStart) {
                mAutoPlayTime = System.currentTimeMillis()
                if (mLogEnable) {
                    logger.info("${this} DXCPlayer 自动播放回调")
                }
            }
//            if(it.code != InfoCode.CurrentPosition && it.code != InfoCode.BufferedPosition){
//                logger.info("${this} DXCPlayer 其他信息的事件 code = ${it.code},msg = ${it.extraMsg},value = ${it.extraValue}")
//            }
        }
//        mAliPlayer?.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
//            override fun onLoadingEnd() {
//                logger.info("${this} DXCPlayer onLoadingEnd")
//            }
//
//            override fun onLoadingBegin() {
//                logger.info("${this} DXCPlayer onLoadingBegin")
//            }
//
//            override fun onLoadingProgress(p0: Int, p1: Float) {
//                logger.info("${this} DXCPlayer onLoadingProgress")
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
                logger.info("${this} DXCPlayer 播放器状态改变事件 it = $it")
            }
            if (it == IPlayer.started) {
                mPlayTime = System.currentTimeMillis()
            }
            if (it == IPlayer.completion || it == IPlayer.error) {
                if(BuildConfig.DEBUG){
                    return@setOnStateChangedListener
                }
                //CDN切换的时候会触发此回调，重新调用播放方法
                Observable.timer(2, TimeUnit.SECONDS)
                    .bindToLifecycle(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //                            logger.info("DXCplayer set 2 it = $it")
                        mAliPlayer?.prepare()
                    }, { it.printStackTrace() })
            }
            mPlayStateListener?.playState(it)
        }
        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                logger("surfaceCreated=${holder.hashCode()}")
                created = true
                mAliPlayer?.setDisplay(holder)
                //防止黑屏
                mAliPlayer?.redraw()
                mSurfaceHolder = holder
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mAliPlayer?.redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                logger("surfaceDestroyed=${holder.hashCode()} useManager=$useManager")
                mAliPlayer?.setDisplay(null)
            }
        })
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

    private fun initSingleModePlayer() {
        mAliPlayer = AliPlayerManager.mAliPlayer

        if (AliPlayerManager.mRendered) {
            posterImage.hide()
        }
        logger("initSingleModePlayer setOnRenderingStartListener")
        mAliPlayer?.setOnRenderingStartListener(mRenderListener)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                logger("AliPlayerManager surfaceCreated=${holder.hashCode()}")
                created = true
                mAliPlayer?.setDisplay(holder)
                //防止黑屏
                mAliPlayer?.redraw()
                mSurfaceHolder = holder
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mAliPlayer?.redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                logger("AliPlayerManager surfaceDestroyed=${holder.hashCode()} useManager=$useManager")
            }
        })

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
        if (mLogEnable) {
            logger.info("DXCPlayer 开始播放playStream")
        }
        //聊天模式 不需要播放视频   && NetUtils.isNetConnected()
        if (mUrl.isNotEmpty() && !mChatMode) {
            val urlSource = UrlSource()
            urlSource.uri = mUrl
            if (useManager && AliPlayerManager.mRendered) {
                logger.info("已经存在单例播放流 不再重复播放")
                return
            }
            mAliPlayer?.setDataSource(urlSource)
            mAliPlayer?.isAutoPlay = true
//            logger.info("PlayerLine 准备播放器")
//            if (useManager) {
//                AliPlayerManager.mRenderListener = mRenderListener
////                AliplayerManager.mUrl = mUrl
//                AliPlayerManager.mRendered = false
//                AliPlayerManager.stoped = false
//            }
            mAliPlayer?.prepare()
//            mAliPlayer?.start()
        }
    }

    /**
     * 剪裁圆角
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun clip(radius: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.outlineProvider = SurfaceVideoViewOutlineProvider(radius);
            this.clipToOutline = true;
        }
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
        mSurfaceHolder = null
        release()
        //释放播放器
        if (!useManager) {
            mAliPlayer?.release();
        } else {
//            if (AliPlayerManager.mRenderListener == mRenderListener) {
//                AliPlayerManager.mRenderListener = null
//            }
            logger("onDetachedFromWindow setOnRenderingStartListener(null) ")
//            mAliPlayer?.setOnRenderingStartListener(null)
        }
        mAliPlayer = null
    }

    /**
     * 悬浮窗关闭的时候，重新将直播间主流的holder设置给播放器
     */
    fun resetHolder() {
        mSurfaceHolder?.let { holder ->
            mAliPlayer?.setDisplay(holder)
            //防止黑屏
            mAliPlayer?.redraw()
        }
    }

    /**
     * 断开播放器与渲染视图的连接 防止一直报错
     */
    fun clearHolder() {
        mAliPlayer?.setDisplay(null)
    }

    fun getPlayerVideo(): AliPlayer? {
        return mAliPlayer
    }

    interface OnVideoListener {
        fun onClickAuthorInfo(authorInfo: MicAnchor)
    }
}