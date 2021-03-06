package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.KeyWordForm
import com.julun.huanque.common.bean.forms.LiveFollowForm
import com.julun.huanque.common.bean.forms.ProgramListForm
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by nirack on 16-11-1.
 */
interface ProgramService {


    /**
     * 直播间关注列表
     */
    @POST("live/program/info/followLivingList")
    fun followLivingList(@Body form: LiveFollowForm): Observable<Root<LiveFollowListData>>

    /**
     * 直播间关注列表
     */
    @POST("live/program/info/followList")
    suspend fun squareFollowList(@Body form: LiveFollowForm): Root<RootListData<AuthorFollowBean>>

    /**
     * 直播间广场列表
     */
    @POST("live/program/info/heatList")
    suspend fun squareHeatList(@Body form: ProgramListForm): Root<RootListData<ProgramLiveInfo>>


    /**
     * 获取直播列表
     */
    @POST("live/program/info/list")
    suspend fun programList(@Body form: ProgramListForm): Root<ProgramListInfo>

    /**
     * 获取直播列表
     */
    @POST("live/program/info/followPrograms")
    suspend fun followPrograms(@Body form: LiveFollowForm): Root<FollowProgramInfo>


    /**
     * 查询主播
     */
    @POST("live/program/info/search")
    suspend fun search(@Body form: KeyWordForm): Root<MutableList<ProgramLiveInfo>>
}