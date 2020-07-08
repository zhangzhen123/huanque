package com.julun.huanque.common.bean.beans

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
    var count: Long = 0
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
 * 亲密数据
 */
class IntimateBean(
    //亲密度等级
    var intimateLevel: Int = 0,
    //亲密度值
    var intimateNum: Int = 0,
    //下一个等级所需亲密度值
    var nextIntimateNum: Int = 0
)

/**
 * 会话的对象数据
 */
class ChatUserBean(
    var activeList: MutableList<String> = mutableListOf(),
    var friendId: Long = 0L,
    var headPic: String = "",
    var intimate : IntimateBean = IntimateBean(),
    var meetStatus: String = "",
    var msgFee: Int = 0,
    var nickname: String = ""

)