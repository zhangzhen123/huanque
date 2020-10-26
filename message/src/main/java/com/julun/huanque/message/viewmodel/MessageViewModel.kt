package com.julun.huanque.message.viewmodel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.beans.ChatRoomBean
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.bean.events.UnreadCountEvent
import com.julun.huanque.common.bean.events.UserInfoChangeEvent
import com.julun.huanque.common.bean.forms.LineStatusForm
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.constant.SystemTargetId
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.*
import com.julun.huanque.message.R
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.math.ceil


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:49
 *@描述 会话列表ViewModel
 */
class MessageViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //获取数据的标记位
    val queryDataFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //会话列表
    val conversationListData: MutableLiveData<MutableList<LocalConversation>> by lazy { MutableLiveData<MutableList<LocalConversation>>() }

    //有变化的数据  <0  刷新整个列表  >=0 刷新单个条目
    val changePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //查询未读消息的标识位
    val queryUnreadCountFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //派单未回复数据
    val chatRoomData: MutableLiveData<ChatRoomBean> by lazy { MutableLiveData<ChatRoomBean>() }

    //主播数据（直播间显示私聊弹窗，会有该数据）
    var anchorData: UserEnterRoomRespBase? = null

    //折叠陌生人消息标识位
    var foldStrangerMsg = false

    //陌生人页面标识
    var mStranger = false

    //直播间内标识位
    var player = false

    //免打扰列表
    var blockListData: MutableList<String>? = null

    //获取免打扰列表成功之后，需要获取会话列表的标识位
    var needQueryConversation = false

    /**
     * 删除会话
     */
    fun removeConversation(cId: String, type: Conversation.ConversationType) {
        RongIMClient.getInstance()
            .removeConversation(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
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
                        //
                        conversationListData.value = removeDuplicates(list)
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }
            })

        //删除会话当中消息
        RongIMClient.getInstance()
            .clearMessages(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
                override fun onSuccess(p0: Boolean?) {
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
        //删除用户数据
        try {
            HuanQueDatabase.getInstance().chatUserDao().removeSingleChatUser(cId.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 去除重复的会话
     */
    private fun removeDuplicates(list: MutableList<LocalConversation>): MutableList<LocalConversation> {
        //会话Id列表
        val targetIdList = mutableListOf<String>()
        val realList = mutableListOf<LocalConversation>()
        list.forEach {
            val targetId = it.conversation.targetId ?: ""
            if (!targetIdList.contains(targetId)) {
                targetIdList.add(targetId)
                realList.add(it)
            }
        }
        return realList
    }

    /**
     * 获取本地会话列表
     */
    fun getConversationList() {
        if (blockListData == null) {
            needQueryConversation = true
            return
        }
        RongIMClient.getInstance()
            .getConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
                override fun onSuccess(p0: List<Conversation>?) {
//                if (!mStranger && !player) {
//                    dealWithStableConversation(p0)
//                }
                    if ((p0 == null || p0.isEmpty()) && !player) {
                        conversationListData.value = null
                        return
                    }
                    showConversation(p0 ?: mutableListOf<Conversation>())

                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            }, Conversation.ConversationType.PRIVATE)
    }

//    /**
//     * 获取免打扰会话列表
//     */
//    fun getBlockedConversationList() {
//        RongIMClient.getInstance().getBlockedConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
//            override fun onSuccess(list: List<Conversation>?) {
//                val blockedIdList = mutableListOf<String>()
//                list?.forEach {
//                    blockedIdList.add(it.targetId)
//                }
//                blockListData.value = blockedIdList
//            }
//
//            override fun onError(errorCode: RongIMClient.ErrorCode?) {
//                logger("errorCode = $errorCode")
//            }
//
//        }, Conversation.ConversationType.PRIVATE)
//    }


    /**
     * 取到Conversation之后的统一处理方法(最后一条消息的顺序进行排序)
     */
    fun sortConversation(conList: MutableList<LocalConversation>): MutableList<LocalConversation> {
        try {
            Collections.sort(conList, Comparator { o1, o2 ->

                anchorData?.let { anchorInfo ->
                    if (o1.conversation.targetId == "${anchorInfo.programId}") {
                        return@Comparator -1
                    } else if (o1.showUserInfo?.userId == anchorInfo.programId) {
                        return@Comparator -1
                    }

                    if (o2.conversation.targetId == "${anchorInfo.programId}") {
                        return@Comparator 1
                    } else if (o2.showUserInfo?.userId == anchorInfo.programId) {
                        return@Comparator 1
                    }
                }
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
        queryUnreadCountFlag.postValue(true)
    }

    /**
     * 刷新消息未读数(只在陌生人页面使用)
     */
    fun refreshUnreadCount(targetId: String) {
        RongIMClient.getInstance()
            .getConversation(
                Conversation.ConversationType.PRIVATE,
                targetId,
                object : RongIMClient.ResultCallback<Conversation>() {
                    override fun onSuccess(p0: Conversation?) {
                        p0 ?: return
                        var realIndex = -1
                        conversationListData.value?.forEachIndexed { index, localConversation ->
                            if (localConversation.conversation.targetId == targetId) {
                                realIndex = index
                                localConversation.conversation = p0
                                return@forEachIndexed
                            }
                        }

                        if (realIndex >= 0) {
                            changePosition.value = realIndex
                        }
                    }

                    override fun onError(p0: RongIMClient.ErrorCode?) {
                    }

                })
    }


    /**
     * 接收到私聊消息之后刷新单个会话
     * @param stranger 消息是否是陌生人发送
     */
    fun refreshConversation(targerId: String, stranger: Boolean) {
//        //判断是否处于免打扰列表当中
//        if (blockListData?.contains(targerId) == true) {
//            //免打扰会话发送过来的消息
//            return
//        }

        RongIMClient.getInstance()
            .getConversation(
                Conversation.ConversationType.PRIVATE,
                targerId,
                object : RongIMClient.ResultCallback<Conversation>() {
                    override fun onSuccess(p0: Conversation?) {
                        if (p0 == null) {
                            return
                        }

                        val oriList = conversationListData.value
                        oriList?.forEachIndexed { index, lmc ->
                            if (lmc.conversation.targetId == targerId) {
                                lmc.conversation = p0
                                //刷新列表（重新排序）
                                if (foldStrangerMsg && stranger != lmc.showUserInfo?.stranger && (targerId != SystemTargetId.systemNoticeSender && targerId != SystemTargetId.friendNoticeSender)) {
                                    //折叠消息开启,陌生人状态有变更，需要在会话列表中删除当前会话
                                    try {
                                        updataStrangerData(targerId.toLong(), stranger)
                                        getConversationList()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                } else {
                                    refreshConversationList(oriList)
                                }
                                return
                            }
                        }

                        //走到这里，标识之前的列表里面没有该会话
                        if (foldStrangerMsg && stranger == mStranger) {
                            //开启陌生人折叠，标识相同，需要在当前会话列表中添加改会话
                            try {
                                if (targerId != SystemTargetId.systemNoticeSender && targerId != SystemTargetId.friendNoticeSender) {
                                    updataStrangerData(targerId.toLong(), stranger)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            return getConversationList()
                        }

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
     * 更新陌生人数据
     * @param userId 用户Id
     * @param stranger 陌生人状态
     */
    private fun updataStrangerData(userId: Long, stranger: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //更新数据库数据
                val user = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(userId)
                if (user != null && user.stranger != stranger) {
                    //更新数据库陌生人状态
                    user.stranger = stranger
                    HuanQueDatabase.getInstance().chatUserDao().insert(user)
                    getConversationList()
                }
            }
        }
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
                    val tempUserList = mutableListOf<ChatUser>()
                    val idCount = idList.size
                    val count = ceil(idCount / 20.toDouble()).toInt()
                    (0 until count).forEach { outIndex ->
                        val tempIdList = mutableListOf<Long>()
                        (0 until 20).forEach { inIndex ->
                            val index = outIndex * 20 + inIndex
                            if (ForceUtils.isIndexNotOutOfBounds(index, idList)) {
                                tempIdList.add(idList[index])
                            }
                        }
                        HuanQueDatabase.getInstance().chatUserDao().queryUsers(tempIdList)?.let {
                            tempUserList.addAll(it)
                        }

                    }
                    tempUserList
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
                        currentUser?.let { user ->
                            withContext(Dispatchers.IO) {
                                //将数据保存到数据库一份
                                HuanQueDatabase.getInstance().chatUserDao().insert(user)
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
                            if (it.conversation.targetId == "${anchorData?.programId}" || it.showUserInfo?.stranger == false ||
                                it.conversation.targetId == SystemTargetId.systemNoticeSender || it.conversation.targetId == SystemTargetId.friendNoticeSender
                            ) {
                                realList.add(it)
                            } else {
                                if (strangerLastestTime < it.conversation.sentTime) {
                                    strangerLastestTime = it.conversation.sentTime
                                    targetName = it.showUserInfo?.nickname ?: ""
                                }
                                if (blockListData?.contains(it.conversation.targetId) != true) {
                                    //不是免打扰消息
                                    strangerUnreadCount += it.conversation.unreadMessageCount
                                    strangerList.add(it)
                                }
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
                    stranger = GlobalUtils.getStrangerBoolean(user.targetUserObj?.stranger ?: "")
                    userType = user.userType
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
                    userType = user.userType
                }
            }

        }
        return currentUser
    }


    /**
     * 刷新会话列表
     */
    private fun refreshConversationList(list: MutableList<LocalConversation>) {
        if (player) {
            anchorData?.let {
                doWithPlayer(list, it)
            }
        }

        val sortResult = sortConversation(list)
        conversationListData.postValue(removeDuplicates(sortResult))
    }


    /**
     * 直播间内做一些处理 直播间内，不显示 系统通知，鹊友消息，陌生人消息
     * 插入模拟主播会话
     */
    private fun doWithPlayer(
        list: MutableList<LocalConversation>,
        anchorInfo: UserEnterRoomRespBase
    ) {
        //直播间内，不显示 系统通知，鹊友消息，陌生人消息
        val deleteList = mutableListOf<LocalConversation>()
        list.forEach {
            val conversation = it.conversation
            val condition = conversation.targetId?.isNotEmpty() != true ||
                    conversation.targetId == SystemTargetId.systemNoticeSender ||
                    conversation.targetId == SystemTargetId.friendNoticeSender
            if (condition) {
                deleteList.add(it)
            }
        }
        deleteList.forEach {
            list.remove(it)
        }

        //模拟插入会话

        //是否包含当前主播的会话
        var hasAnchor = false
        list.forEach {
            if (it.conversation.targetId == "${anchorInfo.programId}" || "${anchorInfo.programId}" == "${it.showUserInfo?.userId}") {
                //会话列表包含当前主播会话
                hasAnchor = true
            }
        }

        if (!hasAnchor) {
            //需要模拟主播聊天会话
            val tempLMC = LocalConversation()
                .apply {
                    showUserInfo = ChatUser(
                        anchorInfo.headPic,
                        "",
                        anchorInfo.programName,
                        anchorInfo.programId,
                        "",
                        SessionUtils.getUserId(),
                        0,
                        "",
                        false
                    )
                    conversation.targetId = "${anchorInfo.programId}"
                }
            //添加模拟聊天
            list.add(tempLMC)
        }
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
                MessageCustomBeanType.FRIEND_MESSAGE,
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
        //陌生人状态变动，更新数据库
        updataStrangerData(bean.userId, bean.stranger)
    }

    /**
     * 刷新系统和鹊友消息(鹊友和系统消息)
     */
    fun refreshSysMessage(targetId: String) {
        RongIMClient.getInstance()
            .getConversation(Conversation.ConversationType.PRIVATE, targetId, object : RongIMClient.ResultCallback<Conversation>() {
                override fun onSuccess(p0: Conversation?) {
                    p0 ?: return
                    var realIndex = -1
                    conversationListData.value?.forEachIndexed { index, localConversation ->
                        if (localConversation.conversation.targetId == targetId) {
                            realIndex = index
                            localConversation.conversation = p0
                            return@forEachIndexed
                        }
                    }

                    if (realIndex >= 0) {
                        changePosition.value = realIndex
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
    }

    /**
     * 接收到互斥消息处理
     * 折叠消息开启   1 消息页面接受到陌生人变动消息   2 陌生人会话页面 接收到好友变动消息
     */
    private fun doWithExclusionEvent(userId: Long) {
        val list = conversationListData.value ?: return
        var index = -1
        var strangerConversation: LocalConversation? = null
        list.forEachIndexed { ind, it ->
            if (it.conversation.targetId == "$userId") {
                index = ind
                strangerConversation = it
                return@forEachIndexed
            }
        }


        if (index >= 0 && strangerConversation != null) {
            //当前列表里面存在该会话，直接删除就可以
            list.removeAt(index)
            //判断是否含有陌生人会话
            var hasStranger = false
            list.forEach {
                if (it.conversation.targetId == null) {
                    hasStranger = true
                }
            }
            if (!hasStranger && !mStranger) {
                //没有陌生人消息会话，添加陌生人消息会话
                val info = hashMapOf<String, String>()

                info.put(
                    LocalConversation.TIME,
                    "${strangerConversation?.conversation?.sentTime ?: 0}"
                )
                val unreadCount = if (blockListData?.contains("$userId") == true) {
                    0
                } else {
                    strangerConversation?.conversation?.unreadMessageCount ?: 0
                }
                info.put(LocalConversation.UNREADCOUNT, "$unreadCount")
                info.put(
                    LocalConversation.NICKNAME,
                    "${strangerConversation?.showUserInfo?.nickname}"
                )
                val strangerConversation = LocalConversation().apply {
                    strangerInfo = info
                }
                list.add(strangerConversation)
            }
            //显示新的列表
            conversationListData.value = removeDuplicates(list)
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
                    val user = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(userId)
                        ?: return@withContext
                    tempConversation?.showUserInfo = user
                    changePosition.postValue(index)
                }

            } else {
                //2列表当中不存在当前会话,不存在  判断本地是否有当前会话(存在本地数据，需要刷新整个列表)
                RongIMClient.getInstance()
                    .getConversation(
                        Conversation.ConversationType.PRIVATE,
                        "$userId",
                        object : RongIMClient.ResultCallback<Conversation>() {
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

    //获取速配数据
    fun chatRoom() {
        viewModelScope.launch {
            request({
                val result = socialService.chatHome().dataConvert()
                chatRoomData.value = result
                SPUtils.commitInt(SPParamKey.Fate_No_Reply_Count, result.fateNoReplyNum)
                getUnreadCount()
            }, {}, needLoadState = true)
        }
    }

    //更新在线状态
    fun updateOnlineStatus(onLine: Boolean) {
        viewModelScope.launch {
            request({
                val status = if (onLine) {
                    ChatRoomBean.Online
                } else {
                    ChatRoomBean.Invisible
                }
                val result = userService.updateOnlineStatus(LineStatusForm(status)).dataConvert()
                val bean = chatRoomData.value ?: ChatRoomBean()
                bean.onlineStatus = status
                chatRoomData.value = bean
                SPUtils.commitInt(SPParamKey.Fate_No_Reply_Count, bean.fateNoReplyNum)
            })
        }
    }

    /**
     * 获取未读数
     */
    fun getUnreadCount() {
        val typeList = arrayOf(Conversation.ConversationType.PRIVATE)
        RongIMClient.getInstance()
            .getUnreadCount(typeList, false, object : RongIMClient.ResultCallback<Int>() {
                override fun onSuccess(p0: Int?) {
                    val count = (p0 ?: 0) + (chatRoomData.value?.fateNoReplyNum ?: 0)
                    unreadMsgCount.value = count
                    EventBus.getDefault().post(UnreadCountEvent(count, false))
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
    }

    /**
     * 更新草稿数据
     */
    fun updateDraft(userId: Long) {
        val conversationList = conversationListData.value ?: return
        var targetIndex = -1
        conversationList.forEachIndexed { index, localConversation ->
            if (localConversation.conversation.targetId == "$userId") {
                targetIndex = index
                return@forEachIndexed
            }
        }
        if (targetIndex >= 0) {
            changePosition.value = targetIndex
        }
    }

//    /**
//     * 显示桌面未读数量
//     */
//    fun showLogoCount() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val mNotificationManager = CommonInit.getInstance().getContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
//            val channelId = getChannelId(mNotificationManager ?: return) ?: return
//            val builder = NotificationCompat.Builder(CommonInit.getInstance().getContext(), channelId)
//            builder.setSmallIcon(R.mipmap.ic_launcher)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setTicker("title")
//                .setAutoCancel(true)
//                .setContentTitle("contentTitle")
//                .setContentText("contentText")
//
//            mNotificationManager.notify(123, builder.build());
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    fun getChannelId(mNotificationManager: NotificationManager): String? {
//
//        val channelId = "1"
//        val channelName = "com.julun.huanque"
//        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
//        channel.enableLights(true) //显示桌面红点
//        channel.lightColor = Color.RED
//        channel.setShowBadge(true)
//        mNotificationManager?.createNotificationChannel(channel)
//        return channel.id
//    }

}