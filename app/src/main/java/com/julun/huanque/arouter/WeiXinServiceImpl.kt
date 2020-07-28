package com.julun.huanque.arouter

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.interfaces.routerservice.WeiXinService
import com.julun.huanque.support.WXApiManager
import com.julun.huanque.support.WeiBoApiManager


/**
 * Created by Android Studio.
 * User: xinchaodong
 * Date: 2019/4/4
 * Time: 11:13
 */
@Route(path = ARouterConstant.WEIXIN_SERVICE)
class WeiXinServiceImpl : WeiXinService {
    override fun weiXinPay(activity: Activity, orderInfo: OrderInfo) {
        //调用SDK支付
        WXApiManager.doPay(activity, orderInfo)
    }

    override fun weiXinAuth(activity: Activity) {
        WXApiManager.doLogin(activity)
    }

    override fun weiXinShare(context: Activity, shareObj: ShareObject) {
        WXApiManager.doShare(context, shareObj)
    }

    override fun weiBoShare(context: Activity, shareObj: ShareObject) {
       WeiBoApiManager.doWeiBoShare(context,shareObj)
    }

    override fun weiBoShareResult(data: Intent) {
        WeiBoApiManager.shareResult(data)
    }

    override fun init(context: Context?) {

    }
}