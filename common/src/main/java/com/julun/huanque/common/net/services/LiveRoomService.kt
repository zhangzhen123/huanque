package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.CouponItemInfo
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.bean.beans.UserEnterRoomRespDto
import com.julun.huanque.common.bean.forms.ProgramIdForm
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


}