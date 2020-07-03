package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListLiveData
import com.julun.huanque.common.basic.VoidForm
import com.julun.huanque.common.bean.beans.HeadNavigateInfo
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
import com.julun.huanque.common.bean.forms.RecomListForm
import io.reactivex.rxjava3.core.Observable
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
     * 查询主页主要信息
     */
    @POST("social/friend/home/info")
    suspend fun homeInfo(@Body form: VoidForm = VoidForm()): Root<HeadNavigateInfo>

    /**
     * 获取列表信息
     */
    @POST("social/friend/home/recomList")
    suspend fun homeRecomList(@Body form: RecomListForm): Root<RootListLiveData<UserLevelInfo>>


}