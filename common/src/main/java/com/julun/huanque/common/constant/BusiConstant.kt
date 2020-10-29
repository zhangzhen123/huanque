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
    const val API_KEY = "2FbdGVkX29"
    const val WEIXIN_FLAG = "weixin://wap/pay?"
    const val REQUEST_HEADER_FLAG = "hqInfo"

    //系统通知栏回调
    var NOTIFICATION_REQUEST_CODE = 300

    const val WEB_URL = "web_url"
    const val ROOM_AD = "ROOM_AD"

    //True
    const val True = "True"

    //False
    const val False = "False"

    //首页图片方格大小
    const val OSS_120 = "!120"

    //所有头像统一裁剪这么大
    const val OSS_160 = "!160"

    //广场封面大小
    const val OSS_350 = "!350"

    const val DESTROY_ACCOUNT_RESULT_CODE = 502 //注销账号result_code

    const val EMPTY_GIFT = -100
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
 * SP使用的key
 */
object SPParamKey {
    //语音通话花费确认弹窗
    const val VOICE_FEE_DIALOG_SHOW = "VOICE_FEE_DIALOG_SHOW"

    //消息通话花费确认弹窗
    const val MESSAGE_FEE_DIALOG_SHOW = "MESSAGE_FEE_DIALOG_SHOW"

    //已经进入过私聊详情页面的标识
    const val JOINED_PRIVATE_CHAT = "JOINED_PRIVATE_CHAT"

    //折叠陌生人消息
    const val FOLD_STRANGER_MSG = "FOLD_STRANGER_MSG"

    //正在语音通话中标识
    const val VOICE_ON_LINE = "VOICE_ON_LINE"

    //异常消息列表
    const val EXCEPTION_MESSAGE_LIST = "EXCEPTION_MESSAGE_LIST"

    //悬浮窗内的ProgramId
    const val PROGRAM_ID_IN_FLOATING = "PROGRAM_ID_IN_FLOATING"

    //客服地址
    const val CUSTOMER_URL = "CUSTOMER_URL"

    //私信聊天框设置
    const val PRIVATE_CHAT_BUBBLE = "PRIVATE_CHAT_BUBBLE"

    //欢迎页有没有显示过隐私弹窗
    const val Welcome_privacy_Fragment = "Welcome_privacy_Fragment"

    //激活接口调用标记位
    const val APP_START = "APP_START"

    //直播间关闭，悬浮窗是否显示标识位
    const val Player_Close_Floating_Show = "Player_Close_Floating_Show"

    //手机关闭悬浮窗标记位
    const val First_Close_Floating = "First_Close_Floating"

    //是否同意隐私协议
    const val AgreeUp = "AgreeUp"

    //是否请求过猜你喜欢
    const val QueryGuessYouLike = "QueryGuessYouLike"

    //oaid是否支持
    const val Support_Oaid = "Support_Oaid"

    //缘分来了铃声是否开启
    const val Fate_Voice_Open = "Fate_Voice_Open"

    //缘分未回复数量
    const val Fate_No_Reply_Count = "Fate_No_Reply_Count"
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

    //零钱不足
    const val CASH_NOT_ENOUGH = 1006

    //创建连麦异常
    const val CREATE_MICRO_FAIL = 501

    //PK相关异常，需要toast
    const val PK_ERROR = 1011

    //PK相关异常，需要toast
    const val PK_ERROR2 = 1014

    //资料完成度100%且封面相册超过3张才能认证主播
    const val NOT_INFO_COMPLETE = 1128

    //完成微信绑定才能认证主播
    const val NOT_BIND_WECHAT = 1129

    //完成实名认证才能认证主播
    const val NOT_REAL_NAME = 1130

    //已经绑定其他账号
    const val HAS_BIND_OTHER = 1016

    //账号已经被封禁
    const val ACCOUNT_HAS_BLOCK = 1018

    //头像认证出错
    const val REAL_HEAD_ERROR = 1301


}

/**
 * 充值入口： 直播间快捷充值Room， 个人中心充值My， 公众号菜单充值MPMenu(可选项：Room、My、Cms)
 */
object RechargeEntry {
    val Room = "Room"//直播间快捷充值
    val My = "My"//个人中心充值
    val CallBack = "CallBack"//回归礼包
}

/**
 * 支付方式
 */
object PayType {
    const val AliPayApp = "AlipayApp"
    const val WXPayApp = "WXPayApp"

    const val WXPayWap = "WXPayWap"
    const val OppoPay = "OppoPay"
    const val AliPayWap = "AlipayWap"
    const val WXPayH5 = "WXPayH5"


}

/**
 * 提现方式
 */
object WithdrawType {
    const val AliWithdraw = "Alipay"
    const val WXWithdraw = "Weixin"

}

object WithdrawCode {
    const val Money1 = 7
    const val Money20 = 8
    const val Money50 = 9
    const val Money100 = 10
    const val Money300 = 11
    const val Money500 = 12
}

/**
 * 提现方式
 */
object WithdrawErrorCode {
    //未绑定手机
    const val NO_BIND_PHONE = 1004

    //未实名认证
    const val NO_VERIFIED = 1005

}

enum class PayResult {
    IS_PAY,//去支付了 但是不知道结果的情况
    PAY_SUCCESS,
    PAY_CANCEL,
    PAY_FAIL,

    //重复请求
    PAY_REPETITION
}

enum class PlatformType {
    IOS,
    ANDROID
}

object Sex {
    const val MALE = "Male"
    const val FEMALE = "Female"
    const val UNKOWN = "Unknow"

    fun getSexText(str: String): String {
        return when (str) {
            MALE -> {
                "男"
            }
            FEMALE -> {
                "女"
            }
            UNKOWN -> {
                "未知"
            }
            else -> ""
        }

    }

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


object AnchorSearch {
    //类型参数
    val TYPE = "TYPE"

    //连麦的type以及回传数据的key
    val CONNECT_MICRO = "CONNECT_MICRO"
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

//SVGA内置动画
object RunWaySVGAType {
    //SVGA动画  暂时只支持飞机和飞艇
    //飞机
    val AIRPLANE = "svga/airplane.svga"

    //飞艇
    val AIRSHIP = "svga/airship.svga"

    //加载动画
    val LOADING = "svga/loading.svga"
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

/***
 * 消息相关
 */
object MsgTag {
    const val PREFIX = "adbQy!0ClKMyWho"
    const val TAG = "lmlive"
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
object UserType {
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

/**
 * 协议相关的code
 */
object XYCode {
    //用户隐私协议
    const val YHYSXY = "YHYSXY"
}

/**
 * 欢遇状态
 */
object MeetStatus {
    //待欢遇
    const val Wait = "Wait"

    //欢遇中
    const val Meet = "Meet"

    //不可欢遇
    const val Not = "Not"
}

/**
 * 关注状态
 */
object FollowStatus {
    //已关注
    const val True = "True"

    //未关注
    const val False = "False"

    //互相关注
    const val Mutual = "Mutual"
}


object ContactsTabType {
    //关注
    const val Follow = "Follow"

    //粉丝
    const val Fan = "Fan"

    //密友
    const val Intimate = "Intimate"

    //谁看过我
    const val Visit = "Visit"

    //好友
    const val Friend = "Friend"
}

/**
 * 会话用户类型（主叫还是被叫）
 */
object ConmmunicationUserType {
    //主叫
    const val CALLING = "CALLING"

    //被叫
    const val CALLED = "CALLED"
}

/**
 * 取消类型
 */
object CancelType {
    //手动取消
    const val Normal = "Normal"

    //超时
    const val Timeout = "Timeout"
}

/**
 * 语音会话结果状态
 */
object VoiceResultType {
    //已接通，正常结束
    const val CONMMUNICATION_FINISH = "CONMMUNICATION_FINISH"

    //对方已拒绝
    const val RECEIVE_REFUSE = "RECEIVE_REFUSE"

    //被叫拒绝通话（被叫方使用）
    const val MINE_REFUSE = "MINE_REFUSE"

    //对方未应答
    const val RECEIVE_NOT_ACCEPT = "RECEIVE_NOT_ACCEPT"

    //对方忙
    const val RECEIVE_BUSY = "RECEIVE_BUSY"

    //主叫取消通话
    const val CANCEL = "CANCEL"

}

/**
 * 私聊  自定义消息 type
 */
object MessageCustomBeanType {
    //礼物消息
    const val Gift = "Gift"

    //特权表情
    const val Expression_Privilege = "Expression_Privilege"

    //动画表情
    const val Expression_Animation = "Expression_Animation"

    //模拟语音通话消息
    const val Voice_Conmmunication_Simulate = "Voice_Conmmunication_Simulate"

    //模拟系统消息
    const val SYSTEM_MESSAGE = "SYSTEM_MESSAGE"

    //模拟好友消息
    const val FRIEND_MESSAGE = "FRIEND_MESSAGE"

    //传送门消息
    const val SendRoom = "sendRoom"

    //创建会话风险提示
    const val PrivateChatRiskWarning = "PrivateChatRiskWarning"
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
    val Room = "LiveRoom"

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

    //邀友
    const val InviteFriend = "InviteFriend"

    //打开星球霸主
    const val OpenLiveRoomAndPlanet = "OpenLiveRoomAndPlanet"
}

/**
 * 操作类型
 */
object OperationType {
    //打开送礼面板
    const val OPEN_GIFT = "OPEN_GIFT"

    //拨打电话
    const val CALL_PHONE = "CALL_PHONE"

}

object SystemTargetId {
    //鹊友通知
    const val friendNoticeSender = "friendNoticeSender"

    //系统通知
    const val systemNoticeSender = "systemNoticeSender"
}

//表情类型
object EmojiType {
    //普通表情
    const val NORMAL = "NORMAL"

    //特权表情
    const val PREROGATIVE = "PREROGATIVE"

    //动画表情
    const val ANIMATION = "ANIMATION"
}

/**
 * 操作类型
 */
object MineToolType {
    const val Office = "Office"
    const val RoomSpecial = "RoomSpecial"
    const val VisitHistory = "VisitHistory"
    const val ChatBubble = "ChatBubble"
    const val InviteFriend = "InviteFriend"
    const val ToMaster = "ToMaster"
    const val ToAnchor = "ToAnchor"
}

/**
 * 更新类型
 */
object UpdateType {
    //强制更新
    const val Force = "Force"

    //推荐更新
    const val Recommend = "Recommend"

    //无更新
    const val None = "None"
}

//    Processing：打款中 Success：已到账 Failure：打款失败
object WithdrawStatus {
    const val Processing = "Processing"
    const val Success = "Success"
    const val Failure = "Failure"
}

//消息错误类型
object MessageFailType {
    //融云
    const val RONG_CLOUD = "RONG_CLOUD"

    //服务端
    const val WEB = "WEB"
}

//猜拳结果
object FingerGuessingResult {
    //石头
    const val ROCK = "rock"

    //布
    const val PAPER = "paper"

    //剪刀
    const val SCISSORS = "scissors"
}

object BooleanType {
    const val TRUE = "True"
    const val FALSE = "False"
}

/**
 * PK结果类型
 */
object PKResultType {
    val WIN = "Win"//
    val DRAW = "Draw"//
    val LOSE = "Lose" //
}

/**
 * PK类型
 */
object PKType {
    const val PK = "Normal"//普通PK
    const val MONTH_PK = "YUESAI"//月赛
    const val DYNAMIC = "DYNAMIC" // 动态配置
    const val WORLDCUP = "WORLDCUP"//世界杯
    const val LANDLORD = "Landlord"//斗地主
}

/**
 * 直播间背景切换
 */
object LiveBgType {
    const val NORMAL = 0//初始状态
    const val PK_TWO = 1//二人PK
    const val PK_THREE = 2//三人PK
    const val PK_LANDLORD = 3//斗地主
}

/**
 * 礼物任务状态：Doing：进行中Finished：已完成Fail：任务失败
 */
object PKTaskStatus {
    const val Doing = "Doing"
    const val Finished = "Finished"
    const val Fail = "Fail"
}

/**
 * 道具状态Competing：抢夺中Fail：抢夺失败UnUse：待使用Effect：生效中Used：已使用
 */
object PKPropStatus {
    const val Competing = "Competing"
    const val Fail = "Fail"
    const val UnUse = "UnUse"
    const val Effect = "Effect"
    const val Used = "Used"
}

/**
 * Blood：加血瓶Smoke：烟雾罐
 */
object PKPropType {
    const val Blood = "Blood"//大血瓶2倍
    const val Smoke = "Smoke"
    const val SmallBlood = "SmallBlood"//小血瓶1.5倍
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

object MessageBanner {
    //活动显示
    const val PopupShow = "PopupShow"

    //活动隐藏
    const val PopupHide = "PopupHide"
}

/**
 * 敏感词校验结果
 */
object ValidateResult {
    //通过
    val PASS = "Pass"

    //仅自己可见
    val ONLY = "Only"

    //发送替换后的文本(非法字符等)
    val RESEND = "Resend"

    //暂时未使用
    val ALERT = "Alert"
}

/**直播间标签类型**/
object ProgramTagType {
    //文本标签  颜色渐变
    val TEXT = "text:"

    //图片标签   直接显示图片
    val IMG = "img:"
}

/**
 * 送礼面板物品类型
 */
object ProdType {
    //礼物
    const val Gift = "Gift"

    //道具
    const val Goods = "Goods"
}

/**
 * 进入直播间的来源
 */
object PlayerFrom {
    //首页
    const val Home = "Home"

    //广场
    const val Social = "Social"

    //关注列表
    const val Follow = "Follow"

    //守护列表
    const val Guard = "Guard"

    //管理列表
    const val Manager = "Manager"

    //看过列表
    const val Watched = "Watched"

    //玩家卡片
    const val ViewerCard = "ViewerCard"

    //悬浮窗
    const val FloatWindow = "FloatWindow"

    //猜你喜欢
    const val GuessYouLike = "GuessYouLike"

    //传送门
    const val SendRoom = "SendRoom"

    //花魁榜
    const val FlowerRank = "FlowerRank"

    const val RN = "RN"
    const val DESTROY_ACCOUNT = "destroyAccount"

    const val Push = "Push"

    //养鹊
    const val Magpie = "Magpie"
}

/**
 * 需要悬浮窗权限的跳转类型
 */
object PermissionJumpType {
    //私聊详情
    const val PrivateChat = "PrivateChat"

    //联系人页面
    const val Contacts = "Contacts"
}

object RNMessageConst {

    /**
     * 给RN发送的消息类型
     */
    //密友关系发送变化param：{friendId: number,intimateLevel:number}
    const val IntimateFriendChange: String = "IntimateFriendChange"

    //关注用户变化param:{userId: number, isFollowed: boolean}
    const val FollowUserChange: String = "FollowUserChange"
}

object MetaKey {
    const val DOWNLOAD_CHANNEL = "UMENG_CHANNEL"//渠道链接  2018/2/26修改本地渠道链接标识DOWNLOAD_CHANNEL为UMENG_CHANNEL
}