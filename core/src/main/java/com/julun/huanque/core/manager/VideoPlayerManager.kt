package com.julun.huanque.core.manager

import android.util.SparseArray
import android.view.*
import androidx.annotation.IdRes
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.BuildConfig
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2018/12/26
 *
 * video 播放器控制器  这个管理器的目的就是 一个播放器 一个独立的渲染view 每次通过重新移除添加view做到多处播放
 *
 **/
object VideoPlayerManager {


    private var dispose: Disposable? = null
    private val logger = ULog.getLogger("VideoPlayerManager")

    //当前播放地址
    var currentPlayUrl = ""

    //推荐页面第一个View
//    private var currentView: ViewGroup? = null
    //播放视频
    private var mAliPlayer: AliPlayer? = null

    //log输出标识位
    private var mLogEnable = true

    /**
     *    最新的做法 这里只会保存一个播放的view 因为[mPlayerViewContainer]是单例唯一的
     *    每次变化的只是这个mPlayerViewContainer被add的容器 所以容器只会同一时间存在一个
     */
    private val playMap = hashMapOf<String, ViewGroup>()


    //性能第一 手动缓存  2元缓存  不同rootView源都有对应的自己的缓存列表
    private val viewMaps: HashMap<View, SparseArray<View>> = hashMapOf()

    //创造一个单例的播放器渲染view
    private val mPlayerViewContainer: View by lazy {
        LayoutInflater.from(CommonInit.getInstance().getContext()).inflate(R.layout.layout_player_view, null)
    }

    private val mSurfaceView: SurfaceView by lazy {
        mPlayerViewContainer.findViewById<SurfaceView>(R.id.surfaceView)
    }

    //SurfaceHolder.Callback唯一
    private val mHolderCallback: SurfaceHolder.Callback by lazy {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mAliPlayer?.setDisplay(holder)
                //防止黑屏
                mAliPlayer?.redraw()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mAliPlayer?.redraw()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mAliPlayer?.setDisplay(null)
            }
        }
    }

    init {
        mSurfaceView.holder?.addCallback(mHolderCallback)
    }

    fun <T : View> getView(rootView: View?, @IdRes viewId: Int): T? {
        if (rootView == null)
            return null
        var views: SparseArray<View>? = viewMaps[rootView]
        if (views == null) {
            views = SparseArray()
            viewMaps.put(rootView, views)
        }
        var view: View? = views.get(viewId)
        if (view == null) {
            view = rootView.findViewById(viewId)
            views.put(viewId, view)
        }
        return view as? T
    }

    /**
     * 开始播放的入口 [url]=""代表当前推荐的流不存在 直接关闭播放
     */
    fun startPlay(url: String, view: ViewGroup): Boolean {
        logger.info("startPlay url=$url show=${view.isShown}")
        if (playMap.isNotEmpty()) {
            logger.info("有已经正在播放的视图 不在重复播放${view.hashCode()}")
            return false
        }

        if (currentPlayUrl == url) {
            logger.info("已经存在 不再重复播放")
            return false
        }
        playMap[url] = view
        //复用播放器
        if (mAliPlayer == null) {
            initPlayer()
        }

        val parent = mPlayerViewContainer.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(mPlayerViewContainer)
        }
//        mPlayerViewContainer.id = R.id.surface_container

        view.tag = 1
        view.addView(
            mPlayerViewContainer,
            0,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        if (url.isEmpty()) {
            mAliPlayer?.stop()
        } else {
            if (view.isShown) {
                playStream(url)
            } else {
                playStream(url, false)
            }
        }


        currentPlayUrl = url
        return true
    }

    /**
     * 初始化播放器
     */
    private fun initPlayer() {
        ULog.i("PlayerLine 创建播放器")
        mAliPlayer = AliPlayerFactory.createAliPlayer(CommonInit.getInstance().getContext())
        //静音
        mAliPlayer?.isMute = true
//        mAliPlayer?.enableLog(false)
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
        mAliPlayer?.setOnRenderingStartListener {
            //首帧渲染显示事件,隐藏封面
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 首帧渲染显示事件")
            }
            hideCover()
        }
        mAliPlayer?.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
            if (it.code == InfoCode.AutoPlayStart) {
                if (mLogEnable) {
                    ULog.i("${this} DXCPlayer 自动播放回调")
                }
            }
        }
        mAliPlayer?.setOnStateChangedListener {
            //播放器状态改变事件
            if (mLogEnable) {
                ULog.i("${this} DXCPlayer 播放器状态改变事件 it = $it")
            }
            if (it == IPlayer.completion || it == IPlayer.error) {
                //CDN切换的时候会触发此回调，重新调用播放方法
                if (BuildConfig.DEBUG) {
                    return@setOnStateChangedListener
                }
                dispose?.dispose()
                dispose = Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //                            ULog.i("DXCplayer set 2 it = $it")
                        playStream(currentPlayUrl)
                    }, {})
            } else if (it == IPlayer.started) {
                hideCover()
            }
        }
        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL


        //禁用硬解码
        //        mAliPlayer?.enableHardwareDecoder(false)
        val config = mAliPlayer?.config ?: return
        config.mNetworkTimeout = 2000
        config.mNetworkRetryCount = 100
        config.mClearFrameWhenStop = true
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 3000;
        // 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
        //        config.mMaxBufferDuration = 50000;
        //高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
        //        config.mHighBufferDuration = 3000;
        // 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
        config.mStartBufferDuration = 50
        mAliPlayer?.config = config
    }

    /**
     * 真正的播放方法
     */
    private fun playStream(url: String, play: Boolean = true) {
        if (url.isNotEmpty()) {
            val urlSource = UrlSource()
            urlSource.uri = url
//            ULog.i("PlayerLine 创建播放源")
            mAliPlayer?.setDataSource(urlSource)
            if (play) {
                mAliPlayer?.isAutoPlay = true
//            ULog.i("PlayerLine 准备播放器")
                mAliPlayer?.prepare()
//            mAliPlayer?.start()
            } else {
                //只准备不播放
                mAliPlayer?.prepare()
            }

        }
//        ULog.i("DXCPlayer 开始播放playStream")
    }

    /**
     * 隐藏部分界面图标
     */
    private fun hideCover() {
        val currentView = playMap[currentPlayUrl] ?: return
        val anchorPicture = getView<View>(currentView, R.id.anchorPicture)
        anchorPicture?.hide()
    }

    /**
     * 显示封面
     */
    private fun showCover() {
        val currentView = playMap[currentPlayUrl] ?: return
        val anchorPicture = getView<View>(currentView, R.id.anchorPicture)
        logger.info("DXCPlayer 显示封面")
        anchorPicture?.show()
    }

    fun removePlay() {
        logger.info("removePlay url=$currentPlayUrl map=${playMap}")
        if (currentPlayUrl.isNotEmpty()) {
            showCover()
            val itemView = playMap.remove(currentPlayUrl)
            itemView?.tag = null
            mAliPlayer?.pause()
            val parent = mPlayerViewContainer.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(mPlayerViewContainer)
            }
            currentPlayUrl = ""
        }
    }

    /**
     * 因为stop是耗时操作 这里只会在界面生命周期暂停时执行
     */
    fun stopPlay() {
        if (currentPlayUrl.isNotEmpty()) {
            showCover()
            val itemView = playMap.remove(currentPlayUrl)
            itemView?.tag = null
            mAliPlayer?.stop()
            val parent = mPlayerViewContainer.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(mPlayerViewContainer)
            }
            currentPlayUrl = ""
        }

    }

    fun clear() {
        logger.info("reset")
        currentPlayUrl = ""
        playMap.clear()
        viewMaps.clear()
        mAliPlayer?.stop()
        mAliPlayer?.release()
        dispose?.dispose()
    }

}