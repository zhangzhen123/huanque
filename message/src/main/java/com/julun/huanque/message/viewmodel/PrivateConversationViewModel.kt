package com.julun.huanque.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.database.table.Balance
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.common.utils.SessionUtils
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/1 10:16
 *@描述 私聊会话ViewModel
 */
class PrivateConversationViewModel : BaseViewModel() {

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //消息列表
    val messageListData: MutableLiveData<MutableList<Message>> by lazy { MutableLiveData<MutableList<Message>>() }

    //message有变化
    val messageChangeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //没有更多了
    val noMoreState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //首次获取历史记录成功标识位
    val firstSuccessState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //对方ID
    val targetIdData: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }


    // 对方数据
    val chatInfoData: MutableLiveData<ChatUserBean> by lazy { MutableLiveData<ChatUserBean>() }

    //基础数据
    val basicBean: MutableLiveData<ConversationBasicBean> by lazy { MutableLiveData<ConversationBasicBean>() }

    //亲密度数据
    val intimateData: MutableLiveData<IntimateBean> by lazy { MutableLiveData<IntimateBean>() }

    //余额数据
    val balance: LiveData<Long> by lazy { BalanceUtils.getBalance() }


    /**
     * 获取消息列表
     * @param first 是否是首次获取历史记录
     */
    fun getMessageList(oldestId: Int = -1, first: Boolean = false) {
        val targetId = targetIdData.value ?: return
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, "$targetId"
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
                }
                RongCloudManager.resetUserInfoData(user)

                chatInfoData.value = result.friendUser
                intimateData.value = result.intimate
                basicBean.value = result
                BalanceUtils.saveBalance(result.beans)
            }, {
                //设置本人数据
                val user = RoomUserChatExtra().apply {
                    headPic = SessionUtils.getHeaderPic()
                    senderId = SessionUtils.getUserId()
                    nickname = SessionUtils.getNickName()
                }
                RongCloudManager.resetUserInfoData(user)
            })
        }

    }

    //获取余额
//    fun getBalance() {
//        mBalanceDisposable?.dispose()
//        BalanceUtils.getBalance()?.let { f ->
//            mBalanceDisposable = f.subscribeOn(Schedulers.io())
//                /* .observeOn(Schedulers.io())*/
//                .subscribe({
//                    if (it.userId == SessionUtils.getUserId()) {
//                        //是当前用户的余额
//                        balance.postValue(it.balance)
//                    }
//                }, { it.printStackTrace() })
//        }
//    }
}