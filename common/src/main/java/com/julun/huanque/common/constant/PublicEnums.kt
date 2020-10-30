package com.julun.huanque.common.constant

/**
 * Created by nirack on 16-11-24.
 */

object SPType {
    const val COMMON_SP = "common_sp"//使用最频繁的
}

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
    const val OTHERS = "OTHERS" //其他的自定义的东西,客户端定义,不是从后台传来
    const val none = "none"
    const val playSeconds = "playSeconds"
    const val width = "width"
    const val height = "height"
    const val top = "top"
    const val left = "left"
    const val GIF = "GIF"
    const val PNG = "PNG"
    const val WEBP: String = "Webp"
    const val SVGA: String = "Svga"

}

//公聊消息的点击触发
object TextTouch {

    //打开游戏面板
    const val OpenGameView = "OpenGameView"

    //切换直播间
    const val OpenLiveRoom = "OpenLiveRoom"

    //打开h5
    const val OpenUrlView = "OpenUrlView"

    //打开卡片
    const val OpenUserCard = "OpenUserCard"

    //打开送礼面板
    const val OpenSendGiftView = "OpenSendGiftView"

    //打开分享
    const val OpenShareView = "OpenShareView"

    //打开幸运转盘
    const val OPEN_BIG_WHEEL = "OpenBigWheel"

    //打开PK段位赛 since 4.23.0
    const val OPEN_PK_RANK = "OpenStagePk"

    //打开礼物面板并切换到背包
    const val OPEN_BAG = "OpenBag"

    //打开养鹊
    const val Magpie = "Magpie"

    //打开H5页面
    const val OpenUrlPage = "OpenUrlPage"

    //打开贵族页面
    const val OpenRoyalPage = "OpenRoyalPage"

    //打开盲盒礼物规则页面
    const val BlindRulePage = "BlindRulePage"
}

object EnterType {
    const val MINI = "Mini" //普通入场
    const val SKILL = "Skill" //特效入场
}

object StopType {
    const val NORMALSTOP = "NormalStop"
    const val FORCESTOP = "ForceStop"
}

/**
 * 直播间用户列表变化时通知类型
 */
object UserChangeType {
    const val New = "New"
    const val Del = "Del"
    const val Mod = "Mod"
}

/** 消息平台  */
object MessageCfgPlatform {
    val WEB = "WEB"
    val IOS = "IOS"
    val ANDROID = "ANDROID"
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

//游戏类型
object GameType {

    //星球霸主
    const val Planet = "Planet"

    //游戏类型
    const val GAME = "Game"

    //锦鲤
    const val TYPE_GAME_KOI = 1

    //猜猜
    const val TYPE_GAME_GUESS = 2

    //翻牌
    const val TYPE_GAME_CARDS = 3

    //抽奖
    const val TYPE_GAME_LOTTO = 4

    //首充
    const val TYPE_FIRST_RECHARGE = 5

    //PK道具
    const val TYPE_PK_PROP = 6

    //宝箱
    const val TYPE_GAME_TREASURE_BOX = 7

    //主题房宝箱
    const val TYPE_GAME_THEME_TREASURE_BOX = 8
}

object ClickType {
    //打开公聊
    const val CHAT_INPUT_BOX = "CHAT_INPUT_BOX"

    //打开私聊
    const val PRIVATE_MESSAGE = "PRIVATE_MESSAGE"

    //打开送礼
    const val GIFT = "GIFT"

    //关闭直播间
    const val CLOSE = "CLOSE"

    //打开游戏面板
    const val GAME = "GAME"

    //星球霸主
    const val PLANTE = "PLANTE"

    //养鹊
    const val BIRD = "BIRD"

    //推流设置页面
    const val PUBLISH_SETTING = "PUBLISH_SETTING"

    //主播更多设置
    const val ANCHOR_MORE_SETTING = "ANCHOR_MORE_SETTING"

    //用户更多设置
    const val USER_MORE_SETTING = "USER_MORE_SETTING"

    //打开粉丝团
    const val FANS = "FANS"

    //横竖屏切换
    const val SWITCH_SCREEN = "SWITCH_SCREEN"

    //屏蔽功能
    const val SHIELD = "SHIELD"

    //主播端功能
    //摄像头切换
    const val SWITH_CAMERA = "SWITH_CAMERA"

    //闪光灯
    const val FLASH_LIGHT = "FLASH_LIGHT"

    //镜像
    const val MIRROR = "MIRROR"

    //声音开关
    const val SOUND = "SOUND"

    //主播更多设置
    const val ANHOR_MORE_SETTING = "ANHOR_MORE_SETTING"

    //打开聊天输入框  发言
    const val SEND_MESSAGE = "SEND_MESSAGE"

    //首充
    const val FIRST_RECHARGE = "FIRST_RECHARGE"
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
 * 直播间打开时附带的操作
 */
object LiveOperaType {
    const val OPEN_GIFT = "open_gift"//打开礼物面板并附带giftId 默认0代表只打开 -1代表不生效
    const val OPEN_SHARE = "open_share"//打开分享
    const val STAR_GAME = "starGame"//打开星球游戏

    //打开粉丝团
    const val OPEN_FANS = "OPEN_FANS"

    //打开守护
    const val OPEN_GUARD = "OPEN_GUARD"

    //打开首充
    const val OPEN_FIRST_RECHARGE = "OPEN_FIRST_RECHARGE"
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
    const val LOCAL_DIALOG_PRIORITY_01 = 1000
}

/**
 * 实名认证常量类
 */
object RealNameConstants {
    const val TYPE_NAME = "RealName"
    const val TYPE_HEAD = "RealHead"

    //取消认证
    const val TYPE_CANCEL = "cancel"

    //网络 or 接口反馈 异常
    const val TYPE_ERROR = "error"

    //认证成功
    const val TYPE_SUCCESS = "success"

    //认证失败
    const val TYPE_FAIL = "fail"
}

/**
 * 配置的byte常量
 */
object ByteConstants {
    const val BYTE_KB_FORM_128 = 128
}

/**
 * 消息常量类
 */
object MessageConstants {
    //跳转H5
    const val ACTION_URL = "URL"

    //跳转主页
    const val ACTION_MAIN_PAGE = "main_page"

    //跳转官方认证页面 RN
    const val OfficialCertPage = "OfficialCertPage"

    //跳转主播认证页面 RN
    const val AnchorCertPage = "AnchorCertPage"

    //跳转我的资料编辑页面
    const val EditMineHomePage = "EditMineHomePage"

    //跳转贵族页面
    const val RoyalPage = "RoyalPage"

    //跳转我的主页
    const val MineHomePage = "MineHomePage"

    //跳转私聊
    const val ACTION_MESSAGE = "打开私聊"

    //什么都不做
    const val ACTION_None = "None"

    //好友类型
    //关注
    const val FRIEDN_FOLLOW = "Follow"

    //成为好友
    const val FRIEDN = "Friend"

    //成为密友
    const val FRIEDN_INTIMATE = "IntimateFriend"

    //亲密度等级提升
    const val FRIEDN_LEVEL_UP = "IntimateLevelUp"

    //花魁榜
    const val PlumFlower = "PlumFlower"

    //搭讪常用语页面
    const val AccostWords = "AccostWords"

    //缘分来了页面
    const val FateCome = "FateCome"

    //消息页面
    const val Message = "Message"

    //交友页面
    const val FriendHome = "FriendHome"

}

/**
 * activity codes
 */
object ActivityCodes {
    //响应码
    //刷新
    const val RESPONSE_CODE_REFRESH = 20000

    //请求码
    //通用基础请求码
    const val REQUEST_CODE_NORMAL = 10000
}

/**
 * 手机登录还是绑定
 */
object PhoneLoginType {
    const val TYPE_LOGIN = 0
    const val TYPE_BIND = 1
}

/**
 * 分享类型
 */
object ShareTypeEnum {
    const val WeChat = "WeChat"
    const val FriendCircle = "FriendCircle"
    const val Sina = "Sina"
    const val ShareImage = "ShareImage"
    const val SaveImage = "SaveImage"
}

/**
 * 	分享到对话:
SendMessageToWX.Req.WXSceneSession
分享到朋友圈:
SendMessageToWX.Req.WXSceneTimeline
分享到收藏:
SendMessageToWX.Req.WXSceneFavorite

 */
object ShareWayEnum {
    //分享到对话
    const val WXSceneSession = "WXSceneSession"

    //分享到朋友圈
    const val WXSceneTimeline = "WXSceneTimeline"

    //分享到收藏
    const val WXSceneFavorite = "WXSceneFavorite"

    const val Sina = "Sina"

    const val SaveImage = "SaveImage"
}

object WeiXinShareType {
    //文字类型分享
    const val WXText = "WXText"

    //图片类型分享
    const val WXImage = "WXImage"

    //音乐类型分享
    const val WXMusic = "WXMusic"

    //视频类型分享
    const val WXVideo = "WXVideo"

    //网页链接
    const val WXWeb = "WXWeb"
}

object WeiBoShareType {
    //文字类型分享
    const val WbText = "WbText"

    //图片类型分享
    const val WbImage = "WbImage"

    //多图分享
    const val WbMultiImage = "WbMultiImage"

    //音乐类型分享
    const val WbMusic = "WbMusic"

    //视频类型分享
    const val WbVideo = "WbVideo"

    //网页
    const val WbWeb = "WbWeb"
}

/**
 * 分享的模块分类
 */
object ShareFromModule {
    //    适用模块，Invite：邀友，Program：直播间，Magpie：养鹊(可选项：Invite、Program、Magpie)
    const val Invite = "Invite"
    const val Program = "Program"
    const val Magpie = "Magpie"
}

/**
 * 用户和主播权限字段
 */
object CardManagerOperate {
    //用户
    /** 踢除 **/
    const val MANAGER_CONTENT_KICK: String = "kickUser"

    /** 屏蔽 **/
    const val PAGE_NAME_SHIELD: String = "shieldUser"

    /** 禁言 **/
    const val PAGE_NAME_GAG: String = "gagUser"

    /** 全平台禁言 **/
    const val PAGE_NAME_PLATFORM_GAG: String = "platformGagUser"

    /** 全站封禁 **/
    const val PAGE_NAME_PLATFORM_BLOCK: String = "blockUser"

    /** 取消房管 **/
    const val PAGE_NAME_CANCEL_MANAGER: String = "cancelRoomManager"

    /** 提升房管 **/
    const val PAGE_NAME_UP_MANAGER: String = "upToRoomManager"

    //主播
    /** 封禁 **/
    const val PAGE_NAME_BLOCK_PROGRAM: String = "blockProgram"

    /** 监控 **/
    const val PAGE_NAME_MONITOR_PROGRAM: String = "monitorProgram"

    /** 警告 **/
    const val PAGE_NAME_WARN_ANCHOR: String = "warnAnchor"

    /** 巡查提醒 **/
    const val PAGE_NAME_REMIND_ANCHOR: String = "remindAnchor"

    /** 取消监控 **/
    const val PAGE_NAME_UNMONITOR_PROGRAM: String = "unMonitorProgram"
}

object ImageActivityOperate {
    const val REPORT: String = "report"
    const val SAVE: String = "save"
}

object ImageActivityFrom {
    const val CHAT: String = "chat"
    const val HOME: String = "home"
    const val RN: String = "rn"
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

object HomeMakeMoneyType {
    const val Invite = "Invite"
    const val Game = "Game"
    const val Task = "Task"
}

object BirdTaskJump {
    const val LiveRoom = "LiveRoom"
    const val InviteFriend = "InviteFriend"
    const val FriendHome = "FriendHome"
    const val Message = "Message"
    const val AnonyVoiceCall = "AnonyVoiceCall"

}

object BirdTaskStatus {
    const val NotReceive = "NotReceive"
    const val NotFinish = "NotFinish"
    const val Received = "Received"

}

object BirdTaskAwardType {
    const val Small = "Small"
    const val Middle = "Middle"
    const val Big = "Big"
}

object MainPageIndexConst {
    const val MAIN_FRAGMENT_INDEX = 0
    const val PROGRAM_FRAGMENT_INDEX = 1
    const val MESSAGE_FRAGMENT_INDEX = 2
    const val MINE_FRAGMENT_INDEX = 3
}

object PushDataActionType {
    //这个类型测试用
    const val EditMineHomePage = "EditMineHomePage"
    const val LiveRoom = "LiveRoom"
    const val Url = "Url"//打开指定url
    const val PrivateChat = "PrivateChat"//定位到私信
    const val OfficialCertPage = "OfficialCertPage"//跳转到官方认证页面

    const val AnchorCertPage = "AnchorCertPage"//跳转到主播认证页面
    const val MineHomePage = "MineHomePage"//跳转到我的主页
    const val SystemNotice = "SystemNotice" //（打开系统通知页面）
    const val FriendNotice = "FriendNotice"// (打开好友通知页面)

    //Yesterday：昨日榜单Tab，
    //Today:今日榜单Tab（暂无这样的跳转需求），
    //Famous: 名人榜单Tab
    const val PlumFlower = "PlumFlower"//（跳转到今日花魁）

    const val AccostWords = "AccostWords"//定位到搭讪常用语页面

    //缘分来了
    const val FateCome = "FateCome"

    //消息列表
    const val Message = "Message"

    //交友页面
    const val FriendHome = "FriendHome"
}

object HomePageOnlineStatus {
    const val Online = "Online"
    const val Offline = "Offline"
}

/**
 * 关闭流的类型
 */
object StopAllStreamState {
    //初始状态 不做任何操作
    const val Nothing = 0

    //1代表常规关闭
    const val StopNormal = 1

    //2代表关闭所有（常规和单例播放器）
    const val StopAll = 2

    //3代表关闭所有 同时单例播放器断开视图连接
    const val StopAllWithDisConnect = 3
}