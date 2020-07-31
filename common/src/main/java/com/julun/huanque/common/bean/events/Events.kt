package com.julun.huanque.common.bean.events

import com.julun.huanque.common.constant.PayResult
import java.io.Serializable

/**
 * 接收到私聊消息使用
 * @param targetId 目标ID
 * @param stranger 陌生人状态
 */
class EventMessageBean(var targetId: String = "", var stranger: Boolean = false) : Serializable

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
 * 阿里授权code
 */
class AliAuthCodeEvent(val code: String)

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

/**
 * 实名认证结果的广播
 */
class RPVerifyResult(val result: String)

/**
 * 语音签名修改成功的广播
 */
class VoiceSignEvent()

/**
 * 融云连接上的事件
 */
class RongConnectEvent(var state: String)

/**
 * 动画播放完后的回调
 */
class AnimatorEvent()