package com.julun.huanque.common.manager

import android.app.Application
import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.message_dispatch.EventMessageType
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.message_dispatch.MessageReceptor
import com.julun.huanque.common.suger.removeScope
import com.julun.huanque.common.utils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.rong.imlib.IRongCallback
import io.rong.imlib.RongIMClient
import io.rong.imlib.RongIMClient.ErrorCode
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.MessageContent
import io.rong.message.CommandMessage
import io.rong.message.TextMessage
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * Created by djp on 2016/12/19.
 */
object RongCloudManager {
    private val logger = ULog.getLogger("RongCloudManager")

    var tokenIncorrectCount = 0
    val RONG_CONNECTED = "connected"
    private val reTryCount = 3

    // 聊天室id
    var roomId: String? = null

    //进入的房间类型
    var mRoomType = ""
    private var currentUserObj: RoomUserChatExtra? = null

    /**
     * 有顺序 而且查询效率高
     */
    private val cacheList = LinkedHashSet<String>(10050)

    /**
     * 记录下来真正进入的聊天室  因为现在设置了融云同时允许进入多个直播间的功能 进入一个新的直播间时 上一个聊天室不会自动退出 只能记录下来统一退出
     *
     * 在真正进入聊天室成功的时候添加进队列
     *
     *
     * 在每次切换房间前将队列中的直播间一一退出（如果新进的直播间id 已经存在于队列中 排除出来 无需退出）
     *
     *
     * 只有退出成功时才从队列移除
     *
     */
    private val roomIdList = hashSetOf<String>()

    // 融云初始化
    fun rongCloudInit(app: Application, extraWork: () -> Unit) {
        ULog.i("融云初始化.....")

        if (AppHelper.isMainProcess(app)) {
            ULog.i("融云初始化..... 实际执行.............")
            var rong_yun_key = BuildConfig.RONGYUN_APP_KEY_TEST
            if (CommonInit.getInstance().getBaseUrl() == BuildConfig.SERVICE_BASE_URL_PRODUCT) {
                rong_yun_key = BuildConfig.RONGYUN_APP_KEY_PRODUCT
            }
            RongIMClient.init(app, rong_yun_key)
            RongIMClient.setConnectionStatusListener(connectionStatusListener)
            RongIMClient.setOnReceiveMessageListener(messageListener)
            RongIMClient.setChatRoomActionListener(chatRoomActionListener)
            //            imState = RCIM_STATE_INITED
            extraWork()
        }
    }

    var maxConnectCount = 0
    fun connectRongCloudServerWithComplete(
        callback: (Boolean) -> Unit = {}, isFirstConnect: Boolean = false
    ) {
        //connect方法需要在主线程调用
        var imToken = SessionUtils.getRongImToken()
        //        var imToken = SessionUtils.getRongImToken()
        logger.info("链接融云使用token $imToken ")
        if (TextUtils.isEmpty(imToken)) {
            ToastUtils.show("缺少聊天Token，无法连接聊天服务器")
            callback(false)
            return
        }
        //        imState = RCIM_STATE_CONNECTED
        if (RongCloudUtils.RongCloudIsConnected()) {
            //融云已经处于连接状态
            callback(true)
            return
        }
        if (RongCloudUtils.RongCloudNeedConnectedManually() || isFirstConnect) {
            //融云处于DISCONNECTED状态时才会调用connect方法
            RongIMClient.connect(imToken, object : RongIMClient.ConnectCallback() {
                override fun onSuccess(p0: String?) {
                    callback(true)
                    //                    EventBus.getDefault().post(EventMessageBean())
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {
                    callback(false)
                    logger.info("连接融云失败！！！错误代码${errorCode!!.value}")
                    //                ToastUtils.show("连接至聊天服务器失败，错误代码${errorCode!!.value}")
                    reportCrash("连接融云失败！！！错误代码${errorCode!!.value}")
                }


                //Token 已经过期
                override fun onTokenIncorrect() {
                    tokenIncorrectCount++
                    logger.info("token已过期，更新token后重连融云！！！")
                    //token过期或者不正确。
                    //如果设置了token有效期并且token过期，请重新请求您的服务器获取新的token
                    //如果没有设置token有效期却提示token错误，请检查您客户端和服务器的appkey是否匹配，还有检查您获取token的流程。
                    logger.info("token错误，正在重新刷新中")
                    //                    imState = RCIM_STATE_INITED
                    if (tokenIncorrectCount <= reTryCount) {
                        //token失效，最多重试3次
                        //todo
                        //                        Requests.create(LiveRoomService::class.java).updateToken(SessionForm()).success {
                        //                            // 刷新后台Token，重连融云
                        //                            SessionUtils.setRongImToken(it.imToken)
                        //                            connectRongCloudServerWithComplete(callback, true)
                        //                        }
                    }
                }
            })
        }
    }

    //    /**
    //     * 进入聊天室
    //     */
    //    fun joinChatRoom(programId: String, callback: (Boolean) -> Unit = {}): Unit {
    //        roomId = programId
    //        quitAllChatRoom(false)
    //        // -1: 不拉取任何消息
    //        RongIMClient.getInstance().joinChatRoom(programId, -1, object : RongIMClient.OperationCallback() {
    //            override fun onSuccess() {
    //                logger.info("DXC 进入聊天室成功$programId")
    ////                imState = RCIM_STATE_CHATROOMED
    //                MessageReceptor.destoryBufferedTimer()
    //                MessageReceptor.createBufferedTimer()
    //                callback(true)
    //            }
    //
    //            override fun onError(errorCode: RongIMClient.ErrorCode?) {
    //                roomId = null
    //                handleErrorCode(errorCode, "joinChatRoom")
    //                callback(false)
    //            }
    //        })
    //
    //    }

    /**
     * 退出聊天室
     */
    private fun quitChatRoom(programId: String, callback: (Boolean) -> Unit = {}) {
        logger.info("退出聊天室")
        //        roomId = null
        RongIMClient.getInstance()
            .quitChatRoom(programId, object : RongIMClient.OperationCallback() {
                override fun onSuccess() {
                    logger.info("退出聊天室成功")
                    //                imState = RCIM_STATE_CONNECTED
                    callback(true)
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {
                    logger.info("退出聊天室失败")
                    handleErrorCode(errorCode, "quitChatRoom")
                    callback(false)
                }
            })
    }

    /**
     * 每次进入新房间 或者退出  如果进入新直播间 遍历退出时如果当前新进roomId已存在 保留已存在的那个id不要退出
     *
     * isQuit 代表退出
     */
    fun quitAllChatRoom(isQuit: Boolean) {
        if (isQuit) {
            roomIdList.forEach {
                logger.info("全部退出的遍历$it")
                quitChatRoom(it)
            }
        } else {
            roomIdList.remove(roomId)
            roomIdList.forEach {
                logger.info("切换退出的遍历$it")
                quitChatRoom(it)
            }
        }
    }

    fun handleErrorCode(errorCode: ErrorCode?, from: String = "") {
        logger.info("融云错误代码 $from error = ${errorCode?.value}  描述 ${errorCode?.message}")
        when (errorCode) {
            ErrorCode.KICKED_FROM_CHATROOM -> {
                //                ToastUtils.show("您已被踢出该直播间，请稍后再试~")
            }
            //            ErrorCode.KICKED_FROM_CHATROOM -> ToastUtils.show("您已被踢出该直播间，请稍后再试~")
            ErrorCode.RC_CHATROOM_NOT_EXIST -> ToastUtils.show("直播间不存在，请联系管理员~")
            ErrorCode.RC_CHATROOM_IS_FULL -> ToastUtils.show("直播间过于火爆，请稍后重试~")
            ErrorCode.RC_NET_UNAVAILABLE -> ToastUtils.show("当前连接不可用，请检查网络设置~")
            ErrorCode.NOT_IN_CHATROOM -> ToastUtils.show("不在聊天室中~")
            ErrorCode.FORBIDDEN_IN_CHATROOM -> ToastUtils.show("您已被禁止发言~")
            ErrorCode.RC_MSG_RESP_TIMEOUT, ErrorCode.RC_CONN_ACK_TIMEOUT -> ToastUtils.show("网络不稳定，发送超时~")
            else -> {
                logger.info("聊天错误代码：${errorCode!!.value}")
                if (errorCode != RongIMClient.ErrorCode.RC_NET_CHANNEL_INVALID) {
                    //                    ToastUtils.show("聊天错误代码：${errorCode!!.value}")
                }
            }
        }
        reportCrash("融云错误代码：${errorCode.value} 描述 ${errorCode.message}")
    }

    /**
     * 发送聊天消息
     */
    fun send(message: String, toUserId: String? = null, targetUserObj: TargetUserObj? = null, callback: (Boolean) -> Unit = {}) {
        val chatMessage: TextMessage = TextMessage.obtain(message)
        currentUserObj?.targetUserObj = targetUserObj
        currentUserObj?.userAbcd = AppHelper.getMD5("${currentUserObj?.userId ?: ""}")
        chatMessage.extra = JsonUtil.seriazileAsString(currentUserObj)

        var conversationType = Conversation.ConversationType.CHATROOM
        var targetId: String? = ""
        if (!TextUtils.isEmpty(toUserId)) {
            targetId = toUserId!!
            conversationType = Conversation.ConversationType.PRIVATE
//            EventBus.getDefault().post(EventMessageBean(targetId))
        }
        RongIMClient.getInstance().sendMessage(conversationType,
            targetId,
            chatMessage,
            null,
            null,
            object : IRongCallback.ISendMessageCallback {
                override fun onAttached(message: Message?) {
                }

                override fun onSuccess(message: Message?) {
                    logger.info("融云发送消息成功 ${message?.targetId} 当前的线程：${Thread.currentThread()}")
                    if (message != null) {
                        //                            try {
                        //                                onReceived(message)
                        //                            } catch (e: Exception) {
                        //                                e.printStackTrace()
                        //                            }
                        switchThread(message)
                        callback(true)
                    } else {
                        callback(false)
                    }
                }

                override fun onError(
                    message: Message?, errorCode: ErrorCode?
                ) {
                    if (message != null) {
                        switchThread(message)
                    }
                    logger.info(
                        "融云消息发送失败 ${errorCode!!.message} ${JsonUtil.seriazileAsString(
                            message
                        )}"
                    )
                    handleErrorCode(errorCode, "sendMessage")
                    callback(false)
                }

            })
        //        setChatInfo(conversationType == Conversation.ConversationType.PRIVATE)

    }


    /**
     * 私聊重发消息使用
     */
    fun send(oMessage: Message, targetId: String, callback: (Boolean) -> Unit = {}): Unit {
        //        EventBus.getDefault().post(EventMessageBean(targetId))
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.PRIVATE,
            targetId,
            oMessage.content,
            null,
            null,
            object : IRongCallback.ISendMessageCallback {
                override fun onAttached(message: Message?) {
                }

                override fun onSuccess(message: Message?) {
                    logger.info("融云发送消息成功 ${message?.targetId} 当前的线程：${Thread.currentThread()}")
                    callback(true)
                    if (message != null) {
                        //                            try {
                        //                                onReceived(message)
                        //                            } catch (e: Exception) {
                        //                                e.printStackTrace()
                        //                            }
                        switchThread(message)
                    } else {
                    }
                }

                override fun onError(
                    message: Message?, errorCode: ErrorCode?
                ) {
                    callback(false)
                    if (message != null) {
                        //                            ChatUtils.deleteSingleMessage(message.messageId)
                    }
                    logger.info(
                        "融云消息发送失败 ${errorCode!!.message} ${JsonUtil.seriazileAsString(
                            message
                        )}"
                    )
                    handleErrorCode(errorCode, "sendMessage")
                }

            })

    }

    // 消息消费定时器
    fun startMessageConsumerWithCurrentUserObj(userObj: RoomUserChatExtra) {
        // 开始消费数据
        MessageReceptor.startBufferedTimer()
        currentUserObj = userObj

    }

    // 提供给直播间调用，刷新用户数据
    fun resetUserInfoData(userObj: RoomUserChatExtra) {
        currentUserObj = userObj
    }

    //    fun rongCloudIsInited(): Boolean {
    //        return imState == RCIM_STATE_INITED
    //    }

    // 停止消费数据
    fun destroyMessageConsumer() {
        MessageReceptor.destroyBufferedTimer()
        currentUserObj = null
    }

    /**
     * 由于消息可能来自main 融云线程池 后续MessageProcessor操作遍历时会出现多线程异常
     * 该方法作用是把消息统一到一个新的单列线程进行处理
     */
    fun switchThread(message: Message) {
        Single.just(message).observeOn(Schedulers.single()).subscribe { message ->
            try {
                onReceived(message)
            } catch (e: Exception) {
                //                reportCrash(e)
                e.printStackTrace()
            }
        }
    }

    /**
     * 注意：在子线程执行
     * @param message 收到消息对象
     * @param left 剩余未拉取消息数目
     */
    private fun onReceived(message: Message, left: Int = 0) {
        //        logger.info("当前线程：${Thread.currentThread()}")
        //        if (TextUtils.isEmpty(roomId)) return

        val content: MessageContent? = message.content
        val isRetrieved = message.receivedStatus.isRetrieved
        when (content) {
            is TextMessage -> {
                //TextUtils.isEmpty(roomId) ||  首页里面没有roomId
                if (isRetrieved) return


                //文本消息还是需要透过融云的id去重判断
                val rRoomId = if (mRoomType == MessageProcessor.RoomType.ChatRoom.name) {
                    //聊天室
                    "${BusiConstant.USER_CHAT_ROOM_PREFIX}$roomId"
                } else {
                    roomId
                }
                if (message.conversationType == Conversation.ConversationType.CHATROOM && rRoomId != message.targetId) {
                    logger.info("串消息了，当前直播间$roomId, targetId=${message.targetId}")
                    return
                }
                logger.info("收到文本消息 ${JsonUtil.seriazileAsString(content)}")
                val bean = TplBean(textTpl = content.content)

                if (message.conversationType == Conversation.ConversationType.PRIVATE) {
                    //私聊消息  前期模拟数据  直接return
                    bean.privateMessage = true
                    MessageProcessor.processPrivateTextMessageOnMain(message)
                    return
                }
                val user: RoomUserChatExtra? =
                    JsonUtil.deserializeAsObject(content.extra, RoomUserChatExtra::class.java)
                //神秘人功能  发言人ID和userId会出现不一致的情况，删除此条限制
                if (message.senderUserId != "${user?.userId}" && user?.userAbcd != AppHelper.getMD5(
                        "${user?.userId ?: ""}"
                    )
                ) {
                    logger.info("senderId与userId不一致 senderUserId:${message.senderUserId} userId ${user?.userId}")
                    return
                }
                bean.userInfo = user
                try {
                    bean.userInfo?.senderId = message.senderUserId.toLong()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (message.conversationType == Conversation.ConversationType.CHATROOM) {
                    if (MessageProcessor.publicTextProcessor == null) {
                        //直播间聊天室的消息
                        MessageReceptor.putTextMessageWithData(bean)
                    } else {
                        //贵族聊天的消息
                        MessageProcessor.processPublicTextMessageOnMain(message)
                    }
                } else if (message.conversationType == Conversation.ConversationType.PRIVATE) {
                    bean.privateMessage = true
                    MessageProcessor.processPrivateTextMessageOnMain(message)
                }
            }
            is CommandMessage -> {
                logger.info("收到CommandMessage消息 ${JsonUtil.seriazileAsString(content)}")
                // 将数据解析为字典对象
                val dataString = content.data
                val baseList = JsonUtil.deserializeAsObjectList(dataString, BaseData::class.java)

                baseList?.forEach {
                    // 消息类型
                    val msgType = it.msgType
                    if (!checkMessage(it, isRetrieved)) return@forEach
                    if (it.data == null) return@forEach
                    val jsonObject = it.data as JSONObject
                    val jsonString = jsonObject.toJSONString()

                    when (msgType) {
                        MessageProcessor.MessageType.Text.name -> {
                            if (TextUtils.isEmpty(roomId)) return
                            // 文本消息
                            MessageProcessor.parseTextMessage(jsonString, it.msgId)
                        }
                        MessageProcessor.MessageType.Event.name -> {
                            val eventCode = jsonObject.getString(MessageProcessor.EVENT_CODE)
                            if (!TextUtils.isEmpty(eventCode) && eventCode == EventMessageType.MsgCenterNewMsg.name) {
                                if (message.receivedStatus.isRetrieved) {
                                    //如果这条消息被被其他登录的多端收取过，那么直接丢弃
                                    return@forEach
                                }
                                //                                MessageProcessor.parseEventMessage(jsonObject)
                                //                                return@forEach
                            }
                            //                            if (TextUtils.isEmpty(roomId)) return
                            MessageProcessor.parseEventMessage(jsonObject)
                        }
                        MessageProcessor.MessageType.Animation.name -> {
                            if (TextUtils.isEmpty(roomId)) return
                            logger.info("收到动画消息 $jsonString")
                            // 缓存到消息队列
                            MessageReceptor.putAnimationMessage(jsonObject)
                        }
                        else -> {
                            if (TextUtils.isEmpty(roomId)) return
                            val msg = "不支持的消息类型"
                            logger.info(msg)
                            //                    toast!!.showErrorMessage(message)
                            reportCrash(msg)
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查消息的安全性  是不是串消息  是不是重复消息
     * true 代表消息正常
     */
    private fun checkMessage(message: BaseData, isRetrieved: Boolean): Boolean {
        //如果没有msgId的直接过滤
        if (message.msgId.isEmpty()) return false

        when (message.targetType) {
            MessageProcessor.TargetType.Broadcast.name -> {
                if (message.roomTypes.isNotEmpty() && !message.roomTypes.contains(mRoomType)) {
                    return false
                }
            }
            MessageProcessor.TargetType.Room.name -> {
                //直播间消息
                if (message.roomIds != null && !message.roomIds!!.contains(roomId)) {
                    logger.info("消息串了")
                    return false
                }
            }
            MessageProcessor.TargetType.ChatRoom.name -> {
                //聊天室消息
                if (message.roomIds != null && !message.roomIds!!.contains(roomId)) {
                    logger.info("消息串了")
                    return false
                }
            }
            MessageProcessor.TargetType.User.name -> {
                if (isRetrieved) {
                    //其它设备消息
                    return false
                }
                //针对部分消息在快速切换房间时重复发送的问题进行过滤
                if (message.roomIds != null && !message.roomIds!!.contains(roomId)) {
                    logger.info("TargetType.User 消息串了")
                    return false
                }
                val localUserId = SessionUtils.getUserId()
                if (message.userIds != null && !message.userIds!!.contains(localUserId)) {
                    logger.info("个人消息 目标错误")
                    return false
                }
            }
        }

        if (cacheList.contains(message.msgId)) {
            logger.info("消息重复了:${message.msgId}")
            return false
        }

        //        if (cacheList.size >= 5500) {
        //            for (i in 0..500)
        //                cacheList.removeAt(0)
        //        }
        if (cacheList.size >= 10000) {
            cacheList.removeScope(1000)
        }
        //        logger.info("缓存的大小：" + cacheList.size)
        cacheList.add(message.msgId)

        //        logger.info("当前缓存的消息Ids:$cacheList")
        return true
    }

    //logout是费时操作，不会立即返回，必须监听到连接变化为DISCONNECTED才说明登出成功
    private var logoutCallback: () -> Unit = {}

    var stratLoginOut = 0L
    fun logout(callback: () -> Unit = {}) {
        logoutCallback = callback
        stratLoginOut = System.currentTimeMillis() //开始登出
        //退出登录的时候，重置该字段
        //        SessionUtils.setNeedGuideToSpeak(true)
        if (RongCloudUtils.RongCloudIsConnected()) {
            //融云处于连接状态 则断开连接
            RongIMClient.getInstance().logout()
        } else {
            logoutCallback()
        }
        //        imState = RCIM_STATE_INITED
    }

    // 消息接收监听器
    val messageListener: RongIMClient.OnReceiveMessageListener by lazy {
        RongIMClient.OnReceiveMessageListener { message, left ->
            //            try {
            //                logger.info("接收消息成功当前的线程：${Thread.currentThread()}")
            //                //由于使用很多json解析 以及枚举转化 很容易出现crash 这里增加catch 提高健壮性 另外try catch并不会影响性能
            //                onReceived(message, left)
            //            } catch (e: Exception) {
            //                reportCrash(e)
            //                e.printStackTrace()
            //            }
            switchThread(message)
            return@OnReceiveMessageListener false
        }
    }

    val chatRoomActionListener = object : RongIMClient.ChatRoomActionListener {
        override fun onJoining(chatRoomId: String?) {

        }

        override fun onJoined(chatRoomId: String?) {
            chatRoomId?.let {
                Flowable.just(it).observeOn(AndroidSchedulers.mainThread())
                    .subscribe { roomIdList.add(it) }
            }

        }

        override fun onQuited(chatRoomId: String?) {
            chatRoomId?.let {
                Flowable.just(it).observeOn(AndroidSchedulers.mainThread())
                    .subscribe { roomIdList.remove(it) }
            }
        }

        override fun onError(chatRoomId: String?, code: ErrorCode?) {

        }

    }

    // 连接变化监听器
    val connectionStatusListener: RongIMClient.ConnectionStatusListener by lazy {
        RongIMClient.ConnectionStatusListener {
            when (it) {
                RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED -> {
                    /**
                     * 发送广播不能放在connect回调中，防止网络变化的情况下，直播页面收不到消息。用户停留在加载页面
                     * 不用粘性事件 如果用户重复登录账号 就会连接两次融云 从而发送两个粘性事件 再进入直播间就会收到连续两个连续触发
                     */
                    //                    EventBus.getDefault().post(RongConnectEvent(RONG_CONNECTED))
                }
                RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT -> {
                    ToastUtils.showErrorMessage("您已在其它客户端登陆，当前客户端已下线~")
                    //抢登事件
                    //                    EventBus.getDefault().post(RongForcedReturn())
                    //                    SessionUtils.deleteSession()
                }
                RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED -> {
                    val loginOutSuccess = System.currentTimeMillis()
                    logger.info("融云连接已断开 DISCONNECTED 用时：${loginOutSuccess - stratLoginOut}")
                    logoutCallback()
                }
                else -> {
                    logger.info("IM连接状态发送变化 ${it.message}")
                }
            }
            logoutCallback = {}
        }
    }

    /**
     * 添加假的的消息  当用户发送敏感消息时 使用这个方法 让他以为发出去了 其实只有他自己看得见
     *
     */
    fun addUnRealMessage(
        message: String,
        type: MessageProcessor.TextMessageType,
        displayT: List<String>? = null,
        targetId: String? = null,
        cType: Conversation.ConversationType = Conversation.ConversationType.PRIVATE
    ) {
        if (cType == Conversation.ConversationType.PRIVATE || cType == Conversation.ConversationType.CHATROOM) {
            //私聊消息，直接显示
            val tMessage = Message().apply {
                sentStatus = Message.SentStatus.SENT

                val chatMessage: TextMessage = TextMessage.obtain(message)
                chatMessage.extra = JsonUtil.seriazileAsString(currentUserObj)

                content = chatMessage
                sentTime = System.currentTimeMillis()
                receivedStatus = Message.ReceivedStatus(1)
                senderUserId = "${SessionUtils.getUserId()}"
                conversationType = cType
                this.targetId = targetId
            }
            switchThread(tMessage)
        } else {
            //其它消息
            val bean = TplBean(textTpl = message)
            currentUserObj?.displayType = displayT
            bean.userInfo = currentUserObj
            MessageProcessor.processTextMessage(arrayListOf(bean), type)
        }
    }

    /**
     * 获取融云私聊所有未读消息数量
     */
    fun queryPMessage(callback: (Int) -> Unit = {}) {
        RongIMClient.getInstance().getUnreadCount(object : RongIMClient.ResultCallback<Int>() {
            override fun onSuccess(count: Int?) {
                logger.info("获取私聊消息数量 : $count}")
                callback(count ?: 0)
            }

            override fun onError(code: ErrorCode?) {
                callback(0)
                logger.info("获取私聊消息数量错误 code : ${code?.value}，msg : ${code?.message}")
            }

        }, Conversation.ConversationType.PRIVATE)
    }

    /**
     * 清除之前加入直播间的列表
     */
    fun clearRoomList() {
        roomIdList.clear()
    }
}