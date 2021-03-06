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
) : Serializable

/**
 * 系统消息刷新通知
 */
class SystemMessageRefreshBean(var targetId: String = "") : Serializable

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
 * 草稿变动事件
 */
class DraftEvent(val userId: Long)

/**
 * 打开私聊页
 */
class OpenPrivateChatRoomEvent(var userId: Long, var nickname: String, var headPic: String)

/**
 * 折叠陌生人消息变化
 */
class FoldStrangerMessageEvent()

/**
 * 用户数据变化通知
 * @param userId 变化的用户ID
 * @param stranger 陌生人状态
 * @param follow 关注状态
 */
//class UserInfoChangeEvent(
//    val userId: Long = 0L,
//    val stranger: Boolean = false,
//    val follow: String = ""
//) : Serializable {
//    constructor(userId: Long = 0L, stranger: Boolean = false) : this(userId, stranger, "")
//}


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
 * 视频播放完成事件
 */
class VideoResult()

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
class UnreadCountEvent(var unreadCount: Int, var player: Boolean)

class ImagePositionEvent(var position: Int)

/**
 * 关闭悬浮窗事件
 */
class HideFloatingEvent() : Serializable

/**
 * 收到封禁通知关闭直播间事件
 * @param programId 直播间ID 可以为null
 */
class BannedAndClosePlayer(var programId: Long? = null) : Serializable

/**
 * 一键登录接口请求成功消息
 */
class FastLoginEvent() : Serializable

/**
 * 关闭养鸟
 */
class HideBirdEvent() : Serializable

/**
 * 关闭养鸟引导领取
 */
class HideBirdAwardViewEvent() : Serializable

/**
 * 刷新语音券消息
 */
class RefreshVoiceCardEvent() : Serializable

/**
 * 悬浮窗关闭消息
 */
class FloatingCloseEvent() : Serializable

/**
 * 登录页面上层页面  finish事件
 */
class FinishToLoginEvent() : Serializable

/**
 * 动态相关分享成功事件
 */
class ShareSuccessEvent(var postId: Long = 0, var commentId: Long? = null) : Serializable

/**
 * 登录分身账号使用
 */
class LoginSubAccountEvent(var userId: Long) : Serializable

/**
 * 创建分身成功消息
 */
class CreateAccountSuccess() : Serializable

/**
 * 喜欢后发个通知
 */
class LikeEvent(var userId: Long, var like: Boolean)

/**
 * 图片切换地址
 */
class PicChangeEvent(
    //用户Id
    var userId: Long = 0L,
    //图片地址
    var imageUrl: String = "",
    //图片下标（主页中的下标）
    var imageIndexInHomePage: Int = -1
) : Serializable

/**
 * 图片可查看数目
 */
class CanSeeMaxCountChangeEvent(
    var seeMaxCoverNum: Int = -1
)