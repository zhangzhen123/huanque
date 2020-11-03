package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.bean.ChatUser
import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/7/6 16:08
 *@描述  存放联系人相关数据
 */

/**
 * 联系人顶部数据
 */
class UserDataTab(
    //条目标识
    var userDataTabType: String = "",
    //条目名称
    var userTabName: String = "",
    //数据条数
    var count: Long = 0,
    //新增数量
    var addCount: Int = 0
)

/**
 * 联系人数据
 */
class SocialUserInfo(
    //用户ID
    var userId: Long = 0,
    //用户昵称
    var nickname: String = "",
    //头像
    var headPic: String = "",
    //欢遇状态
    var meetStatus: String = "",
    //亲密度等级
    var intimateLevel: Int = 0,
    //亲密值
    var num: Long = 0,
    //变化的亲密值
    var changeNum: Long = 0,
    //签名
    var mySign: String = "",
    //关注类型
    var follow: String = "",
    //性别
    var sex: String = ""
)

/**
 * 联系人列表数据
 */
class SocialListBean(
    //顶部列表数据
    var userDataTabList: MutableList<UserDataTab> = mutableListOf(),
    //当前数据的type
    var userDataTabType: String = "",
    //列表数据
    var linkList: MutableList<SocialUserInfo> = mutableListOf(),
    //资料完善度
    var perfection: Int = 0,
    //是否有更多数据
    var hasMore: Boolean = false,

    var officialUserId: Long? = null
)

/**
 * 关注结果使用的Bean
 */
class FollowResultBean(
    var type: String = "",
    var userId: Long = 0L,
    var follow: String = "",
    var formerFollow: String = ""
)

/**
 * 亲密特权
 * @param icon 点亮的图标
 * @param grayIcon 未点亮图标
 * @param key 对应的key
 * @param minLevel 特权对应的最小亲密度等级
 * @param title 标题
 * @param attentionContent 提醒文案，特权弹窗中显示
 * @param detailContent 详情弹窗里面显示的文案
 */
class IntimatePrivilege(
    var title: String = "",
    var icon: String = "",
    var grayIcon: String = "",
    var privilegeExplainPic: String = "",
    var key: String = "",
    var minLevel: Int = 0,
    var attentionContent: String = "",
    var detailContent: String = ""
) : Serializable

/**
 * 亲密数据
 */
class IntimateBean(
    //亲密度等级
    var intimateLevel: Int = 0,
    //下一级亲密等级
    var nextIntimateLevel: Int = 0,
    //亲密度值
    var intimateNum: Int = 0,
    //下一个等级所需亲密度值
    var nextIntimateNum: Int = 0,
    //用户ID数据
    var userIds: LongArray = longArrayOf(),
    //是否是陌生人状态
    var stranger: HashMap<Long, Boolean> = hashMapOf(),
    //消息免费标识
    var msgFree: Boolean = false
)

/**
 * 私聊道具
 */
class PrivateProp(
    //道具数量
    var count: Int = 0,
    var goodsId: Long = 0,
    //道具名称
    var goodsName: String = "",
    //道具图片
    var goodsPic: String = ""
) : Serializable

/**
 * 会话详情页面基础信息
 */
class ConversationBasicBean(
    //本人数据
    var usr: ChatUser = ChatUser(),
    //对方数据
    var friendUser: ChatUser = ChatUser(),
    //亲密度信息
    var intimate: IntimateBean = IntimateBean(),
    //与对方欢遇标识
    var meetStatus: String = "",
    //账户余额
    var beans: Long = 0,
    //私信费用
    var msgFee: Long = 0,
    //语音费用
    var voiceFee: Int = 0,
    //对方是否接受语音通话
    var answer: Boolean = false,
    //是否是陌生人
    var stranger: Boolean = false,
    //道具列表
    var propList: MutableList<PrivateProp> = mutableListOf(),
    //聊天券数量
    var chatTicketCnt: Int = 0,
    //语音券数量
    var voiceTicketCnt: Int = 0
)

/**
 * 道具实体类
 */
class PropBean(
//聊天券数量
    var chatTicketCnt: Int = 0,
    //语音券数量
    var voiceTicketCnt: Int = 0
) : Serializable

/**
 * 两人之间的关系
 */
class RelationInfo(
    //亲密度等级
    var intimateLevel: Int = 0,
    //陌生人标识
    var stranger: Boolean = false
) : Serializable

/**
 * 会话详情bean
 */
class NetcallBean(
    var callId: Long = 0,
    //创建人数据
    var callerInfo: ChatUser = ChatUser(),
    //接收人数据
    var receiverInfo: ChatUser = ChatUser(),
    //关系数据
    var relationInfo: RelationInfo = RelationInfo(),
    var channelId: String = "",
    var token: String = "",
    //价格（如果大于0，标识位付费方）
    var beans: Long = 0,
    //是否显示过确认弹窗
    var unconfirmed: Boolean = false,
    //是否取消
    var canceled: String = ""
) : Serializable

/**
 * 关注返回的实体
 */
class FollowBean(var follow: String = "", var stranger: Boolean = false) : Serializable

data class AliAuthInfo(
    var authInfo: String = ""
)

data class BindResultBean(
    var nickname: String = ""
)

data class UserSecurityInfo(
    var bindAliPay: Boolean = false,
    var bindWeChat: Boolean = false,
    var mobile: String = "",
    var security: Boolean = false,
    //是否实名认证
    var hasRealName: String = ""
)

/**
 * 余额使用
 */
data class BeansData(var beans: Long = 0) : Serializable

/**
 * 私信气泡配置类
 */
data class SettingBean(var chatBubble: ChatBubble? = null) : Serializable

/**
 * 新手礼包的单个礼物
 */
data class SingleNewUserGiftBean(
    //数量
    var count: Int = 0,
    //道具ID
    var prodId: Long = 0,
    //道具名称
    var prodName: String = "",
    //道具图片
    var prodPic: String = "",
    //有效时间
    var validDays: Int = 0
) : Serializable

/**
 * 新手礼包
 */
data class NewUserGiftBean(
    //是否领取过新手礼包
    var received: String = "",
    //奖励列表
    var bagList: MutableList<SingleNewUserGiftBean> = mutableListOf(),
    //封面
    var videoCover: String = "",
    var videoId: Long = 0,
    //播放地址
    var videoUrl: String = ""
) : Serializable