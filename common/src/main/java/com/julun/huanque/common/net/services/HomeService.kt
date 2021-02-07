package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/2 10:46
 *
 *@Description: HomeService 首页相关接口
 *
 */
interface HomeService {


    /************************************** 个人中心 **********************************************/

    /**
     * 查询主页主要信息 offset为了0时会附带主页其他信息 否则只返回列表信息
     */
    @POST("social/friend/home/recom")
    suspend fun homeRecom(@Body form: RecomListForm): Root<HomeListData<HomeRecomItem>>

    /**
     * 获取列表信息
     */
    @POST("social/friend/home/recomList")
    suspend fun homeRecomList(@Body form: RecomListForm): Root<RootListData<HomeItemBean>>

    /**
     * 获取附近列表
     */
    @POST("social/friend/home/nearby")
    suspend fun getNearBy(@Body form: NearbyForm): Root<NearbyListData<NearbyUserBean>>

    /**
     * 获取喜欢列表
     */
    @POST("social/friend/home/likeList")
    suspend fun getLikeList(@Body form: LikeForm): Root<FavoriteListData<FavoriteUserBean>>

    /**
     * 喜欢
     */
    @POST("social/friend/home/like")
    suspend fun like(@Body form: FeelLikeForm): Root<VoidResult>

    /**
     * 无感
     */
    @POST("social/friend/home/noFeel")
    suspend fun noFeel(@Body form: FriendIdForm): Root<VoidResult>


}