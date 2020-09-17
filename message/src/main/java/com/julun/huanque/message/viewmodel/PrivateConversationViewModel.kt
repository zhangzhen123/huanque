package com.julun.huanque.message.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.bean.events.UserInfoChangeEvent
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.SendMsgForm
import com.julun.huanque.common.bean.forms.SendRoomForm
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.rong.imlib.IRongCallback
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit
import kotlin.Exception

/**
 *@创建者   dong
 *@创建时间 2020/7/1 10:16
 *@描述 私聊会话ViewModel
 */
class PrivateConversationViewModel : BaseViewModel() {

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //消息列表
    val messageListData: MutableLiveData<MutableList<Message>> by lazy { MutableLiveData<MutableList<Message>>() }

    //增加消息
    val addMessageData: MutableLiveData<Message> by lazy { MutableLiveData<Message>() }

    //message有变化(添加新消息 传true  添加历史消息 传false)
    val messageChangeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //变化的消息列表
    var changeMessageList = mutableListOf<Message>()

    //没有更多了
    var noMoreState: Boolean = false

    //首次获取历史记录成功标识位
    val firstSuccessState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //对方ID
    val targetIdData: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    // 对方数据
    val chatInfoData: MutableLiveData<ChatUser> by lazy { MutableLiveData<ChatUser>() }

    //基础数据
    val basicBean: MutableLiveData<ConversationBasicBean> by lazy { MutableLiveData<ConversationBasicBean>() }

    //私信 道具列表
    val propListData: MutableLiveData<MutableList<PrivateProp>> by lazy { MutableLiveData<MutableList<PrivateProp>>() }

    //亲密度数据
    val intimateData: MutableLiveData<IntimateBean> by lazy { MutableLiveData<IntimateBean>() }

    //送礼成功(送礼成功之后，发送自定义消息)
    val sendGiftSuccessData: MutableLiveData<ChatGift> by lazy { MutableLiveData<ChatGift>() }

    //余额数据
    val balance: LiveData<Long> by lazy { BalanceUtils.getBalance() }

    //当前发送消费花费金额
    val msgFeeData: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //消息状态变化变化使用
    val msgData: MutableLiveData<Message> by lazy { MutableLiveData<Message>() }

    //显示余额不足弹窗标识位
    val balanceNotEnoughFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //传送门结果
    val sendRoomIndoData: MutableLiveData<SendRoomInfo> by lazy { MutableLiveData<SendRoomInfo>() }

    //送礼弹窗的显示标记位
    val sendGiftShowFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //播放动画标识位
    val startAnimationData: MutableLiveData<ChatGift> by lazy { MutableLiveData<ChatGift>() }

    //气泡
    val bubbleData: MutableLiveData<ChatBubble> by lazy { MutableLiveData<ChatBubble>() }

    //用户等级变化消息
    val userExpChangeEvent: MutableLiveData<UserExpChangeEvent> by lazy { MutableLiveData<UserExpChangeEvent>() }

    //操作类型
    var operationType = ""

    //小鹊语料数据
    var wordList = mutableListOf<ActiveWord>()

    //当前选中小鹊语料
    var currentActiveWord: ActiveWord? = null

    //语料position（当前已显示）
    var wordPosition = -1

    //是否允许显示小鹊(默认允许)
    var enableShowXiaoQue = true

    //是否从直播页面跳转过来
    var fromPlayer = false

    //小鹊冷却倒计时
    private var mCoolingDisposable: Disposable? = null

    /**
     * 小鹊冷却时间(被动显示)
     */
    fun xiaoqueCountDown() {
        mCoolingDisposable?.dispose()
        //间隔5分钟
        mCoolingDisposable = Observable.timer(5 * 60, TimeUnit.SECONDS)
            .doOnSubscribe { enableShowXiaoQue = false }
            .subscribe({
                enableShowXiaoQue = true
            }, {})
    }

    /**
     * 获取消息列表
     * @param first 是否是首次获取历史记录
     */
    fun getMessageList(oldestId: Int = -1, first: Boolean = false) {
        val targetId = targetIdData.value ?: return
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, "$targetId"
            , oldestId, 20, object : RongIMClient.ResultCallback<MutableList<Message>>() {
                override fun onSuccess(p0: MutableList<Message>?) {
                    val queryMessageList = mutableListOf<Message>()
                    if (p0 != null) {
                        queryMessageList.addAll(p0)
                    }
                    queryMessageList.reverse()
                    if (queryMessageList.size < 20) {
                        //消息全部获取结束，添加提示消息
                        queryMessageList.add(0, obtainRiskMessage())
                    }
                    noMoreState = (p0?.size ?: 0) < 20
                    val list = messageListData.value
                    if (list != null) {
                        changeMessageList.clear()
                        changeMessageList.addAll(queryMessageList)
                        messageChangeState.postValue(false)
                    } else {
                        messageListData.value = queryMessageList
                    }
                    if (first) {
                        firstSuccessState.postValue(true)
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {

                }
            })
    }

    /**
     * 生成风险提示消息
     */
    private fun obtainRiskMessage(): Message {
        val cusSimulateMessage = CustomSimulateMessage.obtain().apply {
            type = MessageCustomBeanType.PrivateChatRiskWarning
            context = JsonUtil.serializeAsString("欢鹊提倡文明聊天，发布色情、欺骗、违法违规等内容，将被系统永久封禁账号。")
        }
        return Message.obtain("", Conversation.ConversationType.PRIVATE, cusSimulateMessage).apply { senderUserId = "system" }
    }

    /**
     * 删除数据库中单个消息
     */
    fun deleteSingleMessage(msgId: Int) {
        RongIMClient.getInstance().deleteMessages(intArrayOf(msgId))
    }

    /**
     * 接收到当前消息，直接显示
     */
    fun addMessage(message: Message) {
        val content = message.content
        if (content is CustomMessage && content.type == MessageCustomBeanType.Expression_Animation) {
            //动画消息
            changeMessageList.clear()
            changeMessageList.add(message)
            messageChangeState.postValue(true)
        } else {
            addMessageData.value = message
        }
        foWithReceiveMessage(message)
    }

    /**
     * 对接收到到的消息进行处理
     */
    fun foWithReceiveMessage(message: Message) {
        RongIMClient.getInstance().setMessageReceivedStatus(message.messageId, Message.ReceivedStatus(1))
        //        }
        if (message.senderUserId != "${SessionUtils.getUserId()}") {
            //收到对方消息
            EventBus.getDefault().post(EventMessageBean(message.targetId ?: "", chatInfoData.value?.stranger ?: false))
        }
    }


    /**
     * 获取私聊基础信息
     */
    fun chatBasic(targetId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.chatBasic(FriendIdForm(targetId)).dataConvert()
                //设置本人数据
                val mineInfo = result.usr
                val user = RoomUserChatExtra().apply {
                    headPic = mineInfo.headPic
                    senderId = mineInfo.userId
                    nickname = mineInfo.nickname
                    sex = mineInfo.sex
                    chatBubble = if (result.intimate.intimateLevel >= 4) {
                        //亲密度达到4级，有气泡权限
                        bubbleData.value
                    } else {
                        null
                    }
                }
                RongCloudManager.resetUSerInfoPrivate(user)

                chatInfoData.value = result.friendUser.apply { stranger = result.stranger }
                intimateData.value = result.intimate
                msgFeeData.value = result.msgFee
                propListData.value = result.propList
                basicBean.value = result
                BalanceUtils.saveBalance(result.beans)

                withContext(Dispatchers.IO) {
                    //判断当前用户是否和数据库数据保持一致
                    val friendUser = result.friendUser.apply {
                        intimateLevel = result.intimate.intimateLevel
                        meetStatus = result.meetStatus
                        stranger = result.stranger
                    }
                    val userInDb = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(friendUser.userId)
                    if (friendUser.toString() != (userInDb?.toString() ?: "")) {
                        //数据不一致，需要保存用户数据
                        HuanQueDatabase.getInstance().chatUserDao().insert(friendUser)
                        EventBus.getDefault().post(UserInfoChangeEvent(friendUser.userId, friendUser.stranger))
                    }
                }
            }, {
                //设置本人数据
                val user = RoomUserChatExtra().apply {
                    headPic = SessionUtils.getHeaderPic()
                    senderId = SessionUtils.getUserId()
                    nickname = SessionUtils.getNickName()
                    sex = SessionUtils.getSex()
                    chatBubble = bubbleData.value
                }
                RongCloudManager.resetUSerInfoPrivate(user)
            })
        }

    }

    /**
     * 清除未读数
     */
    fun clearUnreadCount(targerId: String) {
        RongIMClient.getInstance()
            .getConversation(Conversation.ConversationType.PRIVATE, targerId, object : RongIMClient.ResultCallback<Conversation>() {
                override fun onSuccess(conversation: Conversation?) {
                    if (conversation == null) {
                        return
                    }
                    val unreadCount = conversation.unreadMessageCount
                    if (unreadCount > 0) {
                        //未读数大于0，需要设置所有消息已读
                        RongIMClient.getInstance().clearMessagesUnreadStatus(Conversation.ConversationType.PRIVATE, targerId)
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    var stranged = false
                                    val userData = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(targerId.toLong())
                                    if (userData == null) {
                                        val userInfo = GlobalUtils.getUserInfoFromMessage(conversation.latestMessage ?: return@withContext)
                                        stranged = userInfo?.stranger ?: false
                                    } else {
                                        stranged = userData?.stranger ?: false
                                    }
                                    EventBus.getDefault().post(EventMessageBean(targerId, stranged, onlyRefreshUnReadCount = true))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
    }

    /**
     * 获取小鹊语料
     */
    fun getActiveWord() {
        viewModelScope.launch {
            request({
                val result = socialService.getActiveWord(FriendIdForm(targetIdData.value ?: 0)).dataConvert()
                wordList.clear()
                wordList.addAll(result.activeList)
                wordPosition = -1
            })
        }
    }

    /**
     * 发送消息接口
     * @param localMsg 在本地显示的消息
     */
    fun sendMsg(targetId: Long, content: String, targetUser: TargetUserObj, type: String = "", localMsg: Message? = null) {
        viewModelScope.launch {
            request({
                val result = socialService.sendMsg(SendMsgForm(targetId, content)).dataConvert(intArrayOf(ErrorCodes.BALANCE_NOT_ENOUGH))
                BalanceUtils.saveBalance(result.beans)
                if (basicBean.value?.chatTicketCnt ?: 0 <= 0) {
                    msgFeeData.value = result.consumeBeans
                }
                basicBean.value?.chatTicketCnt = result.chatTicketCnt
                propListData.value = result.propList
                if (type.isEmpty()) {
                    localMsg?.sentStatus = Message.SentStatus.SENT
                    msgData.value = localMsg
                    RongCloudManager.send(content, "$targetId", targetUserObj = targetUser.apply { fee = result.consumeBeans }) { result, message ->
                        if (!result) {
                            //发送失败
                            localMsg?.messageId = message.messageId
                            sendMessageFail(localMsg ?: return@send, MessageFailType.RONG_CLOUD)
                        } else {
                            localMsg?.sentStatus = message.sentStatus
                            msgData.value = localMsg
                        }
                    }
                } else {
                    //发送特权表情消息
                    RongCloudManager.sendCustomMessage(
                        "$targetId",
                        targetUser.apply { fee = result.consumeBeans },
                        Conversation.ConversationType.PRIVATE, MessageCustomBeanType.Expression_Privilege, content
                    ) { result, message ->
                        if (!result) {
                            //发送失败
                            localMsg?.messageId = message.messageId
                            sendMessageFail(localMsg ?: return@sendCustomMessage, MessageFailType.RONG_CLOUD)
                        } else {
                            localMsg?.sentStatus = message.sentStatus
                            msgData.value = localMsg
                        }
                    }
                }
            }, {
                sendMsgFailInWeb(it)

                sendMessageFail(localMsg ?: return@request, MessageFailType.WEB)
            })
        }
    }

    /**
     * 消息发送失败
     */
    fun sendMessageFail(msg: Message, type: String) {
        msg.sentStatus = Message.SentStatus.FAILED
        val hashMap = GlobalUtils.addExtra(
            msg.extra,
            ParamConstant.MSG_FAIL_TYPE, type
        )
        msgData.value = msg.apply { extra = JsonUtil.serializeAsString(hashMap) }
    }

    /**
     * 发送图片消息
     */
    fun sendPic(uploader: IRongCallback.MediaMessageUploader?, targetId: Long, content: String) {
        viewModelScope.launch {
            request({
                val result = socialService.sendPic(SendMsgForm(targetId, content)).dataConvert()
                BalanceUtils.saveBalance(result.beans)
                if (basicBean.value?.chatTicketCnt ?: 0 <= 0) {
                    msgFeeData.value = result.consumeBeans
                }
                basicBean.value?.chatTicketCnt = result.chatTicketCnt
                uploader?.success(Uri.parse(content))
            }, {
                if (it !is ResponseError) {
                    ToastUtils.show("网络不可用，发送失败")
                } else {
                    when (it.busiCode) {
                        ErrorCodes.BALANCE_NOT_ENOUGH -> {
                            balanceNotEnoughFlag.value = true
                        }
                        else -> {
                        }
                    }
                }
                uploader?.error()
            })
        }
    }

    /**
     * 发送骰子表情
     */
    fun sendDice(targetId: Long, content: String, localMsg: Message) {
        viewModelScope.launch {
            request({
                val result = socialService.sendDice(SendMsgForm(targetId, content)).dataConvert()
                BalanceUtils.saveBalance(result.beans)
                if (basicBean.value?.chatTicketCnt ?: 0 <= 0) {
                    msgFeeData.value = result.consumeBeans
                }
                basicBean.value?.chatTicketCnt = result.chatTicketCnt
                try {
                    val cMessage = localMsg.content as? CustomMessage

                    val user = JsonUtil.deserializeAsObject<RoomUserChatExtra>(cMessage?.extra ?: "", RoomUserChatExtra::class.java)
                    user.targetUserObj?.fee = result.consumeBeans
                    (localMsg.content as? CustomMessage)?.extra = JsonUtil.serializeAsString(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                RongCloudManager.sendCustomMessage(localMsg) { result, message ->
                    if (!result) {
                        //发送失败
                        localMsg.messageId = message.messageId
                        sendMessageFail(localMsg, MessageFailType.RONG_CLOUD)
                    } else {
                        localMsg.sentStatus = message.sentStatus
                        msgData.value = localMsg
                    }
                }
            }, {
                sendMsgFailInWeb(it)
                sendMessageFail(localMsg ?: return@request, MessageFailType.WEB)
            })
        }
    }

    /**
     * 发送猜拳表情
     */
    fun sendFinger(targetId: Long, content: String, localMsg: Message) {
        viewModelScope.launch {
            request({
                val result = socialService.sendFinger(SendMsgForm(targetId, content)).dataConvert()
                BalanceUtils.saveBalance(result.beans)
                if (basicBean.value?.chatTicketCnt ?: 0 <= 0) {
                    msgFeeData.value = result.consumeBeans
                }
                basicBean.value?.chatTicketCnt = result.chatTicketCnt
                try {
                    val cMessage = localMsg.content as? CustomMessage

                    val user = JsonUtil.deserializeAsObject<RoomUserChatExtra>(cMessage?.extra ?: "", RoomUserChatExtra::class.java)
                    user.targetUserObj?.fee = result.consumeBeans
                    (localMsg.content as? CustomMessage)?.extra = JsonUtil.serializeAsString(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                RongCloudManager.sendCustomMessage(localMsg) { result, message ->

                    if (!result) {
                        //发送失败
                        localMsg.messageId = message.messageId
                        sendMessageFail(localMsg, MessageFailType.RONG_CLOUD)
                    } else {
                        //发送成功
                        localMsg.sentStatus = message.sentStatus
                        msgData.value = localMsg
                    }
                }
            }, {
                sendMsgFailInWeb(it)
                sendMessageFail(localMsg ?: return@request, MessageFailType.WEB)
            })
        }
    }


    override fun onCleared() {
        super.onCleared()
        mCoolingDisposable?.dispose()
    }


    /**
     * 更新亲密度
     */
    fun updateIntimate(intimateLevel: Int, stranger: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val user = HuanQueDatabase.getInstance().chatUserDao().querySingleUser(targetIdData.value ?: 0) ?: return@withContext
                if (user.intimateLevel != intimateLevel || user.stranger != stranger) {
                    //需要更新亲密度数据
                    user.intimateLevel = intimateLevel
                    user.stranger = stranger
                    HuanQueDatabase.getInstance().chatUserDao().insert(user)
                }
            }

        }
    }

    /**
     * 计算动画结果
     */
    fun calcuteAnimationResult(name: String): String {
        when (name) {
            "[猜拳]" -> {
                return when (java.util.Random().nextInt(3) + 1) {
                    1 -> {
                        FingerGuessingResult.PAPER
                    }
                    2 -> {
                        FingerGuessingResult.ROCK
                    }
                    else -> {
                        FingerGuessingResult.SCISSORS
                    }
                }
            }
            "[骰子]" -> {
                return "${java.util.Random().nextInt(6) + 1}"
//                logger.info("Message 骰子结果 $result")
            }
            else -> {
//                logger.info("Message 未兼容该表情")
                return ""
            }
        }
    }

    /**
     * 发送消息过程中，服务端异常
     */
    private fun sendMsgFailInWeb(t: Throwable) {
        if (t !is ResponseError) {
            ToastUtils.show("网络不可用，发送失败")
        } else {
            when (t.busiCode) {
                ErrorCodes.BALANCE_NOT_ENOUGH -> {
                    balanceNotEnoughFlag.value = true
                }
                else -> {
                }
            }
        }
    }

    /**
     * 发送传送门
     */
    fun sendRoom(programId: Long) {
        viewModelScope.launch {
            request({
                val sendRoomResult = socialService.sendRoom(SendRoomForm(targetIdData.value ?: return@request, programId)).dataConvert()
                sendRoomIndoData.value = sendRoomResult.sendRoomInfo
                BalanceUtils.saveBalance(sendRoomResult.beans)
            })
        }
    }

    /**
     * 获取设置
     */
    fun getSetting() {
        viewModelScope.launch {
            request({
                val result = userService.settings().dataConvert()
                val chatBubble = result.chatBubble
                if (chatBubble == null) {
                    SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
                } else {
                    SPUtils.commitObject(SPParamKey.PRIVATE_CHAT_BUBBLE, chatBubble)
                }
                bubbleData.value = result.chatBubble
            })
        }
    }

    /**
     *刷新私信道具
     */
    fun propList() {
        viewModelScope.launch {
            request({
                val result = socialService.propList().dataConvert()
                propListData.value = result.propList
            })
        }
    }


}