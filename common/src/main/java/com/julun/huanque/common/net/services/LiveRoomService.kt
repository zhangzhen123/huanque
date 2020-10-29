package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.basic.VoidForm
import com.julun.huanque.common.bean.forms.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url


interface LiveRoomService {
    /**
     * 用户进入直播间之前，获取基础信息
     */
    @POST("live/room/info/basic")
    suspend fun getLivRoomBase(@Body form: ProgramIdForm): Root<UserEnterRoomRespBase>

    /**
     * 用户进入直播间
     */
    @POST("live/room/info/enter")
    suspend fun enterLivRoom(@Body form: UserEnterRoomForm): Root<UserEnterRoomRespDto>

    /**
     * 获取直播间上下切换列表
     */
    @POST("live/room/info/switchList")
    suspend fun switchList(@Body form: SwitchForm): Root<ArrayList<SwitchBean>>

    /**
     * 获取用户名片
     */
    @POST("live/room/user/info")
    suspend fun userInfo(@Body form: UserProgramForm): Root<UserInfoInRoom>

    /**
     * 发送公聊消息
     */
    @POST("live/room/consume/sendPubMessage")
    suspend fun sendPubMessage(@Body form: ValidateForm): Root<ValidateSpamResult>


    /*************************************** 贡献榜 *********************************************/
    /**
     * 本月(默认每次查询10条)
     */
    @POST("live/room/rank/month")
    suspend fun queryScoreByMonth(@Body from: ScoreRankResultForm): Root<RootListData<RankingsResult>>

    /**
     * 本场(默认每次查询10条)
     */
    @POST("live/room/rank/show")
    suspend fun queryScoreByThis(@Body from: ScoreRankResultForm): Root<RootListData<RankingsResult>>

    /**
     * 本周(默认每次查询10条)
     */
    @POST("live/room/rank/week")
    suspend fun queryScoreByWeek(@Body from: ScoreRankResultForm): Root<RootListData<RankingsResult>>


    /*************************************** 用户在线列表 *************************************************/
    /**
     * 查询直播间贵族用户列表
     */
    @POST("live/room/info/listRoomRoyalUser")
    suspend fun queryRoyalList(@Body form: OnLineForm): Root<OnlineListData<OnlineUserInfo>>

    /**
     * 查询直播间普通用户列表
     */
    @POST("live/room/info/listRoomUser")
    suspend fun queryNormalList(@Body form: OnLineForm): Root<OnlineListData<OnlineUserInfo>>

    /**
     * 查询直播间守护用户列表
     */
    @POST("live/room/info/listRoomGuardUser")
    suspend fun queryGuardList(@Body form: OnLineForm): Root<OnlineListData<OnlineUserInfo>>

    /**
     * 查询直播间管理用户列表
     */
    @POST("live/room/info/listRoomMangerUser")
    suspend fun queryManagerList(@Body form: OnLineForm): Root<OnlineListData<OnlineUserInfo>>

    /**
     * 获取礼物数据
     */
    @POST("live/room/consume/giftsInfo")
    suspend fun giftsInfo(@Body form: LiveGiftForm): Root<GiftDataDto>


    /**
     * 查询背包数据
     */
    @POST("live/room/consume/bag")
    suspend fun bag(@Body form: ProgramIdForm = ProgramIdForm()): Root<MutableList<LiveGiftDto>>

    /**
     * 赠送礼物
     */
    @POST("live/room/consume/sendGift")
    suspend fun sendGift(@Body form: ConsumeForm): Root<SendGiftResult>


    //===================================== 管理相关接口 start ======================================
    /**
     * 查询管理权限
     */
    @POST("live/room/user/getManage")
    suspend fun getManage(@Body from: CardManagerForm): Root<ArrayList<ManagerOptionInfo>>

    /**
     * 保存管理操作
     */
    @POST("live/room/user/saveManage")
    suspend fun saveManage(@Body from: CardManagerForm): Root<VoidResult>

    /**
     * 离开直播间
     */
    @POST("live/room/info/leave")
    suspend fun leave(@Body form: ProgramIdForm): Root<VoidResult>

    /**
     * 刷新用户信息
     */
    @POST("live/room/info/refreshUser")
    suspend fun getRoomUserInfo(@Body form: UserEnterRoomForm): Root<UserInfo>

    /**
     * 获取礼物的规则数据
     */
    @POST("live/room/consume/giftRule")
    suspend fun giftRule(@Body form: GiftRuleForm): Root<GiftRuleBean>


    //===================================== 管理相关接口 end ========================================

    /**
     * 随机获取一个直播间
     */
    @POST("live/room/info/randomRoom")
    suspend fun randomRoom(@Body form: RandomRoomForm): Root<ProgramRoomBean>

    /**
     * 请求标记直播间欢鹊领取任务
     */
    @POST("social/magpie/watchStart")
    suspend fun watchStart(@Body form: ProgramIdForm): Root<VoidResult>

    /**
     * 观看直播任务结束
     */
    @POST("social/magpie/watchLive")
    suspend fun watchLiveEnd(@Body form: ProgramIdForm): Root<VoidResult>


}