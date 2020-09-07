package com.julun.huanque.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.bean.events.UnreadCountEvent
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.bean.forms.SaveLocationForm
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

class MainViewModel : BaseViewModel() {

    //当前的fragment下标
    val indexData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
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
                ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()

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
        RongIMClient.getInstance().getMessage(msgId, object : RongIMClient.ResultCallback<io.rong.imlib.model.Message>() {
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
                val bean = JsonUtil.deserializeAsObject<VoiceConmmunicationSimulate>(content.context, VoiceConmmunicationSimulate::class.java)
                bean.sentTime = msg.sentTime

                viewModelScope.launch {
                    request({
                        val result = socialService.netcallResult(NetcallIdForm(bean.callId)).dataConvert()
                        bean.billUserId = result.billUserId
                        bean.duration = result.duration
                        bean.totalBeans = result.totalBeans
                        bean.needRefresh = false

                        val chatExtra = JsonUtil.deserializeAsObject<RoomUserChatExtra>(content.extra, RoomUserChatExtra::class.java)

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

    /**
     * 获取未读数
     */
    fun getUnreadCount() {
        val typeList = arrayOf(Conversation.ConversationType.PRIVATE)
        RongIMClient.getInstance().getUnreadCount(typeList, false, object : RongIMClient.ResultCallback<Int>() {
            override fun onSuccess(p0: Int?) {
                unreadMsgCount.value = p0 ?: 0
                EventBus.getDefault().post(UnreadCountEvent(p0 ?: 0, false))
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
    }


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
                SPUtils.commitObject(SPParamKey.PRIVATE_CHAT_BUBBLE, result.chatBubble ?: return@request)
            })
        }
    }


}