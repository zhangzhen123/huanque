package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
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
     * 关注节目
     */
    @POST("live/room/anchor/follow")
    suspend fun queryFollow(@Body form: ProgramIdForm): Root<VoidResult>

    /**
     * 取消关注节目
     */
    @POST("live/room/anchor/unFollow")
    suspend fun queryUnFollow(@Body form: ProgramIdForm): Root<VoidResult>

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

    @POST("live/room/consume/sendPubMessage")
    suspend fun sendPubMessage(@Body form: ValidateForm): Root<ValidateSpamResult>
}