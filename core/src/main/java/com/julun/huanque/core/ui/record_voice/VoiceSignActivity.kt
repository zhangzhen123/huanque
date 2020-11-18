package com.julun.huanque.core.ui.record_voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.facebook.react.bridge.Arguments
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.SignPoint
import com.julun.huanque.common.bean.events.VoiceSignEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.manager.audio_record.AudioRecordManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.core.R
import com.julun.rnlib.RnManager
import kotlinx.android.synthetic.main.activity_voice_sign.*
import kotlinx.android.synthetic.main.activity_withdraw.pagerHeader
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.imageResource
import java.io.File
import kotlin.properties.Delegates

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/21 20:49
 *
 *@Description: VoiceSignActivity 语音签名界面
 *
 */
@Route(path = ARouterConstant.VOICE_SIGN_ACTIVITY)
class VoiceSignActivity : BaseVMActivity<VoiceSignViewModel>() {

    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(this) }

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    // 0代表初始状态 可以开始录音
    // 1代表录音中
    // 2代表已经录音完成
    // 3播放中
    private var recordState: Int by Delegates.observable(0) { _, oldValue, newValue ->
        if (oldValue == newValue) {
            logger.info("recordState没有变化")
            return@observable
        }
        when (newValue) {
            0 -> {
                iv_main_btn.loadImageLocal(R.mipmap.icon_record_voice)
                ll_right_btn.hide()
                ll_left_btn.hide()
                tv_tips.show()
                tv_tips.text = "长按录制"
                tv_time.hide()
            }
            1 -> {
//                ImageUtils.loadGifImageLocal(iv_main_btn, R.mipmap.anim_voice_play)
                iv_main_btn.loadImageLocal(R.mipmap.icon_voice_doing)
                tv_tips.show()
                tv_tips.text = "录制中..."
            }
            2 -> {
                iv_main_btn.loadImageLocal(R.mipmap.icon_record_play)
                ll_right_btn.show()
                ll_left_btn.show()
                tv_tips.show()
                tv_tips.text = "点按播放"
            }
            3 -> {
//                ImageUtils.loadGifImageLocal(iv_main_btn, R.mipmap.anim_voice_play)
                iv_main_btn.loadImageLocal(R.mipmap.icon_voice_doing)
                tv_tips.show()
                tv_tips.text = "播放中..."
            }
        }
    }
    var mWavFile: File? = null //记录当前的录制的语音文件
    private var mTotalTime: Long = 0L

    //    var isVideoPlaying: Boolean = false
    private val recordCallBack: AudioRecordManager.RecordCallBack by lazy {
        object : AudioRecordManager.RecordCallBack {
            override fun process(time: Long) {
                logger.info("录音时间：$time")
                tv_time.show()
                tv_time.text = "${time}S"
                recordState = 1
                //到达30秒自动停止
                if (time >= 30L) {
                    AudioRecordManager.stopRecord()
                    recordState = 2
                }
            }

            override fun finish(wavFile: File?, totalTime: Long?) {
                logger.info("录音finish：${wavFile?.absolutePath}")
                if (totalTime != null && totalTime < 3) {
                    ToastUtils.show("录制时长不足3秒哦")
                    recordState = 0
                } else if (wavFile != null && totalTime != null) {
                    mTotalTime = totalTime
                    mWavFile = wavFile
                    tv_time.show()
                    tv_time.text = "${totalTime}S"
                    recordState = 2
                } else {
                    //录音异常 恢复初始状态
                    recordState = 0
                    ToastUtils.show("录音出错了，请重试！")
                }
            }
        }
    }


    var points: List<SignPoint>? = null

    override fun getLayoutId(): Int = R.layout.activity_voice_sign


    override fun setHeader() {
        pagerHeader.initHeaderView(titleTxt = "语音签名")
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initViewModel()

        mViewModel.queryInfo(queryType = QueryType.INIT)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initEvents(rootView: View) {

        pagerHeader.imageViewBack.onClickNew {
            finish()
        }

        iv_main_btn.setOnTouchListener { _, event ->
            if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                ToastUtils.show("正在语音通话，请稍后再试")
                return@setOnTouchListener false
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (recordState == 0) {
                    logger.info("事件-开始录制")
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermission()
                        return@setOnTouchListener false
                    } else {
                        recordState = 1
                        AudioRecordManager.startRecord(recordCallBack)
                    }
                } else if (recordState == 2) {
                    logger.info("事件-开始播放")
                    if (mWavFile != null) {
                        recordState = 3
                        audioPlayerManager.setFilePlay(mWavFile)
                        audioPlayerManager.start()
                    }

                }


            } else if (event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP) {
                if (recordState == 2) {
                    logger.info("事件-播放的点击 不处理")
                } else if (recordState == 1) {
                    logger.info("事件-结束录制")
                    AudioRecordManager.stopRecord()
                }

            }
            true
        }

        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
                recordState = 2
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
                recordState = 2
                tv_time.text = "${mTotalTime}S"
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
                logger.info("onSeekBarProgress=${progress / 1000}")
                tv_time.show()
                tv_time.text = "${mTotalTime - (progress / 1000)}S"
            }
        })

        ll_left_btn.onClickNew {
            recordState = 0
            audioPlayerManager.stop()
            mWavFile = null
            mTotalTime = 0L
        }
        tv_change.onClickNew {
            changePointText()
        }
        ll_right_btn.onClickNew {
            startUploadVoiceFile()
        }
    }

    private fun startUploadVoiceFile() {
        if (mWavFile == null || mTotalTime == 0L) {
            ToastUtils.show("录音参数错误，请录音重试")
            return
        }
        if (!mLoadingDialog.isShowing) {
            mLoadingDialog.showDialog()
        }
        OssUpLoadManager.uploadFile(
            mWavFile!!.absolutePath,
            OssUpLoadManager.VOICE_POSITION,
            object : OssUpLoadManager.FileUploadCallback {
                override fun onProgress(currentSize: Long, totalSize: Long) {
                    logger.info("上传进度：${currentSize}/${totalSize}")
                }

                override fun onResult(code: Int, imgPath: String?) {
                    if (code == OssUpLoadManager.CODE_SUCCESS && imgPath != null) {
                        logger("音频上传oss成功结果的：$imgPath")
                        mViewModel.updateVoice(imgPath, mTotalTime)
                    } else {
                        ToastUtils.show("上传失败，请稍后重试")
                    }

                }

            })

    }

    private fun requestPermission() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.RECORD_AUDIO/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger("获取权限成功")
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger("获取权限被永久拒绝")
                        val message = "无法获取到录音权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }


    private fun initViewModel() {
        mViewModel.signPoints.observe(this, Observer {
            if (it.isSuccess()) {
                points = it.requireT().points
                changePointText()
            }
        })

        mViewModel.updateVoiceResult.observe(this, Observer {
            if (mLoadingDialog.isShowing) {
                mLoadingDialog.dismiss()
            }
            if (it.state == NetStateType.SUCCESS) {
//                    it.getT()
                recordState = 0
                EventBus.getDefault().post(VoiceSignEvent())
                ToastUtils.show("修改语音签名成功")
                finish()
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show(it.error?.message)
            }
        })

    }

    private fun changePointText() {
        if (points == null || points!!.isEmpty()) {
            return
        }
        val rd = points?.random()
        tv_point_title.text = "${rd?.voiceTitle}"
        tv_points.text = "${rd?.voiceContent}"
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioRecordManager.destroy()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
                ll_content.hide()
            }
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
                ll_content.show()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                ll_content.hide()
                state_pager_view.showError(btnClick = View.OnClickListener {
                    mViewModel.queryInfo(QueryType.INIT)
                })
            }

        }

    }


}