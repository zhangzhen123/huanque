package com.julun.huanque.common.manager

import com.julun.huanque.common.bean.beans.PostShareBean
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.model.Conversation

/**
 *@创建者   dong
 *@创建时间 2020/11/26 10:58
 *@描述 外界发送消息manager
 */
object SendMessageManager {
    //发送动态消息
    fun sendPostMessage(bean: PostShareBean, socialUser: SocialUserInfo) {
        val targetUser = TargetUserObj()

        val chatExtra = RoomUserChatExtra()
        val tId = "${socialUser.userId}"

        //本人是主叫(插入发送消息)
        val sId: String = "${SessionUtils.getUserId()}"
        targetUser.apply {
            headPic = socialUser.headPic
            nickname = socialUser.nickname
            meetStatus = socialUser.meetStatus
            sex = socialUser.sex
            userId = socialUser.userId
            userType = socialUser.userType
        }
        chatExtra.apply {
            headPic = SessionUtils.getHeaderPic()
            senderId = SessionUtils.getUserId()
            nickname = SessionUtils.getNickName()
            sex = SessionUtils.getSex()
            targetUserObj = targetUser
            userAbcd = AppHelper.getMD5(sId)
            userType = SessionUtils.getUserType()
        }

        chatExtra.targetUserObj?.intimateLevel = socialUser.intimateLevel


        val customMessage = RongCloudManager.obtainCustomMessage(
            tId, targetUser, Conversation.ConversationType.PRIVATE
            , MessageCustomBeanType.PostShare, bean,chatExtra
        ).apply {
            sentTime = System.currentTimeMillis()
        }

        RongCloudManager.sendCustomMessage(
            customMessage
        )
    }
}