package com.julun.huanque.common.interfaces.routerservice

import android.app.Activity
import com.alibaba.android.arouter.facade.template.IProvider
import com.julun.huanque.common.bean.beans.OrderInfo

/**
 *
 *@author zhangzhen
 *@data 2019/4/11
 *微信支付接口
 *
 **/

interface WeiXinPayService : IProvider {
    /**
     * 微信支付
     */
    fun weixinPay(activity: Activity, orderInfo: OrderInfo)
}