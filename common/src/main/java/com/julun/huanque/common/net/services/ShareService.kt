package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.beans.SharePosterInfo
import com.julun.huanque.common.bean.forms.SharePosterImageForm
import com.julun.huanque.common.bean.forms.SharePosterQueryForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/27 14:07
 *
 *@Description: 分享相关
 *
 */
interface ShareService {

    /**
     * 获取分享的海报
     */
    @POST("user/share/sharePoster")
    suspend fun sharePoster(@Body form: SharePosterQueryForm): Root<SharePosterInfo>


    @POST("user/share/programShare")
    suspend fun programShare(@Body form: SharePosterImageForm): Root<String>
    //获取直播间链接分享信息
    @POST("user/share/room")
    suspend fun programShareInfo(@Body form: SharePosterImageForm): Root<ShareObject>

}