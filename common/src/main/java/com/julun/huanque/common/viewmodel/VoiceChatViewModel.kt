package com.julun.huanque.common.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.agora.AgoraManager
import com.julun.huanque.common.agora.handler.EventHandler
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.NetcallCancelForm
import com.julun.huanque.common.bean.forms.NetcallHangUpForm
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.commonviewmodel.BaseApplicationViewModel
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.ChannelCodeHelper.logger
import com.julun.huanque.common.interfaces.VoiceChangeListener
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.VoiceFloatingManager
import com.julun.huanque.common.manager.VoiceManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.suger.whatEver
import com.julun.huanque.common.utils.*
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/7/2 11:56
 *@描述 语音聊天ViewModel
 */
class VoiceChatViewModel(application: Application) : BaseApplicationViewModel(application), EventHandler {

    companion object {
        //语音通话状态
        //拨号状态
        const val VOICE_CALLING = "VOICE_CALLING"

        //接听状态
        const val VOICE_ACCEPT = "VOICE_ACCEPT"

        //待接听状态
        const val VOICE_WAIT_ACCEPT = "VOICE_WAIT_ACCEPT"

        //挂断状态
        const val VOICE_CLOSE = "VOICE_CLOSE"
    }

    init {
        AgoraManager.mHandler.addHandler(this)
    }

    //语音状态变动监听
    var mVoiceChangeListener: VoiceChangeListener? = null

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    var mType = ""

    //当前语音状态
    val currentVoiceState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //对方用户数据
    val targetUserBean: MutableLiveData<ChatUser> by lazy { MutableLiveData<ChatUser>() }

    //会话详情数据
    val netcallBeanData: MutableLiveData<NetcallBean> by lazy { MutableLiveData<NetcallBean>() }

    //免提功能是否可用
    val handsFreeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //余额不足标识，显示余额不足弹窗
    val notEnoughData: MutableLiveData<NetCallBalanceRemindBean> by lazy { MutableLiveData<NetCallBalanceRemindBean>() }

    //更新通话时长布局的标识位
    val updateDurationParamsFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //通话时长
    val duration: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //语音卡数量
    val voiceCardCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //语音卡时长倒计时
    val voiceCardDuration: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //token
    var agoraToken = ""

    //channelId
    var channelId = ""

    //语音通话ID
    var callId = 0L


    //创建者ID
    var createUserId = 0L

    //等待关闭状态
    var waitingClose = false

    /**
     * 接受会话
     */
    fun acceptVoice() {
        viewModelScope.launch {
            request({
                socialService.netcallAccept(NetcallIdForm(callId)).dataConvert()
            })
        }
    }

    /**
     * 被叫直接拒绝或者加入频道失败，调用该接口 挂断通话
     * @param timeOut 是否已超时
     */
    fun refuseVoice(timeOut: Boolean = false) {
        if (waitingClose) {
            return
        }
        waitingClose = true
        viewModelScope.launch {
            request({
                socialService.netcallRefuse(NetcallIdForm(callId)).dataConvert()
            }, {}, {
                //挂断当前语音
                setVoiceFinish()
                if (timeOut) {
                    sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.CANCEL))
                } else {
                    sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.MINE_REFUSE))
                }
            })
        }
    }

    /**
     * 被叫忙
     */
    fun busy(callId: Long) {
        viewModelScope.launch {
            request({
                socialService.voiceBusy(NetcallIdForm(callId)).dataConvert()
            }, {})
        }
    }

    /**
     * 主叫取消通话
     */
    fun calcelVoice(type: String) {
        if (waitingClose) {
            return
        }
        waitingClose = true
        viewModelScope.launch {
            request({
                socialService.netcallCancel(NetcallCancelForm(callId, type)).dataConvert()
            }, {}, {
                if (type == CancelType.Timeout) {
                    ToastUtils.show("对方无应答")
                    sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_NOT_ACCEPT))
                } else {
                    sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.CANCEL))
                }
                setVoiceFinish()
            })
        }
    }

    /**
     * 挂断语音通话
     */
    fun hangUpVoice() {
        if (waitingClose) {
            return
        }
        waitingClose = true
        viewModelScope.launch {
            request({
                val result = socialService.netcallHangUp(NetcallHangUpForm(callId, duration.value ?: 0)).dataConvert()
                sendSimulateMessage(
                    VoiceConmmunicationSimulate(
                        VoiceResultType.CONMMUNICATION_FINISH,
                        result.duration,
                        billUserId = result.billUserId,
                        totalBeans = result.totalBeans
                    )
                )
            }, {
                sendSimulateMessage(
                    VoiceConmmunicationSimulate(
                        VoiceResultType.CONMMUNICATION_FINISH,
                        duration.value ?: 0,
                        totalBeans = 0,
                        needRefresh = true,
                        callId = callId
                    )
                )
            }, { setVoiceFinish() })
        }
    }

    /**
     * 标记付费弹窗已经显示过
     */
    fun markFeeRemind() {
        socialService.markFeeRemind().whatEver()
    }


    /**
     * 注册消息
     */
    fun registerMessage() {
        MessageProcessor.removeProcessors(this)
        //语音通话开始消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallAcceptProcessor {
            override fun process(data: NetCallAcceptBean) {
                //接通消息
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                //开始计时
                recordCallDuration()
                if (mType == ConmmunicationUserType.CALLING) {
                    //主叫  加入channel
                    joinChannel()
                }
                setVoiceAccept()
                //取消超时倒计时
                mDisposable?.dispose()
            }
        })

        //语音开始消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallAcceptProcessor {
            override fun process(data: NetCallAcceptBean) {
                //接通消息
                //开始计时
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                recordCallDuration()
                if (mType == ConmmunicationUserType.CALLING) {
                    //主叫  加入channel
                    joinChannel()
                }
                setVoiceAccept()
                //取消超时倒计时
                mDisposable?.dispose()
                val currentTicketBean = data.ticketInfo["${SessionUtils.getUserId()}"]
                currentTicketBean?.let { ticket ->
                    //更新语音券数量
                    voiceCardCount.value = ticket.ticketCnt
                    //更新语音券倒计时
                    voiceCardCountDown(ticket.ticketTtl)
                }

            }
        })
        //主叫取消会话消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallCancelProcessor {
            override fun process(data: NetCallReceiveBean) {
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                if (waitingClose) {
                    return
                }
                waitingClose = true
                sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.CANCEL))
                setVoiceFinish()
            }
        })
        //挂断消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallHangUpProcessor {
            override fun process(data: NetCallHangUpBean) {
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                if (waitingClose) {
                    return
                }
                waitingClose = true
                if (data.hangUpId != SessionUtils.getUserId()) {
                    //非本人挂断
                    sendSimulateMessage(
                        VoiceConmmunicationSimulate(
                            VoiceResultType.CONMMUNICATION_FINISH,
                            duration.value ?: 0,
                            0,
                            data.billUserId,
                            data.totalBeans
                        )
                    )
                }
                setVoiceFinish()
            }
        })
        //被叫拒绝消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallRefuseProcessor {
            override fun process(data: NetCallReceiveBean) {
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                if (waitingClose) {
                    return
                }
                waitingClose = true
                sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_REFUSE))
                setVoiceFinish()
            }
        })
        //服务端断开消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallDisconnectProcessor {
            override fun process(data: VoidResult) {
                if (waitingClose) {
                    return
                }
                waitingClose = true
                sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.CONMMUNICATION_FINISH, duration.value ?: 0))
                setVoiceFinish()
            }
        })
        //余额不足提醒消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallBalanceRemindProcessor {
            override fun process(data: NetCallBalanceRemindBean) {
                notEnoughData.value = data
            }
        })

        //对方忙
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallBusyProcessor {
            override fun process(data: NetCallReceiveBean) {
                if (data.callId != callId) {
                    //不是当前语音通话的消息
                    return
                }
                if (waitingClose) {
                    return
                }
                waitingClose = true
                ToastUtils.show("对方忙")

                sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.RECEIVE_BUSY))
                setVoiceFinish()
            }

        })

        //语音通话扣费消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.NetCallDeductBeansProcessor {
            override fun process(data: NetCallDeductBeansDetail) {
                //更新语音券数量
                voiceCardCount.value = data.ticketCount
                //更新语音券倒计时
                voiceCardCountDown(data.ticketTtl)
            }
        })
    }

    private var mVoiceCardDisposable: Disposable? = null

    /**
     * 语音卡通话时长倒计时
     */
    private fun voiceCardCountDown(total: Long) {
        mVoiceCardDisposable?.dispose()
        mVoiceCardDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(total)
            .subscribe({
                voiceCardDuration.postValue(total - it)
            }, {})
    }

    /**
     * 发送模拟消息
     */
    fun sendSimulateMessage(bean: VoiceConmmunicationSimulate) {
        if (callId == 0L) {
            //当前语音通话已经结束
            return
        }
        val targetChatInfo = targetUserBean?.value ?: return
        val relationInfo = netcallBeanData?.value?.relationInfo ?: return
        val targetUser = TargetUserObj()

        val createUserId = createUserId
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
                userType = targetChatInfo.userType
            }
            chatExtra.apply {
                headPic = SessionUtils.getHeaderPic()
                senderId = SessionUtils.getUserId()
                nickname = SessionUtils.getNickName()
                sex = SessionUtils.getSex()
                targetUserObj = targetUser
                userAbcd = AppHelper.getMD5(sId)
                userType = SessionUtils.getUserType()
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
                userType = SessionUtils.getUserType()
            }
            chatExtra.apply {
                headPic = targetChatInfo.headPic
                senderId = targetChatInfo.userId
                nickname = targetChatInfo.nickname
                sex = targetChatInfo.sex
                targetUserObj = targetUser
                userAbcd = AppHelper.getMD5(sId)
                userType = targetChatInfo.userType
            }
        }

        chatExtra.targetUserObj?.intimateLevel = relationInfo.intimateLevel
        chatExtra.targetUserObj?.stranger = GlobalUtils.getStrangerString(relationInfo.stranger)

        val conversationType = if (bean.needRefresh) {
            Conversation.ConversationType.GROUP
        } else {
            Conversation.ConversationType.PRIVATE
        }

        RongCloudManager.sendSimulateMessage(
            tId,
            sId,
            chatExtra,
            conversationType,
            MessageCustomBeanType.Voice_Conmmunication_Simulate,
            bean,
            sId == tId
        )
    }


    private var mVoiceDuration: Disposable? = null

    /**
     * 记录通话时长
     */
    private fun recordCallDuration() {
        updateDurationParamsFlag.value = true
        mVoiceDuration?.dispose()
        mVoiceDuration = ObservableTake.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                duration.value = it
                mVoiceChangeListener?.voiceChange(VOICE_ACCEPT, it)
            }, {})
    }

    /**
     * 设置语音结束
     */
    fun setVoiceFinish() {
        callId = 0
        waitingClose = false
        currentVoiceState.value = VOICE_CLOSE
        VoiceManager.stop()

        VoiceManager.playFinish()

        leaveChannel()
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
        //取消超时定时器
        mDisposable?.dispose()
        //关闭悬浮窗
        VoiceFloatingManager.hideFloatingView()
        //停止倒计时
        mVoiceCardDisposable?.dispose()
        mVoiceDuration?.dispose()

        Observable.timer(1, TimeUnit.SECONDS)
            .subscribe({ VoiceManager.destroy() }, {}, {})
    }

    /**
     * 设置语音接通
     */
    fun setVoiceAccept() {
        currentVoiceState.value = VOICE_ACCEPT
        //停止音效播放
        VoiceManager.stop()
    }


    //对方未加入直播间定时器
    var mOtherNoJoinDisposable: Disposable? = null

    //对方是否加入频道的标记位
    private var otherJoinChannel = false

    /**
     * 对方未加入频道 倒计时
     */
    fun otherNoJoinCountDown() {
        logger.info("AGORA Message 开始未加入频道倒计时：otherJoinChannel = $otherJoinChannel")
        if (otherJoinChannel || mOtherNoJoinDisposable != null) {
            //对方已经加入
            return
        }
        mOtherNoJoinDisposable = Observable.timer(10, TimeUnit.SECONDS)
            .subscribe({
                hangUpVoice()
            }, {})

    }

    private var mDisposable: Disposable? = null

    /**
     * 主叫或者被叫 的等待定时挂断方法
     */
    fun timer() {
//        mVoiceChatViewModel?.createConmmunication(mVoiceChatViewModel?.targetUserBean?.value?.userId ?: 0)
        //超时计算
        mDisposable = Observable.timer(60, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (mType == ConmmunicationUserType.CALLING) {
                    calcelVoice(CancelType.Timeout)
                } else {
                    sendSimulateMessage(VoiceConmmunicationSimulate(VoiceResultType.CANCEL))
                    setVoiceFinish()
                }

            }, {})
    }

    // Tutorial Step 2
    fun joinChannel() {
        var accessToken = agoraToken
        val channelId = channelId
        if (accessToken.isEmpty()) {
            return
        }
        //设置为主播身份
        AgoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        AgoraManager.mRtcEngine?.enableAudio()
        AgoraManager.mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
        //默认开启声音
        AgoraManager.mRtcEngine?.adjustRecordingSignalVolume(100)
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
            hangUpVoice()
        }
        logger.info("$this joinResult = $result")
    }

    // Tutorial Step 3
    private fun leaveChannel() {
        AgoraManager.mRtcEngine?.leaveChannel()
    }

    //接下来是Agora监听
    override fun onRemoteAudioStats(stats: IRtcEngineEventHandler.RemoteAudioStats?) {
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats?) {
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        logger.info("AGORA Message 用户加入频道消息 uid = $uid mOtherNoJoinDisposable = ${mOtherNoJoinDisposable?.isDisposed}")
        if (uid.toLong() != SessionUtils.getUserId()) {
            otherJoinChannel = true
            mOtherNoJoinDisposable?.dispose()
        }
    }

    override fun onLocalVideoStats(stats: IRtcEngineEventHandler.LocalVideoStats?) {
    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        //用户掉线 挂断通话
        logger.info("AGORA Message 用户掉线")
        hangUpVoice()
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
        logger.info("VoiceChatViewModel 加入channel成功 channel = $channel,uid = $uid")
    }

    override fun onLastmileQuality(quality: Int) {
    }

    override fun onRequestToken() {
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
    }

    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats?) {
        logger.info("AGORA Message onLeaveChannel stats = $stats")
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        if (reason == Constants.CONNECTION_CHANGED_BANNED_BY_SERVER) {
            //被服务端踢出（重连失败）
            hangUpVoice()
        }
        logger.info("AGORA Message onConnectionStateChanged state = $state,reason = $reason")
    }

    /**
     * 网络连接中断，且 SDK 无法在 10 秒内连接服务器回调。
     */
    override fun onConnectionLost() {
        logger.info("AGORA Message onConnectionLost")
        val bean = VoiceConmmunicationSimulate(
            VoiceResultType.CONMMUNICATION_FINISH,
            duration.value ?: 0,
            totalBeans = 0, needRefresh = true, callId = callId ?: 0
        )
        sendSimulateMessage(bean)
        setVoiceFinish()
    }

    override fun onAudioRouteChanged(routing: Int) {
        handsFreeState.value = !(routing == Constants.AUDIO_ROUTE_HEADSET || routing == Constants.AUDIO_ROUTE_HEADSETBLUETOOTH)
    }

    override fun onError(err: Int) {
        logger.info("AGORA Message onError err = $err")
    }
}