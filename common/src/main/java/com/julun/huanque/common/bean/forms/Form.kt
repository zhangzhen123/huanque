package com.julun.huanque.common.bean.forms

import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/7/6 16:06
 *@描述 用于存储请求的form
 */

//空的form
class EmptyForm()

class FriendIdForm(var friendId: Long)

class TargetIdForm(var targetUserId: Long)

class SendChatGiftForm(var friendId: Long, var chatGiftId: Int, var count: Int)

/**
 * 创建语音通话的form
 */
class CreateCommunicationForm(var friendId: Long, var confirmed: String = "True")

class NetcallIdForm(var callId: Long)

class NetcallCancelForm(var callId: Long, var cancelType: String)


/**
 * 挂断通话的form
 */
class NetcallHangUpForm(var callId: Long = 0, var duration: Long = 0) : Serializable

/**
 * 发送消息 form
 * @param friendId 发送的好友ID
 * @param content 消息内容
 */
class SendMsgForm(var friendId: Long = 0, var content: String = "")

class ReportForm {
    // 举报用户
    var userId: Long = 0
    var reportType: String = ""
    var detail: String = ""
    var pics: String? = null
}


/**
 * 联系人使用的form
 */
class ContactsForm(var userDataTabType: String = "", var offset: Int = 0)

/**
 * 更新用户消息使用的form
 */
class SettingForm(
    //非好友私信费用
    var privateMsgFee: Int? = null,
    //折叠非好友 密友消息
    var foldMsg: String? = null,
    //接听语音
    var answer: String? = null,
    //私信提醒
    var privateMsgRemind: String? = null,
    //关注提醒
    var followRemind: String? = null
)

/**
 * 节目关注列表使用的form
 */
class LiveRemindForm(
    var offset: Int? = null,
    var pushOpen: String? = null,
    var programId: Int? = null
)

class FindNewsForm {
    var startAdsVersion: String? = null
}

class BindForm(var code:String)
class BindPhoneForm(var code:String,var mobile:String)
class WithdrawApplyForm(var tplId:Int,var type:String)

class WithdrawHistoryForm ( var lastId: Long?=null)

/**
 * 校验nickname使用
 */
class NicknameForm(var nickname: String)
