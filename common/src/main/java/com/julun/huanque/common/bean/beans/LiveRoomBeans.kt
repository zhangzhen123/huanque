package com.julun.huanque.common.bean.beans

import android.app.Activity
import android.graphics.drawable.LevelListDrawable
import android.os.Bundle
import com.alibaba.fastjson.annotation.JSONField
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.forms.ConsumeForm
import java.io.Serializable

class UserLevelMap : Serializable {
    var ROYAL_LEVEL = 0
    var USER_LEVEL = 0
    var ANCHOR_LEVEL = 0

    constructor() {
    }

    constructor(ROYAL_LEVEL: Int = 0, USER_LEVEL: Int = 0, ANCHOR_LEVEL: Int = 0) {
        this.ROYAL_LEVEL = ROYAL_LEVEL
        this.USER_LEVEL = USER_LEVEL
        this.ANCHOR_LEVEL = ANCHOR_LEVEL
    }
}

/**
 * @param useing 是否正在使用该数据
 * @param currentLifeUsed 当前轮是否使用过该数据
 * @param time 时间
 */
class DanmuEvent(
    var factoryCarPrizeCount: Int = 0,
    var useing: Boolean = false,
    var currentLifeUsed: Boolean = false,
    var time: Long = 0
) :
    Serializable, TplBean()

/**
 * 攻击模式轮播使用
 */
class SlideEvent(var used: Boolean = false, val tplBean: TplBean) : Serializable

class RunwayCache(
    var cacheIt: Boolean = false,
    var seconds: Long = 0,
    var cacheValue: Long = 0
) : Serializable

/**
 * userId, nickname, userLevel, royalLevel, roomGuardPic, roomManagerPic, officalManagerPic, badgesPic, nickcolor,textColor
 */
class RoomUserChatExtra(
    var userId: Long = -1,
    var userAbcd: String = "",
    var nickname: String = "",
    var royalLevel: Int = 0,
    var userLevel: Int = 0,
    var anchorLevel: Int = -1,
    //添加勋章列表
    var badgesPic: List<String?> = ArrayList(),
    var nickcolor: String = "",
    var textColor: String = "",
    //发送者的ID
    var senderId: Long = 0,
    //发送者的头像
    var headPic: String = "",
    //发送者的性别
    var sex: String = "",
    //是否是陌生人
    var stranger: Boolean = false,
    var targetUserObj: TargetUserObj? = null,
    //贵族图片，远程图片
    var royalPic: String = "",
    //贵族图片，远程图片(圆角小图片)
    var royalSmallPic: String = "",
    //msgType= 1 表示为系统消息
    var msgType: Int = 0,
    //显示类别 炫彩发言使用
    var displayType: List<String>? = null
)

/**
 * 消息体内部 携带的用户数据
 */
class TargetUserObj(
    //头像
    var headPic: String = "",
    //昵称
    var nickname: String = "",
    //亲密等级
    var intimateLevel: Int = 0,
    //欢遇标识
    var meetStatus: String = "",
    //性别
    var sex: String = "",
    //用户ID
    var userId: Long = 0L,
    //本次花费金额（发送消息的时候传值）
    var fee: Long = 0,
    //本地图片地址（图片消息使用）
    var localPic: String = "",
    //是否是陌生人
    var stranger: Boolean = false
)

/**
 * 直播间内的一些bean,并不一定跟其他的bean通用,尤其进入直播间返回的数据...
 * Created by nirack on 16-11-7.
 */
data class UserInfoForLmRoom(
    var userId: Long = -1
    , var headPic: String = ""//头像
    , var nickname: String = ""//昵称
    , var score: Double = 0.toDouble()
    , var smallPic: String = ""
    , var royalLevel: Int = 0
    , var type: String? = null
    ,
    //下面为原有字段  删除
    var nickName: String = ""
    , var picId: String = ""
    , var identityId: Int = -1
    , var programId: Long? = null
//                             , var score: Double = 0.toDouble()
    , var introduce: String? = null
    , var guard: String? = null
    , var levelMap: UserLevelMap = UserLevelMap()
    , var goodsList: List<String?> = ArrayList()
    // 发言字数
    , var speakCount: Int = 0
    , var nickColor: String = ""// 周星的用户昵称颜色不一样
    , var online: Boolean = true //是否在线
    , var sex: String = ""

) : Serializable {
    companion object {
        val TOP = 0
        val OTHERS = 1
    }
}

class FreeGiftInfo(
    var numForEachTime: ArrayList<Int>? = null,
    var timesForToday: Int? = null,
    var totalCountForNow: Int? = null,
    var detailUrl: String = ""
)

class ObtainFreeGiftDto(var total: Int = 0, var timesForToday: Int = 0)

data class GuardInfo(
    var beans: Long = 0,
    var goodsId: Int = 0,
    var goodsName: String = "",
    var guardList: List<RoomUserInfo> = listOf(),
    var pic: String = "",
    var tips: String = "",
    var hasMore: Boolean = false
)

data class RoomUserInfo(
    var userId: Long = -1,
    var headPic: String = "",//头像
    var userLevel: Int = -1,
    var royalLevel: Int = -1,
    var roomGuardPic: String = "",
    var roomManagerPic: String = "",
    var royalSmallPic: String = "",
    var badgesPic: ArrayList<String> = arrayListOf(),
    var nickname: String = ""//昵称
    , var score: Double = 0.toDouble(),
    //4.15新增字段
    //贵族勋章地址
    var royalPic: String = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is RoomUserInfo) {
            return userId == other.userId
        }
        return false
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}


class RoomBanner : ShareObject() {
    var adCode: String = ""

    //广告标题
    var adTitle: String = ""

    //是否可以分享
    var canShare: String = "False"

    //首页banner
    var resType: String = ""
    var resUrl: String = ""

    //排序
    var orderNum: Int = 0

    //图片地址
    var linkUrl: String = ""

    //是否显示H5 本地字段
    var showWeb: Boolean = false

    //H5高度
    var maxHeight: Int = 0

    //本地字段
    //banner的位置
    var position: Int = 0
}

/**
 * 直播间引导广告信息
 * @author WanZhiYuan
 * @version 4.19.1
 */
class LiveAdConfig : Serializable {
    /** 弹窗最大高度 **/
    var maxHeight: Int = 0

    /** 弹窗最大宽度 **/
    var maxWidth: Int = 0

    /** 弹出倒计时（单位：秒） **/
    var delayTtl: Long = 0

    /** 弹出次数类型（一次（Once）, 每日一次(EveryDay) **/
    var showTimesType: String = ""
}

class UserEnterRoomRespBase : Serializable {
    //    var anchorId: Long = 0//主播id
    var prePic: String = ""//封面图
    var headPic: String = ""//头像图

    @JSONField(name = "isLiving")
    var isLiving: Boolean = false//是否在直播中
    var programName: String = ""//主播昵称
    var programId: Long = 0//节目id

    //主播靓号
    var prettyId: Long? = null

    @JSONField(name = "isPcLive")
    var isPcLive: Boolean = false//获取直播间基本信息 增加pushPlatformType	推流平台类型(可选项：iOS、Android、Assistant)

    //播放地址
    var playUrl: String = ""

    //播放数据
    var playInfo: PlayInfo? = null

    //当前用户ID
    var userId: Long = 0
    var lastShowTimeDiffText: String = ""
    var anchorLevel: Int = 0

    //    var visitorSession: Session? = null
    var micing: Boolean = false//是否有连麦
    var roomMicInfo: MicInfo? = null

    //横竖屏相关
    var screenType: String = ""
    var typeCode: String = ""
}

/**
 * 播放相关数据
 */
class PlayInfo : Serializable {
    companion object {
        //url格式
        const val Url = "Url"

        //正则表达式格式
        const val Regex = "Regex"
    }

    //rtmp格式流
    var rtmp: String = ""
    var type: String = ""
    var domain: String = ""
    var streamKey: String = ""
}


class UserEnterRoomRespDto : Serializable {

    var onlineUserNum: Int = 0//在线人数
    var royalCount: Int = 0//贵宾人数

    //    var living: String = ""//是否正在直播
    //广告轮播图
    var adList: ArrayList<RoomBanner> = ArrayList()

    //是否关注
    var follow: Boolean = false
    var pking: Boolean = false

    //跑道消息
    var runway: RunWayMessage? = null

    //总贡献值
    var totalContribute = 0L

    //当前身份为用户时返回
    var user: UserInfo? = null

    //当前身份为主播时返回
    var anchor: AnchorBasicInfo? = null
    /*后台暂未实现*/
    /**房间用户列表**/
    var onlineUsers: ArrayList<UserInfoForLmRoom> = ArrayList()

    //分享相关
    var share: ShareObject = ShareObject()


    var roomMicInfo: MicInfo? = null

    //活动数据
//    var extData: ExtData? = null
    //年度盛典红包
    var redPackets: ArrayList<YearRedPackageResult> = ArrayList()

    var showGame: Boolean? = null

    //显示任务是否可以领取弹窗
    var showReceive: Boolean = false

    //是否显示每日任务标识
    var showTasks: Boolean = false

    //萌新礼包定时器时间 -1 表示不需要设置定时器
    var newUserPacksTimer: Int? = null

    //炫彩发言时间
    var speakTtl: Long = 0

    //神秘人剩余时间
    var mysteryTtl: Long = 0

    //4.15新增字段 author:WanZhiYuan
    //是否加入粉丝团
    var groupMember: Boolean = false

    //是否打卡
    var fansClockIn: Boolean = false
    var wishingShowType: String = ""

    //4.18.1新增字段 author:WanZhiYuan
    /** 引导广告信息 **/
    var poppuAds: ArrayList<RoomBanner>? = null

    //4.19.0新增主播拯救信息
    var salvationInfo: PkMicSalvationStartInfo? = null
    //4.23.0新增字段
    /** 直播间底部气泡提示相关信息 **/
    var bottomPuppo: BottomPuppo? = null
    //4.24新增字段 4.25更新 只剩贵族数量
    /** 贵族数量 **/
    var honorCount: Int? = null

    /** 守护剩余天数 **/
    var guardSurplusDay: Int? = null

    //主播靓号(主播身份的时候使用  appStartLiving接口返回)
    var prettyId: Long? = null
    //4.25新增字段
    /** 守护数量 **/
    var guardCount: Int? = null
    //4.27新增字段
    /** 动作 **/
    var action: String? = null

    /**游戏列表数据**/
    var gameList: MutableList<SingleGame> = mutableListOf()
}

/**
 * 播间底部气泡提示相关信息
 */
data class BottomPuppo(
    var adCode: String = "",
    var adTitle: String = "",
    var extJsonCfg: String? = null,
    //自定义字段
    //4.23.0
    /** 保存实例化的配置信息 **/
    var config: Any? = null
)

/**
 * 进入直播间 次要信息
 */
data class EnterExt(
    //私聊相关配置信息
    var experienceSms: ExperienceSms? = null,
    //发言模板
    var words: List<SingleWord> = listOf(),
    //一元首充数据
    var firstRecharge: OneYuanInfo? = null,
    //主播头条数据
    var headlineInfo: HeaderInfo? = null,
    //体验守护
    var experienceGuard: ExperienceGuard? = null,
    //新增主题房相关数据返回
    var themeRoomVO: ThemeRoomVO? = null
) : Serializable

data class ThemeRoomVO(
    var showGuides: Boolean = false,
    //当前上场的主播id
    var programId: Long = 0,
    var programName: String = "",
    var headPic: String = ""
)

/**
 * 私聊体验卡相关内容
 */
data class ExperienceSms(
    //剩余次数
    var leftCnt: Int = 0,
    //倒计时
    var countDown: Long = 0L
) : Serializable

/**
 * 单个私聊信息
 */
data class SingleWord(
    var id: Int = 0,
    var type: String = "",
    var word: String = ""
) : Serializable

/**
 * 抽奖信息
 */
data class DrawInfo(
    var draw: String = "",
    var drawStatus: String = "",
    //正在抽奖的数量
    var drawingNum: Int = 0,
    //抽奖ID
    var logId: Long = 0,
    var ttl: Long = 0L//这个倒计时根据状态代表抽奖中的倒计时/结束后的显示倒计时
)


/**
 * 开始直播接口获取的信息实例
 */
open class AnchorBasicInfo : ProgramAnchor(), Serializable {
    /** 用户昵称 **/
    var programName: String = ""

    /** 用户头像 **/
    var headPic: String = ""

    /** 主播等级 **/
    var anchorLevel: Int = 0

}

open class ProgramAnchor : Serializable {
    /** 节目id **/
    var programId: Long = 0
}

/**
 * 当前用户信息
 */
class UserInfo {
    var headPic: String = ""
    var nickname: String = ""
    var royalLevel: Int = 0
    var userId: Long = 0
    var userLevel: Int = 0

    //勋章图标
    var badgesPic: List<String> = arrayListOf()

    //昵称颜色
    var nickcolor: String = ""

    //是否守护
    var roomGuard: Boolean = false

    //贵族图片，远程图片
    var royalPic: String = ""

    //贵族图片，远程图片（圆角小图标）
    var royalSmallPic: String = ""

    //房管
    var roomManager: Boolean = false

    //官方
    var officalManager: Boolean = false

    //是否是神秘人
    var mystery: Boolean = false

    //是否免费弹幕(粉丝特权)
    var hasFreeDanMu: Boolean = false
    var danMuCard: Int = 0
}


/**
 * 开播数据
 */
class GuardAgainst(
    var programId: Long? = 0,
    var streamId: String? = "",
    var prePic: String = "",
    //推流地址,当sdkProvider为Agora时有值
    var pushUrl: String = "",
    //SDK模式
    var sdkProvider: String = "",
    var sdkParams: SdkParam? = null
) : Serializable {
    companion object {
        //单流
        const val Single = "Single"

        //混流
        const val Multi = "Multi"

        const val Zego = "Zego"
        const val Agora = "Agora"
    }

}

/**
 * SDK配置数据
 */
class SdkParam(
    var streamId: String = "",
    var authToken: String = ""
) : Serializable

class YueSaiDto(
    var label1: String? = "",
    var label2: String? = "",
    var url: String? = null,
    var top: String? = null,
    var type: String? = null,
    var icon: String? = ""
)

class RunWayMessage : Serializable {
    var message: String = ""
    var seconds: Long = 0
}

class UserInfoInRoom : Serializable {
    var userInfo: UserInfoForLmRoom? = null

    //年龄
    var age: Int = 0

    //主播等级
    var anchorLevel: Int = 0

    //认证标识
    var authMark: String = ""

    //勋章图标列表(不含房管勋章),已排序
    var badgesPic: List<String> = ArrayList()

    //互动标识
    var canInteractive: Boolean = false

    //是否显示举报拉黑
    var canReport: Boolean = false

    //城市
    var city: String = ""

    //关注状态
    var follow: Boolean = false

    //是否有管理功能
    var hasOperate: Boolean = false

    //用户头像
    var headPic: String = ""

    //小鹊数据
    var magpieList: List<String> = mutableListOf()

    //个性签名
    var mySign: String = ""

    //用户昵称
    var nickname: String = ""

    //贵族等级
    var royalLevel: Int = 0

    //贵族勋章
    var royalPic: String = ""

    //性别：男(M)、女(F)(可选项：Male、Female、Unknow)
    var sex: String = ""

    //是否显示AT按钮
    var showAt: Boolean = false

    //用户等级
    var userLevel: Int = 0

    //标签
    var userTags: List<String> = mutableListOf()


    /**是否显示加好友**/
    var showAddFriend: Boolean = false


    /** 昵称颜色 **/
    var nickcolor: String = ""

    /** 是否可以私聊 **/
    var showPrivateChat: Boolean = false

    /**是否显示用户主页入口**/
    var showHomePage: Boolean = false

    /** 靓号 **/
    var prettyNum: Long? = null

    /**主播靓号**/
    var prettyId: Long? = null

    /** 靓号等级 1、小主号(红色靓号) 2、绿色靓号 3、蓝色靓号 4、紫色靓号' **/
    var prettyNumLevel: Int = 0

    //是否是神秘人
    var mystery: Boolean = false


    //4.15新增字段

    //4.19.0新增字段
    var intimateVO: IntimateVO? = null

    //主播头像边框
    var headPicFrame: String = ""

    //主播勋章
    var anchorBadges: List<SingleBadge> = listOf()

    //成就相关
    var achievementInfo: UserAchievementInfo? = null
}

class UserAchievementInfo : Serializable {
    //成就数量
    var num: Int = 0

    //成就图片
    var pic: String = ""

}

class UserPrerogativeCardInfo : Serializable {
    //拥有特权数
    var privilegeNum: String = ""

    //是否是本人
    var isSelf: Boolean = false
}

/**
 * 单个勋章
 */
class SingleBadge : Serializable {
    //勋章名称
    var badgeName: String = ""

    //勋章图片
    var badgePic: String = ""

    //勋章说明
    var remark: String = ""
}

class IntimateVO {
    var userId: Long = 0
    var living: Boolean = false
    var headPic: String = ""
    var programId: Long = 0

}

class RankingsResult : Serializable {
    /** 用户id **/
    var userId: Long = 0

    /** 用户昵称 **/
    var nickname: String = ""

    /** 用户头像 **/
    var headPic: String = ""

    /** 用户等级 **/
    var userLevel: Int = 0

    /** 贵族等级 **/
    var royalLevel: Int = 0

    /** 勋章图标列表 **/
    var badgesPic: List<String> = ArrayList<String>()

    /** 比分 **/
    var score: Long = 0L
    //4.15新增字段
    /** 贵族勋章地址 **/
    var royalPic: String = ""

    override fun equals(other: Any?): Boolean {
        if (other is RankingsResult) {
            return userId == other.userId
        }
        return false
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}


/**
 * 奖励的物品
 */
class AwardGood : Serializable {
    var count: Int = 0
    var pic: String = ""
    var prizeName: String = ""
}

class BoxResult : Serializable {
    var prizeCount: Int = 0
    var playMusic: Boolean = false
    var awardGoods: List<AwardGood>? = null
    var addExpValue: Long = 0
    var deepSeaKey: AwardGood? = null//新增中钥匙奖励
}


/**
 * @param liveMinutes 直播时长 (分钟)
 * @param presentBeans 萌豆数目
 */
class StopBean(var liveMinutes: Int = 0, var presentBeans: Int = 0)

//显示TopDialog的Bean
data class TopDialogBean(var content: String = "", var drawable: Int)

//显示余额不足的Bean
data class NotEnoughBalanceBean(
    var content: String = "",
    var count: Int? = null,
    var beans: Long = 0
) : BaseDialogBean()

//赠送礼物的结果bean
data class SendGiftResult(
    //背包数量
    var bagCount: Int = 0,
    //最新余额
    var beans: Long = 0,
    //当前等级
    var level: Int = 0,

    var bagCntMap: HashMap<String, Int>? = null,
    var form: ConsumeForm? = null,
//                          var form: NewSendGiftForm? = null,

    var ttl: Int = 0,

    var processInfo: ProcessInfo? = null,
    var giftChange: GiftChange? = null,
    var needFlushGift: Boolean = false,
    // 当前礼物气泡消息，值不为空需替换当前气泡消息
    var popMsg: String = "",
    // 提示消息，值不为空，需显示
    var alertMsg: String = "",
    //礼盒奖励礼物列表
    var feedbackList: ArrayList<BoxGainGift>? = null,
    //背包变动标识
    var bagChange: Boolean = false,
    //4.17新增字段
    //4.21新增 author WanZhiYuan
    /** 最大可获得萌豆 */
    var maxBean: Long? = null
) : EggHitSumResult()

//礼盒奖励礼物
data class BoxGainGift(
    var fromUserName: String = "",
    var goodsCnt: Int = 0,
    var goodsName: String = "",
    var goodsPic: String = ""
) : Serializable

//回归收到的奖励礼物
data class ReturnAwardGiftBean(
    var name: String = "",
    var count: Int = 0,
    var pic: String = "",
    var useInfo: String = "",
    //本地使用
    var boxType: String = ""
) : Serializable

//回归奖励礼物
data class ReturnGiftBean(
    //需要取消
    var skipType: String = "",
    var nameAndNum: String = "",
    var pic: String = "",
    var expireDate: String = "",
    var skipValue: String = ""
) : Serializable

//直播间查询的回顾礼包数据
data class ReturnBeanInPlayer(
    //剩余时间
    var ttl: Long = 0,
    //礼物列表
    var packDetailList: ArrayList<ReturnGiftBean> = ArrayList(),
    //是否有可领取福利
    var canReceive: Boolean = false,
    //是否当天首次请求
    var firstLogin: Boolean = false
) : Serializable

//回归礼包使用的bean(第一重礼包礼物)
data class FirstGiftBean(
    //奖励数量
    var awardCnt: Int = 0,
    //奖励名称
    var awardName: String = "",
    //栏目标题
    var tabName: String = "",
    //领取状态
    var awardStatus: String = "",
    //对应的CODE
    var code: String = "",
    //图片
    var pic: String = "",
    //使用说明
    var useInfo: String = ""
) : Serializable

//第二重礼包礼物数据
data class SecondGiftBean(
    var awardCode: String = "",
    //数量
    var count: Int = 0,
    //奖励名称
    var name: String = "",
    var pic: String = "",
    //使用说明
    var useInfo: String = ""
) : Serializable

//第二重礼包数据
data class SecondActivityBean(
    //奖励列表
    var awards: MutableList<SecondGiftBean> = mutableListOf(),
    //是否抽取了折扣券
    var drawDiscount: Boolean = false,
    //是否购买
    var hasBuy: Boolean = false,
    //原价
    var money: Int = 0,
    //实际价格
    var realMoney: Int = 0,
    //充值模板
    var tplId: Int = 0
) : Serializable

//回归礼包使用的bean(第三重礼包礼物)
data class ThirdBoxBean(
    //宝箱名称
    var boxName: String = "",
    var boxId: String = "",
    //需要观看时长
    var min: Int = 0,
    //奖励状态
    var awardStatus: String = ""
) : Serializable


//回归礼包接口返回的总对象
data class ReturnBean(
    //当前观看时间
    var nowMin: Int = 0,
    //第一重礼包是否处于可领取状态
    var oneWaitReceive: Boolean = false,
    //第一重礼包
    var oneList: MutableList<FirstGiftBean> = mutableListOf(),
    //第二重礼包
    var welfareTwoVO: SecondActivityBean? = null,
    //第三重礼包
    var threeList: MutableList<ThirdBoxBean> = mutableListOf()
) : Serializable

/**
 * 图鉴数据
 */
data class Handbook(
    var bookId: Int = 0,
    var detailPic: String = "",
    var name: String = "",
    var orderNo: Int = 0,
    //图鉴图标
    var pic: String = "",
    var accessList: MutableList<Access> = mutableListOf()
) : Serializable

/**
 * 上次显示礼物说明的实体bean
 * 本地使用实体类
 */
data class LastShowGiftStateBean(var giftId: Int = 0, var showed: Boolean = false) : Serializable

/**
 * 图鉴入口
 */
data class Access(
    //入口code
    var accessCode: String = "",
    //入口名称
    var accessName: String = "",
    //URL
    var url: String = "",
    //礼物ID
    var giftId: Int = 0
) : Serializable

//贵族体验卡消息
data class RoyalCardBean(
    var clientType: String = "",
    var money: Int = 0,
    var royalCardExpDate: String = "",
    var royalCardGoodId: Int = 0,
    var royalCardName: String = "",
    var royalCardValidDate: Int = 0,
    var subTitle: String = "",
    var title: String = "",
    var royalCardBgPic: String = "",
    var userId: Long = 0
) : Serializable

data class GiftChange(
    var giftId: Int = 0,
    var vo: LiveGiftDto? = null
)

data class CountItem(
    var countName: String = "",
    var countValue: Int = 0
)

//显示私聊的bean
data class PrivateMessageBean(var userId: Long = -1, var nickName: String = "") : Serializable

//页面跳转的Bean
data class JumpActivityBean(
    var next: Class<out Activity>,
    var intentFlag: Int = 0,
    var extra: Bundle? = null
)

//显示用户信息弹窗的Bean
data class UserInfoBean(
    var userId: Long,
    var isAnchor: Boolean = false,
    var royalLevel: Int = -1,
    var userPortrait: String = "",
    var displayType: List<String>? = null,
    var programName: String = "",
    var nickname : String = ""
)

//页面跳转的Bean
data class GoToUrl(var needLogin: Boolean = false, var url: String = "")

/**
 * 管理类型(踢人、举报)
 * @param imtemKey 字段主键
 * @param itemName 字段名称
 * @param itemValue 字段类型
 *
 */
data class ManagerInfo(
    var itemKey: String = "",
    var itemName: String = "",
    var itemValue: String = "",

    //本地字段
    //设备标识
    var deviceUuid: String = "",
    //权限类型
    var mangeType: String = "",
    //权限类型标题
    var mangeTypeDesc: String = ""
) : Option()

/**
 * 管理权限信息
 * @alter WanZhiYuan
 * @since 1.0.0
 * @date 2020/08/04
 */
data class ManagerOptionInfo(
    //权限时间或详细信息列表
    var manageOptions: ArrayList<ManagerInfo> = arrayListOf(),
    //权限类型
    var mangeType: String = "",
    //权限类型标题
    var mangeTypeDesc: String = ""
) : Option()

open class Option() : Serializable

/**
 * 查询余额的返回对象
 */
data class BeansResult(var beans: Long = 0, var danMuCard: Int = 0, var lightGunCardNum: Int = 0) :
    Serializable

/**
 * 连麦信息返回对象
 */
data class QueryMicInfo(
    var anchors: ArrayList<MicAnchor> = arrayListOf(),
    var micId: Long = 0,
    var status: String = ""
) : Serializable

/**
 * 连麦设置接口
 */
data class MicroSettingInfo(var micRuleUrl: String = "")

/**
 * 获取直播间推流URL
 */
data class PublishUrl(var pushUrl: String = "")

open class EggHitSumResult(
    //奖励萌豆
    var prizeBeans: Long = 0,
    //奖励列表
    var prizeList: List<AwardGood> = listOf()
)

//新增序列化EggHitSumResult实体类
open class EggHitSumResultSerial(
    var prizeBeans: Long = 0,
    var prizeList: List<AwardGood> = listOf()
) : Serializable

/**
 * 背包列表数据
 */
class PackageResult : Serializable {
    /** 节目id **/
    var programId: Long = 0

    /** 节目名称 **/
    var programName: String = ""

    /** 开播状态 **/
    var isLiving: Boolean = false

    /** 主播图片 **/
    var anchorPic: String = ""

    /** 主播昵称 **/
    var nickName: String = ""

    /** 主播等级 **/
    var anchorLevel: Int = 0

    /** 勋章id **/
    var goodId: Int = 0

    /** 勋章名称 **/
    var goodName: String = ""

    /** 勋章图片 **/
    var goodPic: String = ""

    /** 是否佩戴中 **/
    var isEquiped: Boolean = false

    /** 失效时间 **/
    var expTime: String = ""
}


//主播拷贝信息
class LiveBean : Serializable {
//    var living: Boolean = false

    // 直播间背景图url
    var programPoster: String = ""

    // 主播图片url
    var anchorPicId: String = ""
    var programId: Long = 0
    var isAppShow: Boolean = false//是不是手机直播

    //    var anchorName: String = ""
    var playinfo: PlayInfo? = null
}

/**
 * pk竞猜数据
 */
data class GuessInfoResult(
    //下注结果
    var guessResult: String = "",
    //账户萌币余额
    var moeCoins: Long = 0,
    //主播昵称
    var nickname: String = "",
    //PK是否结束
    var pkFinished: Boolean = false,
    //封盘剩余时间
    var sealedTtl: Int = 0,
    //下注数据
    var pkInfo: ArrayList<DoublePkGuessInfo> = arrayListOf()
) : Serializable

/**
 * 每对投注信息
 */
data class DoublePkGuessInfo(
    var miniList: ArrayList<SinglePkGuessInfo> = arrayListOf(),
    var validity: Boolean = false,
    var resultText: String = ""
) : Serializable

/**
 * 单个下注信息
 */
data class SinglePkGuessInfo(
    //赔率
    var odds: String = "",
    //竞猜类型
    var pkGuessType: String = "",
    //下注总金额
    var totalMocoins: Long = 0,
    //自己下注金额
    var selfMocoins: Long = 0
) : Serializable

/**
 * 萌新礼包信息
 */
data class NewUserGiftInfo(
    //奖励数量
    var count: Int? = null,
    //奖励图片
    var pic: String? = null,
    //奖励名称
    var awardName: String? = null
) : Serializable

/**
 * 一元礼包信息
 * [status]
领取状态枚举：
/** 待领取 */
Unclaimed,
/** 已支付 */
Paid,
/** 已发放 */
Granted,
/** 已确认 */
Confirmed;
 */
data class OneYuanInfo(
    var status: String = "",
    //礼物面板提示文案
    var giftRemind: String = "",
    //是否自动弹窗
    var autoPopup: Boolean = false,
    var coupon: Coupon? = null,
    //奖励列表
    var packs: MutableList<SinglePack> = mutableListOf(),
    //获奖用户
    var users: MutableList<SingleOneYuanUser> = mutableListOf(),
    //奖励内容
    var awards: MutableList<GiftIcon> = mutableListOf(),
    //本地字段是否是自动弹出的
    var isAuto: Boolean = false
) : BaseDialogBean() {
    companion object {
        const val Unclaimed = "Unclaimed"
        const val Paid = "Paid"
        const val Granted = "Granted"
        const val Confirmed = "Confirmed"

        //没有礼包了
        const val None = "None"
    }
}

/**
 * 主播头条数据
 */
data class HeaderInfo(
    //剩余总时间
    var ttl: Int = 0,
    //上一轮结果
    var result: String = "",
    //辅助数据
    var extData: HeaderInfoExt = HeaderInfoExt()
) : Serializable

/**
 * 主播头条的辅助数据
 */
data class HeaderInfoExt(var RoomData: HashMap<String, String> = hashMapOf()) : Serializable


/**
 * 单个礼包相关数据
 */
data class SinglePack(
    //单个礼包内容
    var icons: MutableList<GiftIcon> = mutableListOf(),
    //是否选中
    var selected: Boolean = false,
    //Tag相关
    var tag: String = "",
    //标题
    var title: String = ""
) : Serializable {
    companion object {
        //超值豪礼
        const val Super = "Super"

        //Hot
        const val Hot = "Hot"
    }
}


/**
 * 一元首充，单个中奖用户
 */
data class SingleOneYuanUser(
    var headPic: String = "",
    var nickname: String = ""
) : Serializable

/**
 * 奖励礼物
 */
data class GiftIcon(
    var pic: String = "",
    var name: String = "",
    //描述
    var desc: String = ""
) : Serializable

data class Coupon(
    var hasCoupon: String = "",
    var hasCouponIcon: String = ""
) : Serializable

/**
 * 粉丝榜单
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/09
 * @iterativeVersion 4.20
 * @updateDetail 粉丝团2.0更新字段
 * @date 2019/11/18
 * @since 4.21
 * @detail 新增字段
 */
class FansRankInfo(
    /** 是否已经打卡 **/
    var clockIn: Boolean = false,
    /** 粉丝团展示 头部信息 **/
    var fansGroupInfoVO: FansRankHeadInfo? = null,
    /** 粉丝团列表 **/
    var fansInfos: ArrayList<FansListInfo>? = null,
    /** 粉丝团名称 **/
    var groupName: String = "",
    /** 本人粉丝信息 **/
    var self: UserFansInfo? = null,
    /** 粉丝牌子是否激活 **/
    var signActive: Boolean = false,
    /** 是否登录 **/
    var login: Boolean = false,
    /** 成员人数 **/
    var groupSize: Int = 0,
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 加入粉丝团花费 **/
    var price: Long = 0,
    /**免费倒计时**/
    var deadline: Long = 0,
    //4.19新增参数
    /**亲密度获取情况**/
    var intimateList: ArrayList<Intimate>? = null,
    /**特权信息**/
    var privilegeList: ArrayList<Privilege>? = null,
    /**是否粉丝团成员**/
    var groupMember: Boolean = false,
    /**用户昵称**/
    var nickname: String = "",
    /**用户头像**/
    var headPic: String = "",
    /**亲密度**/
    var intimate: String = "",
    /**粉丝等级**/
    var fansLevel: Int = 0,
    /**下个粉丝等级**/
    var nextFansLevel: Int = 0,
    /**当前等级增加亲密度**/
    var nowLevelAddIntimate: Long = 0,
    /**升级亲密度差值**/
    var diffIntimate: Long = 0,
    //4.20新增字段
    /**当前是否获取了新特权**/
    var gainNewPrivilege: Boolean? = null,
    //4.21新增字段
    /**是否还有更多数据**/
    var hasMore: Boolean = false,

    //自定义参数
    /**
     * @see BusiConstant.FansTabName
     * 页面类型常量类型
     */
    var tabType: String = ""
) : Serializable

/**
 * 粉丝团头部信息
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/09/29
 * @iterativeVersion 4.19
 */
class FansRankHeadInfo(
    /** 头像 **/
    var headPic: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 显示小主回馈图标 **/
    var showStock: Boolean? = null,
    //4.19 新增参数
    /** 周榜排名 **/
    var weekRank: Long = -1,
    /** 粉丝团类型 Common 普通团、Thirty 30人团 and Fifty 50人团**/
    var fansGroupType: String = ""
) : Serializable

/**
 * 粉丝团列表信息
 * @date 2019/11/18
 * @since 4.21
 * @detail 新增字段
 */
class FansListInfo(
    /** 粉丝牌子是否激活 **/
    var signActive: Boolean = false,
    /** 粉丝团名称 **/
    var groupName: String = "",
    /** 亲密度 **/
    var intimate: String = "",
    /** 头像 **/
    var headPic: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 粉丝等级 **/
    var fansLevel: Int = 0,
    /** 排名 **/
    var rank: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 亲密度色值 **/
    var color: String = "",

    //4.17新增字段
    /** 显示对应浮动箭头 **/
    var increase: Boolean? = null,
    //4.21新增字段
    /** 节目id **/
    var programId: Long = 0,
    /** 用户id **/
    var userId: Long = 0,

    //自定义字段
    /** 成员人数 **/
    var fansMember: String = ""
) : Serializable {

    override fun hashCode(): Int {
        if (this.userId != 0L) {
            return this.userId.hashCode()
        }
        if (this.programId != 0L) {
            return this.programId.hashCode()
        }
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is FansListInfo) {
            if (other.userId != 0L && this.userId != 0L) {
                return other.userId == this.userId
            }
            if (other.programId != 0L && this.programId != 0L) {
                return other.programId == this.programId
            }
        }
        return false
    }
}

/**
 * 当前粉丝信息
 */
class UserFansInfo(
    /** 是否是该粉丝团成员 **/
    var groupMember: Boolean = false,
    /** 头像 **/
    var headPic: String = "",
    /** 亲密度 **/
    var intimate: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 排名 **/
    var rank: String = "",
    /** 粉丝等级 **/
    var fansLevel: Int = 0,
    /** 粉丝牌是否点亮 **/
    var signActive: Boolean = false,
    /** 粉丝牌名称 **/
    var groupName: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 亲密度色值 **/
    var color: String = "",

    //4.17新增字段
    /** 显示对应浮动箭头 **/
    var increase: Boolean? = null
) : Serializable

/**
 * 粉丝团特权信息
 * @createAuthor WanZhiYuan
 * @createDate 2019/10/10
 * @iterativeVersion 4.19
 */
class FansPrivilegeInfo : Serializable {
    /** 说明框背景色 **/
    var color: String = ""

    /** 当前粉丝团类型(可选项：Common、Thirty、Fifty) **/
    var fansGroupType: String = ""

    /** 粉丝团类型与特权关系 **/
    var fansGroupTypes: ArrayList<FansPrivilegeItemInfo>? = null

    /** 粉丝团特权详情 **/
    var privilegeTypes: ArrayList<FansPrivilegeItemInfo>? = null
}

/**
 * 粉丝团特权item信息
 * @createAuthor WanZhiYuan
 * @createDate 2019/10/11
 * @iterativeVersion 4.19
 */
class FansPrivilegeItemInfo : Serializable {
    /** 当前粉丝团类型(可选项：Common、Thirty、Fifty) **/
    var fansGroupType: String = ""

    /** tab标签名称 **/
    var groupTypeDesc: String = ""

    /** 粉丝团类型对应开放特权 **/
    var privileges: ArrayList<String>? = null

    //特权说明相关信息
    /** 特权类型 **/
    var code: String = ""

    /** 特权名称 **/
    var desc: String = ""

    /** 特权说明和文字颜色 **/
    var remarks: ArrayList<Remarks>? = null

    //自定义参数
    //是否标记高亮显示
    var isLight: Boolean = false

    //是否拥有此特权
    var isOwner: Boolean? = null
}

class JoinFansResult {
    var salvationInfo: PkMicSalvationStartInfo? = null
    var isFree: Boolean? = null
    var price: Long? = null
}

class Remarks : Serializable {
    var color: String = ""
    var remark: String = ""
}

/**
 *  主播信息
 */
//class PlayerInfo : Serializable {
//    /** 用户昵称 **/
//    var programName: String = ""
//    /** 用户头像 **/
//    var headPic: String = ""
//    var anchorId:Int=0;
//    /** 节目id **/
//    var programId: Long = 0
//    //流id
//    var streamID:String=""
//    //是不是主主播
//    var isMainAnchor=true
//}

/**
 * @author WanZhiYuan
 * @date 2020/03/05 0005
 * @since 4.27
 * @detail 增加 miniUserName -> 小程序id
 */
data class MenuActionItem(
    var itemCode: String = "",
    var itemName: String = "",
    var itemPic: String = "",
    var extJsonCfg: String = "",
    var itemUrl: String = "",
    var tag: String? = null,
    //小程序id
    var miniUserName: String? = null
)

/**
 * 粉丝特权相关实体类
 */
data class FansPrerogative(
    //升级亲密度差值
    var diffIntimate: Int = 0,
    //粉丝团名称
    var fansGroupName: String = "",
    //当前粉丝等级
    var fansLevel: Int = 0,
    //下一粉丝等级
    var nextFansLevel: Int = 0,
    //用户头像
    var headPic: String = "",
    //亲密度
    var intimate: String = "",
    //今日亲密度获取情况
    var intimateList: MutableList<Intimate> = mutableListOf(),
    //用户昵称
    var nickname: String = "",
    //当前等级增加亲密度
    var nowLevelAddIntimate: Int = 0,
    //特权信息
    var privilegeList: MutableList<Privilege> = mutableListOf(),
    //粉丝牌子图片
    var fansSignPic: String = "",
    //粉丝牌子是否佩戴
    var active: Boolean = false
) : Serializable

/**
 * 亲密度相关实体类
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/09
 * @iterativeVersion 4.19
 * @updateDetail 继承特权实例
 */
data class Intimate(
    //是否已完成
    var achieved: Boolean = false,
    //完成情况
    var achievedInfo: String = "",
    //颜色是否高亮
    var fontColor: Boolean = false,
    //4.19新增字段
    var intimateType: String = ""
) : Privilege()

/**
 * 特权信息实体类
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/12
 * @iterativeVersion 4.19
 * @updateDetail 特权实例作为亲密度的父类使用
 */
open class Privilege(
    //描述
    var description: String = "",
    //图片
    var pic: String = ""
) : Serializable

/**
 * banner校验的bean
 */
data class BannerStatusBean(var showPopup: Boolean = false, var adCode: String = "") : Serializable

/**
 * banner校验结果
 */
data class SingleBannerCheckResult(var adCode: String = "", var showPopup: Boolean = false) :
    Serializable


/**
 * 主页的Bean
 */
data class HomePageBean(
    /** 是否可以私聊 */
    var canPrivateChat: Boolean = false,
    /** 是否可以举报拉黑 */
    var canReport: Boolean = false,
    /** 用户昵称 */
    var nickname: String = "",
    /** 昵称颜色 */
    var nickcolor: String = "",
    /** 用户头像 */
    var headPic: String = "",
    /** 性别：男(M)、女(F) */
    var sex: String = "",
    /** 个性签名 */
    var mySign: String = "",
    /** 房间号 */
    var programId: Long = 0,
    /**主题房*/
    var themeRoom: Boolean = false,
    /** 主播等级 */
    var anchorLevel: Int = 0,
    /** 是否已被关注 */
    @JSONField(name = "isFollowed")
    var isFollowed: Boolean = false,
    /** 勋章图标列表(不含房管勋章),已排序 */
    var badgesPic: MutableList<String> = mutableListOf(),
    /** 是否显示私聊 */
    var showPrivateChat: Boolean = false,
    /** 背景图（照片墙） */
    var bgImgs: MutableList<String> = mutableListOf(),
    /** 粉丝数 */
    var fansCount: String = "",
    /** 视频数 */
    var videoCount: Long = 0,
    /** 动态数 */
    var postCount: Long = 0,
    //成就数目
    var achievementCount: Int = 0,
    /** 用户等级 */
    var userLevel: Int = 0,
    /** 是否直播中 */
    var isLiving: Boolean = false,
    /** 贵族等级 */
    var royalLevel: Int = 0,
    /**主播靓号**/
    var prettyId: Long? = null,
    /** 靓号 */
    var prettyNum: String = "",
    /** 靓号等级  1、小主号(红色靓号)  2、绿色靓号   3、蓝色靓号  4、紫色靓号' */
    var prettyNumLevel: Int = 0,
    /** 是否展示加好友 */
    var showAddFriend: Boolean = false,
    /** 是否神秘人 */
    var mystery: Boolean = false,
    /** 用户类型 */
    var userType: String = "",
    /**位置*/
    var city: String = "",
    /**贵族等级地址*/
    var royalPic: String = "",
    //主播勋章
    var anchorBadges: List<SingleBadge> = listOf()
) : Serializable

//直播间关注列表
data class LiveFollowListData(
    var dataType: Int = -1,
    var isPull: Boolean = false,
    var list: MutableList<LiveFollowBean> = mutableListOf(),
    var hasMore: Boolean = false
)

//直播间关注列表bean
data class LiveFollowBean(
    var coverPic: String = "",
    var onlineUserNum: Int = 0,
    var programId: Long = 0,
    var programName: String = "",
    var living: Boolean = false
)

//上下切换列表bean
data class SwitchBean(
    var coverPic: String = "",
    var programId: Long = 0
) {
    override fun hashCode(): Int {
        return programId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is SwitchBean) {
            return programId == other.programId
        }
        return false
    }
}

/**
 * 猜字谜气泡消息信息
 */
data class BubbleResult(
    var riddleIndex: Int = 0,
    var riddleInfo: String = "",
    var bubbleMsg: String = ""
) : Serializable

/**
 * 猜字谜第一名中奖消息
 */
data class WordPuzzleAward(
    //奖励图片
    var riddleAwardPic: String = "",
    //奖励数量
    var riddleAwardCount: Int = 0,
    //奖励说明
    var riddleAwardWord: String = "",
    //礼物名称
    var riddleAwardName: String = ""
) : Serializable

/**
 * 横竖屏切换实体类
 */
data class SwitchScreen(
    var programId: Long = 0,
    var cutoverScreenType: String = ""
) : Serializable

/**
 * 翻牌游戏消息信息
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 */
data class FlipCardResult(
    var turnCarOpen: Boolean? = null
) : Serializable

/**
 * 卡牌样式以及内容列表
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 */
data class FlipCardExplainResult(
    var awards: ArrayList<FlipCardExplainItemInfo>? = null,
    var pics: ArrayList<FlipCardPic>? = null,
    var goodRate: Long = 0,
    var programName: String = "",
    var giftName: String = "",
    var prodCount: Int = 0,
    var giftPic: String = "",
    var sendBeans: Long = 0,
    var beans: Long = 0,
    var open: Boolean = false,
    var userType: String = "",
    var prodId: Int = 0,
    var badCount: Int = 0
) : Serializable

/**
 * 卡牌样式
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 */
data class FlipCardPic(
    var backPic: String = "",
    var frontPic: String = "",
    var lockPic: String = ""
) : Serializable

/**
 * 底牌说明item
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 * @iterativeDate 2019/09/09
 * @iterativeAuthor WanZhiYuan
 * @iterativeVersion 4.17.1
 * @iterativeDetail 重写equals和hashCode方法
 */
data class FlipCardExplainItemInfo(
    var awardContent: String = "",
    var rate: Long = 0,
    var beans: Long = 0,
    var configId: Int = 0
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is FlipCardExplainItemInfo) {
            return this.configId == other.configId
        }
        return false
    }

    override fun hashCode(): Int {
        return configId.hashCode()
    }
}

/**
 * 翻牌中更多节目列表
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 */
data class FlipCardMoreItemInfo(
    var headPic: String = "",
    var nickname: String = "",
    var programId: Long = 0,
    var turnTime: Int = 0,
    var living: Boolean = false
) : Serializable

/**
 * 翻盘历史记录列表
 * @createDate 2019/06/06
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.16
 */
data class FlipCardHistoryItemInfo(
    /** 奖励内容 **/
    var awardContent: String = "",
    /** 主播是否发奖 **/
    var hasSend: Boolean = false,
    var anchorName: String = "",
    var nickname: String = "",
    var programId: Long = 0,
    /** 翻牌奖励记录id **/
    var recordId: Int = 0,
    var userId: Long = 0,
    /** 创建时间 **/
    var createTime: String = "",
    /** 评价类型 满意：Satisfied 不满意：Bad  未评价为空 **/
    var commentType: String = ""
) : Serializable

/**
 * 小主回馈Item相关信息
 * @createDate 2019/08/20
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.17
 */
data class FansFeedbackItemInfo(
    /** 库存数量 **/
    var count: Int = 0,
    /** 库存名称 **/
    var name: String = "",
    /** 库存图标 **/
    var pic: String = "",
    /** 回馈数量 **/
    var awardCount: Int = 0,
    /** 回馈礼物名称 **/
    var awardName: String = "",
    /** 回馈时间 **/
    var createTime: String = "",
    /** 回馈玩家昵称 **/
    var userInfo: String = ""
) : Serializable

/**
 * 直播间 底部导航栏  点击使用的Bean
 * @param type 操作的类型
 * @param actionValue 操作的值。可以不传
 */
data class BottomActionBean(
    //操作类型(用于打开视图)
    var type: String = "",
    var actionValue: Any? = null,
    //操作分类（在视图内进行区分）
    var innerActionType: String = ""
) : Serializable


/**
 * 折扣券Item相关信息
 * @createDate 2019/08/26
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.17
 */
data class DiscountItemInfo(
    /** 剩余数量 **/
    var count: Int = 0,
    /** 折扣比例 **/
    var discount: Int = 0,
    /** 过期时间 **/
    var expTime: String = "",
    /** 适用商品类型 **/
    var prodGoodType: String = "",
    /** 折扣券适用商品id **/
    var prodId: Int = 0,
    /** 商品名称 **/
    var prodName: String = "",
    /** 商品图片 **/
    var prodPic: String = ""
) : Serializable

/**
 *1.发起拯救节目Id：startProgramId（比对与当前所在直播间是否一致，一致：参数2、7起作用；不一致，参数3、4、5、6、7起作用）
 *2.礼物气泡提示秒钟：giftPopSeconds
 *3.主播昵称：programName
 *4.拯救提醒显示秒钟：remindSeconds
 *5.主播头像：programPic
 *6.PK连麦拯救提醒弹窗结束毫秒: remindEndMills
 *7.PK连麦拯救Id: salvationId
 * progressSeconds倒计时总秒数
 * progressPassedSeconds倒计时已过秒数
 * participatedNums已参与人数
 * maxParticipateNums 最大参与人数
 *
 */
//拯救周星动画消息
data class PkMicSalvationInfo(
    var startprogramId: Long = 0,
    var programName: String = "",
    var remindSeconds: Int = 0,
    var programPic: String = "",
    var remindEndMills: Long = 0L,
    var giftPopSeconds: Long = 0,
    var salvationId: Int = 0
)

//PK拯救开始
data class PkMicSalvationStartInfo(
    var participatedNums: Int = 0,
    var giftPopSeconds: Long = 0,
    var maxParticipateNums: Int = 0,
    var programId: Long = 0,
    var programName: String = "",
    var progressPassedSeconds: Int = 0,
    var progressSeconds: Int = 0
)

//PK拯救消息变化
data class PkMicSalvationChangeInfo(
    var participatedNums: Int = 0,
    var maxParticipateNums: Int = 0
)

/**
 * 热度跑道
 */
data class HotTrackInfo(var hotPositionChangeText: String = "")

/**
 * 主播段位信息
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankAnchorInfo(
    /** 赛季名称 **/
    var seasonName: String = "",
    /** 上赛季冠军主播信息 **/
    var lastTop1: PkRankLastChampionInfo? = null,
    /** 主播信息 **/
    var anchorInfo: PkAnchorInfo? = null,
    /** 段位信息（如果没有该字段，表示还没有参与） **/
    var stageInfo: PkRankInfo? = null,
    /** 本赛季PK场数 **/
    var pkCnt: Int = 0,
    /** 本赛季胜率 **/
    var winRatio: String = "",
    /** 今日连胜数 **/
    var todayWinCnt: Int = 0,
    /** 本赛季累计PK分 **/
    var totalPkScore: Long = 0,
    /** 主播当前赛季段位排名 **/
    var ranking: String = ""
)

/**
 * 上赛季冠军信息
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankLastChampionInfo(
    /** 头像 **/
    var headPic: String = "",
    /** 第一名图标 **/
    var icon: String = "",
    /** 是否开播 **/
    var isLiving: Boolean = false,
    /** 昵称 **/
    var nickname: String = "",
    /** 直播间ID 没有该字段表示虚位以待 **/
    var programId: Long? = null,
    /** 赛季名称 **/
    var seasonName: String = "",
    /**  用户ID **/
    var userId: Long = 0
)

/**
 * 当前主播相关信息
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkAnchorInfo(
    /** 昵称 **/
    var nickname: String = "",
    /** 头像 **/
    var headPic: String = ""
)

/**
 * 当前主播段位相关信息
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankInfo(
    /** 当前段位等级 **/
    var currStageLevel: Int = 0,
    /** 当前段位图标 **/
    var currStageIcon: String = "",
    /** 当前段位名称 **/
    var currStageName: String = "",
    /** 当前最低段位分 **/
    var currStageScore: Long = 0,
    /** 当前段位分 **/
    var nowScore: Long = 0,
    /** 距离下一个段位的分 **/
    var diffScore: Long = 0,
    /** 下一个段位图标 **/
    var nextStageIcon: String = "",
    /** 下一个段位等级(如果没有该字段，表示已达到最高等级) **/
    var nextStageLevel: Int? = null,
    /** 下一个段位名称 **/
    var nextStageName: String = "",
    /** 下一个段位分 **/
    var nextStageScore: Long = 0,
    /** 下一个段位的描述 **/
    var remark: String = ""
)

/**
 * 当前主播PK记录列表
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankRecordListInfo(
    /** 赛季名称 **/
    var seasonName: String = "",
    /** 赛季时间 **/
    var seasonTime: String = "",
    /** PK记录 **/
    var pkList: ArrayList<PkRankRecordInfo> = arrayListOf(),

    //自定义字段
    /** 是否还有更多 **/
    var hasMore: Boolean = false,
    /** 是否是下拉 **/
    var isPull: Boolean = true
)

/**
 * 当前主播单条PK记录
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankRecordInfo(
    /** 对手的直播间ID **/
    var programId: Long = 0,
    /** 对手是否开播 **/
    var isLiving: Boolean = false,
    /** 对手昵称 **/
    var nickname: String = "",
    /** 对手头像 **/
    var headPic: String = "",
    /** 我本场获得的Pk分 **/
    var pkScore: Long = 0,
    /** 我本场获得的段位分 **/
    var stageScore: String = "",
    /** pk时间 **/
    var pkDate: String = "",
    /** pk结果 Win -> 胜利，Lose -> 战败，Draw -> 战平 **/
    var result: String = "",
    /** 主播ID,用户跳转首页 **/
    var anchorId: Int = 0
)

/**
 * 玩家贡献榜
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankUserRankInfo(
    /** 赛季名称 **/
    var seasonName: String = "",
    /** 赛季时间 **/
    var seasonTime: String = "",
    /** 我的排位信息 **/
    var rankInfo: PkRankUserInfo? = null,
    /** 列表数据 **/
    var rankPage: PkRankUserListInfo? = null,

    //本地字段
    var isPull: Boolean = true
)

/**
 * 玩家贡献列表
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankUserListInfo(
    /** 是否还有更多 **/
    var hasMore: Boolean = false,
    /** 玩家贡献列表 **/
    var list: ArrayList<PkRankUserInfo> = arrayListOf()
)

/**
 * 单条玩家贡献信息
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankUserInfo(
    /** 排名 **/
    var ranking: String = "",
    /** 当前用户贵族等级 **/
    var royalLevel: Int = 0,
    /** 当前用户等级 **/
    var userLevel: Int = 0,
    /** 头像 **/
    var headPic: String = "",
    /** 用户昵称 **/
    var nickname: String = "",
    /** 贡献值 **/
    var score: Long = 0,
    /** 用户ID **/
    var userId: Long = 0
)

/**
 * 用户MVP记录列表
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankUserMvpListInfo(
    /** 用户ID **/
    var userId: Long = 0,
    /** 用户昵称 **/
    var nickname: String = "",
    /** 下一个勋章已有的Mvp次数 **/
    var nextBadgeMvpCnt: Int = 0,
    /** 兑换一个勋章所需的mvp次数 **/
    var badgeNeedCnt: Int = 0,
    /** 头像 **/
    var headPic: String = "",
    /** mvp总次数 **/
    var mvpCnt: Int = 0,
    /** 玩家赛季MVP次数列表 **/
    var mvpList: ArrayList<PkRankUserMvpInfo> = arrayListOf(),
    /** 文案 **/
    var remark: String = ""
)

/**
 * 用户单条MVP记录
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankUserMvpInfo(
    /** mvp次数 **/
    var mvpCnt: Int = 0,
    /** 赛季名称 **/
    var seasonName: String = "",
    /** 赛季时间 **/
    var seasonTime: String = ""
)

/**
 * 主播赛季记录列表
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankAnchorRankListInfo(
    /** 赛季名称 **/
    var seasonName: String = "",
    /** 榜单 **/
    var rankList: ArrayList<PkRankAnchorRankInfo> = arrayListOf()
)

/**
 * 主播单条赛季记录
 * @author WanZhiYuan
 * @since 4.23
 */
data class PkRankAnchorRankInfo(
    /** 主播头像 **/
    var headPic: String = "",
    /** 是否开播 **/
    var isLiving: Boolean = false,
    /** 昵称 **/
    var nickname: String = "",
    /** 直播间ID **/
    var programId: Long = 0,
    /** 段位分 **/
    var score: Long = 0,
    /** 段位Icon **/
    var stageIcon: String = "",
    /** 段位等级 **/
    var stageLevel: Int = 0,
    /** 段位名称 **/
    var stageName: String = "",
    /** 用户ID **/
    var userId: Long = 0
)

/**
 * 在线用户信息
 * @author WanZhiYuan
 * @since 4.24
 */
data class OnlineUserInfo(
    var userId: Long = -1L,
    //头像
    var headPic: String = "",
    var userLevel: Int = -1,
    var royalLevel: Int = -1,
    var roomGuardPic: String = "",
    var roomManagerPic: String = "",
    var royalSmallPic: String = "",
    var badgesPic: ArrayList<String> = arrayListOf(),
    //昵称
    var nickname: String = "",
    var score: Double = 0.toDouble(),
    //4.15新增字段
    //贵族勋章地址
    var royalPic: String = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is RoomUserInfo) {
            return userId == other.userId
        }
        return false
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}

/**
 * 在线用户信息
 * @author WanZhiYuan
 */
class OnlineListData<T>(
    isPull: Boolean = false,
    hasMore: Boolean = false,
    extDataJson: String? = null
) :
    RootListData<T>(isPull, arrayListOf(), hasMore, extDataJson) {
    var royalHonorList: MutableList<T> = mutableListOf()

    //贵族说明
    var royalLevelUrl: String? = null

    //贵族特权图片
    var privilegeUrl: String? = null

    //贵族人数
    var royalCount: Int? = null

    //当前用户是否是贵族
    var royaling: Boolean = false

    //当前守护人数
    var guardCount: Int? = null

    //剩余守护时间
    var surplusDay: Int? = null

    //是否是守护
    var guarding: Boolean = false

    //守护特权图片
    var guardUrl: String? = null

    //观众人数
    var userCount: Int? = null

    //管理人数
    var managerCount: Int? = null

    //守护费用
    var spend: Long? = null

    //守护商品id
    var goodsId: Int? = null

    var heatTips: String = ""
    var royalTips: String = ""
}

/**
 * 消息使用的播放对象
 */
data class PlayInfoBean(
    var programId: Long = 0,
    //当前主题房场次
    var themeSessionId: Long = 0,
    //当前上场的主播id
    var themeShowingprogramId: Long = 0,
    var programName: String = "",
    var headPic: String = "",
    @JSONField(name = "isPcLive")
    var isPcLive: Boolean = true,
    var prePic: String = "",
    var playInfo: PlayInfo? = null
) : Serializable

/**
 * 体验守护
 * @author WanZhiYuan
 * @since 4.24
 */
data class ExperienceGuard(
    /** 体验守护资格状态 None(没有)|Unclaimed(待领取)|UnUsed(已领取)|Used(已使用)|Expired(已失效) **/
    var status: String = "",
    /** 是否自动弹窗 **/
    var autoPopup: Boolean = false,
    /** 体验守护资格倒计时 **/
    var experienceCountDown: Long = 0,
    /** 提示文案 **/
    var tips: String = "",
    /** 图片 **/
    var descUrl: String = "",
    /** 图片title **/
    var descTitle: String = ""
) : Serializable

/**
 * 贵族数量信息
 * @author WanZhiYuan
 * @since 4.25
 */
data class RoyalMessageBean(
    var royalCount: Int? = null,
    var guardCount: Int? = null
) : Serializable

/**
 * PK礼物相关数据
 */
//data class PKGiftBalanceBean(
//        var beans: Long = 0,
//        var groupInfo: MutableList<PKGiftBean> = mutableListOf()
//) : Serializable
//
//
//data class PKGiftBean(
//        var gifts: MutableList<LiveGiftDto> = mutableListOf()
//) : Serializable

/**
 * 优惠券相关信息
 * @author WanZhiYuan
 * @since 4.26
 */
data class CouponItemInfo(
    /** 充值折扣券类型(可选项：MJ -> 满减、DJ -> 定减、ZK -> 折扣) **/
    var couponType: String = "",
    /** 折扣金额 **/
    var typeValue: Int = 0,
    /** 使用阈值 **/
    var useValue: Int = 0,
    /** 过期时间 **/
    var expDate: String = "",
    /** 折扣券名称 **/
    var couponName: String = "",
    /** 折扣券记录id **/
    var couponRecordId: Long = 0,
    /** 使用客户端 **/
    var useClients: String = "",
    /** 能否使用 **/
    var canUse: Boolean = false,


    //本地字段
    /** 是否选中 **/
    var isSelector: Boolean = false,
    /** 可用数量 **/
    var useCount: Int = 0,
    /** 优惠券总数 **/
    var total: Int = 0
) : Serializable


/**
 * 星球霸主CP对象
 */
data class CpInfo(
    var nickname: String = "",
    var headPic: String = "",
    var userNickname: String = "",
    var userHeadPic: String = ""
) : Serializable

/**
 * 星球霸主基础信息
 */
data class PlanetHomeBean(
    //萌豆余额
    var beans: Long = 0L,
    //推荐模式
    var recommend: String = "",
    //是否幸运星球模式
    var isLuckyMode: Boolean = false,
    var cpInfo: CpInfo? = null,
    //榜单数据
    var rankUrl: String = "",
    //规则地址
    var ruleUrl: String = ""

) : Serializable {
    companion object {
        //攻击模式
        const val AttackMode = "AttackMode"

        //休闲模式
        const val CasualMode = "CasualMode"

        //幸运模式
        const val LuckyMode = "LuckyMode"
    }

}


/**
 * 休闲模式当前最高数据玩家
 */
data class TopUserInfo(
    var headPic: String = "",
    var nickname: String = "",
    //分数
    var score: Long = 0L,
    var userId: Long = 0L,
    //攻击模式使用
    var anchorScore: Long = 0L
) : Serializable

/**
 * 休闲模式  基础数据查询
 */
data class PlanetCasualBean(
    //萌豆数量
    var beans: Long = 0L,
    //光线抢卡数量
    var lightGunCardNum: Int = 0,
    //奖励模式
    var preAwardDesc: String = "",
    //奖励图片
    var preAwardPic: String = "",
    var topUser: TopUserInfo? = null
) : Serializable

data class Weapon(
    //价格
    var beans: Long = 0L,
    //商品ID
    var goodsId: Int = 0,
    var pic: String = "",
    //枪名称
    var weaponName: String = "",
    //枪对应的Type
    var weaponType: String = "",
    //武器卡数量
    var bagCnt: Int = 0,
    //最大可中奖金额
    var maxAwardBeans: Long = 0
) : Serializable

/**
 * 攻击模式  基础数据查询
 */
data class PlanetAttackBean(
    //萌豆数量
    var beans: Long = 0L,
    //主播今日积分
    var anchorScore: Long = 0L,
    //模式
    var mode: String = "",
    //幸运模式倒计时
    var ttl: Long = 0,
    //top用户数据
    var currTopUser: TopUserInfo? = null,
    //当前血量（幸运星球）
    var blood: Long = 0L,
    //枪杆数据
    var weaponList: MutableList<Weapon> = mutableListOf(),
    //榜单
    var rankUrl: String = "",
    //缓存的中奖消息
    var msgList: MutableList<String> = mutableListOf()

) : Serializable {
    companion object {
        //攻击模式
        const val AttackMode = "AttackMode"

        //幸运模式
        const val LuckyMode = "LuckyMode"
    }
}

/**
 * 休闲模式 击打过程 返回的数据
 */
data class HitPlanetBean(
    //获取爆能枪卡标识位
    var gotEnergyCard: Boolean = false,
    //增加的分数
    var addScore: Int = 0,
    //是否获得萌币
    var gotMoeCoins: Boolean = false,
    //总分数
    var totalScore: Long = 0,
    //本地使用字段
    var type: String = ""
) : Serializable

/**
 * 打开直播间的同时 需要打开[operate]弹窗的操作 附带[params]参数
 */
data class OpenOperateBean(var operate: String, var params: HashMap<String, Any>? = null) :
    Serializable

/**
 * 星球  奖励
 */
data class PlanetAward(
    //奖励描述
    var desc: String = "",
    //中奖数量
    var count: Int = 0,
    //奖励名称
    var name: String = "",
    //奖励图片
    var pic: String = ""
) : Serializable

/**
 * 休闲模式 结果数据
 */
data class RelaxationResultBean(
    //奖励列表
    var awardList: MutableList<PlanetAward> = mutableListOf(),
    //击败比例
    var beatRate: String = "",
    //本场精准值
    var score: Int = 0
) : Serializable

/**
 * 攻击模式下的奖励
 */
data class PlanetAttackAward(
    var count: Int = 0,
    var pic: String = "",
    var prizeName: String = ""
) : Serializable

/**
 * 发射子弹返回的结果
 */
data class UseWeaponResult(
    var beans: Long = 0,
    //中奖萌豆余额
    var awardBeans: Long = 0,
    var awardList: MutableList<PlanetAttackAward> = mutableListOf(),
    var showType: String = "",
    //剩余的武器卡数量
    var bagCnt: Int = 0,
    //显示类型
    var popupType: String = "",
    //武器名称
    var weaponName: String = "",
    //发射子弹的时间
    var shootTime: Long = 0L
) : Serializable {
    companion object {
        const val Style1 = "Style1"
        const val Style2 = "Style2"
        const val Style3 = "Style3"
        const val Popup = "Popup"

        //不弹窗
        const val None = "None"

        //小奖弹窗
        const val Popup1 = "Popup1"

        //大奖弹窗
        const val Popup2 = "Popup2"
    }
}

/**
 * 武器卡数量 类
 */
data class WeaponCountBean(
    //武器类型
    var type: String = "",
    //武器卡数量
    var count: Int = 0,
    //是否需要刷新
    var refresh: Boolean = false
) : Serializable

/**
 * 星球星球消失消息  返回的数据
 */
data class LuckyPlanetDisappearBean(
    var luckyPlanetTimeOut: String = "",
    //用户ID
    var userId: Long = 0L,
    //奖励数据   所有用户的都在一起
    var luckyPlanetUserInfo: HashMap<String, LuckyPlanetDisappearSingleAward> = hashMapOf()
) : Serializable

/**
 * 幸运星球  消息  当个用户的奖励数据
 */
data class LuckyPlanetDisappearSingleAward(
    //霸道值
    var score: Long = 0L,
    //直播间ID
    var programId: Long = 0L,
    //奖励列表
    var awardValue: MutableList<PlanetAward> = mutableListOf()
) : Serializable

/**
 * 霸主信息变更消息
 */
data class PlanetScoreChangeBean(
    var planetScoreInfo: TopUserInfo? = null
) : Serializable

/**
 * 直播间banner变化
 */
data class RoomBannerChangeBean(
    var bannerInfo: RoomBanner? = null,
    var remove: Boolean = false
) : Serializable

/**
 * 直播间数据变化
 */
data class PlayerDataChanged(
    //弹幕卡数量
    var danmuCardNum: Int = 0
) : Serializable

/**
 * 幸运星球开始的BEAN
 */
data class LuckyStartBean(
    //血量
    var luckyPlanetBlood: Long = 0,
    //倒计时时间
    var luckyPlanetTime: Long = 0,
    //本直播间显示文案
    var releaseLuckyPlanetRoomText: String = "",
    //星球开启 弹窗文案
    var releaseLuckyPlanetUserText: String = ""
) : Serializable

/**
 * 休闲模式中奖记录
 */
data class RelaxationAwardBean(
    //日期
    var dateStr: String = "",
    //萌币数量
    var moeCoins: Int = 0,
    //中奖分数
    var score: Long = 0,
    //爆能枪卡次数
    var energyCardTimes: Int = 0
) : Serializable

/**
 * 星球霸主使用的主播信息对象
 */
data class PlanetAnchorInfo(
    var anchorId: Int = 0,
    var headPic: String = "",
    var isLiving: Boolean = false,
    var nickname: String = "",
    var programName: String = "",
    //直播间ID
    var programId: Long = 0
) : Serializable

/**
 * 攻击模式 中奖记录
 */
data class AttackAwardRecord(
    var awardCount: Int = 0,
    var prodId: Int = 0,
    var prodName: String = "",
    var prodPic: String = "",
    var prodType: String = ""
) : Serializable

/**
 * 攻击模式中奖记录
 */
data class AttackAwardBean(
    //幸运模式 还是 攻击模式
    var mode: String = "",
    //中奖萌豆（霸道值）
    var anchorInfo: PlanetAnchorInfo? = null,
    var awardBeans: Long = 0L,
    //消费的武器名称
    var goodsName: String = "",
    //武器图片
    var goodsPic: String = "",
    var date: String = "",
    //奖励列表
    var awardList: MutableList<AttackAwardRecord> = mutableListOf(),
    //本地地段
    var style: String = NORMAL
) : Serializable {
    companion object {
        //默认样式
        const val NORMAL = "NORMAL"

        //详情样式
        const val DETAIL = "DETAIL"
    }
}

/**
 * 显示星球资源使用的Bean
 */
data class ShowStarBean(
    //使用的资源
    var drawableResource: LevelListDrawable,
    //位置数据
    var tyle: String,
    //帧数
    var frameCount: Int,
    //动画时间
    var animaDuration: Long
) : Serializable

//单个中奖助手
data class SingleAwardAssistant(
    //奖励范围
    var awardBeans: String = "",
    //奖励数量
    var awardCnt: Int = 0,
    //最近奖励时间
    var lastAwardTime: String = ""
) : Serializable

/**
 * 中奖助手数据
 */
data class AwardAssistantBean(
    //中奖助手记录
    var awardList: MutableList<SingleAwardAssistant> = mutableListOf(),
    //武器类型
    var currWeaponType: String = "",
    //武器列表
    var weaponList: MutableList<Weapon> = mutableListOf()
) : Serializable

/**
 * 嘉宾相关信息
 * @author WanZhiYuan
 * @since 4.31
 * @date 2020/05/08
 */
data class GuestInfo(
    var programName: String = "",
    var programId: Long = 0,
    var headPic: String = "",
    var host: Boolean = false
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is GuestInfo) {
            return other.programId == this.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.hashCode()
    }
}

data class ThemeSessionFinishBean(
    var programId: Long = 0,//这里指的是主题房id
    var themeSessionId: Int = 0
)

//通用的只含programId字段的类
data class ProgramRoomBean(
    var programId: Long = 0//
)

//宝箱可领取消息
data class ThemeOnlineTreasureEnableInfo(
    var treasureCode: String = ""
)

data class ThemeProgramBean(
    var curSessionList: ThemeSessionList? = null,
    var nextSessionList: ThemeSessionList? = null,
    var sessionId: Int = 0
)

data class ThemeSessionList(
    var programList: List<ThemeProgram> = listOf(),
    var showTime: String = "",
    var showType: String = ""
)

data class ThemeProgram(
    var anchorId: Int = 0,
    var headPic: String = "",
    @JSONField(name = "isFollowed")
    var isFollowed: Boolean = false,
    @JSONField(name = "isLiving")
    var isLiving: Boolean = false,
    var nickname: String = "",
    var programId: Long = 0,
    var showTime: String = "",
    var showTimeTtl: Long = 0,
    var showType: String = ""
)

/**
 * 节目关注列表
 * @author WanZhiYuan
 * @date 2020/07/15
 * @since 1.0.0
 */
data class LiveRemindBeans(
    //主播id
    var anchorId: Int = 0,
    //主播头像
    var anchorPic: String = "",
    //直播中
    var livingStatus: Boolean = false,
    //PC直播中
    var pcLiveStatus: Boolean = false,
    //开启通知
    var pushOpen: Boolean = false,
    //最后直播时间
    var lastShowTime: String = "",
    //签名
    var mySign: String = "",
    //昵称
    var nickname: String = "",
    //在线人数
    var onlineUserNum: Int = 0,
    //节目id
    var programId: Long = 0,
    //节目名称
    var programName: String = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is LiveRemindBeans) {
            return other.programId == this.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.hashCode()
    }
}

