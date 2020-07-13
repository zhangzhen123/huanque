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
    var tagCount:Int =0
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
    //亲密值
    var num: Long = 0,
    //变化的亲密值
    var changeNum: Long = 0,
    //签名
    var mySign: String = "",
    //关注类型
    var follow: String = ""
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
    var linkList: MutableList<SocialUserInfo> = mutableListOf()
)

/**
 * 关注结果使用的Bean
 */
class FollowResultBean(var type: String, var userId: Long, var follow: Boolean)

/**
 * 亲密特权
 */
class IntimatePrivilege(var icon: String = "", var key: String = "", var minLevel: Int = 0, var title: String = "")

/**
 * 亲密数据
 */
class IntimateBean(
    //亲密度等级
    var intimateLevel: Int = 0,
    //下一级亲密等级
    var intimateNextLevel: Int = 0,
    //亲密度值
    var intimateNum: Int = 0,
    //下一个等级所需亲密度值
    var nextIntimateNum: Int = 0,
    //亲密特权
    var intimatePrivilegeList: MutableList<IntimatePrivilege> = mutableListOf()
)

/**
 * 会话的对象数据
 */
class ChatUserBean(
    var anchorExp: Long = 0,
    var anchorLevel: Long = 0,
    var cash: Long = 0,
    var headRealPeople: Boolean = false,
    var royalBjExp: Long = 0,
    var royalExp: Long = 0,
    var royalLevel: Int = 0,
    var userExp: Long = 0,

    var sex: String = "",
    var userId: Long = 0L,
    var headPic: String = "",
    var meetStatus: String = "",
    var nickname: String = ""
) : Serializable

/**
 * 会话详情页面基础信息
 */
class ConversationBasicBean(
    //小鹊提醒数据
    var activeList: MutableList<String> = mutableListOf(),
    //本人数据
    var usr: ChatUserBean = ChatUserBean(),
    //对方数据
    var friendUser: ChatUserBean = ChatUserBean(),
    //亲密度信息
    var intimate: IntimateBean = IntimateBean(),
    //与对方欢遇标识
    var meetStatus: String = "",
    //账户余额
    var beans: Long = 0,
    //私信费用
    var msgFee: Int = 0,
    //语音费用
    var voiceFee: Int = 0
)

/**
 * 会话详情bean
 */
class NetcallBean(
    var callId: Long = 0,
    //创建人数据
    var callerInfo: ChatUserBean = ChatUserBean(),
    //接收人数据
    var receiverInfo: ChatUserBean = ChatUserBean(),
    var channelId: String = "",
    var token: String = ""
) :
    Serializable