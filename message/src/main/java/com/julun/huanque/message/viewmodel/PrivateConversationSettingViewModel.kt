package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import io.rong.imlib.RongIMClient
import io.rong.imlib.RongIMClient.ResultCallback
import io.rong.imlib.model.Conversation

/**
 *@创建者   dong
 *@创建时间 2020/7/2 9:59
 *@描述 消息设置ViewModel
 */
class PrivateConversationSettingViewModel : BaseViewModel() {

    //当前会话的免打扰状态
    val disturbStatus: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //会话Id
    var targetId = ""

    /**
     * 消息免打扰设置
     * @param notification true 标识 提醒 false  标识不提醒
     */
    fun conversationDisturbSetting(notification: Boolean) {
        if (targetId.isEmpty()) {
            return
        }
        val conversationType = Conversation.ConversationType.PRIVATE
        val notificationStatus = if (notification) {
            Conversation.ConversationNotificationStatus.NOTIFY
        } else {
            Conversation.ConversationNotificationStatus.DO_NOT_DISTURB
        }


        RongIMClient.getInstance().setConversationNotificationStatus(conversationType, targetId, notificationStatus,
                object : ResultCallback<Conversation.ConversationNotificationStatus>() {
                    override fun onSuccess(status: Conversation.ConversationNotificationStatus?) {
                        disturbStatus.value = status?.value
                    }

                    override fun onError(errorCode: RongIMClient.ErrorCode?) {
                    }

                })
    }

    /**
     * 获取当前会话免打扰状态
     */
    fun getConversationDisturbStatus() {
        if (targetId.isEmpty()) {
            return
        }
        val conversationType = Conversation.ConversationType.PRIVATE;

        RongIMClient.getInstance().getConversationNotificationStatus(conversationType, targetId, object : RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

            override fun onSuccess(status: Conversation.ConversationNotificationStatus?) {
                disturbStatus.value = status?.value
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
            }
        });
    }

}