package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.LiveFollowListData
import com.julun.huanque.common.bean.forms.LiveFollowForm
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

}