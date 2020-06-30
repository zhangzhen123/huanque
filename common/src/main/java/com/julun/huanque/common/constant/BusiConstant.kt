package com.julun.huanque.common.constant


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/22 14:04
 *
 *@Description: BusiConstant
 *
 */
object BusiConstant {
    //用户创建聊天室的前缀
    const val USER_CHAT_ROOM_PREFIX = "U"
    val API_KEY = "2FsdGVkX19"
    const val WEIXIN_FLAG = "weixin://wap/pay?"

    //系统通知栏回调
    var NOTIFICATION_REQUEST_CODE = 300
}

/**
 * 测试环境地址
 */
object EnvironmentAddress {
    const val Environment250 = "http://192.168.96.250"
    const val Environment251 = "http://192.168.96.251"
    const val Environment252 = "http://192.168.96.252"
    const val Environment248 = "http://192.168.96.248"
    const val Environment245 = "http://192.168.96.245"
    const val Environment246 = "http://192.168.96.246"
    const val Environment247 = "http://192.168.96.247"

    val H5_PRODUCT_NAME = "https://www.51lm.tv/activity"//h5正式环境域名
    val H5_TEST_NAME_OFFICE205 = "http://office250.katule.cn/activity"//office测试环境域名
    val H5_TEST_NAME_OFFICE251 = "http://office251.katule.cn/activity"//office1测试环境域名
    val H5_TEST_NAME_OFFICE2 = "http://office2.katule.cn/activity"//office2测试环境域名
    val H5_TEST_NAME_OFFICE248 = "http://office248.katule.cn/activity"//office3测试环境域名

    //    val H5_DEVELOP_NAME = "http://testwechat.katule.cn/activity"//h5开发环境域名
    val H5_DEVELOP_NAME = "http://office204.katule.cn/activity"//h5开发环境域名
    val H5_TEST_NAME_OFFICE245 = "http://office245.katule.cn/activity"//245测试环境域名
    val H5_TEST_NAME_OFFICE246 = "http://office246.katule.cn/activity"//246测试环境域名
    val H5_TEST_NAME_OFFICE247 = "http://office247.katule.cn/activity"//247测试环境域名
}


object SDKType {
    const val BAOMIHUA = "BAOMIHUA"
    const val XINLIAO = "XINLIAO"
}

/**
 * 网络端返回码
 */
object ErrorCodes {
    //返回成功
    const val SUCCESS = 200
    const val NOT_ENOUGH_TIME = 7206//领取免费礼物时  时间不够
    const val GET_ALL_GIFTS = 7207//领取免费礼物时   当天礼物全部领取完
    const val SESSION_PAST = 500//Session过期
    const val MONTH_TICKETS_NOT_ENOUGH = 7209//免费礼物不足
    const val BALANCE_NOT_ENOUGH = 1001//余额不足
    const val SYSTEM_ERROR = -1//系统异常

    //非法字符
    const val MESSAGENOTPERMIT = 501

    //创建连麦异常
    const val CREATE_MICRO_FAIL = 501

    //视频点赞错误码
    const val DYNAMIC_VIDEO_FAIL = 501

    //PK相关异常，需要toast
    const val PK_ERROR = 1011

    //PK相关异常，需要toast
    const val PK_ERROR2 = 1014

    //豪车升级异常
    const val CAR_ERROR = 501

    //宝箱重复领取异常
    const val BOX_REPEAT = 1601

    //宝箱领取  活跃值不足
    const val BOX_ACTIVE_NO_ENOUGH = 1602

    //包厢领取需要验证手机
    const val BOX_PHONE_CHECK = 1104

    //返回的错误码为1204时，客户端标记该用户ID，不再调用该接口
    const val NEARBY_NO_NEED = 1204

    //翻牌未关注主播
    const val ERROR_CODE_FLIP_CARD_NO_FOLLOW = 4000

    //翻牌不是粉丝团成员
    const val ERROR_CODE_FLIP_CARD_NO_FANS = 4001

    //翻牌不是本直播间守护
    const val ERROR_CODE_FLIP_CARD_NO_GUARD = 4002

    //翻牌游戏已关闭
    const val ERROR_CODE_FLIP_CARD_CLOSE = 4003

    //携手闯关 关卡已失效
    const val PASS_LEVEL_SEND_LEVEL_LOSE = 1206

    //携手闯关 单个送礼已经满额的情况
    const val PASS_LEVEL_SEND_GIFT_FULL = 1207

    //携手闯关 关卡信息已更新 需要刷新
    const val PASS_LEVEL_SEND_GIFT_PASS_UPDATE = 1208

    //送礼嘉宾出错
    const val ERROR_CODE_GIFT_GUEST = 5000
}


/**
 * PK类型
 */
object PKType {
    val PK = "Normal"//普通PK
    val MONTH_PK = "YUESAI"//月赛
    val DYNAMIC = "DYNAMIC" // 动态配置
    val WORLDCUP = "WORLDCUP"//世界杯
}

object PKAction {
    const val CREATE = "Create" //空闲状态
    const val EXISTED = "Existed" //
    const val REJECT = "Reject"
    const val CANCEL = "Cancel"
    const val ACCEPT = "Accept"
    const val TIMEOUT = "Timeout"
    const val WAITING = "Waiting"//等待确认

    const val PKING = "Pking"//正在PK中
    const val ALL = "All" //代表无限制礼物
    const val ONE = "One" //代表选择一个礼物

    const val WIN = "Win" //代表胜利
    const val DRAW = "Draw" //代表战平
    const val LOSE = "Lose" //代表失败
}

/**
 * PK结果类型
 */
object PKResultType {
    val WIN = "Win"//
    val DRAW = "Draw"//
    val LOSE = "Lose" //
}

// "groupMatch",//groupMatch小组赛,final1To8 1/8决赛,final1To4 1/4决赛 semifinal半决赛,
// threeFourFinal 三四名决赛,lastfinal决赛
object WorldCupType {
    val GROUPMATCH = "groupMatch"
    val FINAL1TO8 = "final1To8"
    val FINAL1TO4 = "final1To4"
    val SEMIFINAL = "semifinal"
    val THREEFOURFINAL = "threeFourFinal"
    val LASTFINAL = "lastfinal"
}

enum class PayResult {
    IS_PAY,//去支付了 但是不知道结果的情况
    WX_PAY_SUCCESS,
    WX_PAY_CANCEL,
    WX_PAY_FAIL,
    ALIPAY_SUCCESS,
    ALIPAY_CACEL,
    ALIPAY_FAIL,

    //重复请求
    ALIPAY_REPETITION
}

enum class PlatformType {
    IOS,
    ANDROID
}

enum class Sex private constructor(private val text: String, private val value: String) {
    MALE("男", "Male"),
    FEMALE("女", "Female"),
    UNKOWN("未知", "Unknow");


    companion object {

        fun getSexText(str: String): String {
            for (sex in Sex.values()) {
                if (sex.value == str) {
                    return sex.text
                }
            }
            return ""
        }

        fun getSexValue(str: String): String {
            for (sex in Sex.values()) {
                if (sex.text == str) {
                    return sex.value
                }
            }
            return ""
        }
    }
}

/**
 * 主播或者管理的管理类型
 */
object ManagerType {
    /**
     * 踢人
     */
    val KICK = "KICK"

    /**
     * 禁言
     */
    val GAG = "MUTE"

    /**
     * 关小黑屋
     */
    val BLACKUSER = "BLACKUSER"
}

/**
 * 常用的常量   eg：Intent跳转使用
 */
object CommonConstant {
    /**
     * 直播间ID
     */
    val PROGRAM_ID = "PROGRAM_ID"

    /**
     * 用户ID
     */
    val USER_ID = "USER_ID"

    /**
     * 用户昵称
     */
    val NICKNAME = "NICKNAME"

    /**
     * 房管
     */
    val ROOM_MANAGER = "ROOM_MANAGER"

}

//当前奉茶节的状态
object TeaStatus {
    val INIT = "INIT"//初始化
    val ING = "ING"//进行中
    val END = "END"//已结束
}

//当前出题状态
object QuestionsStstus {
    val WAIT = "WAIT"//未开始
    val ING = "ING"//答题中
    val DONE = "DONE"//已完成
}

//SVGA内置动画
object SVGAType {
    //SVGA动画  暂时只支持飞机和飞艇
    //飞机
    val AIRPLANE = "svga/airplane.svga"

    //飞艇
    val AIRSHIP = "svga/airship.svga"

    //加载动画
    val LOADING = "svga/loading.svga"
}

object HomeBannerLocation {
    val RIGHT_TOP = 1
    val LEFT_TOP = 2
    val RIGHT_BOTTOM = 3
    val LEFT_BOTTOM = 4
}

object MetaKey {
    val DOWNLOAD_CHANNEL = "UMENG_CHANNEL"//渠道链接  2018/2/26修改本地渠道链接标识DOWNLOAD_CHANNEL为UMENG_CHANNEL
}

/**直播间标签类型**/
object ProgramTagType {
    //文本标签  颜色渐变
    val TEXT = "text:"

    //图片标签   直接显示图片
    val IMG = "img:"
}

/**
 * 新手礼包活动状态
 */
object ActivityState {
    //有效的
    val ACTIVE = 1

    //进行中
    val PROCESS = 2

    //已结束
    val END = 3
}

//用户身份类型
object UserType {
    val Viewer = "Viewer"//注册用户
    val Anchor = "Anchor"//主播
    val Manager = "Manager"//运管
    val Visitor = "Visitor"//游客
    val Customer = "Customer"//客服
}

//直播权限
object PushPermis {
    val True = "True"
    val False = "False"
    val None = "None"//也当成False
}

/**
 * 充值入口
 */
object PayEntry {
    val Room = "Room"//直播间快捷充值
    val My = "My"//个人中心充值
    val CallBack = "CallBack"//回归礼包
}

object TextTouch {
    //打开深海游戏
    val OpenDeepSeaGame = "OpenDeepSeaGame"

    //打开游戏面板
    val OpenGameView = "OpenGameView"

    //切换直播间
    val OpenLiveRoom = "OpenLiveRoom"

    //打开h5
    val OpenUrlView = "OpenUrlView"

    //打开卡片
    val OpenUserCard = "OpenUserCard"

    //打开送礼面板
    val OpenSendGiftView = "OpenSendGiftView"

    //打开分享
    val OpenShareView = "OpenShareView"

    //打开豪车工厂
    val OpenFactoryCarView = "OpenFactoryCarViewV2"

    //打开锦鲤池
    val OpenWishView = "OpenWishView"

    //打开携手闯情关 since4.19.0
    val OpenPassLevelView = "OpenPassLevelView"

    //打开幸运转盘
    const val OPEN_BIG_WHEEL = "OpenBigWheel"

    //打开PK段位赛 since 4.23.0
    const val OPEN_PK_RANK = "OpenStagePk"

    //跳转目标直播间并打开星球霸主
    const val OpenLiveRoomAndPlanet = "OpenLiveRoomAndPlanet"

    //打开礼物面板并切换到背包
    const val OPEN_BAG = "OpenBag"

    val Empty = ""
}


/**
 * banner资源类型
 */
object BannerResType {
    //图片
    val Pic = "Pic"

    //视频
    val Video = "Video"
}

/**
 * banner点击效果
 */
object BannerTouchType {
    //活动页面
    val Url = "Url"

    //直播间
    val Room = "Room"

    //充值
    val Recharge = "Recharge"

    //首页
    val Home = "Home"

    //打开原生页面
    val Native = "Native"

    //弹窗类型
    const val Toast = "Toast"

    //小程序
    const val MINI = "Mini"

    //打开星球霸主
    const val OpenLiveRoomAndPlanet = "OpenLiveRoomAndPlanet"
}

object BannerTouchValue {
    //代表打开 携手闯关
    val RoomPassLevel = "RoomPassLevel"
}

object UpdateType {
    //强制更新
    val Force = "Force"

    //推荐更新
    val Recommend = "Recommend"

    //无更新
    val None = "None"
}

object BooleanType {
    val TRUE = "True"
    val FALSE = "False"
}

object RoyalStatus {
    //不是贵族
    val None = "None"

    //无需保级
    val NoNeed = "NoNeed"

    //正在保级
    val Ing = "Ing"

    //保级成功
    val Done = "Done"
}

/**
 * 用户和主播权限字段
 * @see UserManagerFragment
 */
object ManagerOperate {
    //用户
    /** 踢除 **/
    val MANAGER_CONTENT_KICK: String = "kickUser"

    /** 屏蔽 **/
    val PAGE_NAME_SHIELD: String = "shieldUser"

    /** 禁言 **/
    val PAGE_NAME_GAG: String = "gagUser"

    /** 全平台禁言 **/
    val PAGE_NAME_PLATFORM_GAG: String = "platformGagUser"

    /** 全站封禁 **/
    val PAGE_NAME_PLATFORM_BLOCK: String = "blockUser"

    /** 取消房管 **/
    val PAGE_NAME_CANCEL_MANAGER: String = "cancelRoomManager"

    /** 提升房管 **/
    val PAGE_NAME_UP_MANAGER: String = "upToRoomManager"

    //主播
    /** 封禁 **/
    val PAGE_NAME_BLOCK_PROGRAM: String = "blockProgram"

    /** 监控 **/
    val PAGE_NAME_MONITOR_PROGRAM: String = "monitorProgram"

    /** 警告 **/
    val PAGE_NAME_WARN_ANCHOR: String = "warnAnchor"

    /** 巡查提醒 **/
    val PAGE_NAME_REMIND_ANCHOR: String = "remindAnchor"

    /** 取消监控 **/
    val PAGE_NAME_UNMONITOR_PROGRAM: String = "unMonitorProgram"
}


object UserBadgeType {
    val TITLE: Int = 0
    val ITEM: Int = 1
}

object AnchorSearch {
    //类型参数
    val TYPE = "TYPE"

    //连麦的type以及回传数据的key
    val CONNECT_MICRO = "CONNECT_MICRO"
}

object UserStoreType {
    val BANNER: Int = 0
    val ITEM: Int = 1
    val FOOTER: Int = 2

    val PRETTY_NUM: String = "pretty_num"
    val PRETTY_NUM_LEVEL: String = "pretty_num_level"
    val KEYWORD: String = "keyword"
    val NICKNAME: String = "nickname"
    val USER_ID: String = "user_id"
    val PAY_BEAS: String = "pay_beans"
    val BEAS: String = "beans"
}

/**
 * 连麦操作类型
 */
object ConnectMicroOperate {
    //连麦进行中
    val Progress = "Progress"

    //创建
    val Create = "Create"

    //拒绝
    val Reject = "Reject"

    //超时
    val Timeout = "Timeout"

    //已经接受
    val Accept = "Accept"

    //等待
    val Waiting = "Waiting"

    //没有连麦
    val None = "None"
}

object PlayerLiveCode {
    val REQUEST_CODE: Int = 20000
    val IS_PLAYER_PAGE: String = "isPlayerPage"
}

/**
 * 心聊登录相关 参数
 */
object HeartChatLogin {
    //昵称
    val NICKNAME = "NICKNAME"

    //头像
    val HEADPIC = "HEADPIC"

    //openId
    val OPENID = "OPENID"

    //性别
    val SEX = "SEX"

    //是否清除上次的登录信息
    const val CLEAR = "CLEAR"
}

/**
 * 关注的分类
 */
object FollowType {
    //关注
    const val FOLLOW = "FOLLOW"

    //守护
    const val GUARD = "GUARD"

    //管理
    const val MANAGER = "MANAGER"

    //看过
    const val SEE = "SEE"
}

/**
 * 红包类型
 */
object RedPackageType {
    val WILL = 1//将要领取
    val SUCCESS = 2//领取成功
    val FAIL = 3//红包领取失败
    val NETWORK_FAIL = 4//网络异常
    val TIME_OUT = 5//红包等待超时
}

/**
 * 宝箱类型
 */
object BoxType {
    val WILL = 1//未领取
    val SUCCESS = 2//领取成功
    val LATER = 3//领取失败
    val FAIL = 4//领取异常
    val NETWORK_FAIL = 5//网络异常
    val NOT_SUBSCRIBE = 6//网络异常
    val RECEIVED = 7//领过了
}

/**
 * 连麦视图类型  PK  or   连麦
 */
object ConnectType {
    //pk
    const val PK = "PK"

    //连麦
    const val CONNECTMICRO = "CONNECTMICRO"
}

object BannerType {
    val NOMAL = "nomal"
    val PK = "pk"
    val YEAR_BANNER = "year_banner"
    val NEW_YEAR_BANNER = "new_year_banner"
}

object TaskStatus {
    /** 未完成 */
    const val NOTFINISH = "NotFinish"

    /** 待领取 */
    const val NOTRECEIVE = "NotReceive"

    /** 已领取 */
    const val RECEIVED = "Received"
}

object TaskAction {
    //发言
    const val Speak = "Speak"

    //充值
    const val Recharge = "Recharge"

    //分享
    const val Share = "Share"

    //送礼
    const val SendGift = "SendGift"

    //观看直播
    const val WatchShow = "WatchShow"

    //粉丝打卡
    const val FansCheckIn = "FansCheckIn"

    //每日转盘
    const val MoeCoinRotate = "MoeCoinRotate"

    //观看直播
    const val Room = "Room"

    //用户中心入口
    const val UserCenter = "UserCenter"

    //验证手机号
    const val Phone = "Phone"


}


/**
 * 回调类型
 */
object CallBackType {
    val SavePhoto = "save_photo"
    val DeleteVideo = "delete_video"
}

/**
 * 消息中心消息类型
 */
object MessageType {
    //displayType
    val MESSAGE_SM = "SystemMsg"//系统消息
    val MESSAGE_AM = "ActivityMsg"//活动消息
    val MESSAGE_DM = "DiscountMsg"//优惠消息
    val MESSAGE_MM = "ManageMsg"//运营消息

    //sysType
    val MESSAGE_BADGE = "GetBadge"//勋章消息
    val MESSAGE_VEHICLE = "GetCar"//座驾消息
    val MESSAGE_NUMBER = "GetPrettyNum"//靓号消息
    val MESSAGE_ROYAL = "RoyalDownWarning"//贵族消息
    val TEMP_ROYAL_AVAILABLE = "TempRoyalAvailable"//贵族消息
    val MESSAGE_GUARD = "GuardExpireWarning"//守护消息
    val MESSAGE_BEANS = "GetBeans"//萌豆消息
    val MESSAGE_GGOT = "GetGiftOrTicket"//获得礼物或者消费券
    val MESSAGE_GETEBUFF = "GetExpBuff"//获得用户经验值加X倍效果
    val MESSAGE_GETEBUFF_WARNING = "ExpBuffExpireWarning"//用户经验值加倍过期预警
    const val MESSAGE_NEW_MONTH = "NewMonthCard"//新月卡
    const val MESSAGE_GET_COUPON = "GetCoupon"//获取优惠券
    const val MESSAGE_GET_DISCOUNT = "GetDiscount"//获取折扣券
    const val MESSAGE_SHOW_RECHARGE = "ShowRecharge"//跳转充值中心
    const val MESSAGE_MINI = "Mini"//唤起小程序

    //touchType
    val MESSAGE_URL = "Url"//网页地址
    val MESSAGE_ROOM = "Room"//直播间
    val MESSAGE_RECHARGE = "Recharge"//充值首页
    val MESSAGE_HOME = "Home"//应用首页

}

object GiftGroupType {
    val Normal = "Normal"
    val Super = "Super"
}

object MessageDisplayType {
    //炫彩发言
    const val COLORFUL = "Colorful"

    //神秘人
    const val MYSTERY = "Mystery"

    //上神
    const val SSCOLORFUL = "SSColorful"

    //聊天模式
    const val CHATMODE = "ChatMode"
}

/***
 * 消息相关
 */
object MsgTag {
    const val PREFIX = "adbQy!0ClKMyWho"
    const val TAG = "lmlive"
}

/**
 * 周星展示类型
 */
object WeekType {
    const val WEEK = "week"
    const val REDPACKET = "RedPacket"
    const val HOTRANGE = "HotRange"
    const val RESCUE = "Rescue"

    //主播头条
    const val HEADLINE = "Headline"

    //抽奖
    const val DrawAct = "DrawAct"
}

//好友状态
object FriendStatus {
    //非好友
    const val None = "None"

    //等待加好友
    const val Wait = "Wait"

    //拉黑
    const val Black = "Black"

    //屏蔽
    const val Shield = "Shield"

    //正常
    const val Normal = "Normal"
}

//用户类型
object ChatUserType {
    //注册用户
    const val Viewer = "Viewer"

    //主播
    const val Anchor = "Anchor"

    //运管
    const val Manager = "Manager"

    //游客
    const val Visitor = "Visitor"

    //客服
    const val Customer = "Customer"
}

//好友申请 类型
object FriendRequestType {
    //请求成为好友
    const val REQUEST = "request"

    //已经接受
    const val ACCEPT = "accept"
}

/**
 * 粉丝团页面名称
 */
object FansTabName {
    const val FANS_TODAY = "FansToday"
    const val FANS_MEMBER = "FansMember"
    const val FANS_WEEK = "FansWeek"
    const val FANS_LAST_WEEK = "FansLastWeek"
}

/**
 * 名片弹出入口
 */
object CardEnterType {
    //直播间弹出
    const val CARD_TYPE_LIVE = 1

    //im弹出
    const val CARD_TYPE_IM = 2

    //个人主页弹出
    const val CARD_TYPE_HOME_PAGE = 0X0003
}

/**
 * 祝福卡榜类型
 */
object WishCardType {
    //祝福卡榜
    const val CARD_TYPE_NOMAL = 1

    //上神卡榜
    const val CARD_TYPE_ONGOD = 2

    //list类型
    //本周
    const val CARD_LIST_TYPE_WEEK = "本周"

    //本月
    const val CARD_LIST_TYPE_MONTH = "本月"

    //上月
    const val CARD_LIST_TYPE_LAST_MONTH = "上月"

}

/**
 * 粉丝团事件
 */
object FansEvent {
    //粉丝团打卡
    const val FANS_CLOCK_IN = 1

    //加入粉丝团
    const val FANS_JOIN = 2
}

/**
 * 礼物常量
 */
object GiftConstant {
    //礼物价值足够，且愿意在跑道显示
    const val ENOUGH_SHOW = "T"

    //礼物价值足够，不愿意在跑道显示
    const val ENOUGH_NOT_SHOW = "F"

    //礼物价值不够，不足以在跑道显示
    const val NOT_ENOUGH = "N"
}

/**
 * 礼物面板横幅类型
 */
object GiftBannerType {
    const val OPEN_WISH_VIEW = "OpenWishView"

    //打开星球霸主休闲模式
    const val PlanetCasual = "PlanetCasual"

    //打开星球霸主攻击模式
    const val PlanetAttack = "PlanetAttack"
    const val SEND_DANMU = "SendDanmu"
}


/**
 * 视频和动态相关内容的类型
 */
object SquareTypes {
    //广场
    const val SQUARE = "SQUARE"

    //个人主页
    const val USER_HOME_PAGE = "USER_HOME_PAGE"

    //用户本人
    const val USER = "USER"

    //互动消息
    const val INTERACTION = "INTERACTION"

    //单个视频
    const val SINGLE = "SINGLE"

    //动态类型
    const val TYPE_DYNAMIC = "dynamic"

    //关注类型
    const val TYPE_FOLLOW = "follow"

    //推荐类型
    const val TYPE_RECOMMEND = "recommend"
}

object SquareType {
    //视频
    const val VIDEO = "Video"

    //动态
    const val POST = "Post"
}

/**
 * 输入框的类型（聊天室 or  评论）
 */
object InputTextViewType {
    const val CHATROOM = "CHATROOM"
    const val COMMENT = "COMMENT"

    //输入弹窗
    const val INPUTDIALOG = "INPUTDIALOG"

    //动态详情弹窗
    const val COMMENT_DYNAMIC = "Comment_Dynamic"
}

/**
 * 评论操作的类型
 */
object CommentPraiseOperate {
    const val Cancel = "Cancel"
    const val Praise = "Praise"
}

/**
 * 评论输入框的类型
 */
object CommentInputType {
    //文字
    const val Text = "Text"

    //表情
    const val Emoji = "Emoji"
}

/**
 * 月卡状态
 */
object MonthCardType {
    //未验证手机号
    const val MC_CHECK_PHONE = 1

    //余额不足
    const val MC_CHECK_NO_BEANS = 2

    //不是贵族
    const val MC_CHECK_NO_ROYAL = 3

    //已购买月卡
    const val MC_CHECK_PURCHASED = 4

    //刷新页面
    const val MC_REFRESH = 5
    const val MC_HTML = "MC_HTML"
}

/**
 * 协议类型
 */
object AgreementType {
    //用户隐私协议
    const val UserPrivacy = "UserPrivacy"

    //倡议书
    const val SecretDeal = "SecretDeal"

    /** 实名认证协议 **/
    const val TYPE_USER_CERTIFICATION = "UserCertification"
}


/**
 * 客户端静态地址
 */
object NativeUrl {
    //月卡说明页
    const val MC_URL = "/rules/royalMonthlyCard.html"

    //锦鲤池玩法说明页
    const val WK_URL = "/rules/fishExplain.html"

    //祝福卡榜单说明页
    const val WC_URL = "/rules/blessingCardRules.html"

    //翻牌玩法说明页
    const val FC_URL = "/rules/welfareflop.html"

    //萌币任务说明
    const val MCOIN_URL = "/mcoin"

    //萌币幸运商城
    const val MCOIN_DETAIL_URL = "/rules/mcoinDetail.html"

    //主播等级进度说明
    const val ANCHOR_AWARD_URL = "/rules/expAward.html"

    //守护
    const val GUARD_URL = "/rules/guard.html"

    //周星
    const val WEEKSTAR_URL = "weekstar"

    //折扣券说明页
    const val DISCOOUNT_EXPLAIN = "/rules/discountCoupon.html"

    //签到说明页
    const val SIGNIN_EXPLAIN = "/rules/signInExplain.html"

    //转盘规则说明页
    const val LUCKY_TURNTABLE_EXPLAIN = "/rules/luckyBigTurntableExplain.html"

    //粉丝团说明页
    const val FANS_GROUP_URL = "/rules/fansGroup.html"

    //福利中心说明地址
//        const val WELFARE_INTRODUCE = "lm/user/welfare/welfare_introduce.png"
    //主播勋章墙
    const val MEDAL_WALL = "/medal"

    //段位PK规则页
    const val PK_RANK_URL = "/rules/pkLevelDetail.html"

}

/**
 * 协议匹配的部分字段
 */
object MatchString {
    //用户注册协议
    const val MATCH_URL_USER_REG = "protocolUserReg"

    //用户隐私协议
    const val MATCH_URL_USER_PRIVACY = "protocolUserPrivacy"

    //用户管理协议
    const val MATCH_URL_USER_MGR_SPEC = "userMgrSpec"

    //主播管理协议
    const val MATCH_URL_ANCHOR_MGR_SPEC = "anchorMgrSpec.html"

    //倡议书
    const val MATCH_OFFICIAL_PROPOSAL = "officialProposal.html"

    //羚萌
    const val MATCH_PG_LM = "huanque"

    //番茄
    const val MATCH_PG_LGMG = "lgmg"

    //黄瓜
    const val MATCH_PG_HG = "huanggua"

    //樱桃
    const val MATCH_PG_YT = "yingtao"

    //tag
    const val MATCH_TAG_LM = "lm"
    const val MATCH_TAG_FQ = "fq"
    const val MATCH_TAG_CJXFQ = "cjxfq"
    const val MATCH_TAG_HG = "hg"
    const val MATCH_TAG_DHHG = "dhhg"
    const val MATCH_TAG_HG_LIVE = "hg_live"
    const val MATCH_TAG_YT = "yt"

}

/**
 * 根据包名协议添加固定值
 */
object UrlValue {
    const val VALUE_LM = "jl_huanque"
    const val VALUE_JCX_LM = "cjx_huanque"
    const val VALUE_FQ = "cjx_fanqie"
    const val VALUE_CJX_FQ = "cjx_chengjuxingfanqie"
    const val VALUE_HG_VIDEO = "dhkl_huangguashipin"
    const val VALUE_HG_LIVE = "dhkl_huanggua"
    const val VALUE_DH_HG = "dhkl_dehuihuangguashipin"
    const val VALUE_YT = "dhkl_yingtao"
}

/**
 * 翻牌配置类型
 */
object FlipCardType {
    //核心
    const val TYPE_CORE = 1

    //提醒
    const val TYPE_EXPLAIN = 2

    //提醒2
    const val TYPE_EXPLAIN_TWO = 3

    //更多
    const val TYPE_MORE = 4

    //历史
    const val TYPE_HISTORY = 5

    //异常
    const val TYPE_ERROR = 6
}

/**
 * 福利中心配置类型
 */
object WelfareCenterType {
    const val TYPE_WELFARE_TASK = 1
    const val TYPE_LOGIN = "Login"
    const val TYPE_CENTER = "UserCenter"
    const val SIGNIN_INFO = "signin_info"
    const val REQUEST_TYPE = "request_type"
}

/**
 * 屏幕类型
 */
object ScreenType {
    //横屏
    const val HP = "HP"

    //竖屏
    const val SP = "SP"
}

/**
 * Activity的请求码
 */
object RequestCode {
    const val RELOAD_URL = 10001
    const val REAL_NAME_RESULT = 10002
}

/**
 * 新粉丝弹窗常量类
 */
object NewFansType {
    //粉丝榜单列表
    const val TYPE_FANS_RANK = 0

    //粉丝团成员
    const val TYPE_FANS_MEMBER = 1

    //小主回馈
    const val TYPE_FEEDBACK = 2

    //打赏
    const val TYPE_INTIMATE_REWARD = "SendGift"

    //打卡
    const val TYPE_INTIMATE_CLOCK_IN = "ClockIn"

    //观看
    const val TYPE_INTIMATE_WATCH = "WatchTime"

    //聊天
    const val TYPE_INTIMATE_CHAT = "ChatTimes"

    //分享
    const val TYPE_INTIMATE_SHARE = "ShareTimes"
}

/**
 * 人脸识别对应常量类型
 */
object RealNameType {
    //取消认证
    const val TYPE_CANCEL = "cancel"

    //网络异常
    const val TYPE_ERROR = "error"

    //认证成功
    const val TYPE_SUCCESS = "success"

    //认证失败
    const val TYPE_FAIL = "fail"
}

object GunLocationType {
    const val BOTTOM = "BOTTOM"

    const val LEFT = "LEFT"

    const val RIGHT = "RIGHT"

    const val MIDDLE = "MIDDLE"
}

/**
 * 武器类型
 */
object WeaponType {
    //爆能枪
    const val BNQ = "BNQ"

    //加农炮
    const val JNP = "JNP"

    //漩涡炮
    const val XWP = "XWP"

    //质子激光
    const val ZZJG = "ZZJG"
}

/**
 * 星球霸主  星球类型
 */
object PlanetType {
    //冰星
    const val IceStar = "IceStar"

    //死星
    const val DeathStar = "DeathStar"

    //主星
    const val MainStar = "MainStar"
}


/**
 * 星球霸主相关配置
 */
object PlanetConfig {
    //文件夹
    const val FOLDER = "planet"

    //文件名称
    val FILENAME = "planet.zip"

    //解压成功标志文件
    val SUCCESS = "success"

    //下载链接
    val URL = "resource/game/planet/animation_planet_6.zip"

    /*以下是音效*/
    //button音效
    const val BUTTON = "button.mp3"

    //质子激光音效
    const val SHOOT_ZZJG = "shoot_zzjg.mp3"

    //漩涡跑音效
    const val SHOOT_XWP = "shoot_xwp.mp3"

    //加农炮音效
    const val SHOOT_JNP = "shoot_jnp.mp3"

    //爆能枪音效
    const val SHOOT_BNQ = "shoot_bnq.mp3"

    //时间音效
    const val READY_TIME = "ready_time.mp3"

    //准备音效
    const val READY = "ready.mp3"

    //幸运星球爆炸音效
    const val PLANET_BOMB = "planet_bomb.mp3"

    //金币掉落
    const val AUDIO_GOLD = "audio_gold.mp3"

    /*以下是动画*/
    //休闲模式 主星动画
    const val STAR_MAIN_RELAXATION = "earth_normal.webp"

    //死星动画
    const val STAR_DEATH = "star_death.webp"

    //冰星动画
    const val STAR_ICE = "star_ice.webp"

    //攻击模式动画
    const val STAR_MAIN_ATTACK = "star_attack.webp"

    //幸运星球
    const val STAR_LUCKY = "star_lucky.webp"

    //幸运星球爆炸
    const val STAR_LUCKY_BOMB = "star_lucky_bomb.webp"

    //直播中效果
    const val PLANET_LIVING = "planet_living.webp"

    /*以下是图片资源*/
    //攻击模式没有霸主的背景
    const val AGGRESSIVENESS_NO_OWNER = "bg_aggressiveness_no_owner.png"

    //攻击模式下有霸主的背景
    const val BG_AGGRESSIVENESS_OWNER = "bg_aggressiveness_owner.png"
    const val BG_AWARD_CONTENT = "bg_award_content.png"
    const val BG_BAG_ATTACK = "bg_bag_attack.png"
    const val BG_BAG_RELAXATION = "bg_bag_relaxation.png"
    const val BG_AWARD_AWWISTANT = "bg_award_awwistant.png"
    const val BG_BEST_SCORE = "bg_best_score.png"
    const val BG_GOLD_AWARD_PAN = "bg_gold_award_pan.png"
    const val BG_GUN_SWITCH = "bg_gun_switch.png"
    const val BG_GUN_SWITCH_INSIDE_CIRCULAR = "bg_gun_switch_inside_circular.png"
    const val BG_GUN_SWITCH_OUTSIDE_CIRCULAR = "bg_gun_switch_outside_circular.png"
    const val BG_LIGHT_GUN = "bg_light_gun.png"
    const val BG_LUCKY_COUNT_DOWN = "bg_lucky_count_down.png"
    const val BG_MENGDOU_PLANET = "bg_mengdou_planet.png"
    const val BG_NUM_ZHUAN = "bg_num_zhuan.png"
    const val BG_PLANET = "bg_planet.png"
    const val BG_PLANET_ATTACK = "bg_planet_attack.png"
    const val BG_PLANET_AWARD_COUNT = "bg_planet_award_count.png"
    const val BG_PLANET_BIG_PRIZE = "bg_planet_big_prize.png"
    const val BG_PLANET_BTN_CANCEL = "bg_planet_btn_cancel.png"
    const val BG_PLANET_BTN_EXIT = "bg_planet_btn_exit.png"
    const val BG_PLANET_COUNT_DOWN = "bg_planet_count_down.png"
    const val BG_PLANET_CP = "bg_planet_cp.png"
    const val BG_PLANET_EXIT = "bg_planet_exit.png"
    const val BG_PLANET_LUCKY = "bg_planet_lucky.png"
    const val BG_PLANET_RELAXATION_AWARD = "bg_planet_relaxation_award.png"
    const val BG_PLANET_SCORE = "bg_planet_score.png"
    const val BG_PLANET_AWARD = "bg_planet_award.png"
    const val BG_PLANET_AWARD_SMALL = "bg_planet_award_small.png"
    const val BG_REAL_SCORE = "bg_real_score.png"
    const val BG_RECORD_ATTACK = "bg_record_attack.png"
    const val BG_RECORD_ATTACK_ARROW = "bg_record_attack_arrow.png"
    const val BG_RELAXATION_START = "bg_relaxation_start.png"
    const val BG_SCORE_LABEL = "bg_score_label.png"
    const val BOMB_BLASTER_ONE = "bomb_blaster_one.png"
    const val BOMB_BLASTER_TWO = "bomb_blaster_two.png"
    const val BOMB_JNP_FOUR = "bomb_jnp_four.png"
    const val BOMB_JNP_ONE = "bomb_jnp_one.png"
    const val BOMB_JNP_THREE = "bomb_jnp_three.png"
    const val BOMB_JNP_TWO = "bomb_jnp_two.png"
    const val BOMB_XWP_FOUR = "bomb_xwp_four.png"
    const val BOMB_XWP_ONE = "bomb_xwp_one.png"
    const val BOMB_XWP_THREE = "bomb_xwp_three.png"
    const val BOMB_XWP_TWO = "bomb_xwp_two.png"
    const val BOMB_ZZJG_FIVE = "bomb_zzjg_five.png"
    const val BOMB_ZZJG_FOUR = "bomb_zzjg_four.png"
    const val BOMB_ZZJG_ONE = "bomb_zzjg_one.png"
    const val BOMB_ZZJG_SIX = "bomb_zzjg_six.png"
    const val BOMB_ZZJG_THREE = "bomb_zzjg_three.png"
    const val BOMB_ZZJG_TWO = "bomb_zzjg_two.png"
    const val BULLET_BLASTER = "bullet_blaster.png"
    const val BULLET_JNP = "bullet_jnp.png"
    const val BULLET_XWP_FOUR = "bullet_xwp_four.png"
    const val BULLET_XWP_ONE = "bullet_xwp_one.png"
    const val BULLET_XWP_THREE = "bullet_xwp_three.png"
    const val BULLET_XWP_TWO = "bullet_xwp_two.png"
    const val BULLET_ZZJG_FIVE = "bullet_zzjg_five.png"
    const val BULLET_ZZJG_FOUR = "bullet_zzjg_four.png"
    const val BULLET_ZZJG_ONE = "bullet_zzjg_one.png"
    const val BULLET_ZZJG_SEVEN = "bullet_zzjg_seven.png"
    const val BULLET_ZZJG_SIX = "bullet_zzjg_six.png"
    const val BULLET_ZZJG_THREE = "bullet_zzjg_three.png"
    const val BULLET_ZZJG_TWO = "bullet_zzjg_two.png"
    const val CARD_BNQ = "card_bnq.png"
    const val CONTENT_AWARD = "content_award.png"
    const val CONTENT_AWARD_CHARGEING = "content_award_chargeing.png"
    const val COUNT_DOWN_ONE = "count_down_one.png"
    const val COUNT_DOWN_READY = "count_down_ready.png"
    const val COUNT_DOWN_THREE = "count_down_three.png"
    const val COUNT_DOWN_TWO = "count_down_two.png"
    const val FORWORD_NUM_ZHUAN = "forword_num_zhuan.png"
    const val ICON_ACC_BAG = "icon_acc_bag.png"
    const val ICON_AGGRESSIVENESS = "icon_aggressiveness.png"
    const val ICON_ARROW_GAME = "icon_arrow_game.png"
    const val ICON_ARROW_WHITE_PLANET = "icon_arrow_white_planet.png"
    const val ICON_AWARD_LIGHT = "icon_award_light.png"
    const val ICON_CLOSE_CONTENT = "icon_close_content.png"
    const val ICON_CLOSE_PLANET = "icon_close_planet.png"
    const val ICON_CLOSE_PLANET_BIG_PRIZE = "icon_close_planet_big_prize.png"
    const val ICON_CP_CONTENT = "icon_cp_content.png"
    const val ICON_DAMAGE = "icon_damage.png"
    const val ICON_GOLD_ADD = "icon_gold_add.png"
    const val ICON_GOLD_EIGHT = "icon_gold_eight.png"
    const val ICON_GOLD_FIVE = "icon_gold_five.png"
    const val ICON_GOLD_FORE = "icon_gold_fore.png"
    const val ICON_GOLD_NINE = "icon_gold_nine.png"
    const val ICON_GOLD_ONE = "icon_gold_one.png"
    const val ICON_GOLD_SEVEN = "icon_gold_seven.png"
    const val ICON_GOLD_SIX = "icon_gold_six.png"
    const val ICON_GOLD_THREE = "icon_gold_three.png"
    const val ICON_GOLD_TWO = "icon_gold_two.png"
    const val ICON_GOLD_ZERO = "icon_gold_zero.png"
    const val ICON_MENGDOU_PLANET = "icon_mengdou_planet.png"
    const val ICON_PLANET_BAG = "icon_planet_bag.png"
    const val ICON_PLANET_BOTTOM_LEFT = "icon_planet_bottom_left.png"
    const val ICON_PLANET_BOTTOM_RIGHT = "icon_planet_bottom_right.png"
    const val ICON_PLANET_GOLD_EIGHT = "icon_planet_gold_eight.png"
    const val ICON_PLANET_GOLD_FIVE = "icon_planet_gold_five.png"
    const val ICON_PLANET_GOLD_FORE = "icon_planet_gold_fore.png"
    const val ICON_PLANET_GOLD_NINE = "icon_planet_gold_nine.png"
    const val ICON_PLANET_GOLD_ONE = "icon_planet_gold_one.png"
    const val ICON_PLANET_GOLD_SEVEN = "icon_planet_gold_seven.png"
    const val ICON_PLANET_GOLD_SIX = "icon_planet_gold_six.png"
    const val ICON_PLANET_GOLD_TEN = "icon_planet_gold_ten.png"
    const val ICON_PLANET_GOLD_THREE = "icon_planet_gold_three.png"
    const val ICON_PLANET_GOLD_TWO = "icon_planet_gold_two.png"
    const val ICON_PLANET_LIST = "icon_planet_list.png"
    const val ICON_PLANET_LOVE = "icon_planet_love.png"
    const val ICON_PLANET_RULE = "icon_planet_rule.png"
    const val ICON_PLANET_TITLE = "icon_planet_title.png"
    const val ICON_PLANET_TOP_LEFT = "icon_planet_top_left.png"
    const val ICON_PLANET_TOP_RIGHT = "icon_planet_top_right.png"
    const val ICON_PLANET_TYPE_RECOMMEND = "icon_planet_type_recommend.png"
    const val ICON_PURPLE_ADD = "icon_purple_add.png"
    const val ICON_PURPLE_EIGHT = "icon_purple_eight.png"
    const val ICON_PURPLE_FIVE = "icon_purple_five.png"
    const val ICON_PURPLE_FORE = "icon_purple_fore.png"
    const val ICON_PURPLE_NINE = "icon_purple_nine.png"
    const val ICON_PURPLE_ONE = "icon_purple_one.png"
    const val ICON_PURPLE_SEVEN = "icon_purple_seven.png"
    const val ICON_PURPLE_SIX = "icon_purple_six.png"
    const val ICON_PURPLE_THREE = "icon_purple_three.png"
    const val ICON_PURPLE_TWO = "icon_purple_two.png"
    const val ICON_PURPLE_ZERO = "icon_purple_zero.png"
    const val ICON_SCORE = "icon_score.png"
    const val ICON_SILVER_ADD = "icon_silver_add.png"
    const val ICON_SILVER_EIGHT = "icon_silver_eight.png"
    const val ICON_SILVER_FIVE = "icon_silver_five.png"
    const val ICON_SILVER_FORE = "icon_silver_fore.png"
    const val ICON_SILVER_NINE = "icon_silver_nine.png"
    const val ICON_SILVER_ONE = "icon_silver_one.png"
    const val ICON_SILVER_SEVEN = "icon_silver_seven.png"
    const val ICON_SILVER_SIX = "icon_silver_six.png"
    const val ICON_SILVER_THREE = "icon_silver_three.png"
    const val ICON_SILVER_TWO = "icon_silver_two.png"
    const val ICON_SILVER_ZERO = "icon_silver_zero.png"
    const val ICON_SWEEP_LIGHT = "icon_sweep_light.png"
    const val PARTICAL_GO = "partical_go.png"
    const val PIC_ATTACK_TYPE = "pic_attack_type.png"
    const val PIC_CONGRATULATION_PLANET = "pic_congratulation_planet.png"
    const val PIC_LIGHT_GUN = "pic_light_gun.png"
    const val PIC_MARQUEE_1 = "pic_marquee_1.png"
    const val PIC_MARQUEE_2 = "pic_marquee_2.png"
    const val PIC_MARQUEE_3 = "pic_marquee_3.png"
    const val PIC_MARQUEE_4 = "pic_marquee_4.png"
    const val PIC_MARQUEE_5 = "pic_marquee_5.png"
    const val PIC_MARQUEE_6 = "pic_marquee_6.png"
    const val PIC_MARQUEE_7 = "pic_marquee_7.png"
    const val PIC_MARQUEE_8 = "pic_marquee_8.png"
    const val PIC_MARQUEE_9 = "pic_marquee_9.png"
    const val PIC_MARQUEE_10 = "pic_marquee_10.png"
    const val PIC_MARQUEE_11 = "pic_marquee_11.png"
    const val PIC_MARQUEE_12 = "pic_marquee_12.png"
    const val PIC_MARQUEE_13 = "pic_marquee_13.png"
    const val PIC_MARQUEE_14 = "pic_marquee_14.png"
    const val PIC_MARQUEE_15 = "pic_marquee_15.png"
    const val PIC_PLANET_CONFIRM = "pic_planet_confirm.png"
    const val PIC_RELAXATION_TYPE = "pic_relaxation_type.png"
    const val RELAXATION_ADD = "relaxation_add.png"
    const val RELAXATION_EIGHT = "relaxation_eight.png"
    const val RELAXATION_FIVE = "relaxation_five.png"
    const val RELAXATION_FORE = "relaxation_fore.png"

    const val RELAXATION_NINE = "relaxation_nine.png"
    const val RELAXATION_ONE = "relaxation_one.png"
    const val RELAXATION_SEVEN = "relaxation_seven.png"
    const val RELAXATION_SIX = "relaxation_six.png"
    const val RELAXATION_THREE = "relaxation_three.png"
    const val RELAXATION_TWO = "relaxation_two.png"
    const val RELAXATION_ZERO = "relaxation_zero.png"
    const val SWEEP_FIVE = "sweep_five.png"
    const val SWEEP_EIGHT = "sweep_eight.png"
    const val SWEEP_FORE = "sweep_fore.png"
    const val SWEEP_NINE = "sweep_nine.png"
    const val SWEEP_ONE = "sweep_one.png"
    const val SWEEP_SEVEN = "sweep_seven.png"
    const val SWEEP_SIX = "sweep_six.png"
    const val SWEEP_THREE = "sweep_three.png"
    const val SWEEP_TWO = "sweep_two.png"
    const val SWEEP_ZEGO = "sweep_zego.png"
    const val TEXT_DE = "text_de.png"
    const val TEXT_GONG = "text_gong.png"
    const val TEXT_HUO = "text_huo.png"
    const val TEXT_XI = "text_xi.png"
    const val WEAPON_BNQ = "weapon_bnq.png"
    const val WEAPON_JNP = "weapon_jnp.png"
    const val WEAPON_LIGHT = "weapon_light.png"
    const val WEAPON_XWP = "weapon_xwp.png"
    const val WEAPON_ZZJG = "weapon_zzjg.png"
    const val ZIDAN_LASER_FORE = "zidan_laser_fore.png"
    const val ZIDAN_LASER_ONE = "zidan_laser_one.png"
    const val ZIDAN_LASER_THREE = "zidan_laser_three.png"
    const val ZIDAN_LASER_TWO = "zidan_laser_two.png"
    const val BG_MAX_AWARD = "bg_max_award.png"
    const val ICON_AWARD_ASSISTANT = "icon_award_assistant.png"


}

/**
 * 音效的key
 */
object PlanetVoiceKey {
    //button音效
    const val BUTTON = "BUTTON"

    //质子激光音效
    const val SHOOT_ZZJG = "SHOOT_ZZJG"

    //漩涡跑音效
    const val SHOOT_XWP = "SHOOT_XWP"

    //加农炮音效
    const val SHOOT_JNP = "SHOOT_JNP"

    //爆能枪音效
    const val SHOOT_BNQ = "SHOOT_BNQ"

    //时间音效
    const val READY_TIME = "READY_TIME"

    //准备音效
    const val READY = "READY"

    //幸运星球爆炸音效
    const val PLANET_BOMB = "PLANET_BOMB"

    //金币掉落
    const val AUDIO_GOLD = "AUDIO_GOLD"
}


