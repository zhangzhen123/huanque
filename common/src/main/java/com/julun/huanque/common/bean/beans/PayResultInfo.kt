package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * Created by djp on 2016/11/17.
 */
// 微信订单信息
class OrderInfo {
    var appid: String = ""
    var partnerid: String = ""
    var prepayid: String = ""
    var noncestr: String = ""
    var `package`: String = ""
    var timestamp: String = ""
    var sign: String = ""
}

class PayResultInfo : Serializable {
    var content:Any= Any()
    var alipayOrderInfo: String = ""

    var wxOrderInfo: OrderInfo = OrderInfo()

    var payType: String = ""
    //网页跳转链接
    var contentString: String = ""
    //oppo支付
    var oppoOrderInfo : OppoPayInfo? = null
    //订单号，oppo支付
    var orderNo : String? = null

}

/**
 * oppo支付参数
 */
class OppoPayInfo : Serializable {
    //商品描述
    var productDesc: String = ""
    //支付金额（单位：分）
    var amount: String = ""
    //回调服务器的地址
    var callbackUrl: String = ""
    //商品名称
    var productName: String = ""
}

data class WithdrawInfo(
    var myCash: Int = 0,
    var todayCash: Int = 0,
    var totalWithdraw: Int = 0,
    var tplList: MutableList<WithdrawTpl> = mutableListOf()
)

data class WithdrawTpl(
    var fast: Boolean = false,
    var money: Int = 0,
    var moneyText: String = "",
    var tplCode: String = ""
)