package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.events.SystemMessageRefreshBean
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.utils.ToastUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import org.greenrobot.eventbus.EventBus


/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
class SysMsgViewModel : BaseViewModel() {

    val getSysMsgList: MutableLiveData<RootListData<Message>> by lazy { MutableLiveData<RootListData<Message>>() }
    val getNewSysMsg: MutableLiveData<Message> by lazy { MutableLiveData<Message>() }
    val refreshErrorStats: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val loadMoreErrorStats: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val finalState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var targetId: String = ""

    fun getHistoryMessages(tgetId: String, lastMessageId: Int = -1) {
        targetId = tgetId
        RongIMClient.getInstance()
            .getHistoryMessages(
                Conversation.ConversationType.PRIVATE, tgetId, lastMessageId, 20,
                object : RongIMClient.ResultCallback<MutableList<Message>>() {
                    /**
                     * 成功时回调
                     * @param messages 获取的消息列表
                     */
                    override fun onSuccess(messages: MutableList<Message>) {
//                        var cMessage: Message? = null
//                        messages.forEach { msg ->
//                            if (msg.content is CustomSimulateMessage) {
//                                //插入的消息
//                                cMessage = msg
//                                return@forEach
//                            }
//                        }
//                        cMessage?.let {
//                            messages.remove(it)
//                            if (messages.size > 1) {
//                                RongIMClient.getInstance().deleteMessages(intArrayOf(it.messageId))
//                            }
//                        }
                        getSysMsgList.value = RootListData<Message>().apply {
                            if (lastMessageId == -1) {
                                isPull = true
                            }
                            if (messages.size >= 20) {
                                hasMore = true
                            }
                            list = messages
                        }
                        finalState.value = true
                    }

                    /**
                     * 错误时回调。
                     * @param errorCode 错误码
                     */
                    override fun onError(errorCode: RongIMClient.ErrorCode?) {
                        if (lastMessageId == -1) {
                            refreshErrorStats.value = true
                        } else {
                            loadMoreErrorStats.value = true
                        }
                        finalState.value = true
                    }
                })


    }

    fun queryNewMessage(tgetId: String) {
        if (targetId != tgetId) {
            return
        }
        RongIMClient.getInstance()
            .getConversation(Conversation.ConversationType.PRIVATE, tgetId, object : RongIMClient.ResultCallback<Conversation>() {
                override fun onSuccess(p0: Conversation?) {
                    if (p0 == null) {
                        return
                    }
                    getNewSysMsg.value = p0.latestMessage as? Message
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }
            })
    }

    /**
     * 清除未读数
     */
    fun clearUnReadCount(targetId: String) {
        RongIMClient.getInstance()
            .getConversation(Conversation.ConversationType.PRIVATE, targetId, object : RongIMClient.ResultCallback<Conversation>() {
                override fun onSuccess(conversation: Conversation?) {
                    if (conversation == null) {
                        return
                    }
                    val unreadCount = conversation.unreadMessageCount
                    if (unreadCount > 0) {
                        //有未读数 //清除该会话所有未读消息
                        RongIMClient.getInstance()
                            .clearMessagesUnreadStatus(
                                Conversation.ConversationType.PRIVATE,
                                targetId,
                                object : RongIMClient.ResultCallback<Boolean>() {
                                    override fun onSuccess(p0: Boolean?) {
                                        if (p0 == true) {
                                            //通知列表  刷新系统和鹊友通知
                                            EventBus.getDefault().post(SystemMessageRefreshBean(targetId))
                                        }
                                    }

                                    override fun onError(p0: RongIMClient.ErrorCode?) {
                                    }
                                })
                    }


                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })


    }

    /**
     * 清空所有消息
     */
    fun clearMessage() {
        RongIMClient.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, targetId, object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
                ToastUtils.show("清除成功")
                getHistoryMessages(targetId)
                EventBus.getDefault().post(SystemMessageRefreshBean(targetId))
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
    }
}

