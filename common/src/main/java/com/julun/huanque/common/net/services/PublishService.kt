package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.forms.PublishStateForm
import com.julun.huanque.common.bean.forms.PublishVideoForm
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@author zhangzhen
 *@data 2019/2/18
 *
 **/

interface PublishService {

    @POST("user/social/pubPost")
    fun publishState(@Body body: PublishStateForm): Observable<Root<VoidResult>>


    @POST("user/social/pubVideo")
    fun publishVideo(@Body body: PublishVideoForm): Observable<Root<VoidResult>>

//    @POST("user/social/checkContent")
//    fun checkContent(@Body body: CheckForm): Observable<Root<VoidResult>>
}