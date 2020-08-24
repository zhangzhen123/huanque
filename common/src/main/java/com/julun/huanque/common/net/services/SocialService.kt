package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidForm
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.EmptyForm
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.SendChatGiftForm
import com.julun.huanque.common.bean.beans.ChatGiftInfo
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.bean.beans.NetcallBean
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.forms.*
import io.reactivex.rxjava3.core.Observable
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
    fun createCommunication(@Body form: CreateCommunicationForm): Observable<Root<NetcallBean>>

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
    suspend fun getActiveWord(@Body form: FriendIdForm): Root<ActiveBean>

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

    /**
     * 获取用户消息设置
     */
    @POST("social/user/message/settings")
    suspend fun settings(@Body form: VoidForm = VoidForm()): Root<MessageSettingBean>

    /**
     * 更新用户消息设置
     */
    @POST("social/user/message/updateSettings")
    suspend fun updateSettings(@Body form: SettingForm): Root<VoidResult>

    /**
     * 发送骰子表情
     */
    @POST("social/friend/chat/sendDice")
    suspend fun sendDice(@Body form: SendMsgForm): Root<SendMsgBean>

    /**
     * 发送猜拳表情
     */
    @POST("social/friend/chat/sendFinger")
    suspend fun sendFinger(@Body form: SendMsgForm): Root<SendMsgBean>

    /**
     * 被叫占线
     */
    @POST("social/friend/netcall/busy")
    suspend fun voiceBusy(@Body form: NetcallIdForm): Root<VoidResult>

    /**
     * 获取用户基本信息及关系
     */
    @POST("social/friend/chat/userInfo")
    suspend fun userInfo(@Body form: FriendIdForm): Root<ChatUser>

    /**
     * 获取语音通话的结果
     */
    @POST("social/friend/netcall/result")
    suspend fun netcallResult(@Body form: NetcallIdForm): Root<NetcallResultBean>

    /**
     * 标记语音收费已经显示过
     */
    @POST("social/friend/netcall/markFeeRemind")
    fun markFeeRemind(@Body form: EmptyForm = EmptyForm()): Observable<Root<VoidResult>>

    @POST("social/friend/chat/sendRoom")
    suspend fun sendRoom(@Body form: SendRoomForm): Root<SendRoomBean>

    /**
     * 获取匿名语音基础信息
     */
    @POST("social/friend/avoice/homeInfo")
    suspend fun avoiceHomeInfo(@Body form: EmptyForm = EmptyForm()): Root<AnonymousBasicInfo>

    /**
     * 开始匹配
     */
    @POST("social/friend/avoice/startMatch")
    suspend fun startMatch(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 取消匹配
     */
    @POST("social/friend/avoice/cancelMatch")
    suspend fun cancelMatch(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 挂断匿名语音
     */
    @POST("social/friend/avoice/hangUp")
    suspend fun avoiceHangUp(@Body form: NetcallIdForm): Root<VoidResult>

    /**
     * 公开身份
     */
    @POST("social/friend/avoice/openIdentify")
    suspend fun openIdentify(@Body form: NetcallIdForm): Root<UserInfoInRoom>

    /**
     * 匿名语音 检测余额
     */
    @POST("social/friend/avoice/checkBeans")
    suspend fun checkBeans(@Body form: EmptyForm = EmptyForm()): Root<CheckBeansData>

    /**
     * 揭秘主播身份
     */
    @POST("social/friend/avoice/unveilIdentity")
    suspend fun unveilIdentity(@Body form: NetcallIdForm): Root<UserInfoInRoom>

    /**
     * 接受匿名语音
     */
    @POST("social/friend/avoice/accept")
    suspend fun avoiceAccept(@Body form: InviteUserIdForm): Root<VoidResult>

    /**
     * 拒绝匿名邀请
     */
    @POST("social/friend/avoice/reject")
    suspend fun avoiceReject(@Body form: InviteUserIdForm): Root<VoidResult>

    /**
     * 花魁 昨日榜
     */
    @POST("social/flower/rank/yesterday")
    suspend fun flowerYesterday(@Body form: EmptyForm = EmptyForm()): Root<FlowerDayListBean>

    /**
     * 花魁 今日榜
     */
    @POST("social/flower/rank/today")
    suspend fun flowerToday(@Body form: EmptyForm = EmptyForm()): Root<FlowerDayListBean>

    /**
     * 名人榜
     */
    @POST("social/flower/rank/famous")
    suspend fun flowerFamous(@Body form: EmptyForm = EmptyForm()): Root<FamousListBean>
}