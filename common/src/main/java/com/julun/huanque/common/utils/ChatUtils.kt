package com.julun.huanque.common.utils

import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.bean.beans.UserEnterRoomRespDto
import com.julun.huanque.common.bean.beans.UserInfo
import io.rong.imlib.RongIMClient

/**
 *@创建者   dong
 *@创建时间 2019/5/21 14:28
 *@描述  和聊天有关的工具类
 */
object ChatUtils {

    /**
     * 创建聊天时使用的用户对象
     */
    fun createRoomUserChat(
        roomData: UserEnterRoomRespDto? = null,
        baseData: UserEnterRoomRespBase? = null,
        isAnchor: Boolean = false
    ): RoomUserChatExtra {
        val roomUserChat: RoomUserChatExtra

        val userInfo = roomData?.user
        if (userInfo == null) {
            //用户信息为空，表示为主播
            val anchorInfo = roomData?.anchor
            if (anchorInfo == null) {
                roomUserChat = RoomUserChatExtra(
                    userId = SessionUtils.getUserId(),
                    nickname = SessionUtils.getNickName(),
                    nickColor = "#FFD630"
                )
            } else {
                roomUserChat = RoomUserChatExtra(
                    userId = SessionUtils.getUserId(), nickname = anchorInfo.programName,
                    nickColor = "#FFD630",
                    anchorLevel = anchorInfo.anchorLevel
                )
            }
        } else {
            //主播信息为空，表示为普通用户
            var anchorLevel: Int = -1
            if (isAnchor && baseData != null) {
                anchorLevel = baseData.anchorLevel
            }
            roomUserChat = RoomUserChatExtra(
                headPic = userInfo.headPic,
                userId = userInfo.userId, nickname = userInfo.nickname,
                royalLevel = userInfo.royalLevel, userLevel = userInfo.userLevel,
                anchorLevel = anchorLevel,
                badgesPic = userInfo.badgesPic,
                royalPic = userInfo.royalPic,
                nickColor = userInfo.nickColor,
                lightColor = userInfo.lightColor
//                chatBubble = userInfo.chatBubble
            )
        }
        return roomUserChat
    }

    /**
     * 创建用户   上神聊天室使用
     */
    fun createRoomUserChat(userInfo: UserInfo): RoomUserChatExtra {
        return RoomUserChatExtra(
            userId = userInfo.userId, nickname = userInfo.nickname,
            royalLevel = userInfo.royalLevel, userLevel = userInfo.userLevel,
            royalPic = userInfo.royalPic, royalSmallPic = userInfo.royalSmallPic,
            headPic = userInfo.headPic, anchorLevel = 0, nickColor = userInfo.nickColor
        )
    }

    /**
     * 创建用户   聊天列表使用
     */
    fun createRoomUserChat(cUser: ChatUser): RoomUserChatExtra {
        return RoomUserChatExtra(
            userId = cUser.userId, nickname = cUser.nickname
            /*royalLevel = cUser.royalLevel, userLevel = cUser.userLevel,
            anchorLevel = cUser.anchorLevel,
            nickcolor = cUser.nickcolor*/
        )
    }


    /**
     * 删除单条消息
     */
    fun deleteSingleMessage(mid: Int, callBack: (Boolean) -> Unit = {}) {
        RongIMClient.getInstance().deleteMessages(intArrayOf(mid), object : RongIMClient.ResultCallback<Boolean>() {
            override fun onSuccess(p0: Boolean?) {
                callBack(true)
            }

            override fun onError(p0: RongIMClient.ErrorCode?) {
                callBack(false)
            }

        })
    }

}