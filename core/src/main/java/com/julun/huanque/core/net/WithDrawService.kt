package com.julun.huanque.core.net


import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.beans.RechargeRespDto
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.bean.forms.*
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

interface WithDrawService {


    /**
     * 获取提现信息
     */
    @POST("cash/withdraw/info")
    suspend fun queryWithdrawInfo(@Body form: EmptyForm = EmptyForm()): Root<WithdrawInfo>


    /**
     * 支付宝授权信息
     */
    @POST("cash/authorize/alipayAuthInfo")
    suspend fun alipayAuthInfo(@Body form: EmptyForm = EmptyForm()): Root<String>

    /**
     * 支付宝授权回调
     */
    @POST("cash/authorize/alipay")
    suspend fun alipayCallback(@Body form: WithDrawAuthForm): Root<WithdrawInfo>

    /**
     * 微信授权回调
     */
    @POST("cash/authorize/weixin")
    suspend fun weixinCallback(@Body form: WithDrawAuthForm): Root<WithdrawInfo>


    /**
     * 申请提现
     */
    @POST("cash/withdraw/apply")
    suspend fun applyWithdraw(@Body form: WithdrawApplyForm): Root<WithdrawInfo>

    /**
     * 获取提现记录
     */
    @POST("cash/withdraw/history")
    suspend fun withdrawHistory(@Body form: WithdrawHistoryForm): Root<RootListData<Any>>


}