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
object BusiConstant{
    //用户创建聊天室的前缀
    const val USER_CHAT_ROOM_PREFIX = "U"
    val API_KEY = "2FsdGVkX19"
    const val WEIXIN_FLAG = "weixin://wap/pay?"
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


