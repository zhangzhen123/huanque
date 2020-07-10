package com.julun.huanque.common.bean.beans

/**
 * Created by nirack on 16-11-10.
 */

class LiveGiftDto {
    var giftId: Int? = null

    /** 物品名称  */
    var giftName: String? = null
    var bagCount: Int = 0       //背包数量

    /** 价格  */
    var beans: Long = 0
    var realRunwayBeans: Long = 0

    /** 图标  */
    var pic: String? = null
    var tagContent: String? = null

    //可选数量列表
    var countItems: List<GoodsOptionCount> = listOf()

    //守护卡类型
    var tips: String? = null

    var typeCode: String = ""//Normal Super

    //是否有表白功能
    var showLove: Boolean = true
    var popMsg: String? = null
    var processInfo: ProcessInfo? = null
    var userExp: Long = 0

    //是否匿名礼物
    var anonymous: Boolean = false

    //规则地址
    var ruleUrl: String = ""

    //是否显示幸运和高爆模式
    var luckyOrHigh: Boolean = false

    //到期时间
    var expTime: String = ""

    //点击事件 标识
    var tipSkipType: String = ""

    //4.17新增字段
    /** 折后价格  since 4.17.0  */
    var discountBean: Int? = null

    /** 折扣券图片  */
    var discountPic: String? = null

    /** 折扣 */
    var discount: Int? = null

    /** 折扣券数量 */
    var discountCount: Int? = null

    //是否可以赠送
    var couldSend: Boolean = true

}

// 当前礼物可选数量（一见钟情，十全十美）
class GoodsOptionCount {
    var countName: String = ""
    var countValue: Int = 0
}

//class FetchGiftDataDto {
//    var gifts: List<LiveGiftDto> = listOf()
//    var beans: Long = 0
//
//    // 上跑道的最小萌豆值
//    var runwayMinBean: Long = -1
//    //当前跑道金额
//    var runwayMaxBeans: Long = -1
//    //爱的表白最小值控制值,最小能发送表白的值
//    var runwayMinLoveBean: Long = -1
//}
//新版礼物接口
data class GiftDataDto(
    var beans: Long = 0,
    //背包tab 红点标识位
    var bagChange: Boolean = false,
    var groupInfo: List<GroupInfo> = listOf(),
    var needExp: Long = 0,//下一级的经验总值
    var runwayMaxBeans: Long = 0,
    var runwayMinBean: Long = 0,
    var runwayMinLoveBean: Long = 0,
    var showTab: String = "",
    var userExp: Long = 0,//当前级的经验进度值
    var userLevel: Int = 0,
    var expRatio: Double = 0.0,//经验比例
    var expRatioTtl: Long = 0,//经验卡倒计时
    //是否匿名状态
    var anonymousHitEgg: Boolean = false,
    //是幸运砸蛋还是高爆砸蛋
    var luckyHitEgg: Boolean = false,
    //4.17新增字段
    //是否使用折扣券
    var discountFirst: Boolean = false,
    //4.17.1新增字段
    //是否显示月卡入口
    var showMonthCard: Boolean? = null,
    //月卡气泡显示内容
    var monthCardPopMsg: String? = null,
    //月卡气泡显示时长
    var monthCardPopMsgSeconds: Long? = null
)

data class GroupInfo(
    var gifts: MutableList<LiveGiftDto> = mutableListOf(),
    var typeCode: String = "",
    var typeName: String = "",
    var version: Int = 0
)

//本地使用
data class TabItemInfo(
    var typeCode: String = "",
    var typeName: String = "",
    //tab对应的版本
    var version: Int = 0
)

data class ProcessInfo(
    var giftId: Int = 0,
    var needCount: Int = 0,
    var nowCount: Int = 0
)

data class ChatGiftInfo(
    var beans: Int = 0,
    var giftList: List<ChatGift> = listOf(),
    var tips: String = "",
    //本地字段
    var viewPagerData: MutableList<ChatGroupGift> = mutableListOf()
)

data class ChatGroupGift(
    var gifts: MutableList<ChatGift> = mutableListOf(),
    var version: Int = 0
)


data class ChatGift(
    var beans: Int = 0,
    var chatGiftId: Int = 0,
    var giftName: String = "",
    var pic: String = "",
    var selPic: String = ""
)