package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.forms.EmptyForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *@创建者   dong
 *@创建时间 2020/7/6 16:04
 *@描述 联系人相关接口
 */
interface SocialService {

    @POST("social/message/link/info")
    suspend fun socialList(@Body form : EmptyForm = EmptyForm()) : Root<SocialListBean>
}