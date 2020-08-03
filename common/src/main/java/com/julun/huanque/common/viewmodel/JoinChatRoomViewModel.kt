package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.message_dispatch.MessageReceptor
import com.julun.huanque.common.suger.logger
import io.rong.imlib.RongIMClient

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
        RongIMClient.getInstance().joinChatRoom(rId.toString(), 20, object : RongIMClient.OperationCallback() {
            override fun onSuccess() {
//                imState = RCIM_STATE_CHATROOMED
                logger("当前的线程：${Thread.currentThread().name}")
                MessageReceptor.destroyBufferedTimer()
                MessageReceptor.createBufferedTimer()
                joinData.value = true
            }

            override fun onError(errorCode: RongIMClient.ErrorCode?) {
                RongCloudManager.roomId = null
                RongCloudManager.handleErrorCode(errorCode, "joinChatRoom")
                joinData.value = false
            }
        })

    }

}