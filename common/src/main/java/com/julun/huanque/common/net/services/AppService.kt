package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.FindNewsForm
import com.julun.huanque.common.bean.forms.RecomListForm
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/17 14:20
 *
 *@Description: AppService
 *
 */
interface AppService {

    /**
     * 获取列表信息
     */
    @POST("user/app/findNews")
    fun findNews(@Body form: FindNewsForm): Observable<Root<FindNewsResult>>

    /**
     * 获取广告
     */
    @POST("user/app/findStartAds")
    fun findStartAds(@Body form: Any): Observable<Root<Any>>
}