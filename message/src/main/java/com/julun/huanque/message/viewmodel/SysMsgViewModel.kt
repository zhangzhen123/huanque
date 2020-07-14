package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.beans.RootListLiveData
import com.julun.huanque.common.bean.beans.SysMsgContent
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.utils.JsonUtil
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.message.TextMessage


/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
class SysMsgViewModel : BaseViewModel() {

    val getSysMsgList: MutableLiveData<RootListLiveData<Message>> by lazy { MutableLiveData<RootListLiveData<Message>>() }
    val getNewSysMsg: MutableLiveData<Message> by lazy { MutableLiveData<Message>() }
    val refreshErrorStats: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val loadMoreErrorStats: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

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
                        getSysMsgList.value = RootListLiveData<Message>().apply {
                            if (lastMessageId == -1) {
                                isPull = true
                            }
                            if (messages.size >= 20) {
                                hasMore = true
                            }
                            list = messages
                        }
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
                    }
                })


    }

    fun queryNewMessage(tgetId: String) {
        if(targetId != tgetId){
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
}