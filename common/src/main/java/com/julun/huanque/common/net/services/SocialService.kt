package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
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
    suspend fun socialList(@Body form: EmptyForm = EmptyForm()): Root<SocialListBean>

    /**
     * 关注好友
     */
    @POST("social/friend/relation/follow")
    suspend fun follow(@Body form: FriendIdForm): Root<VoidResult>

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
    suspend fun netcallCancel(@Body form : NetcallCancelForm) : Root<VoidResult>

    /**
     * 挂断通话
     */
    @POST("social/friend/netcall/hangUp")
    suspend fun netcallHangUp(@Body form : NetcallHangUpForm) : Root<VoidResult>

    /**
     * 好友聊天礼物列表
     */
    @POST("social/friend/chat/giftsInfo")
    suspend fun giftsInfo(@Body form : EmptyForm = EmptyForm()) : Root<ChatGiftInfo>
}