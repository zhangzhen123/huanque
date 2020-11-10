package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.bean.events.OpenPrivateChatRoomEvent
import com.julun.huanque.common.bean.events.UnreadCountEvent
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.constant.SystemTargetId
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/8/4 20:14
 *@描述 直播间向私聊页面传递数据
 */
class PlayerMessageViewModel : BaseViewModel() {

    //直播间内，主播基础数据
    val anchorData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }

    //打开私聊详情
    val privateConversationData: MutableLiveData<OpenPrivateChatRoomEvent> by lazy { MutableLiveData<OpenPrivateChatRoomEvent>() }

    //打开联系人
    val contactsData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //直播间内的消息未读数
    val unreadCountInPlayer: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //参与未读数计算的会话ID列表
    var unreadList = mutableListOf<String>()

    //免打扰列表
    val blockListData: MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }

    //需要刷新会话列表
    val needRefreshConversationFlag: MutableLiveData<EventMessageBean> by lazy { MutableLiveData<EventMessageBean>() }

    //获取免打扰列表成功之后，需要获取未读数的标识位
    var needQuerUnreadCount = false

    /**
     * 获取融云未读消息(直播间使用)
     */
    private  var resultCallback: RongIMClient.ResultCallback<List<Conversation>>?=null
    fun queryRongPrivateCount(targetId: String = "") {
        if (blockListData.value == null) {
            //未获取到免打扰列表
            needQuerUnreadCount = true
            unreadList.clear()
            return
        }
        if (unreadList.contains(targetId)) {
            realUnreadCount()
            return
        }
        if(resultCallback!=null){
            RongCloudManager.removeConversationCallback(resultCallback)
        }
        resultCallback=object : RongIMClient.ResultCallback<List<Conversation>>() {
            override fun onSuccess(list: List<Conversation>?) {
                if (list?.isNotEmpty() != true) {
                    return
                }
                unreadList.clear()
                val blockList = blockListData.value ?: return
                list.forEach {
                    val targetID = it.targetId
                    if (targetID == null || targetID == SystemTargetId.friendNoticeSender || targetID == SystemTargetId.systemNoticeSender) {
                        //不处理系统消息
                        return@forEach
                    }
                    if (blockList.contains(targetID)) {
                        //不处理免打扰消息
                        return@forEach
                    }

                    if (SharedPreferencesUtils.getBoolean(SPParamKey.FOLD_STRANGER_MSG, false)) {
                        //开启折叠消息，需要过滤陌生人消息
                        var currentUser: ChatUser? = null

                        when (val lastMessage = it.latestMessage) {
                            is ImageMessage -> {
                                lastMessage.extra?.let {
                                    currentUser = getMessageUserInfo(it)
                                }
                            }

                            is CustomMessage -> {
                                lastMessage.extra?.let {
                                    currentUser = getMessageUserInfo(it)
                                }
                            }

                            is CustomSimulateMessage -> {
                                lastMessage.extra?.let {
                                    currentUser = getMessageUserInfo(it)
                                }
                            }
                            is TextMessage -> {
                                //文本消息
                                lastMessage.extra?.let {
                                    currentUser = getMessageUserInfo(it)
                                }
                            }
                            else -> {
                            }
                        }
                        if (currentUser?.stranger == true) {
                            //陌生人，过滤
                            return@forEach
                        }
                    }
                    unreadList.add(targetID)
                }
                //获取未读数
                realUnreadCount()
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        }
        RongCloudManager.queryConversationList(resultCallback!!, Conversation.ConversationType.PRIVATE)
    }

    /**
     * 真正获取未读消息的方法
     */
    private fun realUnreadCount() {
        var tempUnreadCount = 0
        var tempQueryCount = 0
        val totalQueryCount = unreadList.size

        unreadList.forEach {
            RongIMClient.getInstance().getUnreadCount(Conversation.ConversationType.PRIVATE, it, object : RongIMClient.ResultCallback<Int>() {
                override fun onSuccess(unreadCount: Int?) {
                    tempUnreadCount += unreadCount ?: 0
                    tempQueryCount++
                    if (tempQueryCount == totalQueryCount) {
                        unreadCountInPlayer.value = tempUnreadCount
                        EventBus.getDefault().post(UnreadCountEvent(tempUnreadCount, true))
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                    tempQueryCount++
                    if (tempQueryCount == totalQueryCount) {
                        unreadCountInPlayer.value = tempUnreadCount
                        EventBus.getDefault().post(UnreadCountEvent(tempUnreadCount, true))
                    }
                }
            })
        }

    }

    /**
     * 获取消息内部的用户信息
     */
    private fun getMessageUserInfo(extra: String): ChatUser? {
        if (extra.isEmpty()) {
            return null
        }
        var user: RoomUserChatExtra? = null
        try {
            user = JsonUtil.deserializeAsObject(extra, RoomUserChatExtra::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var currentUser: ChatUser? = null
        if (user != null) {
            if (user.senderId == SessionUtils.getUserId()) {
                //本人发送消息，取targetUserObj
                currentUser = ChatUser().apply {
                    headPic = user.targetUserObj?.headPic ?: ""
                    nickname = user.targetUserObj?.nickname ?: ""
                    intimateLevel = user.targetUserObj?.intimateLevel ?: 0
                    //欢遇标识
                    meetStatus = user.targetUserObj?.meetStatus ?: ""
                    //用户ID
                    userId = user.targetUserObj?.userId ?: 0
                    //用户性别
                    sex = user.targetUserObj?.sex ?: ""
                    stranger = GlobalUtils.getStrangerBoolean(user.targetUserObj?.stranger ?: "")
                }
            } else {
                //对方发送消息
                currentUser = ChatUser().apply {
                    headPic = user.headPic
                    nickname = user.nickname
                    intimateLevel = user.targetUserObj?.intimateLevel ?: 0
                    //欢遇标识
                    meetStatus = user.targetUserObj?.meetStatus ?: ""
                    //用户ID
                    userId = user.senderId
                    //用户性别
                    sex = user.sex
                    stranger = GlobalUtils.getStrangerBoolean(user.targetUserObj?.stranger ?: "")
                }
            }

        }
        return currentUser
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
                HuanViewModelManager.blockList = blockedIdList

                blockListData.value = blockedIdList
                if (needQuerUnreadCount) {
                    needQuerUnreadCount = false
                    queryRongPrivateCount()
                }
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                logger("errorCode = $errorCode")
            }

        }, Conversation.ConversationType.PRIVATE)
    }

    override fun onCleared() {
        super.onCleared()
        RongCloudManager.removeConversationCallback(resultCallback)
    }
}