package com.julun.huanque.common.constant

/**
 * Created by nirack on 16-11-24.
 */

object SPType{
    val COMMON_SP = "common_sp"//使用最频繁的
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
