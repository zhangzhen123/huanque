package com.julun.huanque.agora.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.agora.AgoraManager
import com.julun.huanque.agora.R
import com.julun.huanque.agora.handler.EventHandler
import com.julun.huanque.agora.viewmodel.VoiceChatViewModel
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClick
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import io.agora.rtc.IRtcEngineEventHandler
import kotlinx.android.synthetic.main.act_voice_chat.*
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/7/2 14:23
 *@描述 1对1 语音页面
 */
@Route(path = ARouterConstant.VOICE_CHAT_ACTIVITY)
class VoiceChatActivity : BaseActivity(), EventHandler {
    private val TAG = "VoiceChatActivity"
    private var mVoiceChatViewModel: VoiceChatViewModel? = null

    private val REQUEST_PERMISSION = 0x545

    override fun getLayoutId() = R.layout.act_voice_chat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        AgoraManager.mHandler.addHandler(this)
        initViewModel()
        //检查权限
        if (checkPermission()) {
            //有权限，发起呼叫
            calling()
        } else {
            //无权限，申请权限
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_PERMISSION
            )
        }
    }

    override fun initEvents(rootView: View) {
        tv_joinChannel.onClickNew {
            //加入频道
            joinChannel()
        }

        tv_leaveChannel.onClick {
            //离开频道
            AgoraManager.mRtcEngine?.leaveChannel()
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mVoiceChatViewModel = ViewModelProvider(this).get(VoiceChatViewModel::class.java)

        mVoiceChatViewModel?.currentVoiceState?.observe(this, Observer {
            if (it != null) {
                when (it) {
                    VoiceChatViewModel.VOICE_CALLING -> {
                        //呼叫状态
                        ll_quiet.hide()
                        ll_close.show()
                        ll_hands_free.hide()
                        ll_voice_accept.hide()
                    }
                    VoiceChatViewModel.VOICE_WAIT_ACCEPT -> {
                        //待接听状态
                        ll_quiet.hide()
                        ll_close.show()
                        ll_hands_free.hide()
                        ll_voice_accept.show()
                    }
                    VoiceChatViewModel.VOICE_ACCEPT -> {
                        //接听状态
                        ll_quiet.show()
                        ll_close.show()
                        ll_hands_free.show()
                        ll_voice_accept.hide()
                    }
                    VoiceChatViewModel.VOICE_CLOSE -> {
                        //结束状态
//                        ll_quiet.show()
//                        ll_close.show()
//                        ll_hands_free.show()
//                        ll_voice_accept.hide()
                    }
                }
            }
        })
    }


    // Tutorial Step 2
    private fun joinChannel() {
        //用户ID=49的token
//        var accessToken =
//                "00678fb9516fe5b4e759c51a5d880e1fd1bIAC3NfAb/qngU67zD269hcBInLyrBqiVsoExPhnOPdxCVQBp9qUAAAAAEADXzqF+G+j+XgEAAQAa6P5e"
        //用户ID=48的token
        var accessToken = "00678fb9516fe5b4e759c51a5d880e1fd1bIAB9QjBCK/oVIycggr/fz7ykMCywT8qmfuSo+CEMSJyqiwBp9qUAAAAAEADXzqF+dCv/XgEAAQBzK/9e"
        if (accessToken.isEmpty()) {
            return
        }
        // Allows a user to join a channel.
        val result = AgoraManager.mRtcEngine?.joinChannel(accessToken, "49", "Extra Optional Data", 0) // if you do not specify the uid, we will generate the uid for you
        logger.info("$TAG joinResult = $result")
    }

    // Tutorial Step 3
    private fun leaveChannel() {
        AgoraManager.mRtcEngine?.leaveChannel()
    }

    // Tutorial Step 4
    private fun onRemoteUserLeft(uid: Int, reason: Int) {
        ToastUtils.show("语音已经挂断")
    }

    // Tutorial Step 6
    private fun onRemoteUserVoiceMuted(uid: Int, muted: Boolean) {

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    //未获取到权限，直接返回
                    return
                }
            }
            //获取到对应权限,加入channel
            calling()
        }
    }

    /**
     * 发起呼叫
     */
    private fun calling() {

    }

    override fun onRemoteAudioStats(stats: IRtcEngineEventHandler.RemoteAudioStats?) {
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats?) {
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
    }

    override fun onLocalVideoStats(stats: IRtcEngineEventHandler.LocalVideoStats?) {
    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        //用户掉线
    }

    override fun onRemoteVideoStats(stats: IRtcEngineEventHandler.RemoteVideoStats?) {
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
    }

    override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
    }

    override fun onLastmileProbeResult(result: IRtcEngineEventHandler.LastmileProbeResult?) {
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        logger.info("$TAG 加入channel成功 channel = $channel,uid = $uid")
    }

    override fun onLastmileQuality(quality: Int) {
    }

    override fun onRequestToken() {
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
    }

    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats?) {
    }

    override fun onError(err: Int) {
        logger.info("$TAG onError err = $err")
    }


}