package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.events.UserInfoChangeEvent
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.MessageCustomBeanType
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
import kotlin.math.max

/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:49
 *@描述 会话列表ViewModel
 */
class MessageViewModel : BaseViewModel() {

    //会话列表
    val conversationListData: MutableLiveData<MutableList<LocalConversation>> by lazy { MutableLiveData<MutableList<LocalConversation>>() }

    val blockListData: MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }

    //有变化的数据  <0  刷新整个列表  >=0 刷新单个条目
    val changePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //折叠陌生人消息标识位
    var foldStrangerMsg = false

    //陌生人标识
    var mStranger = false

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
                    conversationListData.value = list
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
                if (!mStranger) {
                    dealWithStableConversation(p0)
                }
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
        try {
            Collections.sort(conList, Comparator { o1, o2 ->
                val time1 = if (o1.conversation.sentTime != 0L) {
                    o1.conversation.sentTime
                } else {
                    o1.strangerInfo[LocalConversation.TIME]?.toLong() ?: 0L
                }
                val time2 = if (o2.conversation.sentTime != 0L) {
                    o2.conversation.sentTime
                } else {
                    o2.strangerInfo[LocalConversation.TIME]?.toLong() ?: 0L
                }
                when {
                    time1 > time2 -> return@Comparator -1
                    time1 == time2 -> return@Comparator 0
                    else -> return@Comparator 1
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

                    if (currentUser?.stranger == true && add && !mStranger) {
                        //包含陌生人数据，直接重新刷新列表
                        getConversationList()
                        return@withContext
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
                val realList = mutableListOf<LocalConversation>()
                //陌生人列表
                val strangerList = mutableListOf<LocalConversation>()
                var strangerLastestTime = 0L
                var strangerUnreadCount = 0
                var targetName = ""

                if (mStranger) {
                    //陌生人消息页面
                    allList.forEach {
                        if (it.showUserInfo?.stranger == true) {
                            realList.add(it)
                        }
                    }
                } else {
                    //首页  消息列表
                    if (foldStrangerMsg) {
                        //折叠陌生人消息
                        allList.forEach {
                            if (it.showUserInfo?.stranger == false || it.conversation.targetId == SystemTargetId.systemNoticeSender || it.conversation.targetId == SystemTargetId.friendNoticeSender) {
                                realList.add(it)
                            } else {
                                if (strangerLastestTime < it.conversation.sentTime) {
                                    strangerLastestTime = it.conversation.sentTime
                                    targetName = it.showUserInfo?.nickname ?: ""
                                }
                                strangerUnreadCount += it.conversation.unreadMessageCount
                                strangerList.add(it)
                            }
                        }
                        //插入陌生人item
                        if (strangerLastestTime > 0) {
                            //包含陌生人数据
                            val info = hashMapOf<String, String>()
                            info.put(LocalConversation.TIME, "$strangerLastestTime")
                            info.put(LocalConversation.UNREADCOUNT, "$strangerUnreadCount")
                            info.put(LocalConversation.NICKNAME, targetName)
                            val strangerConversation = LocalConversation().apply {
                                strangerInfo = info
                            }
                            realList.add(strangerConversation)
                        }
                    } else {
                        realList.addAll(allList)
                    }
                }
                refreshConversationList(realList)
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
                    stranger = user.stranger
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
                    stranger = user.stranger
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

    /**
     * 对固定会话进行处理
     */
    private fun dealWithStableConversation(list: List<Conversation>?) {
        var hasSystem = false
        var hasFriend = false
        list?.forEach {
            if (it.targetId == SystemTargetId.friendNoticeSender) {
                hasFriend = true
            }
            if (it.targetId == SystemTargetId.systemNoticeSender) {
                hasSystem = true
            }
        }
        if (!hasSystem) {
            //系统消息发送自定义消息
            RongCloudManager.sendSimulateMessage(
                SystemTargetId.systemNoticeSender,
                "${SessionUtils.getUserId()}",
                null,
                Conversation.ConversationType.PRIVATE,
                MessageCustomBeanType.SYSTEM_MESSAGE,
                ""
            )
        }

        if (!hasFriend) {
            //鹊友消息发送自定义消息
            RongCloudManager.sendSimulateMessage(
                SystemTargetId.friendNoticeSender,
                "${SessionUtils.getUserId()}",
                null,
                Conversation.ConversationType.PRIVATE,
                MessageCustomBeanType.SYSTEM_MESSAGE,
                ""
            )
        }
        //刷新列表
        if (!hasSystem || !hasFriend) {
            getConversationList()
        }
    }

    /**
     * 用户数据更新
     */
    fun userInfoUpdate(bean: UserInfoChangeEvent) {

        viewModelScope.launch {
            //用户ID
            val userId = bean.userId
            //陌生人状态
            val stranger = bean.stranger

            if (!foldStrangerMsg) {
                //未折叠消息
                doWithSameEvent(userId)
                return@launch
            }

            if (mStranger) {
                //当前处于陌生人列表页面
                if (stranger) {
                    //陌生人状态变化
                    doWithSameEvent(userId)
                } else {
                    //非陌生人状态变化
                    doWithExclusionEvent(userId)
                }
            } else {
                //会话列表页面
                if (stranger) {
                    //会话列表，接收到变为陌生人消息
                    doWithExclusionEvent(userId)
                } else {
                    //会话列表 接收到好友数据变化消息
                    doWithSameEvent(userId)
                }
            }
        }
    }

    /**
     * 接收到互斥消息处理
     * 折叠消息开启   1 消息页面接受到陌生人变动消息   2 陌生人会话页面 接收到好友变动消息
     */
    private fun doWithExclusionEvent(userId: Long) {
        val list = conversationListData.value ?: return
        var index = -1
        list.forEachIndexed { ind, it ->
            if (it.conversation.targetId == "$userId") {
                index = ind
                return@forEachIndexed
            }
        }

        if (index > 0) {
            //当前列表里面存在该会话，直接删除就可以
            list.removeAt(index)
            //显示新的列表
            conversationListData.value = list
        }
        //不存在该会话，什么都不用处理
    }

    /**
     * 接收到相同类型消息处理
     * 折叠消息开启   1 消息页面接受到好友变动消息   2 陌生人会话页面 接收到陌生人变动变动消息
     * 折叠消息关闭   收到变化消息
     */
    private fun doWithSameEvent(userId: Long) {
        viewModelScope.launch {
            val list = conversationListData.value
            var index = -1
            var tempConversation: LocalConversation? = null

            list?.forEachIndexed { ind, it ->
                if (it.conversation.targetId == "$userId") {
                    index = ind
                    tempConversation = it
                    return@forEachIndexed
                }
            }

            if (index >= 0) {
                //1列表当中存在当前会话,刷新用户数据
                withContext(Dispatchers.IO) {
                    val user = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(userId) ?: return@withContext
                    tempConversation?.showUserInfo = user
                    changePosition.postValue(index)
                }

            } else {
                //2列表当中不存在当前会话,不存在  判断本地是否有当前会话(存在本地数据，需要刷新整个列表)
                RongIMClient.getInstance()
                    .getConversation(Conversation.ConversationType.PRIVATE, "$userId", object : RongIMClient.ResultCallback<Conversation>() {
                        override fun onSuccess(p0: Conversation?) {
                            if (p0 == null) {
                                //不存在本地会话，什么都不用处理
                                return
                            } else {
                                //存在本地会话，需要刷新整个列表
                                getConversationList()
                            }
                        }

                        override fun onError(p0: RongIMClient.ErrorCode?) {
                        }
                    })
            }
        }
    }

}