package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * 系统消息
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
data class SysMsgContent(
    var context: SysMsgBean? = null,
    var eventCode: String = ""
) : Serializable

data class SysMsgBean(
    var subTitle: String = "",
    var touchValue: String = "",
    var touchType: String = "",
    var icon: String = "",
    var title: String = "",
    var body: String = ""
) : Serializable

/**
 * 好友通知消息
 * @author WanZhiYuan
 * @date 2020/07/15
 * @since 1.0.0
 */
data class FriendContent(
    var context: FriendBean? = null,
    var eventCode: String = ""
) : Serializable

/**
 * 点赞和评论消息
 */
data class ActionMessageContent(
    var context: SingleActionInMessage? = null,
    var eventCode: String = ""
) : Serializable

data class FriendBean(
    var friendHeadPic: String = "",
    var friendId: String = "",
    var friendNickname: String = "",
    var userId: String = "",
    //（好友关系变化类型）： Follow（关注），Friend（成为好友），IntimateFriend（成为密友），IntimateLevelUp（亲密度等级提升）
    var relationChangeType: String = "",
    // relationChangeType = IntimateLevelUp 时返回
    var intimateLevel: Int = 0
) : Serializable

/**
 * 用户消息设置
 * @author WanZhiYuan
 * @date 2020/07/15
 * @since 1.0.0
 */
data class MessageSettingBean(
    //接听语音
    var answer: Boolean = false,
    //折叠非好友 密友消息
    var foldMsg: Boolean = false,
    //关注提醒
    var followRemind: Boolean = false,
    //非好友私信费用
    var privateMsgFee: Int = 0,
    //私信推送通知提醒
    var privateMsgRemind: Boolean = false,
    //已设置开播提醒主播数量
    var showRemindCnt: Int = 0
) : Serializable