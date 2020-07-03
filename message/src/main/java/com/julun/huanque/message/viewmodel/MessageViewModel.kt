package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation

/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:49
 *@描述 会话列表ViewModel
 */
class MessageViewModel : BaseViewModel() {

    //会话列表
    val conversationListData: MutableLiveData<List<LocalConversation>> by lazy { MutableLiveData<List<LocalConversation>>() }

    /**
     * 删除会话
     */
    fun removeConversation(cId: String, type: Conversation.ConversationType) {
        RongIMClient.getInstance().removeConversation(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
                //刷新会话列表
//                val list = conversationList
//                var tempData: LMConversation? = null
//                conversationList.forEach {
//                    if (it.conversation.targetId == cId) {
//                        //匹配到自己
//                        tempData = it
//                        return@forEach
//                    }
//                }
//                tempData?.let {
//                    list.remove(it)
//                    conversationList = list
//                }
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }
        })

        //删除会话当中消息
        RongIMClient.getInstance().clearMessages(type, cId, object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
    }

    /**
     * 获取本地会话列表
     */
    fun getConversationList() {
        RongIMClient.getInstance().getConversationList(object : RongIMClient.ResultCallback<List<Conversation>>() {
            override fun onSuccess(p0: List<Conversation>?) {
//                val lmcList = conversation2LMC(p0)
//                simulateLMC(lmcList)
//                doWithconversation(lmcList)
//                getUserInfo(p0)
                val localConversationList = mutableListOf<LocalConversation>()
                p0?.forEach {
                    val localConversation = LocalConversation(conversation = it)
                    localConversationList.add(localConversation)
                }
                conversationListData.value = localConversationList
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
            }

        })
    }


}