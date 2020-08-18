package com.julun.huanque.common.bean.beans

import java.io.Serializable
import kotlin.collections.ArrayList


data class RechargeRespDto(
    var adList: MutableList<RechargeAdInfo> = mutableListOf(),
    var beans: Long = 0,
    var headPic: String = "",
    var helpUrl: String = "",
    var nickname: String = "",
    var payTypes: String = "",
    var platformBeans: Long = 0,
    var rechargeBeans: Long = 0,
    var tplList: MutableList<RechargeTpl> = mutableListOf(),
    var userId: Long = 0L,
    //客服地址
    var customerUrl: String = ""
)

data class RechargeTpl(
    var beans: String = "",
    var money: String = "",
    var moneyValue: Long = 0,
    var rcvBeans: String = "",
    var tplId: Int = 0,
    var discountType: String = "",
    var discountTag: String = ""
)

/**
 * 充值页广告数据
 * @author WanZhiYuan
 * @iterativeVersion 4.19.1
 */
class RechargeAdInfo : Serializable {
    /** 引导广告代码 **/
    var adCode: String = ""

    /** 广告标题 **/
    var adTitle: String = ""

    /** 资源类型 **/
    var resType: String = ""

    /** 资源地址 **/
    var resUrl: String = ""

    /** 打开类型 **/
    var touchType: String = ""

    /** 打开类型值 **/
    var touchValue: String = ""
}