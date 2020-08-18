package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.BuyBirdForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.bean.forms.RecomListForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/2 10:46
 *
 *@Description: 养鸟相关
 *
 */
interface LeYuanService {

    /**
     * 获取养鹊首页
     */
    @POST("social/magpie/home")
    suspend fun birdHome(@Body form: ProgramIdForm): Root<BirdHomeInfo>

    /**
     * 买鸟
     */
    @POST("social/magpie/buy")
    suspend fun buyBird(@Body form: BuyBirdForm): Root<BuyBirdResult>
}