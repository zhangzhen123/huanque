package com.julun.huanque.viewmodel

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.viewmodel.HuanQueViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel : BaseViewModel() {

    //当前的fragment下标
    val indexData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    private val liveRoomService: LiveRoomService by lazy { Requests.create(LiveRoomService::class.java) }

    //进入直播间之前的基础信息
    val baseData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }

//    //未读消息数量
//    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val newUserBean: MutableLiveData<NewUserGiftBean> by lazy { MutableLiveData<NewUserGiftBean>() }

    //用户隐私协议是否签署
    val userProtocolSignFlag: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }

    /**
     * 获取新手礼包数据
     */
    fun getNewUserGift() {
        viewModelScope.launch {
            request({
                val bag = userService.newUserBag().dataConvert()
                if (bag.received == BusiConstant.False && SessionUtils.getSex() == Sex.MALE) {
                    //男性未领取，显示弹窗
                    newUserBean.value = bag
                }
                if (SessionUtils.getSex() == Sex.FEMALE && !SPUtils.getBoolean(GlobalUtils.getNewUserKey(SessionUtils.getUserId()), false)) {
                    //女性 未观看视频
                    newUserBean.value = bag
                }
                if (SessionUtils.getSex() == Sex.MALE && bag.received == BusiConstant.True) {
                    //男性 领取过 保存
                    SPUtils.commitBoolean(GlobalUtils.getNewUserKey(SessionUtils.getUserId()), true)
                }
            })
        }
    }

    /**
     * 领取新手礼包
     */
    fun receiveNewUserBag() {
        viewModelScope.launch {
            request({
                userService.receiveNewUserBag().dataConvert()
            })
        }
    }


    /**
     * 获取语音会话详情
     */
    fun getVoiceCallInfo(callId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.voiceCallInfo(NetcallIdForm(callId)).dataConvert()
                val bundle = Bundle()
                bundle.putString(ParamConstant.TYPE, ConmmunicationUserType.CALLED)
                bundle.putSerializable(ParamConstant.NetCallBean, result)
                ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle)
                    .navigation()

            })
        }
    }

    /**
     * 获取对方数据以及亲密度数据
     */
    fun insertMessage(userId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.userInfo(FriendIdForm(userId)).dataConvert()
                //插入模拟消息
                val targetUser = TargetUserObj()

                val chatExtra = RoomUserChatExtra()

                val sId = "$userId"
                targetUser.apply {
                    headPic = SessionUtils.getHeaderPic()
                    nickname = SessionUtils.getNickName()
                    meetStatus = result.meetStatus
                    this.userId = SessionUtils.getUserId()
                    sex = SessionUtils.getSex()
                    intimateLevel = result.intimateLevel
                    stranger = GlobalUtils.getStrangerString(result.stranger)
                }

                chatExtra.apply {
                    headPic = result.headPic
                    senderId = result.userId
                    nickname = result.nickname
                    sex = result.sex
                    targetUserObj = targetUser
                    userAbcd = AppHelper.getMD5(sId)
                }

                val bean = VoiceConmmunicationSimulate(type = VoiceResultType.MINE_REFUSE)

                RongCloudManager.sendSimulateMessage(
                    "$userId",
                    sId,
                    chatExtra,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Voice_Conmmunication_Simulate,
                    bean
                )
            }, {})
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
     * 保存定位地址
     */
    fun saveLocation(form: SaveLocationForm) {
        viewModelScope.launch {
            request({
                userService.saveLocation(form = form).dataConvert()
            })
        }
    }

    /**
     * 刷新语音通话结果
     */
    fun netcallResult(msgId: Int) {
        RongIMClient.getInstance()
            .getMessage(msgId, object : RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
                override fun onSuccess(msg: io.rong.imlib.model.Message?) {
                    val content = msg?.content
                    if (content is CustomSimulateMessage && content.type == MessageCustomBeanType.Voice_Conmmunication_Simulate) {
                        //该ID对应的消息是模拟插入的语音消息，进行刷新操作
                        getCallResult(msg)
                    } else {
                        //该消息ID不是插入的语音消息，直接删除
                        GlobalUtils.removeSingleRefreshMessageId(msgId)
                    }

                }

                override fun onError(code: RongIMClient.ErrorCode?) {
                    logger("netcallResult code = ${code}")
                }
            })
    }

    private fun getCallResult(msg: io.rong.imlib.model.Message) {
        val content = msg.content
        if (content is CustomSimulateMessage && content.type == MessageCustomBeanType.Voice_Conmmunication_Simulate) {
            try {
                val bean = JsonUtil.deserializeAsObject<VoiceConmmunicationSimulate>(
                    content.context,
                    VoiceConmmunicationSimulate::class.java
                )
                bean.sentTime = msg.sentTime

                viewModelScope.launch {
                    request({
                        val result =
                            socialService.netcallResult(NetcallIdForm(bean.callId)).dataConvert()
                        bean.billUserId = result.billUserId
                        bean.duration = result.duration
                        bean.totalBeans = result.totalBeans
                        bean.needRefresh = false

                        val chatExtra = JsonUtil.deserializeAsObject<RoomUserChatExtra>(
                            content.extra,
                            RoomUserChatExtra::class.java
                        )

                        RongCloudManager.sendSimulateMessage(
                            msg.targetId, msg.senderUserId, chatExtra,
                            Conversation.ConversationType.PRIVATE,
                            MessageCustomBeanType.Voice_Conmmunication_Simulate,
                            bean
                        )
                        RongIMClient.getInstance().deleteMessages(intArrayOf(msg.messageId))
                        GlobalUtils.removeSingleRefreshMessageId(msg.messageId)
                    })
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    /**
//     * 获取未读数
//     */
//    fun getUnreadCount() {
//        val typeList = arrayOf(Conversation.ConversationType.PRIVATE)
//        RongIMClient.getInstance()
//            .getUnreadCount(typeList, false, object : RongIMClient.ResultCallback<Int>() {
//                override fun onSuccess(p0: Int?) {
//                    unreadMsgCount.value = p0 ?: 0
//                    EventBus.getDefault().post(UnreadCountEvent(p0 ?: 0, false))
//                }
//
//                override fun onError(p0: RongIMClient.ErrorCode?) {
//                }
//
//            })
//    }


    /**
     * 刷新某些语音消息
     */
    fun refreshMessage() {
        //如果有语音消息有问题，需要重新处理
        val oriSet = GlobalUtils.getNeedRefreshMessageIdSet()
        //需要刷新一些消息
        try {
            val iterator = oriSet.iterator()
            if (iterator.hasNext()) {
                val msgId = oriSet.iterator().next().toInt()
                netcallResult(msgId)
//                refreshCallId(msgId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取气泡设置
     */
    fun getSetting() {
        viewModelScope.launch {
            request({
                val result = userService.settings().dataConvert()
                val chatBubble = result.chatBubble
                if (chatBubble != null) {
                    SPUtils.commitObject(SPParamKey.PRIVATE_CHAT_BUBBLE, chatBubble)
                } else {
                    SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
                }
            })
        }
    }

    /**
     * 检查协议
     */
    fun checkProtocol(protocol: String) {
        viewModelScope.launch {
            request({
                val result = userService.checkProtocol(CheckProtocolForm(protocol)).dataConvert()
                userProtocolSignFlag.value = result.sign
            }, {})
        }
    }

    /**
     * 获取上次观看数据
     */
    fun lastWatch(programId: Long) {
        viewModelScope.launch {
            request({

                val form = if (programId == 0L) {
                    ProgramIdForm()
                } else {
                    ProgramIdForm(programId)
                }
                val result = liveRoomService.getLivRoomBase(form).dataConvert(intArrayOf(1201, 1202))
                baseData.value = result
            }, {})
        }
    }

    /**
     * 获取免打扰会话列表
     */
    fun getBlockedConversationList() {
        RongIMClient.getInstance().getBlockedConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
            override fun onSuccess(list: List<Conversation>?) {
                //保存在数据库
                val targetList = mutableListOf<String>()
                list?.forEach { targetList.add(it.targetId) }
                HuanViewModelManager.blockList = targetList
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                logger("errorCode = $errorCode")
            }

        }, Conversation.ConversationType.PRIVATE)
    }

    //发送搭讪消息使用的Disposable
    private var mSendAccountDisposable: Disposable? = null

    /**
     * 发送搭讪消息的定时器
     */
    fun accostMessageInterval(bean: QuickAccostResult) {
        mSendAccountDisposable?.dispose()
        val msgList = bean.msgList
        mSendAccountDisposable = Observable.interval(200, TimeUnit.MILLISECONDS)
            .take(msgList.size.toLong())
            .subscribe({
                val index = it.toInt()
                if (ForceUtils.isIndexNotOutOfBounds(index, msgList)) {
                    sendAccostMessage(msgList[index])
                }
            }, {})
    }

    /**
     * 发送消息
     */
    private fun sendAccostMessage(accostMsg: AccostMsg) {
        val targetUser = accostMsg.targetUserInfo
        //设置本人数据
        val user = RoomUserChatExtra().apply {
            headPic = SessionUtils.getHeaderPic()
            senderId = SessionUtils.getUserId()
            nickname = SessionUtils.getNickName()
            sex = SessionUtils.getSex()
            chatBubble = if (targetUser.intimateLevel >= 4) {
                //亲密度达到4级，有气泡权限
                SPUtils.getObject<ChatBubble>(SPParamKey.PRIVATE_CHAT_BUBBLE, ChatBubble::class.java)
            } else {
                null
            }
        }
        //设置本人数据
        RongCloudManager.resetUSerInfoPrivate(user)

        if (accostMsg.contentType == "Text") {
            //文本消息
            RongCloudManager.send(accostMsg.content, "${targetUser.userId}", targetUserObj = targetUser.apply { fee = accostMsg.consumeBeans })
        } else if (accostMsg.contentType == "Gift") {
            //送礼消息
            val msg = RongCloudManager.obtainCustomMessage(
                "${targetUser.userId}",
                targetUser,
                Conversation.ConversationType.PRIVATE,
                MessageCustomBeanType.Gift,
                accostMsg.gift
            )
            RongCloudManager.sendCustomMessage(msg)
        }

    }
}