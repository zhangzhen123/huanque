package com.julun.huanque.common.interfaces.routerservice

import android.app.Activity
import com.alibaba.android.arouter.facade.template.IProvider
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.bean.beans.ShareObject

/**
 *
 *@author zhangzhen
 *@data 2019/4/11
 *微信支付接口
 *
 **/

interface WeiXinService : IProvider {
    /**
     * 微信支付
     */
    fun weiXinPay(activity: Activity, orderInfo: OrderInfo)

    fun weiXinAuth(activity: Activity)

    fun weiXinShare(context: Activity, scene: Int, shareObj: ShareObject)
}