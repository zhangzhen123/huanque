package com.julun.huanque.common.bean.events

import com.julun.huanque.common.constant.PayResult
import java.io.Serializable

/**
 * 接收到私聊消息使用
 * @param targetId 目标ID
 * @param stranger 陌生人状态
 * @param onlyRefreshUnReadCount 忽略陌生人状态（需要从其他地方获取）
 */
class EventMessageBean(
    var targetId: String = "",
    var stranger: Boolean = false,
    var onlyRefreshUnReadCount: Boolean = false
) :
    Serializable

/**
 * 信息填写完成事件
 * @param sextype 性别
 * @param nickname 昵称
 * @param birthday 生日
 * @param headerPic 头像
 */
class ImformationCompleteBean(
    var sextype: String = "",
    var nickname: String = "",
    var birthday: String = "",
    var headerPic: String = ""
)

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
 * 打开私聊页
 */
class OpenPrivateChatRoomEvent(var userId: Long, var nickname: String)

/**
 * 折叠陌生人消息变化
 */
class FoldStrangerMessageEvent()

/**
 * 用户数据变化通知
 * @param userId 变化的用户ID
 * @param stranger 陌生人状态
 */
class UserInfoChangeEvent(val userId: Long = 0L, val stranger: Boolean = false)


class UserInfoEditEvent(
    val userId: Long = 0L,
    val stranger: Boolean? = null,
    val nickname: String? = null,
    val headPic: String? = null,
    val picList: ArrayList<String>? = null
)

/**
 * 实名认证结果的广播
 */
class RPVerifyResult(val result: String)

/**
 * 头像认证结果的广播
 */
class RHVerifyResult(val result: String)

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

/**
 * 视频页面进入与关闭的Event
 * @param start true 表示进入视频播放页面，false 表示退出视频播放页面
 */
class VideoPlayerEvent(var start: Boolean) : Serializable

/**
 * 退出登录消息
 * @alter WanZhiYuan
 * @since 1.0.0
 * @date 2020/08/06
 */
class LoginOutEvent() : Serializable

/**
 * 给Rn发消息
 */
class SendRNEvent(val action: String, var map: HashMap<String, Any>? = null)

/**
 * 提现成功
 */
class WithdrawSuccessEvent(val cash: String)

class BindPhoneSuccessEvent()

/**
 * 查询消息未读数的事件
 * @param player 是否获取直播间中的未读数 true 为直播间  false  未首页
 */
class QueryUnreadCountEvent(var player: Boolean)

/**
 * 消息未读数事件
 * @param unreadCount 消息未读数
 * @param player 是否是直播间
 */
class UnreadCountEvent(var unreadCount: Int,var  player: Boolean)