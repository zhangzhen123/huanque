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

    /**
     * 返回值resultType说明：
    MovePos：单向移动
    SwapPos：互换位置
    Upgrade：合成升级(客户端需移除合并的两只鹊，并插入合成后的升级鹊)
    Function：产生功能鹊(客户端需移除合并的两只鹊)
     */
    @POST("social/magpie/combine")
    suspend fun combine(@Body form: BirdCombineForm): Root<CombineResult>

    /**
     * 回收升级鹊同时返回当前金币及每秒收益
     */
    @POST("social/magpie/recovery")
    suspend fun recovery(@Body form: RecycleBirdForm): Root<RecycleResult>

    /**
     * 商店信息
     */
    @POST("social/magpie/shop")
    suspend fun shop(@Body form: EmptyForm = EmptyForm()): Root<BirdShopInfo>

    /**
     * 任务信息
     */
    @POST("social/magpie/taskList")
    suspend fun taskList(@Body form: EmptyForm = EmptyForm()): Root<BirdTaskInfo>

    /**
     * 领取任务
     */
    @POST("social/magpie/receiveTaskAward")
    suspend fun receiveTaskAward(@Body form: TaskBirdReceive): Root<BirdTaskReceiveResult>

    /**
     * 领取活跃奖励
     */
    @POST("social/magpie/receiveActiveAward")
    suspend fun receiveActiveAward(@Body form: TaskBirdActiveReceive): Root<BirdTaskReceiveResult>

    /**
     * 领取活跃奖励
     */
    @POST("social/magpie/fly")
    suspend fun fly(@Body form: BirdFunctionForm): Root<BirdFlyResult>

}