package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.EmptyForm
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.SendChatGiftForm
import com.julun.huanque.common.bean.beans.ChatGiftInfo
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.bean.beans.NetcallBean
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.forms.*
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *@创建者   dong
 *@创建时间 2020/7/6 16:04
 *@描述 联系人相关接口
 */
interface SocialService {

    /**
     * 获取联系人
     */
    @POST("social/friend/relation/linkInfo")
    suspend fun socialList(@Body form: ContactsForm): Root<SocialListBean>

    /**
     * 关注好友
     */
    @POST("social/friend/relation/follow")
    suspend fun follow(@Body form: FriendIdForm): Root<FollowBean>

    /**
     * 取消关注好友
     */
    @POST("social/friend/relation/unFollow")
    suspend fun unFollow(@Body form: FriendIdForm): Root<VoidResult>

    /**
     * 获取私聊数据
     */
    @POST("social/friend/chat/basic")
    suspend fun chatBasic(@Body form: FriendIdForm): Root<ConversationBasicBean>

    /**
     * 创建通话
     */
    @POST("social/friend/netcall/create")
    suspend fun createCommunication(@Body form: CreateCommunicationForm): Root<NetcallBean>

    /**
     * 获取语音会话详情
     */
    @POST("social/friend/netcall/info")
    suspend fun voiceCallInfo(@Body form: NetcallIdForm): Root<NetcallBean>

    /**
     * 接收语音通话
     */
    @POST("social/friend/netcall/accept")
    suspend fun netcallAccept(@Body form: NetcallIdForm): Root<VoidResult>

    /**
     * 被叫直接拒绝或者加入频道失败，调用该接口 挂断语音通话
     */
    @POST("social/friend/netcall/refuse")
    suspend fun netcallRefuse(@Body form: NetcallIdForm): Root<VoidResult>

    /**
     * 主叫取消通话
     */
    @POST("social/friend/netcall/cancel")
    suspend fun netcallCancel(@Body form: NetcallCancelForm): Root<VoidResult>

    /**
     * 挂断通话
     */
    @POST("social/friend/netcall/hangUp")
    suspend fun netcallHangUp(@Body form: NetcallHangUpForm): Root<NetCallHangUpBean>

    /**
     * 好友聊天礼物列表
     */
    @POST("social/friend/chat/giftsInfo")
    suspend fun giftsInfo(@Body form: EmptyForm = EmptyForm()): Root<ChatGiftInfo>

    /**
     * 好友聊天赠送礼物
     */
    @POST("social/friend/chat/sendGift")
    suspend fun sendGift(@Body form: SendChatGiftForm): Root<ChatSendResult>

    /**
     * 获取小鹊助手语料
     */
    @POST("social/friend/chat/getActiveWord")
    suspend fun getActiveWord(@Body form: EmptyForm = EmptyForm()): Root<ActiveBean>

    /**
     * 发送私聊消息
     */
    @POST("social/friend/chat/sendMsg")
    suspend fun sendMsg(@Body form: SendMsgForm): Root<SendMsgBean>

    /**
     * 发送图片消息
     */
    @POST("social/friend/chat/sendPic")
    suspend fun sendPic(@Body form: SendMsgForm): Root<SendMsgBean>

    @POST("social/friend/chat/detail")
    suspend fun chatDetail(@Body form: TargetIdForm): Root<ChatDetailBean>

    /**
     * 拉黑
     */
    @POST("social/friend/relation/black")
    suspend fun black(@Body form: FriendIdForm): Root<VoidResult>

    /**
     * 取消拉黑
     */
    @POST("social/friend/relation/recover")
    suspend fun recover(@Body form: FriendIdForm): Root<VoidResult>
}