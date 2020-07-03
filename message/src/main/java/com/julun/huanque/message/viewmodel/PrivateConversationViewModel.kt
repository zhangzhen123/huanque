package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.julun.huanque.common.bean.events.EventMessageBean
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/1 10:16
 *@描述 私聊会话ViewModel
 */
class PrivateConversationViewModel : ViewModel() {

    //消息列表
    val messageListData: MutableLiveData<MutableList<Message>> by lazy { MutableLiveData<MutableList<Message>>() }
    //message有变化
    val messageChangeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //没有更多了
    val noMoreState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //首次获取历史记录成功标识位
    val firstSuccessState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //对方ID
    val targetIdData: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    /**
     * 获取消息列表
     * @param first 是否是首次获取历史记录
     */
    fun getMessageList(oldestId: Int = -1, first: Boolean = false) {
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, targetIdData.value ?: return
            , oldestId, 20, object : RongIMClient.ResultCallback<MutableList<Message>>() {
            override fun onSuccess(p0: MutableList<Message>?) {
                if (p0 == null) {
                    return
                }
                p0.reverse()
                val list = messageListData.value
                if (list != null) {
                    list.addAll(0, p0)
                    messageChangeState.postValue(true)
                } else {
                    messageListData.value = p0
                }
                if (p0.size < 20) {
                    noMoreState.postValue(true)
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
     * 删除单个消息
     */
    fun deleteSingleMessage(message: Message) {
        messageListData.value?.remove(message)
        messageChangeState.postValue(true)
    }

    /**
     * 接收到当前消息，直接显示
     */
    fun addMessage(message: Message) {
        //        var messageId = 0
        //        messageListData.value?.forEach {
        //            if (it.messageId == message.messageId) {
        //                //列表当中存在该消息，标识为重试消息
        //                it.sentStatus = message.sentStatus
        //                messageId = it.messageId
        //            }
        //        }
        //        if (messageId == 0) {
        messageListData.value?.add(message)
        messageChangeState.postValue(true)
        RongIMClient.getInstance().setMessageReceivedStatus(message.messageId, Message.ReceivedStatus(1))
        //        }
        EventBus.getDefault().post(EventMessageBean(message.targetId ?: ""))
    }
}