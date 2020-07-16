package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.SystemTargetId
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.sortList
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:49
 *@描述 会话列表ViewModel
 */
class MessageViewModel : BaseViewModel() {

    //会话列表
    val conversationListData: MutableLiveData<MutableList<LocalConversation>> by lazy { MutableLiveData<MutableList<LocalConversation>>() }

    val blockListData : MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }

    //有变化的数据  <0  刷新整个列表  >=0 刷新单个条目
    val changePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    /**
     * 删除会话
     */
    fun removeConversation(cId: String, type: Conversation.ConversationType) {
        RongIMClient.getInstance().removeConversation(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
                //刷新会话列表
                val list = conversationListData.value
                if (list == null || list.isEmpty()) {
                    return
                }
                var tempData: LocalConversation? = null
                list.forEach {
                    if (it.conversation.targetId == cId) {
                        //匹配到自己
                        tempData = it
                        return@forEach
                    }
                }
                tempData?.let {
                    list.remove(it)
                    changePosition.value = -1
                }
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }
        })

        //删除会话当中消息
        RongIMClient.getInstance().clearMessages(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
    }

    /**
     * 获取本地会话列表
     */
    fun getConversationList() {
        RongIMClient.getInstance().getConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
            override fun onSuccess(p0: List<Conversation>?) {
                if (p0 == null || p0.isEmpty()) {
                    return
                }
                showConversation(p0)

            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
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
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                logger("errorCode = $errorCode")
            }

        }, Conversation.ConversationType.PRIVATE)
    }


    /**
     * 取到Conversation之后的统一处理方法(最后一条消息的顺序进行排序)
     */
    fun sortConversation(conList: MutableList<LocalConversation>): MutableList<LocalConversation> {
        Collections.sort(conList, Comparator { o1, o2 ->
            when {
                o1.conversation.sentTime > o2.conversation.sentTime -> return@Comparator -1
                o1.conversation.sentTime == o2.conversation.sentTime -> return@Comparator 0
                else -> return@Comparator 1
            }

        })
        return conList
    }

    /**
     * 查询私聊未读消息数
     */
    fun queryRongPrivateCount() {
        RongCloudManager.queryPMessage {
            unreadMsgCount.postValue(it)
        }
    }

    /**
     * 接收到私聊消息之后刷新单个会话
     */
    fun refreshConversation(targerId: String) {
        RongIMClient.getInstance()
            .getConversation(Conversation.ConversationType.PRIVATE, targerId, object : RongIMClient.ResultCallback<Conversation>() {
                override fun onSuccess(p0: Conversation?) {
                    if (p0 == null) {
                        return
                    }
                    val oriList = conversationListData.value
                    if (oriList != null) {
                        oriList.forEachIndexed { index, lmc ->
                            if (lmc.conversation.targetId == p0.targetId) {
                                lmc.conversation = p0
                                //刷新列表（重新排序）
                                refreshConversationList(oriList)
                                return
                            }
                        }
                    }

                    //走到这里，标识之前的列表里面没有该会话
                    val singleList = mutableListOf<Conversation>().apply {
                        add(p0)
                    }
                    showConversation(singleList, true)
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
    }

    /**
     * 对conversation进行处理并显示
     * @param p0 conversation列表
     * @param add 是否新增
     */
    private fun showConversation(p0: List<Conversation>, add: Boolean = false) {
        viewModelScope.launch {
            //获取id列表
            val idList = mutableListOf<Long>()
            p0.forEach {
                if (it.conversationType == Conversation.ConversationType.PRIVATE) {
                    //私聊消息
                    try {
                        val targetId = it.targetId
                        if (targetId != SystemTargetId.friendNoticeSender && targetId != SystemTargetId.systemNoticeSender) {
                            idList.add(it.targetId.toLong())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            withContext(Dispatchers.IO) {
                //排序后的conversation
                //对应的用户数据
                val userList = async {
                    HuanQueDatabase.getInstance().chatUserDao().queryUsers(idList)
                }
                //组装数据
                val localConversationList = mutableListOf<LocalConversation>()

                p0.forEach { conversation ->
                    var currentUser: ChatUser? = null
                    userList.await()?.forEach { chatUser ->
                        if (conversation.targetId == "${chatUser.userId}") {
                            currentUser = chatUser
                            return@forEach
                        }
                    }
                    if (currentUser == null) {
                        val lastMessage = conversation.latestMessage
                        when (lastMessage) {
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
                        }

                    }
                    val localConversation = LocalConversation(currentUser, conversation)
                    localConversationList.add(localConversation)
                }
                val allList = mutableListOf<LocalConversation>()

                if (add) {
                    conversationListData.value?.let { list ->
                        allList.addAll(list)
                    }
                }
                allList.addAll(localConversationList)
                refreshConversationList(allList)
            }
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
                }
            }

        }
        return currentUser
    }


    /**
     * 刷新会话列表
     */
    private fun refreshConversationList(list: MutableList<LocalConversation>) {
        val sortResult = sortConversation(list)
        conversationListData.postValue(sortResult)

    }
}