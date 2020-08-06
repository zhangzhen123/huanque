package com.julun.huanque.common.bean.beans

import android.graphics.Typeface
import androidx.annotation.ColorInt
import java.io.Serializable

/**
 * Created by nirack on 16-11-10.
 */

class LiveGiftDto {

    /** 价格  */
    var beans: Long = 0

    //是否可以赠送
    var couldSend: Boolean = true

    //可选数量列表
    var countItemList: List<GoodsOptionCount> = listOf()

    //礼物ID
    var giftId: Int = -1

    //礼物名称
    var giftName: String? = null

    //礼物图片
    var pic: String = ""

    //礼物选中图片
    var selPic: String = ""

    var typeCode: String = ""//Normal Super

    //物品类型（礼物还是道具）
    var prodType: String = ""

    //用户经验
    var userExp: Long = 0

    //是否是背包礼物
    var bag: Boolean = false


    /*以下字段待确认*/

    var bagCount: Int = 0       //背包数量


    var realRunwayBeans: Long = 0


    var tagContent: String? = null


    //守护卡类型
    var tips: String? = null


    //是否有表白功能
    var showLove: Boolean = true
    var popMsg: String? = null
    var processInfo: ProcessInfo? = null

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

    /** 折扣 */
    var discount: Int? = null

    /** 折扣券数量 */
    var discountCount: Int? = null


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
    var giftGroupInfoList: List<GroupInfo> = listOf(),
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
    var giftList: MutableList<LiveGiftDto> = mutableListOf(),
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
    var beans: Long = 0,
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
    var selPic: String = "",
    //本地字段 送礼数量，发送自定义消息场景下使用
    var giftCount: Int = 1
)

data class ChatSendResult(
    var beans: Long = 0,
    var intimateLevel: Int = 0,
    var intimateNum: Int = 0
)

/**
 * 小鹊互动语料
 */
data class ActiveWord(var content: String = "", var wordType: String = "")


data class ActiveBean(var activeList: MutableList<ActiveWord> = mutableListOf())


/**
 * 余额数据
 * @param beans 鹊币余额
 * @param consumeBeans 本次花费金额
 */
data class SendMsgBean(var beans: Long = 0, var consumeBeans: Long = 0)

/**
 * 图文混排对象
 */
class TIBean {
    companion object {
        const val TEXT = 0
        const val IMAGE = 1
    }

    //类型为0表示是文字，其他表示图片
    var type: Int = 0

    //文字颜色
    var textColor: String = ""

    //文字颜色int
    @ColorInt
    var textColorInt: Int = 0

    //文字大小
    var textSize: Int = 0

    //该段文字的内容
    var text: String = ""

    //文字类型
    var styleSpan: Int = Typeface.NORMAL

    //图片地址
    var url: String = ""

    //本地图片
    var imgRes: Int = 0

    //图片宽度
    var width = 0

    //图片高度
    var height = 0

    //是否转换成圆形
    var isCircle: Boolean = false

    //圆形边框颜色
    @ColorInt
    var borderRedId: Int = 0

    //圆形边框宽度
    var borderWidth: Float = 0f
}

data class ChatDetailBean(
    //拉黑状态
    var blacklistSign: Boolean = false,
    //背景所需要等级
    var chatBackgroundLevel: Int = 0,
    //用户ID
    var friendId: Long = 0,
    //头像
    var headPic: String = "",
    //当前亲密度等级
    var intimateLevel: Int = 0,
    //欢遇状态
    var meetStatus: String = "",
    //昵称
    var nickname: String = "",
    //签名
    var mySign: String = "签名不足以表达我自己，找我私聊吧~",
    var remindSign: String = ""
) :
    Serializable