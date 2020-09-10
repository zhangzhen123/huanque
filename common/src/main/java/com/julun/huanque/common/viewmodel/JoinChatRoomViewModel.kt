package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.message_dispatch.MessageReceptor
import com.julun.huanque.common.suger.logger
import io.rong.imlib.IRongCallback
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Message
import io.rong.message.TextMessage

/**
 * Created by dong on 2017/12/13.
 */
class JoinChatRoomViewModel : BaseViewModel() {
    //是否加入直播间的标志
    val joinData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //调用加入直播间方法
    val joinChatRoomFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 进入聊天室
     */
    fun joinChatRoom(programId: String, chatroom: Boolean = false) {
        MessageReceptor.destroyBufferedTimer()
        RongCloudManager.mRoomType = if (chatroom) MessageProcessor.RoomType.ChatRoom.name else MessageProcessor.RoomType.LiveRoom.name
        RongCloudManager.roomId = programId
        RongCloudManager.quitAllChatRoom(false)
        val rId = StringBuffer()
        if (chatroom) {
            //如果是聊天室，加上前缀
            rId.append(BusiConstant.USER_CHAT_ROOM_PREFIX)
        }
        rId.append(programId)
        // -1: 不拉取任何消息
        RongIMClient.getInstance().joinChatRoom(rId.toString(), -1, object : RongIMClient.OperationCallback() {
            override fun onSuccess() {
//                imState = RCIM_STATE_CHATROOMED
                logger("当前的线程：${Thread.currentThread().name}")
                MessageReceptor.createBufferedTimer()
                joinData.value = true
                getHistoryMessage(rId.toString())
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                RongCloudManager.roomId = null
                RongCloudManager.handleErrorCode(errorCode, "joinChatRoom")
                joinData.value = false
            }
        })

    }

    /**
     * 获取缓存的历史消息
     */
    private fun getHistoryMessage(targetID: String) {
        RongIMClient.getInstance().getChatroomHistoryMessages(
            targetID,
            0,
            50,
            RongIMClient.TimestampOrder.RC_TIMESTAMP_DESC,
            object : IRongCallback.IChatRoomHistoryMessageCallback {
                override fun onSuccess(p0: MutableList<Message>?, p1: Long) {
                    p0?.reverse()
                    p0?.forEachIndexed { index, message ->
                        val conent = message.content
                        if (conent is TextMessage) {
                            RongCloudManager.switchThread(message)
                        }
                    }
                }

                override fun onError(p0: RongIMClient.ErrorCode?) {
                }

            })
    }

}