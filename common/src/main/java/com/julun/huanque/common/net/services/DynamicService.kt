package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
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

    /**
     * 发布动态
     */
    @POST("social/post/pubPost")
    suspend fun publishState(@Body body: PublishStateForm): Root<PublishDynamicResult>


    /**
     * 查询动态列表
     */
    @POST("social/post/userPost")
    suspend fun queryUserPosts(@Body body: PostListsForm): Root<DynamicListInfo<DynamicItemBean>>

    /**
     * 动态点赞
     */
    @POST("social/post/postPraise")
    suspend fun postPraise(@Body body: PostForm): Root<VoidResult>

    /**
     * 动态取消点赞
     */
    @POST("social/post/cancelPraisePost")
    suspend fun cancelPraisePost(@Body body: PostForm): Root<VoidResult>

}