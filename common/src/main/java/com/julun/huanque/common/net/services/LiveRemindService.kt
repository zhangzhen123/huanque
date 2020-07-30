package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.LiveRemindBeans
import com.julun.huanque.common.bean.beans.RootListLiveData
import com.julun.huanque.common.bean.forms.LiveRemindForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 节目相关
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15
 */
interface LiveRemindService {
    /**
     * 节目关注列表
     */
    @POST("live/program/info/followList")
    suspend fun followList(@Body form: LiveRemindForm): Root<RootListLiveData<LiveRemindBeans>>

    /**
     * 修改关注节目开播推送
     */
    @POST("live/program/info/changeFollowPush")
    suspend fun changeFollowPush(@Body form: LiveRemindForm): Root<VoidResult>
}