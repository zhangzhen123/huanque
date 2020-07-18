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

}

/**
 * 参数key
 */
object ParamKey {
    //type
    const val TYPE = "TYPE"

    //USER
    const val USER = "USER"

    //语音会话被叫key
    const val CallReceiveBean = "CallReceiveBean"

}

/**
 * SP使用的key
 */
object SPParamKey {
    //语音通话花费确认弹窗
    const val VOICE_FEE_DIALOG_SHOW = "VOICE_FEE_DIALOG_SHOW"

    //已经进入过私聊详情页面的标识
    const val JOINED_PRIVATE_CHAT = "JOINED_PRIVATE_CHAT"

    //折叠陌生人消息
    const val FOLD_STRANGER_MSG = "FOLD_STRANGER_MSG"
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

    //非法字符
    const val MESSAGENOTPERMIT = 501

    //创建连麦异常
    const val CREATE_MICRO_FAIL = 501
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

    //已取消
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

    //模拟语音通话消息
    const val Voice_Conmmunication_Simulate = "Voice_Conmmunication_Simulate"

    //模拟系统消息
    const val SYSTEM_MESSAGE = "SYSTEM_MESSAGE"

    //模拟好友消息
    const val FRIEND_MESSAGE = "FRIEND_MESSAGE"
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

    //高级表情
    const val HIGH = "HIGH"
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
}

/**
 * 更新类型
 */
object UpdateType {
    //强制更新
    val Force = "Force"

    //推荐更新
    val Recommend = "Recommend"

    //无更新
    val None = "None"
}