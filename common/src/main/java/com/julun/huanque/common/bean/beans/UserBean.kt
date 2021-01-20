package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.basic.RootListData
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
    var sex: String = "",
    //用户类型
    var userType: String = ""
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
class UserInfoChangeResult(
    var userId: Long = 0L,
    var follow: String = "",
    var formerFollow: String = "",
    var stranger: Boolean = false
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
    var msgFree: Boolean = false,
    //亲密度变动,需要插入消息
    var tips: MutableList<IntimateTouchBean> = mutableListOf()
)

/**
 * 亲密度变动提示消息操作文案
 */
data class IntimateTouchBean(
    //显示的内容
    var content: String = "",
    //点击类型
    var touchType: String = ""
) : Serializable {
    companion object {
        //语音电话
        const val NetCall = "NetCall"
    }

}

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
    var voiceTicketCnt: Int = 0,
    //直播显示倒计时
    var recomDelaySec: Long = 0,
    //我是否是对方的陌生人
    var strangerToOther: String = ""
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
    var canceled: String = "",
    //语音卡数量
    var ticketCnt: Int = 0
) : Serializable

/**
 * 关注返回的实体
 */
class FollowBean(
    var follow: String = "",
    var stranger: Boolean = false,
    var toastMsg: String = ""
) : Serializable

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


data class ManagerTagTabBean(
    var childList: List<UserTagBean> = listOf(),
    var like: String = "",
    var likeCnt: Int = 0,
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = "",
    var tagPic: String = "",
    var showType: Int = 0//
) : Serializable

data class UserTagBean(
    var like: Boolean = false,
    var likeCnt: Int = 0,
    var parentTagId: Int = 0,
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = "",
    var tagPic: String = "",
    //喜欢或者认证的标记
    var mark: String = "",
    //点赞数量
    var praiseNum: Long = 0,
    //审核通过照片数量
    var picNum: Int = 0
) : Serializable {
    override fun hashCode(): Int {
        return tagId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserTagBean

        if (tagId != other.tagId) return false

        return true
    }
}

data class ManagerParentTagBean(
    var parentTagIcon: String = "",
    var parentTagId: Int = 0,
    var parentTagName: String = ""
)

data class ManagerListData(
    var manageList: MutableList<UserTagBean> = mutableListOf(),
    var tagList: List<ManagerTagTabBean> = listOf()
)

data class MyTagListData(
    var markTagNum: Int = 0,
    var typeTagList: List<TagTypeTag> = listOf()
)

data class TagTypeTag(
    var groupTagList: List<UserTagBean> = listOf(),
    var tabTagName: String = "",
    var showType: String = ""
) : Serializable

data class TagDetailBean(
    var authNum: Int = 0,
    var authPage: RootListData<TagPicBean> = RootListData(),
    var like: Boolean = false,
    var tagDesc: String = "",
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = "",
    var tagPic: String = ""
)

data class AuthTagPicInfo(
    var authPicList: MutableList<AuthPic> = mutableListOf(),
    var otherAuthPicList: List<String> = listOf(),
    var praiseNum: Int = 0,
    var tagDesc: String = "",
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = "",
    var tagPic: String = "",
    var auth: Boolean = false
)

data class AuthPic(
    var applyPic: String = "",
    var auditReason: String = "",
    var auditStatus: String = "",
    var logId: Int = 0,
    var tagId: Int = 0,
    //待删除的logId(替换图片的时候使用)
    var deleteLogId: Int = 0
)

data class TagPicBean(
    var applyPic: String = "",
    var tagId: Int = 0,
    var targetPic: Boolean = false,
    var userId: Long = 0L,
    var picNum: Int = 0
) : Serializable

data class TagUserPicListBean(
    var authPicList: List<TagUserPic> = listOf(),
    var friendSex: String = "",
    var like: Boolean = false,
    var praise: Boolean = false,
    var praiseNum: Int = 0,
    var tagDesc: String = "",
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = ""
)

data class TagUserPic(
    var applyPic: String = "",
    var auditStatus: String = "",
    var logId: Int = 0,
    var tagId: Int = 0
)


/**
 * 选择性别之后返回的 标签和交友意愿数据
 */
data class LoginTagInfo(
    //交友意愿数据
    var socialWishList: MutableList<SocialWishBean> = mutableListOf(),
    //注册的标签数据
    var tagTypeList: MutableList<LoginTagTabBean> = mutableListOf()
) : Serializable

/**
 * 注册  标签大列表的数据
 */
data class LoginTagTabBean(
    //大类的code
    var tagType: String = "",
    //标签数据
    var tagList: MutableList<LoginTagBean> = mutableListOf(),
    //标签名称
    var tagTypeText: String = "",
    //是否选中（本地字段）
    var selected: Boolean = false
) : Serializable

/**
 * 注册  标签数据
 */
data class LoginTagBean(
    //标签ID
    var tagId: Long = 0,
    //标签名称
    var tagName: String = "",
    //标签图标
    var tagIcon: String = "",
    //标签图片
    var tagPic: String = "",
    //是否选中
    var selected: Boolean = false
) : Serializable

data class FilterTagBean(
    var distance: Long = 0,
    var sexType: String = "",
    var typeMap: TypeMap = TypeMap(),
    var minAge: Int = 0,
    var maxAge: Int = 0,
    var wishList: List<FilterWish> = listOf()
)

data class TypeMap(
    var Female: List<FilterGroupTag> = listOf(),
    var Male: List<FilterGroupTag> = listOf()
)

data class FilterWish(
    var selected: Boolean = false,
    var wishType: String = "",
    var wishTypeExplain: String = "",
    var wishTypeText: String = ""
)

data class FilterGroupTag(
    var tagList: MutableList<FilterTag> = mutableListOf(),
    var tagType: String = "",
    var tagTypeText: String = "",
    var isFold: Boolean = true//是否展开
)


data class FilterTag(
    var mark: Boolean = false,
    var tagIcon: String = "",
    var tagId: Int = 0,
    var tagName: String = ""
)

/**
 * 星座对象
 */
data class ConstellationBean(var type: String = "", var name: String = "") : Serializable

/**
 * 查看历史
 */
data class WatchHistoryBean(
    //头像
    var headPic: String = "",
    //昵称
    var nickName: String = "",
    //性别
    var sex: String = "",
    //用户Id
    var userId: Long = 0,
    //查看次数
    var visitCount: Int = 0,
    //查看时间
    var visitTime: String = "",
    //年龄
    var age: Int = 0
) : Serializable