package com.julun.huanque.common.bean.beans

import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.alibaba.fastjson.annotation.JSONField
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.constant.RedPackageType
import com.julun.huanque.common.constant.AnimationTypes
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/** 送礼事件 **/
class SendGiftEvent(
    var giftId: Int = -1,
    var cacheIt: Boolean = false,
    var count: Int = 0,
    var seconds: Int = 0,
    var toUserId: Long = -1,
    var nickname: String = "",
    var giftPic: String = "",
    var userId: Long = -1L,
    var toNickname: String = "",
    var giftName: String = ""
    , var headPic: String = ""
    , var timeStr: String = ""
    , var luckBeans: Int = 0
    , var luckAnima: String = ""
    , var beans: Int = -1
    , var level: Int = -1//流光等级//新版特效动画参数
    , var times: Int = 0//连送次数
    , var upgrade: Boolean = false//是否升级动画
    , var curTime: Long = 0L//排序时间戳
) {
    var generateId: String = "${userId}_$giftId"
        get() = "${userId}_$giftId"

    //本地字段
    var isMyself: Int = 0

    override fun toString(): String {
        return "$generateId-$giftName:$beans-$times-$curTime"
    }
}

/** 排行榜事件 **/
class RankingEvent(var resultList: List<UserInfoForLmRoom> = listOf<UserInfoForLmRoom>())

/** 直播间用户变更事件 **/
class RoomUserListEvent(
    var userCount: Int = 0,
    var visitorCount: Int = 0,
    var userList: List<UserInfoForLmRoom> = listOf()
)

data class RoomUserChangeEvent(
    var changes: List<UserInfoForLmRoom> = listOf(),
    var royalCount: Int = 0,
    var visitCount: Int = 0,
    var totalCount: Int = 0,
    var programId: Long = 0,
    /** 贵族数量 **/
    var honorCount: Int = 0,
    /** 守护数量 **/
    var guardCount: Int = 0
)

/** 开通守护 **/
class OpenGuardEvent(
    val months: Int = 1,
    var nickname: String = "",
    var userId: Long = 0L,
    var headPic: String = ""
)

/**愚人节福蛋**/
class YuRenJieEvent(var yurenjiettl: Int = 0)

/**本场贡献榜变化消息**/
class ContributionEvent(var score: Long = 0)

/** 幸运礼物 **/
class LuckGiftEvent(
    var userId: Long = 0,
    var nickname: String = "",
    var luckTotalBeans: Long = 0,
    var giftName: String = ""
)

/**PK开始**/
class PKStartEvent(
    var pkInfo: PKInfoBean? = null,
    //推流相关数据
    var pushData: HashMap<String, PlayInfoAdd> = hashMapOf()
)

/**
 * 主播PK 开始/结束 消息
 */
class AnchorPKStartEvent(var pkInfo: AnchorPKInfo? = null)

class AnchorPKInfo(var detailList: MutableList<SingleAnchorPkInfo> = mutableListOf())

/**
 * 推流端切换需要使用的基础消息
 */
open class SwitchPublishBaseData(
    var pushUrl: String = "",
    var sdkParams: SdkParam? = null,
    var sdkProvider: String = "",
    var type: String = "",
    //直播间ID
    var programId: Long = 0
) : Serializable

/**
 * 单个主播PK信息
 */
class SingleAnchorPkInfo(
    var anchorId: Long = 0L,
    var anchorLevel: Int = 0,
    var creator: Boolean = false,
    var headPic: String = "",
    var nickname: String = "",
    var prePic: String = "",
    var programName: String = "",
    var propScore: Int = 0,

    var score: Int = 0,
    var scoreTime: Long = 0,
    var status: String = ""
) : SwitchPublishBaseData(), Serializable

/**PK结束**/
class PKFinishEvent(
    var programIds: List<Int> = arrayListOf(),
    var pkInfo: PKInfoBean? = null
)

//PK结束附加消息
class PlayInfoAdd(var playInfo: PlayInfo? = null)

/***PK比分变化*/
class PKScoreChangeEvent(var pkInfo: PKInfoBean? = null)

/**PK结果  4.30.0新增PK段位字段[stageDetailList]**/
class PKResultEvent(
    var detailList: ArrayList<PKResultUser>? = null, var pkType: String = "",
    var programIds: List<Int> = arrayListOf(),
    var stageDetailList: ArrayList<PkStageDetail>? = null,
    var template: PKExtra? = null
) : Serializable

/**月赛结果变化**/
class MonthPKResultEvent(var top: String? = "", var type: String? = "")

/**PK结果用户*/
class PKResultUser(
    var nickname: String? = "",
    var headPic: String = "",
    var result: String = "",
    var programId: Long = 0,
    var score: Long = 0,
    var userId: Long = 0
) : Serializable

/**
 * pk额外参数 用于动态改变ui
 */
class PKExtra : Serializable {
    var resultPic: String? = null//PK结果图片
    var pkTwoPic: String? = null// 2人PK面板图片
    var pkThreePic: String? = null// 3人PK面板图片
    var winPic: String? = null //胜利图片
    var drawPic: String? = null//平局图片
    var closePic: String? = null//PK结果面板关闭按钮图片


    var timePic: String? = null   //倒计时背景图
    var timeLable: String? = null  //倒计时标签名称
    var scoreLabel: String? = null   //分数标签名称
    var timeColour: String? = null   //倒计时字体颜色
    var timeLableColour: String? = null   //倒计时标签字体颜色
    var nameColour: String? = null   //主播标签字体颜色
    var scoreNumColour: String? = null //分数字体颜色
    var scoreLabelColour: String? = null //分数标签名称颜色
    var brLineColour: String? = null// PK面板 主播直接的分割线颜色
    var progressBarColour: String? = null//进度条底色
    var progressCoverColour1: String? = null//第1个主播进度条覆盖色
    var progressCoverColour2: String? = null//第2个主播进度条覆盖色
    var progressCoverColour3: String? = null//第3个主播进度条覆盖色


    //世界杯的相关参数
//        "round":3 ,//轮次
//        "raceCourse":"groupMatch",//groupMatch小组赛,final1To8 1/8决赛,final1To4 1/4决赛 semifinal半决赛, threeFourFinal 三四名决赛,lastfinal决赛
//        "zsTimes": 2,//左边比分
//        "zfTimes":0 //右边比分
    var round: Int? = 0
    var raceCourse: String? = null
    var zsTimes: Int? = 0
    var zfTimes: Int? = 0
}

/**
 * PK创建的信息处理
 */
data class PKCreateEvent(
    var nickname: String = "",
    var action: String = "",
    var pkId: Int = 0,
    var userId: Long = 0,
    var userIds: List<Long> = listOf(),
    var detailList: List<PKUser> = listOf(),
    var countDown: Int = 0,
    /** 节目id **/
    var programId: Long = 0
) : Serializable

/**
 * pk礼物任务
 */
data class PkTaskEvent(
    var programId: Long = 0,
    var taskInfo: PkGiftTaskInfo? = null
)

/**
 * pk积分任务
 */
data class PkScoreEvent(
    var programId: Long = 0,
    var taskInfo: PkScoreTaskInfo? = null
)

/**
 * pk道具任务
 */
data class PkPropEvent(
    var programIds: List<Long> = listOf(),
    var propInfo: PkPropInfo? = null
)

/**
 * pk道具使用提醒
 */
data class PkPropUseWarnEvent(
    var propUseWarnInfo: PkPropUseWarnInfo? = null
) : Serializable

data class PkPropUseWarnInfo(
    var programId: Long = 0,
    var propName: String = "",
    var programName: String = "",
    var propIcon: String = "",
    var warnText: String = ""
) : Serializable

/**
 * 超级幸运礼物
 */
class SuperLuckGiftEvent(
    var goodsName: String? = "",
    var coin: Long = 0,
    var goodsPicId: String? = "",
    var nickname: String? = "",
    var userId: Long? = 0
)

/**主播、用户升级事件**/
class UserUpgradeEvent(
    var userId: Long = 0,
    var nickname: String = "",
    var newLevel: Int = 0,
    var newExp: Long = 0L
) {
    var grantInfo: List<GrantInfo>? = null
}

data class RoyalUpLevelEvent(
    var newLevel: Int = 0,
    var money: Long = 0,
    var newLevelName: String = "",
    /**勋章原名称*/
    var royalName: String = "",
    /**上神勋章剩余修改次数*/
    var changeCount: Int = 0,
    var userId: Long = 0,
    var userIds: List<Int> = listOf(),
    var grantInfo: List<GrantInfo>? = null
//        /** 是否展示引导贵族月卡弹窗 **/
//        var rechargeMonthCardMsg: String? = null
) : Serializable

class GrantInfo : Serializable {
    /** 商品ID  */
    private val prodId: Int? = null

    /** 商品类型  */
    var prodType: String? = null

    /** 商品图片  */
    var prodPic: String? = null

    /** 商品名称  */
    var prodName: String? = null

    /** 商品单价  */
    var beans: Long? = null

    /** 奖励数量  */
    var awardCount: Int = 0

    /** 按数量管理物品的有效期截止日  */
    var expTime: Date? = null
}

/*用户经验变化通知*/
data class UserExpChangeEvent(
    var newLevel: Int = 0,
    var time: Long = 0,
    var needExpValue: Long = 0,
    var userId: Long = 0,
    var newExp: Long = 0,


    var upgrade: Boolean = false,
    var level: Int = 0,
    var giftName: String = "",
    var cacheStartTime: Long = 0,
    var totalBeans: Long = 0,
    /* var isWeekStar: String = "",*/
    var giftBeans: Long = 0,
    var times: Int = 0,
    var giftPic: String = "",
    var cacheIt: Boolean = false
) {
    @JSONField(name = "isLuck")
    var isLuck: Boolean = false
}

class SuperLuckGiftContext(
    var goodsName: String? = "",
    var coin: Long? = 0,
    var goodsPicId: String? = "",
    var nickname: String? = ""
)


class AnimEventItem {
    var animationResId: Int = 1
    var resType: String? = null
    var resValue: String = ""
}

//主播升级事件
data class AnchorUpgradeEvent(
    var nickname: String = "",
    var userId: Long = 0,
    var newExpValue: Long = 0,
    var newLevel: Int = 0, //升级的下一级值
    //升级成功
    var sendUserId: Long = 0L, //升级中奖的用户
    var awardValue: Int = 0,    //奖励萌豆
    var sendNickname: String = ""     //提示的用户名
)

data class AnchorLevelProgressEvent(
    var show: Boolean = false,
    var max: Long = 0,
    var diff: Long = 0,
    var curr: Long = 0,
    var nextLevel: Int = 0, //升级的下一级值
    //前端用字段
    var needExpValue: Long = 0,
    var currExpValue: Long = 0,
    //升级成功
    var senduserId: Long = 0, //升级中奖的用户
    var awardValue: Int = 0,    //奖励萌豆
    var sendNickname: String = ""     //提示的用户名
)

class BoatProgressEventBean {
    var diff: Long = 0L  //差多少点亮 字
    var finied: Int = 0    //  表示完成度

    //下面两个可能为kong
    var finiedName: String = ""      //端午XXX 几个汉字中的一个

    var queryDetailUrl: String? = null

    var redPacketData: ArrayList<RedPacketData>? = null
}

class RedPacketData {
    var redPacketCountdown: Int = -1
    var redPacketId: Int = -1
}

/** 动画消息 **/
class AnimEventCfg(val userId: Long = 0, nickname: String = "") {
    var platform = "WEB"        //暂不处理,所有平台都使用这个
    var msgType = "ANIMATION"
    var display = "PUBLIC_CHAT"
    var animName = "守护天使入场动画"
    var animType: String = AnimationTypes.NONE
    var type = "busi.msg"
    var animId = "1"
    var extraObject: MutableMap<String, Any> = mutableMapOf()//自定义的(不是从后台返回的)属性,用于动画的渲染
    var resources: List<AnimEventItem> = listOf()

    // 主播、用户升级了,最新等级
    var levelValue: Int = 0
}

/** 新版的动画消息 **/
data class AnimEventBean(
    var gifUrl: String = "",
    var svgaUrl: String = "",
    var webUrl: String = ""
)

/**
 * 弹幕事件
 */

data class BarrageEvent(
    var eventCode: String = "",//本地字段
    var userLevel: Int = 0,
    var royalLevel: Int = 0,
    var nickname: String = "",
    var userId: Long = 0,
    var headPic: String = "",
    var royalPic: String = "",
    var content: String = "",
    //主播等级
    var anchorLevel: Int = 0,
    //神秘人
    var mystery: Boolean = false,
    var royalDmLevel: Int = 0//
)

/** 开播事件 **/
class OpenShowEvent(
    var programId: Long = 0,
    var playUrl: String = "",
    var playInfo: PlayInfo? = null
) {
    @JSONField(name = "isPcLive")
    var isPcLive: Boolean = false
}

/** 关播事件 **/
class CloseShowEvent(var programId: Long = 0, var stopType: String = "")


class AnimModel() {
    var animType: String = AnimationTypes.NONE
    var nickname: String = ""
    var levelValue: Int = 0
    var extraObject: MutableMap<String, Any> = mutableMapOf()
}


class EventMessageContent<T> {
    var context: T? = null
}


data class MicAnchor(
    var status: String = "",
    // 主播封面
    var prePic: String = "",
    //流id
    var streamID: String = "",
    //流地址
    var playUrl: String = "",
    //播放数据
    var playInfo: PlayInfo? = null,
    //是不是主播推流端（本地字段）
    var isAnchor: Boolean = true,
    //是不是pk连麦的标志(本地字段)
    var isPK: Boolean = false,
    //PK过程当中切换CDN消息使用的（本地字段）
    var showInfo: Boolean = false,
    var pushUrl: String = "",
    var sdkParams: SdkParam? = null,
    var sdkProvider: String = "",
    //pk类型
    var pkType: String? = null
) : AnchorBasicInfo() {
    @JSONField(name = "isCreator")
    var isCreator: Boolean = false
//        @JSONField(name = "isCreator")
//        set(value) {
//            field = value
//        }

    //重写equals 方便判断
    override fun equals(other: Any?): Boolean {
        if (other is MicAnchor) {
            return programId == other.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.hashCode()
    }

    override fun toString(): String {
        return "MicAnchor(streamID='$streamID' url=${playInfo?.rtmp})"
    }

}

data class MicOperateBean(
    var seconds: Int = 0,
    var action: String = "",
    var micId: Long = 0,
    var userId: Long = 0,
    var userIds: ArrayList<Int> = arrayListOf()
    ,
    var detailList: ArrayList<MicAnchor> = arrayListOf()
) : Serializable


data class MicActionBean(
    var action: String = "",
    var programIds: List<Int> = listOf(),
    var micInfo: MicInfo = MicInfo(),
    var programName: String = ""
)

data class MicInfo(
    var micId: Int = 0,
    var mixStreamName: String = "",
    var joinList: ArrayList<MicAnchor> = arrayListOf()
)

//跑道最多显示 15 分钟.... cacheId 客户端是否缓存 , cacheStartTime 开始缓存的时间 (不处理),   cacheValue  是否能替换跑道的价格
class TplBeanExtraContext(
    var programId: Long = 0,
    var cacheIt: Boolean = false,
    var cacheStartTime: Long = 0L,
    var cacheValue: Long = 0,
    var seconds: Long = 0L,
    var giftPic: String = "",
    //点击卡片时用到
    var userId: Long = 0,
    //调整直播间时用到
    var roomId: Long = 0,
    //礼盒价值
    var giftBeans: Long = 0,
    //礼盒类型
    var runwayType: String = "",
    //打开h5需要
    var url: String = "",
    var needLogin: String = "",//T/F

    /**
     * 新版入场特效字段
     */
    var theme: String = "",
    var svga: String = "",
    var type: String = "",
    var webp: String = "",
    /**
     * 新增红包通知字段
     */
    var programName: String = "",
    var rbIcon: String = "",
    /**
     * 主播拯救相关
     * remindEndMills提醒有效截止时间
     *
     */
    var remindEndMills: Long? = null,
    var salvationId: Int? = null,

    var bgColor: String = "",

    var runwayPlayTimes: Int = 0,

    var runwayPic: String = "",


    /**
     * --------------------------------------------------------
     * 一下都是本地字段
     * 自定义的属性
     * canOnlyPlayOneTime 是否时直播间初始化的时候带过来   的消息,如果是,则当播放一遍之后,如果有新的消息,可以不用等三遍,直接播放下一跳记录
     */
    var canOnlyPlayOneTime: Boolean = false,
    /**
     * 自定义属性 当前缓存消息是否是播三次了(本地字段)
     */
    var isOld: Boolean = false,

    //本地字段 用于判断去重的
    var msgId: String = ""

)


class TplCmdBean(var cmd: String = "", var content: TplBean? = null)

class TplTypeBean(var type: String = "", var content: TplBean? = null) {
    companion object {
        val RIDE = "RIDE"
        val ROYAL = "ROYAL"
        val GUARD = "GUARD"
        val COMMON = "COMMON"
    }
}

//自定义的富文本实体类
class BaseTextBean {
    var text: String = ""
    var realText: String = ""

    //图片的url 位置
    var imgParams: MutableList<ImageParam> = mutableListOf()

    //文字区域
    var textParams: MutableList<TextParam> = mutableListOf()
}

open class BaseParams()

class ImageParam : BaseParams() {
    var url: String = ""
    var index: Int = 0
    var imgRes: Int = 0//本地图片 优先使用 如果没有再使用远程
    var width = 0
    var height = 0

    //是否转化成圆形
    var isCircle: Boolean = false

    //圆形边框颜色
    @ColorInt
    var borderRedId: Int = 0

    //圆形边框宽度
    var borderWidth: Float = 0f
}

//文字的属性
class TextParam : BaseParams() {
    var indexStart: Int = 0 //文字变化开始位置
    var textColor: String = ""

    @ColorInt
    var textColorInt: Int = 0
    var textSize: Int = 0
    var text: String = ""//该段文字的内容
    var styleSpan: Int = Typeface.NORMAL
}

class ValidateSpamResult {
    var resultCode: String = ""
    var newContent: String? = null

    // 炫彩发言卡剩余秒数
    var speakTtl: Long = 0
    var callbackType: String? = null
    var callbackInitJson: String? = null
}

data class PicBean(
    var B: String? = null,
    var S: String? = null,
    var O: String? = null,
    var selected: Boolean = false
)

data class AdPublishContentBean(
    var city_map: String? = null,
    var content_type: String? = null,
    var create_time: Date? = null,
    var publish_sn: String? = null,
    var text_content: String? = null,
    var gender_list: String? = null
)

data class LastPublishBean(
    var publish_type: String? = null,
    var status_date: Date? = null,
    var show_play_minutes: Int = -1,
    var adPublishContent: AdPublishContentBean? = null,
    var has_publish_cnt: Int? = -1,
    var create_optr_id: Int? = -1,
    var fee: Int = -1,
    var put_type: String? = null,
    var remark: String? = null,
    var team_no: Int? = -1,
    var publish_type_text: String? = null,
    var show_anchor_id: String? = null,
    var need_friend_cnt: String? = null,
    var show_sns_type: String? = null,
    var put_progress: Int? = -1,
    var create_time: Date? = null,
    var publish_sn: String? = null,
    var price_rent: Int? = -1,
    var ad_name: String? = null,
    var comment_cnt: Int? = -1,
    var digg_cnt: Int? = -1,
    var ad_id: Int? = -1,
    var has_friend_cnt: Int? = -1,
    var put_type_text: String? = null,
    var status_text: String? = null,
    var channel_id: Int? = -1,
    var status: String? = null,
    var delete_publish_cnt: Int? = -1
)

data class QueryShowSnsTempletResult(
    var textList: ArrayList<String>? = null,
    var picList: ArrayList<PicBean>? = null,
    var lastPublish: LastPublishBean? = null
)

/**
 * 年度盛典 秒杀数据
 * @param giftPic 礼物图片
 * @param secKillUrl 跳转url
 * @param seconds 倒计时
 */
data class YearSeckillResult(
    var giftPic: String = "",
    var secKillUrl: String = "",
    var seconds: Long = 0
)

/**
 * 年度盛典 banner 父类
 */
open class YearResult : Serializable {
    var title: String = ""
    var display: Int? = null//0、隐藏 1、显示
    var status: Int? = null// 0、海选淘汰赛 1、晋级赛 2、总决赛
    var type: Int = 0// 0、宠爱值榜 1、积分榜 2、王牌巅峰之战榜

    //自定义参数，不是服务端传的
    var programId: Long = 0
}

/**
 * 年度盛典 活动banner 宠爱值榜
 * @param title
 * @param ttl
 * @param result 所有直播间排名数据
 * @param programId 节目id
 */
class YearBannerResult : YearResult() {
    var ttl: Long = 0
    var result: String = ""
    var subTitle: String = ""
    var resultObj: YearRankResult? = null
    var resultList: ArrayList<YearRankResult>? = null
    var rate: String = ""//倍率

    //自定义参数，不是服务端传的
    var rankId: Int = 0//宠爱值排名
    var numberResult: String = ""//宠爱值
    var resultType: String = ""//差值类型(1、无差值 2、差上一名xxx 3、差第100名xxx)
    var toResult: String = ""//差上一名
    var fromResult: String = ""//领先下一名

    override fun toString(): String {
        return "YearBannerResult(status=$status,ttl=$ttl,display=$display, status=$status,result=$result, programId=$programId)"
    }
}

/**
 * 年度盛典 积分榜 PK人物
 */
class YearRankResult : Serializable {
    var nameA: String = ""
    var scoreA: Long = 0
    var nameB: String = ""
    var scoreB: Long = 0

    //战榜数据
    var pid: Int = 0
    var name: String = ""
    var score: String = ""

}

/**
 * QQ登录
 */
class QQLoginResult {
    var ret: Int = -1
    var pay_token: String = ""
    var expires_in: String = ""
    var openid: String = ""
    var pfkey: String = ""
    var msg: String = ""
    var access_token: String = ""
    var pf: String = ""
}

/**
 * 年度盛典 红包数据
 * @param redId 红包唯一id
 * @param title
 * @param subTitle
 * @param logoPic 用户头像
 * @param seconds 倒计时时间
 * @param type 红包状态(1、未开启，2、已开启，3、已失效 or 已抢完)
 * @param beans 萌豆数量
 * version 4.14.1 新增参数
 * @param async 是否异步抢红包 True、是 False、否
 * @param resultType 抢红包结果类型 Wait、继续等待 Late、来晚了
 * @param robData 异步红包通知
 */
class YearRedPackageResult : Serializable {

    var redId: Int = 0//红包id
    var coverPic: String = ""//红包封面
    var title: String = ""
    var subTitle: String = ""
    var logoPic: String = ""
    var seconds: Long = 0//秒
    var actionShowTitle: String = ""
    var action: String = ""

    //失效时间
    var existSeconds: Long = 0L

    //抢到的萌豆
    var robBeans: Long = 0L

    //总萌豆
    var beans: Long = 0L

    // 是否异步抢红包 True、是 False、否
    var async: Boolean = false

    // 抢红包结果类型 Wait、继续等待 Late、来晚了
    var resultType: String = ""

    //异步红包通知
    var robData: String = ""

    //4.17.1新增字段
    //抢红包失败是否显示月卡入口
    var showMonthCard: Boolean? = null

    //自定义参数
    //当前红包状态 自定义参数
    var redType: Int = RedPackageType.WILL

    //通用信息
    var msg: String = ""
    var errorCode: Int = 0

    override fun equals(other: Any?): Boolean {
        if (other is YearRedPackageResult) {
            return redId == other.redId
        }
        return false
    }

    override fun hashCode(): Int {
        return redId.hashCode()
    }

    override fun toString(): String {
        return "年度红包参数：" + "redId = ${redId}，title = ${title}，subTitle = ${subTitle}\" +\n" +
                "                        \"，logoPic = ${logoPic}，seconds = ${seconds}，actionShowTitle = ${actionShowTitle}\" +\n" +
                "                        \"，type = ${redType}，beans = ${beans}"
    }

    fun copy(result: YearRedPackageResult) {
        result ?: return
        this.redId = result.redId
        this.coverPic = result.coverPic
        this.title = result.title
        this.subTitle = result.subTitle
        this.logoPic = result.logoPic
        this.seconds = result.seconds
        this.actionShowTitle = result.actionShowTitle
        this.action = result.action
        this.existSeconds = result.existSeconds
        this.robBeans = result.robBeans
        this.beans = result.beans
        this.redType = result.redType
        this.msg = result.msg
        this.errorCode = result.errorCode
        this.showMonthCard = result.showMonthCard
    }
}

/**
 * 双旦 活动banner
 */
class NewYearBannerResult : Serializable {
    var display: Int? = null
    var ttl: Long = 0
    var result: String = ""

    //自定义参数，不是服务端传的
    var programId: Long = 0

    override fun toString(): String {
        return "NewYearBannerResult(display=$display,ttl=$ttl, result=$result, programId=$programId)"
    }
}

/**
 * PK竞猜结果消息
 */
class PkGuessResult() {
    var pkGuessResult: String = ""
}

/**
 * 有完成的任务 代表有领取了 [taskType]代表任务类型
 */
class FinishMissionBean() {
    var taskType: String = ""
}

/**
 * 有完成的成就
 */
class FinishAchievementBean : Serializable {
    var achievementName: String = ""// 成就名称
    var achievementPic: String = ""// 成就图片
}

class ChatFollow(var follow: Boolean = false, var anchorUrl: String)

//宝箱类型消息类
class TreasureBoxBean(var programName: String = "", var programId: Long = 0)

//公聊引导加入粉丝团类型
class FansBean(
    var anchorUrl: String = "",
    var isJoin: Boolean = false,
    var nickname: String? = null,
    var programName: String? = null
)

//额外公聊消息    actionData为动态类这里是JsonObject
class PublicCustomMsgBean(
    var actionType: String = "",
    var actionData: Any? = null,
    var excludeRoomIds: List<Int> = arrayListOf()
)

/**
 * 聊天室祝福卡数量变动消息
 */
class ChatRoomCardsChangeBean(
    var chatRoomId: String = "",
    var cardNum: Int = 0
) : Serializable


/**
 * 聊天室人数变动消息
 */
class ChatRoomUsersChangeBean(
    var chatRoomId: String = "",
    var chatRoomTotalCount: Int? = null,
    var chatRoomMonthRank: String = ""
) : Serializable

/**
 * 关闭直播间消息
 */
class CloseChatRoomBean : Serializable {
    //用户数量
    var userNum: Int = 0

    //祝福卡数量
    var cardNum: Int = 0

    //关闭类型
    var closeType: String = ""

    //用户Id
    var userId: String = ""

    //头像
    var headerPic: String = ""

    //昵称
    var royalPic: String = ""
}

/**
 * 弹窗数据基类
 */
open class BaseDialogBean : Serializable {
    var paramToInt: Int? = null
    var paramsToObject: Any? = null
    var paramsToString: String? = null
}

/**
 * 月卡消息
 * @author WanZhiYuan
 * @since 4.25
 */
class OpenMonthCardMessage : Serializable {
    //是否开通月卡
    var monthCardOpen: Boolean = false

    //月卡开通天数
    var monthCardOpenDays: String = ""

    //奖励信息内容 如果奖励出现问题 这边返回空
    var monthCardOpenInfo: String = ""
}

/**
 * 炮台变更事件
 */
class PlanetBatteryChangeBean : Serializable {
    //直播间ID
    var programId: Long = 0

    //左侧炮台
    var batteryUser1: Int = -1

    //右侧炮台
    var batteryUser2: Int = -1
}

/**
 * 获取爆能枪卡事件
 */
class PlanetCasualCardGetBean : Serializable {
    //获奖信息
    var planetCasualAwardInfo: String = ""

    //获奖人昵称
    var nickname: String = ""
}

/**
 * 射击参数
 */
class ShootParams : Serializable {
    var location: String = ""
    var targetLocation: String = ""
}

/**
 * 直播间 公聊区域  幸运星球消息
 */
class PlanetMessageBean : Serializable {
    var programName: String = ""

    //后缀消息
    var broadcastText: String = ""

    //开启星球星球的直播间ID
    var programId: Long = 0L

    /*以下为本地字段*/
    //本直播间显示的消息
    var localText: String = ""

    //是否是本直播间
    var local: Boolean = false
}

/**
 * 用户管理操作消息
 * @author WanZhiYuan
 * @date 2020/08/06
 * @since 1.0.0
 */
class OperatorMessageBean : Serializable {
    var nickname: String = ""
    var time: String = ""
    var targetNickname: String = ""
}