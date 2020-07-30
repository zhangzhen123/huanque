package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.CouponItemInfo
import com.julun.huanque.common.bean.beans.SwitchBean
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.bean.beans.UserEnterRoomRespDto
import com.julun.huanque.common.bean.forms.AnchorProgramForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.bean.forms.SwitchForm
import com.julun.huanque.common.bean.forms.UserEnterRoomForm
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
}