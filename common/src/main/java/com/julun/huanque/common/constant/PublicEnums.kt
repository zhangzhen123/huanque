package com.julun.huanque.common.constant

/**
 * Created by nirack on 16-11-24.
 */
/** 动画类型  */
class AnimationTypes {
    companion object {
        val NONE = "NONE"//默认值,啥都没有
        val LUCKY = "LUCKY"
        val SUPER_LUCK_GIFT = "SUPER_LUCK_GIFT"//超级幸运礼物
        val OPEN_GUARD = "OPEN_GUARD"//自定义的
        val USER_UPGRADE = "USER_UPGRADE"     //用户升级
        val ANCHOR_UPGRADE = "ANCHOR_UPGRADE"    //主播升级
        val PNG = "PNG"
        val GIF = "GIF"
        val SWF = "SWF"
    }
}

/** 动画资源类型 **/
object AnimEventItemTypes {
    val OTHERS = "OTHERS" //其他的自定义的东西,客户端定义,不是从后台传来
    val none = "none"
    val playSeconds = "playSeconds"
    val width = "width"
    val height = "height"
    val top = "top"
    val left = "left"
    val GIF = "GIF"
    val PNG = "PNG"
    val WEBP: String = "Webp"
    val SVGA: String = "Svga"

}

/** 消息平台  */
object MessageCfgPlatform {
    val WEB = "WEB"
    val IOS = "IOS"
    val ANDROID = "ANDROID"
}

object LoginType {
    //游客
    val VISITOR = "VISITOR"
    // SESSION登陆
    val LOGIN_SESSION = "LOGIN_SESSION"
    // 本站登陆
    val LOGIN_NORMAL = "LOGIN_NORMAL"
    /**靓号登录 */
    val LOGIN_BY_BEAUTY_NUM = "LOGIN_BY_BEAUTY_NUM"
    //第三方登陆已绑定
    val THIRD_BINDED = "THIRD_BINDED"
}

/**
 * 直播间用户列表变化时通知类型
 */
object UserChangeType {
    val New = "New"
    val Del = "Del"
    val Mod = "Mod"
}

object EnterType {
    const val MINI = "Mini" //普通入场
    const val SKILL = "Skill" //特效入场
}

object StopType {
    const val NORMALSTOP = "NormalStop"
    const val FORCESTOP = "ForceStop"
}

//首页标签类型
object TagType {
    const val HOT = "Hot"//热门
    const val RECOMMEMD = "Recom"//推荐
}

//公聊消息样式
object PublicCustomType {
    const val TreasureBox = "TreasureBox"//抢宝箱样式
    //打开幸运星球
    const val RoomLuckyPlanet = "RoomLuckyPlanet"
    //后续扩展
}

object MenuActionType {
    const val MOECOINTASK = "MoeCoinTask"//萌币任务
    const val KINGWAR = "KingWar"//王者之战
    const val LUCKYROTATE = "LuckyRotate"//幸运转盘
    const val MOECOINMARKET = "MoeCoinMarket"//萌币商城
    const val FACTORYCAR = "FactoryCar"//豪车工厂
    const val TURNCARD = "TurnCard"//翻牌游戏
    const val TACTGAMEYY = "TActGameYY"//邀友活动

    const val PASSLEVEL = "PassLevel"//携手闯关
    const val STAGEPK = "StagePk"//PK段位赛
    const val CHATMODE = "ChatMode"//聊天模式
}

object RoyalLevelType {
    const val LEVEL1 = 1
    const val LEVEL2 = 2
    const val LEVEL3 = 3
    const val LEVEL4 = 4
    const val LEVEL5 = 5
    const val LEVEL6 = 6
    const val LEVEL7 = 7
    const val LEVEL8 = 8
    const val LEVEL9 = 9
}

object PayType {
    //微信支付标志
    const val WXPayApp = "WXPayApp"
    //阿里支付标志
    const val AlipayApp = "AlipayApp"
    //OPPO支付
    const val OppoPay = "OppoPay"
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

/**
 * 抽奖条件
 */
object LottoCondition {
    //要求送礼物
    const val SEND_GIFT = "SendGift"
    //抽奖要求加入粉丝团
    const val FANS_TEAM = "FansTeam"
    //抽奖要求关注
    const val FOLLOW = "Follow"
    //抽奖要求弹幕
    const val DANMU = "DanMu"
    //发言
    const val PubMsg = "PubMsg"
}

/**
 * 抽奖状态
 *
 * Success("S", "抽奖成功"),
 * Progress("P", "抽奖中"),
 * Failed("F", "抽奖失败"),
 * Show("S", "抽奖结束,显示状态");
 *
 */
object LottoStatus {
    const val Follow = "Follow"//关注
    const val Success = "Success"
    const val Progress = "Progress"
    const val Failed = "Failed"
    const val Show = "Show"
}

object LottoAwardType {
    const val Bean = "Bean"
    const val Gift = "Gift"
}

/**
 * 进入直播间用户来源
 */
object LiveFromType {
    const val Follow = "Follow"//关注
    const val Guard = "Guard"//守护
    const val Manager = "Manager"//管理
    const val Watched = "Watched"//看过
    const val ViewerCard = "ViewerCard"//用户卡片
    const val Social = "Social"//广场
    const val ThemeRoom = "ThemeRoom"//从主题房跳转到上麦房时传该参数，玩家进入主题房fromType传其它的
}

/**
 * 跳转类型（GiftList：礼物列表、TmpRoyal：贵族体验卡、MoeCoinTask：萌币任务）
 */
object ReturnSkipType {
    const val GiftList = "GiftList"
    const val TmpRoyal = "TmpRoyal"
    const val MoeCoinTask = "MoeCoinTask"
}

/**
 * eventBus事件总线的code值
 */
object EventBusCode {
    //刷新用户
    const val RefreshUserInfo = 100
    //关注变化
    const val SubscribeChange = 101
    //打开私聊
    const val openPrivate = 102
    //刷新礼物面板
    const val RefreshGift = 104
}

/**
 * 任务类型
 */
object TaskType {
    //成长任务
    const val Growth = "Growth"
    //新手任务
    const val NewUser = "NewUser"
    //日常任务
    const val Daily = "Daily"
    //成就任务
    const val Achievement = "Achievement"
    //自动打开的情况 默认定位到Daily
    const val Auto = "Auto"
}

/**
 * TaskFragment的使用地方区分
 */
object TaskFragmentPlaceType {

    //用户中心
    const val UserCenter = 1
    //直播间
    const val Live = 2
}

object TaskBoxId {
    //周宝箱第一个
    const val Box5 = 5
    //周宝箱第二个
    const val Box6 = 6
    //周宝箱第三个
    const val Box7 = 7
}

/**
 * 成就分类
 */
object TaskAchievementType {
    //连击
    const val Combo = "Combo"
    //活跃
    const val Active = "Active"
    //签到
    const val Sign = "Sign"
    //财富
    const val Wealth = "Wealth"
    //榜单
    const val Rank = "Rank"

    //连充
    const val Repeat = "Repeat"

    //充值
    const val Recharge = "Recharge"

    //分享好友
    const val Friends = "Friends"
}

/**
 * 上个页面来源
 */
object Source {
    //来源我的贵族入口
    const val SOURCE_USER_ROYAL = "user_royal"
    const val SOURCE_USER_ROYAL_HOT = "user_royal_hot"
}

/**
 * 成就分类
 */
object PushSetCode {
    //点赞通知
    const val PraiseMsg = "PraiseMsg"
    //评论通知
    const val CommentMsg = "CommentMsg"
    //活动通知
    const val ActivityMsg = "ActivityMsg"
    //好友通知
    const val FriendMsg = "FriendMsg"


    //本地参数 开播提醒
    const val LivingNotify = "LivingNotify"

}

/**
 * 隐私权限list
 * @author WanZhiYuan
 */
object PrivacyEnums {
    //位置
    const val PRIVACY_ADDRESS = "address"
    //通知
    const val PRIVACY_NOTIFICATION = "notification"
    //照相机
    const val PRIVATE_CAMERA = "camera"
    //文件
    const val PRIVATE_FLIE = "file"
    //麦克风
    const val PRIVACY_MIKE = "mike"
}

/**
 * activity codes
 * @author WanZhiYuan
 */
object ActivityCodes {
    //响应码
    //刷新页面
    const val RESPONSE_CODE_REFRESH = 10100
    //传递实例对象
    const val RESPONSE_CODE_BEAN = 10200

    //请求码
    //通用基础请求码
    const val REQUEST_CODE_NORMAL = 10000
    const val REQUEST_CODE_PHOTO = 11000
    const val REQUEST_CODE_VIDEO = 12000
}

/**
 * 页面标识位
 * @author  WanZhiYuan
 */
object TabTags {
    //在线
    const val TAB_TAG_ONLINE = "online"
    //贵族
    const val TAB_TAG_ROYAL = "royal"
    //守护
    const val TAB_TAG_GUARD = "guard"
    //管理
    const val TAB_TAG_MANAGER = "manager"
}

/**
 * 弹窗类型
 * @author WanZhiYuan
 */
object DialogTypes {
    //在线贵族列表弹窗
    const val DIALOG_ROYAL = "royal"
    //普通在线列表弹窗
    const val DIALOG_USER = "user"
    //在线守护列表弹窗
    const val DIALOG_GUARD = "guard"
//    //粉丝团弹窗
//    const val DIALOG_FANS = "fans"
//    //pk段位赛
//    const val DIALOG_PK_RANK = "pk_rank"
//    //翻牌
//    const val DIALOG_FLIP_CARD = "flip_card"
//    //锦鲤池
//    const val DIALOG_WISH_KOI = "wish_koi"
//    //宝箱
//    const val DIALOG_CHEST = "chest"
//    //萌新礼包
//    const val DIALOG_NEW_USER = "new_user"
//    //快捷充值
//    const val DIALOG_RECHARGE = "recharge"
//    //携手闯情关
//    const val DIALOG_PASSTHROUGH = "PassThrough"
//    //一元首充
//    const val DIALOG_ONE_YUAN = "one_yuan"
//    //回归礼包
//    const val DIALOG_RETURN = "return"
//    //引导关注
//    const val DIALOG_GUIDE_FOLLOW = "guide_follow"
//    //名片
//    const val DIALOG_BUSINESS_CARD = "business_card"
//    //转盘
//    const val DIALOG_LUCKYPAN = "luckypan"
//    //游戏
////    const val DIALOG_GAME = "game"
//    //福利中心
//    const val DIALOG_WELFARE = "welfare"
//    //推荐
//    const val DIALOG_RECOMMEND = "recommend"
//    //警告主播
//    const val DIALOG_WARN_AUTHOR = "warn_Author"
//    //豪车工厂
//    const val DIALOG_LUXURY_CAR = "luxury_car"
//    //私聊
//    const val DIALOG_PRIVATE = "private"
//    //关注成功
//    const val DIALOG_FOLLOW_SUCCESS = "follow_success"
//    //玩家升级
//    const val DIALOG_GAMER_LEVEL_UP = "game_level_up"
//    //推荐主播
//    const val DIALOG_RECOMMEND_PROGRAM = "recommend_program"
//    //巡查提醒
//    const val DIALOG_REMIND = "remind"
//    //警告
//    const val DIALOG_WARNING = "warning"
//    //抽奖
//    const val DIALOG_LOTTO = "lotto"
//    //抽奖结果
//    const val DIALOG_LOTTO_RESULT = "lotto_result"
//    //宝箱
//    const val DIALOG_TREASURE_BOX= "treasure_box"
//    //停播
//    const val DIALOG_CLOSE_PROGRAM = "close_program"
//    //横屏设置
//    const val DIALOG_HORIZONTAL_SETTING = "h_setting"
//    //竖屏设置
//    const val DIALOG_VERTICAL_SETTING = "v_setting"
//    //pk猜猜
//    const val DIALOG_PK_GUESS = "pk_guess"
//    //幸运守护
//    const val DIALOG_LUCKY_GUARD = "lucky_guard"
//    //PK道具
//    const val DIALOG_PK_PROP = "pk_prop"
//    //签到弹窗
//    const val DIALOG_SIGN_IN = "DIALOG_SIGN_IN"
//    //星球霸主
////    const val DIALOG_PlANET_GAME = "planet_game"
//
//    //特权弹窗
//    const val DIALOG_PREROGATIVE = "prerogative"
}

/**
 * 页面类型
 * @author WanZhiYuan
 */
object PageTypes {
    //广场 -> 动态
    const val Dynamic = "dynamic"
    //动态图片详情
    const val DynamicPhoto = "dynamic_photo"
    //视频详情
    const val DynamicVideo = "dynamic_video"
    const val AnchorVideo = "anchor_video"
    const val LiveRoom = "live_room"
    const val H5 = "h5"
}

/**
 * 操作符
 * @author WanZhiYuan
 */
enum class Operators {
    REFRESH,
    //    OPEN,
    CLOSE
}

/**
 * 其他常量
 * @author WanZhiYuan
 */
object OtherConstant {
    const val APP_EXPERIENCE_GUARD = "AppExperienceGuard"
    const val CLOSE_TOAST = "CloseToast"
    const val CLOSE_KNOW = "ClickKnow"
    //adapter刷新标记
    const val ADAPTER_CHANGE_TAG = 2
}

/**
 * 我的卡券常量类
 * @author WanZhiYuan
 */
object UserCardsTypes {
    //礼物折扣券
    const val USER_CARD_GIFT_TICKET = "gift_ticket"
    //优惠券
    const val USER_CARD_COUPON_TICKET = "coupon_ticket"
    //充值优惠券
    const val USER_CARD_RECHARGE_COUPON_TICKET = "recharge_coupon_ticket"
    //优惠券类型
    const val USER_CARD_TYPE_TICKET = 1
    //月卡
    const val USER_CARD_TYPE_MONTH = 2
    //热度或体验 等等
    const val USER_CARD_TYPE_NORMAL = 3
}

/**
 * 羚萌月卡常量类
 * @author WanZhiYuan
 */
object UserMonthCardTypes {
    //每日福利特权
    const val USER_MONTH_CARD_EVERYDAY = "everyday"
    //充值补领特权
    const val USER_MONTH_CARD_REPLACE = "replace"
    //专属认证特权
    const val USER_MONTH_CARD_AUTHENTICATION = "authentication"
    //抽折扣券特权
    const val USER_MONTH_CARD_LOTTO = "lotto"
    //红包优先特权
    const val USER_MONTH_CARD_REDPACKET = "redpacket"

    //每日福利
    const val USER_MONTH_TYPE_WELFARE = "type_welfare"
    //补领
    const val USER_MONTH_TYPE_REPLACE = "type_replace"
    //抽奖
    const val USER_MONTH_TYPE_LOTTO = "type_lotto"
    //开通
    const val USER_MONTH_TYPE_DREDGE = "type_dredge"
    //续费
    const val USER_MONTH_TYPE_RECHARGE = "type_recharge"

}

/**
 * 接口异常错误码
 * @author WanZhiYuan
 */
object ResponseErrorCodes {
    //月卡服务接口异常
    const val ERROR_CODE_MONTH = 1052
    //手机号未验证
    const val ERROR_CODE_MOBLIE = 1050
    //手机号已被绑定
    const val ERROR_CODE_USED_MOBLIE = 1051
    //萌币商城刷新通知错误码
    const val ERROR_CODE_STORE_GOODS_REFRESH = 1801
}

/**
 * 我的钱包常量类型
 * @author WanZhiYuan
 * @since 4.27
 */
object UserWalletTypes {
    //福利中心
    const val USER_WALLET_TYPE_WELFARE = 1
    //月卡
    const val USER_WALLET_TYPE_MONTH = 2
    //商城
    const val USER_WALLET_TYPE_STORE = 3
    //转盘
    const val USER_WALLET_TYPE_TURNPLATE = 4


}

/**
 * 商城常量类
 */
object UserStoreConstants {
    //倒计时刷新
    const val REFRESH_TIME_OUT = "time_out"
    //跳转礼物面板
    const val JUMP_TYPE_GIFT = "Gift"
    //跳转我的座驾
    const val JUMP_TYPE_CAR = "Car"
    //跳转我的勋章
    const val JUMP_TYPE_BADGE = "Badge"
    //跳转我的等级
    const val JUMP_TYPE_MYLEVEL = "MyLevel"
}

/**
 * 直播间enter -> action
 * @author WanZhiYuan
 * @since 4.27
 */
object LiveActions {
    //打开幸运转盘
    const val ACTION_OPEN_LUCKY_ROTATE = "OpenLuckyRotate"
    //打开礼物面板
    const val ACTION_OPEN_GIFT_PANEL = "OpenGiftPanel"
    //打开粉丝团
    const val ACTION_OPEN_FANS = "OpenFansGroup"
    //打开守护弹窗
    const val ACTION_OPEN_GUARD = "OpenGuardPanel"
}

/**
 * 直播间 from typs 标识位
 * @author WanZhiYuan
 * @since 4.27
 */
object FromTypeConstants {
    //我的钱包转盘item
    const val FROM_TYPE_LUCKY_ROTATE = "MyWalletLuckyRotate"
    //萌币商城
    const val FROM_TYPE_STORE = "MoeCoinMarketGift"
    //粉丝团
    const val FROM_TYPE_FANS = "PrivilegeCenterFans"
    //守护
    const val FROM_TYPE_GUARD = "PrivilegeCenterGuard"
}

/**
 * 来自页面
 * @author WanZhiYuan
 * @since 4.27
 */
object FromPager {
    val FROM_SQUARE = "from_square"
    val NOMAL = "nomal"
    val GOODNUMBER_PAGE: String = "goodNumberPage"
    val ANCHOR_VIDEO: String = "anchorVideoPage"
    val ANCHOR_DYNAMIC: String = "anchorDynamicPage"
}

/**
 * 配置的byte常量
 */
object ByteConstants {
    const val BYTE_KB_FORM_128 = 128
}

/**
 * 分享常量类
 */
object ShareConstants {
    const val SHARE_WX = "wx"
    const val SHARE_WX_TEXT = "微信"
    const val SHARE_WX_FRIENDS = "wx_friends"
    const val SHARE_WX_FRIENDS_TEXT = "朋友圈"
    const val SHARE_QQ = "qq"
    const val SHARE_QQ_TEXT = "QQ"
    const val SHARE_QQ_ZONE = "qq_zone"
    const val SHARE_QQ_ZONE_TEXT = "QQ空间"
    const val SHARE_COPY = "copy"
    const val SHARE_COPY_TEXT = "复制链接"

}

object AppDialogType {
    const val RoyalUpDialog = "RoyalUpDialog"
    const val RoyalUpFirstDialog = "RoyalUpFirstDialog"
}

object DialogOrderNumber {
    //全局弹窗的排序
    const val PRIVACY_AGREEMENT_FRAGMENT = 1 //隐私协议
    const val LAW_FRAGMENT = 2 //禁止交易
    const val OPERATION_FRAGMENT = 3 //运营弹窗
    const val MONTH_AWARD_FRAGMENT = 4 //月卡每日奖励
    const val SIGN_IN_DIALOG_FRAGMENT = 5 //签到

    const val ROYAL_LEVEL_UP_DIALOG = 6 //贵族升级无奖励的

    const val ROYAL_LEVEL_UP_FIRST_DIALOG = 7 //贵族升级有奖励的

    const val ACHIEVEMENT_GOT_DIALOG = 8 //成就达成弹窗

    const val ROYAL_CARD_ACTIVE_FRAGMENT = 9 //贵族体验卡弹窗


    //局部弹窗的排序  目前不需要优先级排序 默认就排到尾端 这里设置一个固定大值
    const val LOCAL_DIALOG = 9999 //
    //后续如果局部弹窗需要排序 从1000开始定义
}

/**
 * 图片本地地址
 * @author WanZhiYuan
 * @date 2020/04/22
 */
object ImageUrl {
    //首充弹窗规则说明页
    const val ONE_YUAN_EXPLAIN = "lm/activity/usergrow/firstRechareExplain.png"
}

object OpenOperaType {
    const val OPEN_GIFT = "open_gift"//打开礼物面板并附带giftId 默认0代表只打开 -1代表不生效
    const val OPEN_SHARE = "open_share"//打开分享
    const val STAR_GAME = "starGame"//打开星球游戏
}

/***
 *
 * 直播间功能类型 分为普通观看端  主播推流端  主题房
 *
 */
//object LiveRoomType {
//    const val NORMAL = 0//正常直播(观众观看端)
//    const val PUBLISH = 1//推流直播(主播推流端)
//    const val THEME = 3//主题房直播
//}


/**
 * 主题房节目状态
 */
object ThemeShowType {
    const val Waiting = "Waiting"//
    const val Showing = "Showing"//
    const val Ended = "Ended"//

    const val STOP = "Stop"//停播状态
}

/**
 * 主题房领取状态
 */
object ThemeTreasureStatus {
    const val NotFinish = "NotFinish"//未完成
    const val NotReceive = "NotReceive"//待领取
}
