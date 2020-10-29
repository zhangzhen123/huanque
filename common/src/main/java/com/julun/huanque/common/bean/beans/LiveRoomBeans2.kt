package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * 礼物规则对象
 */
class GiftRuleBean(
    //可以获得的奖励列表
    var awardList: MutableList<GiftRuleAward> = mutableListOf(),
    //单价
    var beans: Long = 0,
    //礼物ID
    var giftId: Long = 0,
    //礼物名称
    var giftName: String = "",
    //消息列表
    var msgList: MutableList<String> = mutableListOf(),
    //当前礼物图片
    var pic: String = "",
    //规则背景
    var ruleBgPic: String = ""
) : Serializable

class GiftRuleAward(
    //奖励名称
    var awardName: String = "",
    //获奖比例
    var awardRatio: String = "",
    //奖励的总价值
    var beans: Long = 0,
    //礼物数量
    var count: Int = 0,
    //礼物图片
    var pic: String = ""
) : Serializable