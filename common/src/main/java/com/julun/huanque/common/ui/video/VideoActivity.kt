package com.julun.huanque.common.ui.video

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.source.UrlSource
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.onTouch
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.utils.ULog
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.imageResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/22 20:21
 *
 *@Description: 通用的播放视频页面
 *
 */
class VideoActivity : BaseActivity() {

    companion object {
        /**
         * [activity]起始页面
         * [media]视频地址
         * 其他
         *[operate]要做的操作
         */
        fun start(
            activity: Activity,
            media: String,
            operate: String? = null,
            from: String? = null
        ) {
            val intent = Intent(activity, VideoActivity::class.java)
            intent.putExtra(IntentParamKey.URL.name, media)
            intent.putExtra(IntentParamKey.OPERATE.name, operate)
            intent.putExtra(IntentParamKey.SOURCE.name, from)
            activity.startActivity(intent)
        }
    }

    //log输出标识位
    private var mLogEnable = true
    private var totalDuration: Long = 0

    //阿里播放器
    private var mAliPlayer: AliPlayer? = null
    private var operate: String = ""
    private var url: String = ""
    private var from: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
//        postId = intent.getLongExtra(POST_ID, 0)
//        mUserId = intent.getLongExtra(IntentParamKey.ID.name, 0)
//        operate = intent.getStringExtra(IntentParamKey.OPERATE.name) ?: ""
//        from = intent.getStringExtra(IntentParamKey.SOURCE.name) ?: ""
        url = intent.getStringExtra(IntentParamKey.URL.name) ?: ""
//        PictureSelector.create(this).themeStyle(R.style.picture_me_style_multi)
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTransparent(this)

    }

    private var created = false

    override fun getLayoutId(): Int {
        return R.layout.activity_video
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
//        rlTitleRootView.topPadding= StatusBarUtil.getStatusBarHeight(this)
        initPlayer()
        ivClose.onClickNew {
            finish()
        }
        container_layout.onTouch { _, _ ->
            if (rlTitleRootView.isVisible) {
                rlTitleRootView.hide()
                iv_player_pause.hide()
                if (isComplete) {
                    isComplete = false
                    mAliPlayer?.prepare()
                } else {
                    resume()
                }

            } else {
                rlTitleRootView.show()
                iv_player_pause.show()
                pause()
            }
            false
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
                mAliPlayer?.setDisplay(null)
            }
        })
        mProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val position = (progress / 100f * totalDuration).toLong()
                    logger.info("滑动位置=$position")
                    currentTime?.text = TimeUtils.countDownTimeFormat1(position / 1000L)
                    mAliPlayer?.seekTo(position, IPlayer.SeekMode.Accurate)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        play(url)
    }

    private var isComplete: Boolean = false
    private fun initPlayer() {
        ULog.i("PlayerLine 创建播放器")
        mAliPlayer = AliPlayerFactory.createAliPlayer(this.applicationContext)
//        mAliPlayer?.setOnCompletionListener {
//            //播放完成事件
//            logger.info("播放完成了")
//        }
        mAliPlayer?.setOnErrorListener {
            //出错事件
            if (mLogEnable) {
                ULog.i("Player 出错事件 =${it.code}")
            }
        }
        mAliPlayer?.setOnPreparedListener {
            //准备成功事件
            if (mLogEnable) {
                ULog.i("Player 准备成功事件 duration=${mAliPlayer?.duration}")
            }
            totalDuration = (mAliPlayer?.duration ?: 0L)
            totalTime?.text = TimeUtils.countDownTimeFormat1(totalDuration / 1000L)
        }
        mAliPlayer?.setOnVideoSizeChangedListener { width, height ->
            //视频分辨率变化回调
            if (mLogEnable) {
                ULog.i("Player 视频分辨率变化回调")
            }
            mAliPlayer?.redraw()
        }
        mAliPlayer?.setOnRenderingStartListener(IPlayer.OnRenderingStartListener {
            //首帧渲染显示事件,隐藏封面
            if (mLogEnable) {
                ULog.i("Player 首帧渲染显示事件")
            }
            loading.hide()
        })
        mAliPlayer?.setOnInfoListener {
            //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
            logger.info("InfoListener code=${it.code},msg = ${it.extraMsg},value = ${it.extraValue}")
            if (it.code == InfoCode.AutoPlayStart) {

            }
            if (it.code == InfoCode.CurrentPosition) {
                currentTime?.text = TimeUtils.countDownTimeFormat1(it.extraValue / 1000L)
                val progress = (it.extraValue.toFloat() / totalDuration * 100).toInt()
                mProgressBar.progress = progress
            }
        }
        mAliPlayer?.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
            override fun onLoadingEnd() {
                ULog.i("Player onLoadingEnd")
            }

            override fun onLoadingBegin() {
                ULog.i("Player onLoadingBegin")
            }

            override fun onLoadingProgress(p0: Int, p1: Float) {
                ULog.i("Player onLoadingProgress p0=$p0  p1=$p1")
            }

        })
        mAliPlayer?.setOnStateChangedListener {
            //播放器状态改变事件
            if (mLogEnable) {
                ULog.i("Player 播放器状态改变事件 it = $it")
            }
            if (it == IPlayer.paused || it == IPlayer.stopped || it == IPlayer.completion) {
                iv_player_status.imageResource = R.mipmap.icon_video_play_small
            } else {
                iv_player_status.imageResource = R.mipmap.icon_video_pause_small
            }
            if (it == IPlayer.completion || it == IPlayer.error) {
                isComplete = true
                rlTitleRootView.show()
                iv_player_pause.show()
            }

        }
        mAliPlayer?.scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT

        //禁用硬解码
//        mAliPlayer?.enableHardwareDecoder(false)
        val config = mAliPlayer?.config ?: return
        config.mNetworkTimeout = 2000
        config.mNetworkRetryCount = 100
        //最大延迟。注意：直播有效。当延时比较大时，播放器sdk内部会追帧等，保证播放器的延时在这个范围内。
        config.mMaxDelayTime = 3000
// 最大缓冲区时长。单位ms。播放器每次最多加载这么长时间的缓冲数据。
//        config.mMaxBufferDuration = 50000;
//高缓冲时长。单位ms。当网络不好导致加载数据时，如果加载的缓冲时长到达这个值，结束加载状态。
//        config.mHighBufferDuration = 3000;
// 起播缓冲区时长。单位ms。这个时间设置越短，起播越快。也可能会导致播放之后很快就会进入加载状态。
        config.mStartBufferDuration = 100
        mAliPlayer?.config = config
    }

    /**
     * 播放
     * @param url 流地址
     */
    fun play(url: String) {
        if (url.isEmpty()) {
            return
        }
        playStream()
    }

    /**
     * 真正的播放方法
     */
    private fun playStream() {
        //聊天模式 不需要播放视频   && NetUtils.isNetConnected()
        val urlSource = UrlSource()
        urlSource.uri = url
//            ULog.i("PlayerLine 创建播放源")
        mAliPlayer?.setDataSource(urlSource)
        mAliPlayer?.isAutoPlay = true
//            ULog.i("PlayerLine 准备播放器")
        mAliPlayer?.prepare()
        isComplete = false
//            mAliPlayer?.start()
    }

    /**
     * 释放流相关
     */
    private fun release() {
        url = ""
        mAliPlayer?.stop()
        mAliPlayer?.release()
    }

    /**
     * 暂停播放
     */
    private fun pause() {
        mAliPlayer?.pause()
    }

    /**
     * 恢复播放
     */
    private fun resume() {
        if (mLogEnable)
            ULog.i("DXCPlayer 恢复播放resume")
        if (url.isNotEmpty()) {
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
        mAliPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }
}