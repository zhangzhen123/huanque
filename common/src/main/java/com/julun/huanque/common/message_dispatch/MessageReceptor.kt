package com.julun.huanque.common.message_dispatch

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.VoidResult
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.AnimEventBean
import com.julun.huanque.common.utils.ULog


/**
 * Created by djp on 2016/12/19.
 *
 * 需要队列的消息统一在定时器中使用handle切换到主线程
 */
object MessageReceptor {
    private val logger = ULog.getLogger("MessageProcess")

    /**公聊区消息缓冲区设置**/
    // 消息出队的间隔时间，单位秒
    private const val PUBLIC_CHAT_DURATION = 0.5f
    // 每次最多取出的条数
    private const val PUBLIC_CHAT_SHIFT_LIMIT = 6

    const val EVENT_CLEAR = -1f

    const val EVENT_WAIT_MESSAGE = -2f  //代表去等待分发消息的到来

    /**动画层持续时间，单位秒, 这里需要配合提示层的整个持续时间，包括动画时间，不能超过6秒，这里故意多出2秒，给个缓冲时间**/
    private const val ANIMATION_LAYER_DURATION = 5f

    // 公聊区消息缓冲定时器
    private var publicMessageBuffered: BufferedTimer? = null
    // 浮层或豪华礼物缓冲定时器
    private var layerEventBuffered: BufferedTimer? = null
    //隐藏高级动画标识位
    var mHideAnimation = false

    // 其他消息缓冲定时器 包括私聊 入场 跑道 系统消息等等。。

    // 压入一条文本消息
    fun putTextMessageWithData(messageData: TplBean) {
        publicMessageBuffered?.push(messageData)
    }

    // 压入一条事件消息
    fun putEventMessage(eventCode: String, contextJsonObject: JSONObject) {
        val eventModel = EventMessageModel()
        eventModel.eventCode = eventCode
        val valueOf: EventMessageType = EventMessageType.valueOf(eventCode)
//        eventModel.eventData = JsonUtil.deserializeAsObject(contextJson, valueOf.klass)
        eventModel.eventData = contextJsonObject.toJavaObject(valueOf.klass)
        layerEventBuffered!!.push(eventModel)
    }

    // 压入一条动画消息
    fun putAnimationMessage(jsonObject: JSONObject) {
        val eventModel = EventMessageModel()
        eventModel.eventCode = EventMessageType.ANIMATION.name
//        eventModel.eventData = JsonUtil.deserializeAsObject(jsonString, AnimEventBean::class.java)
        eventModel.eventData = jsonObject.toJavaObject(AnimEventBean::class.java)
        layerEventBuffered!!.push(eventModel)
    }

    // 创建缓冲定时器
    fun createBufferedTimer() {
        if (publicMessageBuffered == null) {
            publicMessageBuffered = BufferedTimer()
            publicMessageBuffered!!.initWithRefreshViewBlock {
                if (it.count() == 0) {
                    return@initWithRefreshViewBlock EVENT_CLEAR
                }
                logger.info("聊天消息循环中Thread:${Thread.currentThread().name}")
                //增加非空判断
                val msl = it.shiftWithCount(PUBLIC_CHAT_SHIFT_LIMIT)
                if (msl != null) {
                    val messageList = msl as List<TplBean>
                    // 发送聊天消息通知
                    MessageProcessor.processTextMessage(
                        messageList,
                        MessageProcessor.TextMessageType.PUBLIC_MESSAGE
                    )
                }


                return@initWithRefreshViewBlock PUBLIC_CHAT_DURATION
            }
        }
        if (layerEventBuffered == null) {
            layerEventBuffered = BufferedTimer()
            layerEventBuffered!!.initWithRefreshViewBlock {
                if (it.count() == 0) {
                    // 分发清除动画事件
                    MessageProcessor.processEventMessage(
                        VoidResult(),
                        EventMessageType.CLEAR_ANIMATION.name
                    )
                    return@initWithRefreshViewBlock EVENT_CLEAR
                }
                logger.info("动画计时器循环中Thread:${Thread.currentThread().name}")

                var eventModel = it.shiftOneObject() as EventMessageModel
                var eventCode = eventModel.eventCode

                val messageType: EventMessageType? = EventMessageType.valueOf(eventCode)

                if (messageType != null) {
                    val eventData: Any? = eventModel.eventData
                    if (eventData != null) {

                        if (EventMessageType.ANIMATION.equals(messageType)) {
                            if (mHideAnimation) {
                                return@initWithRefreshViewBlock EVENT_CLEAR
                            }
                            MessageProcessor.processEventMessage(eventData, messageType.name)
                            return@initWithRefreshViewBlock EVENT_WAIT_MESSAGE
                        } else {
                            MessageProcessor.processEventMessage(eventData, messageType.name)
                        }
                    }
                }

                return@initWithRefreshViewBlock ANIMATION_LAYER_DURATION
            }
        }

    }

    // 启动所有定时器
    fun startBufferedTimer() {
        publicMessageBuffered?.start()
        layerEventBuffered?.start()
    }

    // 停止缓存定时器
    fun destoryBufferedTimer() {
        publicMessageBuffered?.destory()
        publicMessageBuffered = null

        layerEventBuffered?.destory()
        layerEventBuffered = null
    }

}

enum class MessageSourceType {
    // 聊天消息
    TEXT_MESSAGE_SOURCE_CHAT,
    // 后台详细
    TEXT_MESSAGE_SOURCE_BACKEND
}

class PublicMessageQueueData {
    var source: MessageSourceType = MessageSourceType.TEXT_MESSAGE_SOURCE_CHAT
    // 后台消息的TPL及消息参数
    var backendMessageObj: Map<String, Any>? = null
    var chatContent: String = ""
    var userId: Int = 0
    var nickname: String = ""
    var royalLevel: Int = 0
    var userLevel: Int = 0
    var guardPicId: Int = 0
}


class EventMessageModel {
    var eventCode: String = ""
    var eventData: Any? = null
}