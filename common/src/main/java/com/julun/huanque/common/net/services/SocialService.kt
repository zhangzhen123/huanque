package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
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
    suspend fun unFollow(@Body form: FriendIdForm): Root<FollowBean>

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
     * 花魁 周榜
     */
    @POST("social/flower/rank/week")
    suspend fun flowerWeek(@Body form: EmptyForm = EmptyForm()): Root<FlowerDayListBean>

    /**
     * 名人榜
     */
    @POST("social/flower/rank/famous")
    suspend fun flowerFamous(@Body form: EmptyForm = EmptyForm()): Root<FamousListBean>

    /**
     * 道具列表
     */
    @POST("social/friend/chat/propList")
    suspend fun propList(@Body form: EmptyForm = EmptyForm()): Root<PropListBean>

    /**
     * 派单相关数据
     */
    @POST("social/friend/chat/home")
    suspend fun chatHome(@Body form: EmptyForm = EmptyForm()): Root<ChatRoomBean>

    /**
     * 派单列表
     */
    @POST("social/friend/chat/fateList")
    suspend fun fateList(@Body form: OffsetForm): Root<FateListInfo<FateInfo>>

    /**
     * 今日派单列表
     */
    @POST("social/friend/chat/todayFate")
    suspend fun todayFate(@Body form: EmptyForm = EmptyForm()): Root<TodayFateInfo>

    /**
     * 一键快速搭讪
     */
    @POST("social/friend/chat/quickAccost")
    suspend fun quickAccost(@Body form: QuickAccostForm): Root<QuickAccostResult>

    /**
     * 随机一条搭讪常用语
     */
    @POST("social/friend/chat/fateAccost")
    suspend fun chatWordsRandom(@Body form: FriendIdForm): Root<AccostMsg>

    /**
     * 搭讪常用语列表
     */
    @POST("social/friend/chatwords/list")
    suspend fun chatWordsList(@Body form: EmptyForm = EmptyForm()): Root<RootListData<SingleUsefulWords>>

    /**
     * 新增搭讪常用语
     */
    @POST("social/friend/chatwords/add")
    suspend fun chatwordsAdd(@Body form: AddWordsForm): Root<VoidResult>

    /**
     * 修改搭讪常用语
     */
    @POST("social/friend/chatwords/update")
    suspend fun chatwordsUpdate(@Body form: UpdateWordsForm): Root<VoidResult>

    /**
     * 删除搭讪常用语
     */
    @POST("social/friend/chatwords/del")
    suspend fun chatwordsDelete(@Body form: WordsIdForm): Root<VoidResult>

    /**
     * 快捷回复消息
     */
    @POST("social/friend/chat/sendMsg")
    suspend fun chatWordsSendMsg(@Body form: WordsIdForm): Root<VoidResult>

    /**
     * 缘分速配统计信息
     */
    @POST("social/friend/chat/fateStat")
    suspend fun fateStat(@Body form: EmptyForm = EmptyForm()): Root<FateWeekInfo>

    /**
     * 获取评价标签列表
     */
    @POST("social/friend/relation/evaluateTags")
    suspend fun evaluateTags(@Body form: FriendIdForm): Root<EvaluateTags>

    /**
     * 密友评价
     */
    @POST("social/friend/relation/evaluate")
    suspend fun relationEvaluate(@Body form: EvaluateForm): Root<RootListData<AppraiseBean>>

    /**
     * 语音点赞接口
     */
    @POST("social/friend/relation/voicePraise")
    suspend fun voicePraise(@Body form: FriendIdForm): Root<VoidResult>

    /**
     * 获取亲密榜数据
     */
    @POST("user/acct/info/closeConfidantRank")
    suspend fun closeConfidantRank(@Body form: UserIdForm): Root<MutableList<CloseConfidantBean>>

    /**
     * 全部圈子
     */
    @POST("social/group/list")
    suspend fun groupList(@Body form: CircleGroupTypeForm): Root<MyGroupInfo>

    /**
     * 加入圈子
     */
    @POST("social/group/join")
    suspend fun groupJoin(@Body form: GroupIdForm): Root<VoidResult>

    /**
     * 退出圈子
     */
    @POST("social/group/quit")
    suspend fun groupQuit(@Body form: GroupIdForm): Root<VoidResult>

    /**
     * 动态分享
     */
    @POST("social/post/share")
    suspend fun postShare(@Body form: PostShareForm): Root<PostShareBean>

    /**
     * 动态分享成功接口
     */
    @POST("social/post/saveShareLog")
    fun saveShareLog(@Body form: PostShareForm): Observable<Root<StatusResult>>

    /**
     * 评论动态
     */
    @POST("social/post/comment")
    suspend fun postComment(@Body form: PostCommentForm): Root<DynamicComment>

    /**
     * 1级评论更多
     */
    @POST("social/post/commentList")
    suspend fun commentList(@Body form: CommentListForm): Root<RootListData<DynamicComment>>

    /**
     * 2级评论更多
     */
    @POST("social/post/secondCommentList")
    suspend fun secondCommentList(@Body form: SecondCommentList): Root<RootListData<DynamicComment>>

    /**
     * 删除动态
     */
    @POST("social/post/deletePost")
    suspend fun deletePost(@Body body: PostForm): Root<VoidResult>

    /**
     * 删除评论
     */
    @POST("social/post/deleteComment")
    suspend fun deleteComment(@Body body: CommentIdForm): Root<VoidResult>

    /**
     * 分身账号消息数据
     */
    @POST("social/friend/chat/relateAccountMsg")
    suspend fun relateAccountMsg(@Body body: EmptyForm = EmptyForm()): Root<RelateAccountMsg>

    /**
     * 选择圈子接口
     */
    @POST("social/group/chose")
    suspend fun groupChose(@Body form: CircleGroupTypeForm): Root<MyGroupInfo>

    /**
     * 保存标签数据
     */
    @POST("social/user/tag/initLike")
    suspend fun initLikeTag(@Body form: TagIdsForm): Root<VoidResult>

    @POST("social/user/tag/socialType")
    suspend fun socialType(@Body body: EmptyForm = EmptyForm()): Root<LoginTagInfo>

    /**
     * 家乡数据
     */
    @POST("user/acct/card/homeTown")
    suspend fun homeTown(@Body body: FriendIdForm): Root<HomeTownInfo>

    /**
     * 访问历史
     */
    @POST("social/friend/relation/visitLog")
    suspend fun visitLog(@Body body: WatchForm): Root<WatchListInfo<WatchHistoryBean>>

    /**
     * 获取心动列表
     */
    @POST("social/friend/hearttouch/list")
    suspend fun hearttouchList(@Body form: HeartBeanForm): Root<HeartListInfo<SingleHeartBean>>

    /**
     * 刷新引导数据
     */
    @POST("social/friend/hearttouch/guideInfo")
    suspend fun guideInfo(@Body form: GuideInfoForm): Root<HeartListInfo<SingleHeartBean>>

    /**
     * 解锁心动
     */
    @POST("social/friend/hearttouch/unlock")
    suspend fun unlock(@Body form: UnlockForm): Root<VoidResult>
}