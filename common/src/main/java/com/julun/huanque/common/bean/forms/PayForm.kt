package com.julun.huanque.common.bean.forms

/**
 * Created by djp on 2016/11/17.
 */
class PayForm : SessionForm() {
    var payType: String = ""//支付类型(客户端可选项：AlipayApp、WXPayApp)
    var tplId: Int = 0//价格模板ID
    var targetUserId: Long? = null//到账用户ID(如果给自己充值，不需要填)
    var rechargeEntry: String = ""//充值入口： 直播间快捷充值Room， 砸蛋快捷充值Egg， 个人中心充值My， 公众号菜单充值MPMenu(可选项：Room、Egg、My、MPMenu)
    //var openId: String = ""//开放平台唯一标识
    //var returnUrl:String ="" //成功重定向地址(支付宝wap支付必填)
    //var quitUrl:String ="" //取消支跳转地址(支付宝wap支付必填)
    //获取推荐充值信息
    var beans: Long? = null//	商品所需萌豆
}

class RechargeRuleQueryForm  {
    companion object {
        val SEND_GIFT = "SEND_GIFT"
        val DANMU = "DANMU"
        val GUARD = "GUARD"
        val FANS = "FANS"
        val FLIP_CARD = "FLIP_CARD"
        //星球霸主
        val PLANET = "PLANET"
    }

    var channelId: Int? = null  //app端默认就是1
    var toUserId: Int? = null
    var targetUserId: Long? = null
    /**
     * 可选
     * SEND_GIFT, 送礼
     * DANMU,   弹幕
     * GUARD,   开通守护
     */
    var insufficientReason: String? = null

    /**
     * 判断前面的 [.insufficientReason] 计算需要的价格使用的
     * 如果是 送礼，则传礼品 计算之后的价格，
     * 其他情况不用填写
     */
    var giftValue: Int? = null
    /**
     * 数量  eg:砸蛋次数
     */
    var count: Int? = null
}