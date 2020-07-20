package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.ChatDetailBean
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.TargetIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.rong.imlib.RongIMClient
import io.rong.imlib.RongIMClient.ResultCallback
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/2 9:59
 *@描述 消息设置ViewModel
 */
class PrivateConversationSettingViewModel : BaseViewModel() {

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //当前会话的免打扰状态
    val disturbStatus: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //会话详情数据
    val chatDetailData: MutableLiveData<ChatDetailBean> by lazy { MutableLiveData<ChatDetailBean>() }

    //拉黑状态
    val blackStatus : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //会话Id
    var targetId = 0L

    /**
     * 消息免打扰设置
     * @param notification true 标识 提醒 false  标识不提醒
     */
    fun conversationDisturbSetting(notification: Boolean) {
        val conversationType = Conversation.ConversationType.PRIVATE
        val notificationStatus = if (notification) {
            Conversation.ConversationNotificationStatus.NOTIFY
        } else {
            Conversation.ConversationNotificationStatus.DO_NOT_DISTURB
        }


        RongIMClient.getInstance().setConversationNotificationStatus(conversationType, "$targetId", notificationStatus,
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
        val conversationType = Conversation.ConversationType.PRIVATE;

        RongIMClient.getInstance().getConversationNotificationStatus(
            conversationType,
            "$targetId",
            object : RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {

                override fun onSuccess(status: Conversation.ConversationNotificationStatus?) {
                    disturbStatus.value = status?.value
                }

                override fun onError(errorCode: RongIMClient.ErrorCode?) {
                }
            });
    }

    /**
     * 获取聊天详情
     */
    fun getChatDetail() {
        viewModelScope.launch {
            request({
                val result = socialService.chatDetail(TargetIdForm(targetId)).dataConvert()
                chatDetailData.value = result
                blackStatus.value = result.blacklistSign
            })
        }
    }

    /**
     * 拉黑
     */
    fun black() {
        viewModelScope.launch {
            request({
                socialService.black(FriendIdForm(targetId)).dataConvert()
                blackStatus.value = true
            }, { it.printStackTrace() })
        }
    }

    /**
     * 取消拉黑
     */
    fun recover() {
        viewModelScope.launch {
            request({
                socialService.recover(FriendIdForm(targetId)).dataConvert()
                blackStatus.value = false
            }, { it.printStackTrace() })
        }
    }


}