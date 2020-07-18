package com.julun.huanque.common.bean.events

import com.julun.huanque.common.constant.PayResult
import java.io.Serializable

/**
 * 接收到私聊消息使用
 */
class EventMessageBean(var tardetId: String = "") : Serializable

/**
 * 信息填写完成事件
 * @param sextype 性别
 * @param nickname 昵称
 * @param birthday 生日
 * @param headerPic 头像
 */
class ImformationCompleteBean(var sextype: String = "", var nickname: String = "", var birthday: String = "", var headerPic: String = "")

/**
 * 支付结果
 */
class PayResultEvent(var payResult: PayResult, var payType: String)

/**
 * 登录事件true代表登录 false代表退出
 */
class LoginEvent(val result: Boolean)

/**
 * 背景变化通知
 */
class ChatBackgroundChangedEvent(var friendId: Long)

/**
 * 微信登录code
 */
class WeiXinCodeEvent(val code: String)

/**
 * 消息免打扰状态变化通知
 */
class MessageBlockEvent()

/**
 * 折叠陌生人消息变化
 */
class FoldStrangerMessageEvent()

/**
 * 用户数据变化通知
 * @param userId 变化的用户ID
 * @param stranger 陌生人状态
 */
class UserInfoChangeEvent(val userId: Long, val stranger: Boolean)