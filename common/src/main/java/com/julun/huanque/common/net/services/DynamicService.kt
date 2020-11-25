package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.DynamicDetailInfo
import com.julun.huanque.common.bean.beans.HomeDynamicListInfo
import com.julun.huanque.common.bean.beans.PublishDynamicResult
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.forms.EmptyForm
import com.julun.huanque.common.bean.forms.HomePostForm
import com.julun.huanque.common.bean.forms.PostDetailForm
import com.julun.huanque.common.bean.forms.PublishStateForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/24 14:33
 *
 *@Description: DynamicService
 *
 */
interface DynamicService {

    /**
     * 查询动态详情
     */
    @POST("social/post/postDetail")
    suspend fun queryPostDetail(@Body form: PostDetailForm): Root<DynamicDetailInfo>

    /**
     * 查询动态详情
     */
    @POST("social/post/list")
    suspend fun queryHomePost(@Body form: HomePostForm): Root<HomeDynamicListInfo>

    @POST("social/post/pubPost")
    suspend fun publishState(@Body body: PublishStateForm): Root<PublishDynamicResult>


}