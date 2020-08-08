package com.julun.huanque.viewmodel

import android.os.Bundle
import android.os.Message
import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDLocation
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.core.net.UserService
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
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
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch
import java.lang.Exception

class MainViewModel : BaseViewModel() {
    //    val userInfo: LiveData<UserDetailInfo> = liveData {
//        val user = queryUserDetailInfo()
//        emit(user)
//    }
    //当前的fragment下标
    val indexData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    private val getInfo = MutableLiveData<Boolean>()//这个作为开关标识

    //免打扰列表
    val blockListData: MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }

    //协程请求示例
    val userInfo: LiveData<UserDetailInfo> = getInfo.switchMap {
        liveData<UserDetailInfo> {
            if (it) {
                request({
                    val user = userService.queryUserDetailInfo().dataConvert()
                    emit(user)
                }, error = { e ->
                    logger("报错了：$e")
                }, final = {
                    logger("最终返回")
                }, needLoadState = true)
            } else {
                logger("getInfo=false 这里根本不会执行")
            }

        }

    }

    fun getInfo() {
        getInfo.value = true
    }

    //rxjava3的请求示例
    val userLevelInfo = MutableLiveData<UserLevelInfo>()

    //传统的rx请求模式
    fun getUserLevelByRx2() {
        requestRx(
            { userService.queryUserLevelInfoBasic(SessionForm()) },
            onSuccess = {
                logger("请求成功结果：it")
                userLevelInfo.value = it
            },
            error = {
                logger("请求报错了$it")
            },
            final = {
                logger("最终返回的")
            },
            loadState = loadState
        )
    }


    //传统的rx请求模式
    fun getUserLevelByRx() {
        userService.queryUserLevelInfoBasic(SessionForm()).handleResponse(makeSubscriber<UserLevelInfo> {
            logger("请求成功结果：it")
            userLevelInfo.value = it
        }.ifError {
            logger("请求报错了$it")
        }.withFinalCall {
            logger("最终返回的")
        }.withSpecifiedCodes(1, 2, 3))
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
                    stranger = result.stranger
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
        RongCloudManager.queryPMessage {
            unreadMsgCount.postValue(it)
        }
    }

    /**
     * 获取免打扰会话列表
     */
    fun getBlockedConversationList() {
        RongIMClient.getInstance().getBlockedConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
            override fun onSuccess(list: List<Conversation>?) {
                val blockedIdList = mutableListOf<String>()
                list?.forEach {
                    blockedIdList.add(it.targetId)
                }
                blockListData.value = blockedIdList
                BusiConstant.blockList = blockedIdList
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                logger("errorCode = $errorCode")
            }

        }, Conversation.ConversationType.PRIVATE)
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}