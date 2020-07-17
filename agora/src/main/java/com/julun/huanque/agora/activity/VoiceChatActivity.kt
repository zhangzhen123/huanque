package com.julun.huanque.agora.activity

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.agora.AgoraManager
import com.julun.huanque.agora.R
import com.julun.huanque.agora.handler.EventHandler
import com.julun.huanque.agora.viewmodel.VoiceChatViewModel
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.act_voice_chat.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/7/2 14:23
 *@描述 1对1 语音页面
 */
@Route(path = ARouterConstant.VOICE_CHAT_ACTIVITY)
class VoiceChatActivity : BaseActivity(), EventHandler {
    companion object {
        //拨打状态
        const val CALLING = "CALLING"

        //挂断状态
        const val CLOSE = "CLOSE"
    }

    private val TAG = "VoiceChatActivity"
    private var mVoiceChatViewModel: VoiceChatViewModel? = null


    private var mType = ""

    private var mCallingContentDisposable: Disposable? = null

    //音频播放器
    private var mPlayer: MediaPlayer? = null

    override fun getLayoutId() = R.layout.act_voice_chat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        StatusBarUtil.setTransparent(this)
        initViewModel()
        mType = intent?.getStringExtra(ParamKey.TYPE) ?: ""
        if (mType == ConmmunicationUserType.CALLING) {
            //主叫
            mVoiceChatViewModel?.createUserId = SessionUtils.getUserId()
            val mTargetUserInfo = intent?.getSerializableExtra(ParamKey.USER) as? ChatUserBean
            if (mTargetUserInfo == null) {
                ToastUtils.show("没有对方数据")
                return
            }
            mVoiceChatViewModel?.targetUserBean?.value = mTargetUserInfo
            mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CALLING
        } else if (mType == ConmmunicationUserType.CALLED) {
            //被叫
            val netCallBean = intent?.getSerializableExtra(ParamKey.CallReceiveBean) as? NetcallBean
            if (netCallBean == null) {
                ToastUtils.show("没有对方数据")
                return
            }
            mVoiceChatViewModel?.createUserId = netCallBean.callerInfo.userId
            mVoiceChatViewModel?.netcallBeanData?.value = netCallBean
            mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_WAIT_ACCEPT
        }

        AgoraManager.mHandler.addHandler(this)
        if (mType == ConmmunicationUserType.CALLING) {
            checkPermissions()
        }
        //语音通话开始消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallAcceptProcessor {
            override fun process(data: NetCallAcceptBean) {
                //接通消息
                //开始计时
                recordCallDuration()
                if (mType == ConmmunicationUserType.CALLING) {
                    //主叫  加入channel
                    joinChannel()
                }
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_ACCEPT
            }
        })

        //语音开始消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallAcceptProcessor {
            override fun process(data: NetCallAcceptBean) {
                //接通消息
                //开始计时
                recordCallDuration()
                if (mType == ConmmunicationUserType.CALLING) {
                    //主叫  加入channel
                    joinChannel()
                }
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_ACCEPT
            }
        })
        //主叫取消会话消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallCancelProcessor {
            override fun process(data: VoidResult) {
                mVoiceChatViewModel?.voiceBeanData?.value = VoiceConmmunicationSimulate(VoiceResultType.CANCEL)
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CLOSE
            }
        })
        //挂断消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallHangUpProcessor {
            override fun process(data: NetCallHangUpBean) {
                if (data.hangUpId != SessionUtils.getUserId()) {
                    //非本人挂断
                    mVoiceChatViewModel?.voiceBeanData?.value =
                        VoiceConmmunicationSimulate(
                            VoiceResultType.CONMMUNICATION_FINISH,
                            mVoiceChatViewModel?.duration ?: 0,
                            0,
                            data.billUserId,
                            data.totalBeans
                        )
                }
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CLOSE
            }
        })
        //被叫拒绝消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallRefuseProcessor {
            override fun process(data: VoidResult) {
                mVoiceChatViewModel?.voiceBeanData?.value = VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_REFUSE)
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CLOSE
            }
        })
        //服务端断开消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallDisconnectProcessor {
            override fun process(data: VoidResult) {
                mVoiceChatViewModel?.voiceBeanData?.value =
                    VoiceConmmunicationSimulate(VoiceResultType.CONMMUNICATION_FINISH, mVoiceChatViewModel?.duration ?: 0)
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CLOSE
            }
        })
        //余额不足提醒消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallBalanceRemindProcessor {
            override fun process(data: NetCallBalanceRemindBean) {
                showBalanceNotEnoughView(data)
            }
        })

    }


    /**
     * 播放音效
     */
    private fun playAudio(calling: Boolean) {
        mPlayer = if (calling) {
            MediaPlayer.create(this, R.raw.ring)?.apply { isLooping = true }
        } else {
            MediaPlayer.create(this, R.raw.finish).apply { isLooping = false }
        }
        mPlayer?.start()
    }

    override fun initEvents(rootView: View) {
        ll_voice_accept.onClickNew {
            //接收邀请
            checkPermissions()
        }

        ll_hands_free.onClickNew {
            //免提
            ll_hands_free.isSelected = !ll_hands_free.isSelected
            AgoraManager.mRtcEngine?.setEnableSpeakerphone(ll_hands_free.isSelected)
        }
        ll_quiet.onClickNew {
            //静音
            ll_quiet.isSelected = !ll_quiet.isSelected
            val volume = if (ll_quiet.isSelected) 0 else 100
            AgoraManager.mRtcEngine?.adjustRecordingSignalVolume(volume)
        }

        //adjustRecordingSignalVolume()
        ll_close.onClickNew {
            //挂断会话
//            if (mType == ConmmunicationUserType.CALLING) {
            //主叫
//                if (mVoiceChatViewModel?.currentVoiceState?.value == VoiceChatViewModel.VOICE_CALLING) {
//                    //手动取消
//                    mVoiceChatViewModel?.calcelVoice(CancelType.Normal)
//                }
            when (mVoiceChatViewModel?.currentVoiceState?.value) {
                VoiceChatViewModel.VOICE_CALLING -> {
                    //手动取消
                    mVoiceChatViewModel?.calcelVoice(CancelType.Normal)
                }
                VoiceChatViewModel.VOICE_ACCEPT -> {
                    //挂断通话
                    mVoiceChatViewModel?.hangUpVoice()
                }
                VoiceChatViewModel.VOICE_WAIT_ACCEPT -> {
                    //拒绝通话
                    mVoiceChatViewModel?.refuseVoice()
                }
                else -> {
                }
            }
//            } else if (mType == ConmmunicationUserType.CALLED) {
//                //被叫
//                mVoiceChatViewModel?.refuseVoice()
//            }
        }

        tv_recharge.onClickNew {
            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
        }
    }

    /**
     * 记录通话时长
     */
    private fun recordCallDuration() {
        ObservableTake.interval(0, 1, TimeUnit.SECONDS)
            .map {
                mVoiceChatViewModel?.duration = it
                TimeUtils.countDownTimeFormat1(it)
            }
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ tv_call_duration.text = it }, {})
    }

    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.RECORD_AUDIO)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        if (mType == ConmmunicationUserType.CALLING) {
                            calling()
                            playAudio(true)
                        } else if (mType == ConmmunicationUserType.CALLED) {
                            mVoiceChatViewModel?.acceptVoice()
                            //加入会话
                            joinChannel()
                        }
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
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
                        showWaitContent(true)

                    }
                    VoiceChatViewModel.VOICE_WAIT_ACCEPT -> {
                        //待接听状态
                        ll_quiet.hide()
                        ll_close.show()
                        ll_hands_free.hide()
                        ll_voice_accept.show()
                        showWaitContent(false)
                        playAudio(true)
                    }
                    VoiceChatViewModel.VOICE_ACCEPT -> {
                        //接听状态
                        ll_quiet.show()
                        ll_close.show()
                        ll_hands_free.show()
                        ll_voice_accept.hide()

                        mPlayer?.stop()
                        mPlayer = null

                        //取消超时倒计时
                        mDisposable?.dispose()
                        mCallingContentDisposable?.dispose()
                    }
                    VoiceChatViewModel.VOICE_CLOSE -> {
                        //结束状态
                        mPlayer?.stop()
                        mPlayer = null

                        playAudio(false)
                        //退出频道
                        leaveChannel()
//                        ToastUtils.show("通话已结束")
                        mDisposable?.dispose()
                        mDurationDisposable?.dispose()
                        mCallingContentDisposable?.dispose()
                        Observable.timer(1, TimeUnit.SECONDS)
                            .bindUntilEvent(this, ActivityEvent.DESTROY)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ finish() }, {}, {})
//                        ll_quiet.show()
//                        ll_close.show()
//                        ll_hands_free.show()
//                        ll_voice_accept.hide()
                    }
                }
            }
        })
        mVoiceChatViewModel?.targetUserBean?.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })

        mVoiceChatViewModel?.netcallBeanData?.observe(this, Observer { netCallBean ->
            if (netCallBean != null) {
                var backPic = ""
                if (mType == ConmmunicationUserType.CALLING) {
                    //主叫
                    mVoiceChatViewModel?.targetUserBean?.value = netCallBean.receiverInfo
                    backPic = netCallBean.receiverInfo.backPic
                } else if (mType == ConmmunicationUserType.CALLED) {
                    mVoiceChatViewModel?.targetUserBean?.value = netCallBean.callerInfo
                    backPic = netCallBean.callerInfo.backPic
                }
                mVoiceChatViewModel?.agoraToken = netCallBean.token
                mVoiceChatViewModel?.channelId = netCallBean.channelId
                mVoiceChatViewModel?.callId = netCallBean.callId
                mVoiceChatViewModel?.duration = 0
                if (netCallBean.beans > 0) {
                    //付费方
                    tv_price.show()
                    tv_price.text = "${netCallBean.beans}鹊币/分钟"
                }

                ImageUtils.loadImageWithBlur(
                    sdv_bg, backPic, 1, 1, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()
                )
            }
        })

        mVoiceChatViewModel?.voiceBeanData?.observe(this, Observer {
            if (it != null) {
                sendSimulateMessage(it)
            }
        })
    }

    /**
     * 循环显示呼叫状态
     * @param calling  true ： 拨号状态   false : 带接听状态
     */
    private fun showWaitContent(calling: Boolean) {
        mCallingContentDisposable?.dispose()
        mCallingContentDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val index = it % 3
                when (index) {
                    0L -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通."
                        } else {
                            "正在邀请您语音通话."
                        }
                    }
                    1L -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通.."
                        } else {
                            "正在邀请您语音通话.."
                        }
                    }
                    else -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通..."
                        } else {
                            "正在邀请您语音通话..."
                        }
                    }
                }

            }, { it.printStackTrace() })

    }

    /**
     * 显示页面
     */
    private fun showViewByData(bean: ChatUserBean) {
        ImageUtils.loadImage(sdv_header, bean.headPic, 100f, 100f)
        tv_nickname.text = bean.nickname
        val sexImage = when (bean.sex) {
            Sex.MALE -> {
                R.mipmap.icon_sex_male
            }
            Sex.FEMALE -> {
                R.mipmap.icon_sex_female
            }
            else -> {
                0
            }
        }
        if (sexImage > 0) {
            tv_nickname.setCompoundDrawablesWithIntrinsicBounds(0, 0, sexImage, 0)
            tv_nickname.compoundDrawablePadding = 4
        }
    }


    // Tutorial Step 2
    private fun joinChannel() {
        var accessToken = mVoiceChatViewModel?.agoraToken ?: ""
        val channelId = mVoiceChatViewModel?.channelId ?: return
        if (accessToken.isEmpty()) {
            return
        }
        //设置为主播身份
        AgoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        AgoraManager.mRtcEngine?.enableAudio()
        AgoraManager.mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
        //默认听筒
        // Allows a user to join a channel.
        val result = AgoraManager.mRtcEngine?.joinChannel(
            accessToken,
            channelId,
            null,
            SessionUtils.getUserId().toInt()
        )
        if (result != 0) {
            //joinchannel失败
            mVoiceChatViewModel?.hangUpVoice()
        }
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

    /**
     * 发送模拟消息
     */
    private fun sendSimulateMessage(bean: VoiceConmmunicationSimulate) {
        val targetChatInfo = mVoiceChatViewModel?.targetUserBean?.value ?: return
        val targetUser = TargetUserObj()

        val createUserId = mVoiceChatViewModel?.createUserId ?: return
        bean.createUserID = createUserId

        val chatExtra = RoomUserChatExtra()
        val sId: String
        val tId = "${targetChatInfo.userId}"

        if (createUserId == SessionUtils.getUserId()) {
            //本人是主叫(插入发送消息)
            sId = "${SessionUtils.getUserId()}"
            targetUser.apply {
                headPic = targetChatInfo.headPic
                nickname = targetChatInfo.nickname
                meetStatus = targetChatInfo.meetStatus
                sex = targetChatInfo.sex
                userId = targetChatInfo.userId
            }
            chatExtra.apply {
                headPic = SessionUtils.getHeaderPic()
                senderId = SessionUtils.getUserId()
                nickname = SessionUtils.getNickName()
                sex = SessionUtils.getSex()
                targetUserObj = targetUser
                userAbcd = AppHelper.getMD5(sId)
            }
        } else {
            //本人是被叫（插入接收消息）
            sId = "${targetChatInfo.userId}"
            targetUser.apply {
                headPic = SessionUtils.getHeaderPic()
                nickname = SessionUtils.getNickName()
                meetStatus = targetChatInfo.meetStatus
                userId = SessionUtils.getUserId()
                sex = SessionUtils.getSex()
            }
            chatExtra.apply {
                headPic = targetChatInfo.headPic
                senderId = targetChatInfo.userId
                nickname = targetChatInfo.nickname
                sex = targetChatInfo.sex
                targetUserObj = targetUser
                userAbcd = AppHelper.getMD5(sId)
            }
        }



        RongCloudManager.sendSimulateMessage(
            tId,
            sId,
            chatExtra,
            Conversation.ConversationType.PRIVATE,
            MessageCustomBeanType.Voice_Conmmunication_Simulate,
            bean
        )
    }


    private var mDisposable: Disposable? = null

    /**
     * 发起呼叫
     */
    private fun calling() {
        mVoiceChatViewModel?.createConmmunication(mVoiceChatViewModel?.targetUserBean?.value?.userId ?: 0)
        //超时计算
        mDisposable = Observable.timer(10, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .subscribe({
                mVoiceChatViewModel?.calcelVoice(CancelType.Timeout)
            }, {})
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


    //余额不足，剩余时间使用的定时器
    private var mDurationDisposable: Disposable? = null

    /**
     * 显示余额不足弹窗
     */
    private fun showBalanceNotEnoughView(bean: NetCallBalanceRemindBean) {
        if (bean.enough) {
            //隐藏视图
            mDurationDisposable?.dispose()
            view_balance.hide()
            tv_recharge.hide()
            tv_attention.hide()
            tv_surplus_time.hide()

        } else {
            //显示视图
            view_balance.show()
            tv_recharge.show()
            tv_attention.show()
            tv_surplus_time.show()

            mDurationDisposable?.dispose()
            val duration = bean.duration
            mDurationDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(duration + 1)
                .map { "${duration - it}" }
                .bindUntilEvent(this, ActivityEvent.DESTROY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_surplus_time.text = it
                }, { it.printStackTrace() })
        }

    }


    override fun onViewDestroy() {
        super.onViewDestroy()
        leaveChannel()
        mPlayer?.stop()
        mPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageProcessor.clearProcessors(false)
    }


}