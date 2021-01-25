package com.julun.huanque.core.ui.homepage

import android.content.Intent
import android.media.MediaPlayer
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.VoiceBean
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliPlayerManager
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.core.viewmodel.EditInfoViewModel
import kotlinx.android.synthetic.main.frag_voice_status.*
import kotlinx.android.synthetic.main.frag_voice_status.lottie_voice_state
import kotlinx.android.synthetic.main.frag_voice_status.view_voice

/**
 *@创建者   dong
 *@创建时间 2021/1/16 11:05
 *@描述 语音状态弹窗
 */
class VoiceStatusFragment : BaseDialogFragment() {
    private val mEditInfoViewModel: EditInfoViewModel by activityViewModels()
    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(requireContext()) }
    override fun getLayoutId() = R.layout.frag_voice_status

    override fun initViews() {

        //半秒回调一次
        audioPlayerManager.setSleep(500)
        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
            override fun prepared() {
                logger.info("prepared")
            }

            override fun start() {
                logger.info("start 总长=${audioPlayerManager.getDuration()}")
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                //不使用实际的值
//                currentPlayHomeRecomItem?.introduceVoiceLength = (audioPlayerManager.getDuration() / 1000)+1
//                AliplayerManager.soundOff()
//                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
//                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
                lottie_voice_state.playAnimation()
            }

            override fun resume() {
                logger.info("resume")
                AliPlayerManager.soundOff()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
//                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
//                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
                lottie_voice_state.resumeAnimation()
            }

            override fun pause() {
                logger.info("pause")
                AliPlayerManager.soundOn()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
//                val padding = dp2px(0)
//                sdv_voice_state.setPadding(padding, padding, padding, padding)
//                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
                lottie_voice_state.pauseAnimation()
            }

            override fun stop() {
                logger.info("stop")
                AliPlayerManager.soundOn()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
//                val padding = dp2px(0)
//                sdv_voice_state.setPadding(padding, padding, padding, padding)
//                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
                lottie_voice_state.cancelAnimation()
                lottie_voice_state.progress = 0f
            }


        })
        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
                tv_time.text = "${mEditInfoViewModel?.basicInfo?.value?.voice?.length}s"
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
//                logger.info("onSeekBarProgress progress=${progress / 1000}")
                val voiceLength = mEditInfoViewModel?.basicInfo?.value?.voice?.length ?: return
                tv_time.text = "${voiceLength - progress / 1000}s"
            }
        })



        view_voice.onClickNew {
            //播放或者暂停
            if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                ToastUtils.show("正在语音通话，请稍后再试")
                return@onClickNew
            }
            val voiceUrl = mEditInfoViewModel?.basicInfo?.value?.voice?.voiceUrl ?: return@onClickNew
            if (voiceUrl.isEmpty()) {
                return@onClickNew
            }
            //播放音效
            if (audioPlayerManager.musicType == -1 || audioPlayerManager.mediaPlayer == null) {
                //未设置音频地址
                audioPlayerManager.setNetPath(
                    StringHelper.getOssAudioUrl(
                        voiceUrl
                    )
                )
                audioPlayerManager.start(false)
            } else {
                //已设置音频地址
                if (audioPlayerManager.isPlaying) {
                    audioPlayerManager.pause()
                } else {
                    audioPlayerManager.resume()
                }
            }
        }
        tv_left.onClickNew { dismiss() }

        tv_right.onClickNew {
            //重新录制
            val intent = Intent(requireActivity(), VoiceSignActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                mEditInfoViewModel.needFresh = true
                startActivity(intent)
            }
            dismiss()
        }
    }


    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 35, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
    }

    private fun initViewModel() {
        mEditInfoViewModel.basicInfo.observe(this, Observer {
            if (it != null) {
                tv_time.text = "${it.voice?.length}s"
                val voiceBean = it.voice
                when (voiceBean.voiceStatus) {
                    VoiceBean.Wait -> {
                        //审核中
                    }
                    VoiceBean.Pass -> {
                        //审核通过
                        tv_state.text = "审核通过"
                        tv_attention.text = "每周只能修改3次语音签名"

                    }
                    VoiceBean.Reject -> {
                        //被拒绝
                        tv_state.text = "审核未通过"
                        tv_attention.text = "由于${voiceBean.remark}，请重新录制"
                    }
                    else -> {
                    }
                }

            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayerManager.destroy()
    }

}