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

class ReportForm{
    // 举报用户
    var userId: Long = 0
    var reportType: String =""
    var detail: String =""
    var pics: String? = null
}
