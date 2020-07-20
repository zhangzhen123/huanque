package com.julun.huanque.core.net


import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.beans.RechargeRespDto
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.bean.forms.EmptyForm
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.bean.forms.RechargeRuleQueryForm
import com.julun.huanque.common.bean.forms.SessionForm
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/11 17:40
 *
 *@Description: RechargeService
 *
 */

interface RechargeService {
    /**
     * 查询APP的充值规则
     */
    @POST("user/pay/recharge/info")
    suspend fun queryChannelRule(@Body form: RechargeRuleQueryForm): Root<RechargeRespDto>

    /**
     * 创建充值订单
     */
    @POST("user/pay/recharge/order")
    suspend fun createAppPay(@Body form: PayForm): Root<PayResultInfo>

    /**
     * 获取推荐充值信息(快充)
     */
    @POST("user/pay/recharge/recom")
    suspend fun queryRecomPay(@Body form: PayForm): Root<RechargeRespDto>



}