package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.HomeListData
import com.julun.huanque.common.bean.beans.HomeRecomItem
import com.julun.huanque.common.bean.forms.RecomListForm
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
    suspend fun homeRecom(@Body form: RecomListForm ): Root<HomeListData<HomeRecomItem>>

    /**
     * 获取列表信息
     */
    @POST("social/friend/home/recomList")
    suspend fun homeRecomList(@Body form: RecomListForm): Root<RootListData<HomeItemBean>>


}