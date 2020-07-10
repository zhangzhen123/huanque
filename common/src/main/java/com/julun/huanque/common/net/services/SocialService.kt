package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.EmptyForm
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.SendChatGiftForm
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
    suspend fun socialList(@Body form : EmptyForm = EmptyForm()) : Root<SocialListBean>

    /**
     * 关注好友
     */
    @POST("social/friend/relation/follow")
    suspend fun follow(@Body form : FriendIdForm) : Root<VoidResult>

    /**
     * 取消关注好友
     */
    @POST("social/friend/relation/unFollow")
    suspend fun unFollow(@Body form : FriendIdForm) : Root<VoidResult>

    /**
     * 获取私聊数据
     */
    @POST("social/friend/chat/basic")
    suspend fun chatBasic(@Body form : FriendIdForm) : Root<ConversationBasicBean>

    /**
     * 好友聊天礼物列表
     */
    @POST("social/friend/chat/giftsInfo")
    suspend fun giftsInfo(@Body form : EmptyForm = EmptyForm()) : Root<ChatGiftInfo>

    /**
     * 好友聊天赠送礼物
     */
    @POST("social/friend/chat/sendGift")
    suspend fun sendGift(@Body form : SendChatGiftForm) : Root<ChatSendResult>

}