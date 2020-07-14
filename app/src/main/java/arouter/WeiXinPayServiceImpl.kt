package arouter

import android.app.Activity
import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.interfaces.routerservice.WeiXinPayService
import com.julun.huanque.support.WXApiManager


/**
 * Created by Android Studio.
 * User: xinchaodong
 * Date: 2019/4/4
 * Time: 11:13
 */
@Route(path = ARouterConstant.WEIXIN_PAY_SERVICE)
class WeiXinPayServiceImpl : WeiXinPayService {
    override fun weixinPay(activity: Activity, orderInfo: OrderInfo) {
        //调用SDK支付
        WXApiManager.doPay(activity, orderInfo)
    }

    override fun init(context: Context?) {

    }
}