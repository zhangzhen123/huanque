package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/7 19:00
 *
 *@Description: StatisticService 统计接口
 *
 */
interface StatisticService {

    /**
     * 用户行为统计
     */
    @POST("user/app/dataStat")
    fun dataStat(@Body form: StatisticForm): Observable<Root<VoidResult>>

}