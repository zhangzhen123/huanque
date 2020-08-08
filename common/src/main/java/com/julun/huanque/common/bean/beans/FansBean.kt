package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * 粉丝榜单
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/09
 * @iterativeVersion 4.20
 * @updateDetail 粉丝团2.0更新字段
 * @date 2019/11/18
 * @since 4.21
 * @detail 新增字段
 */
class FansRankInfo(
    /** 是否已经打卡 **/
    var clockIn: Boolean = false,
    /** 粉丝团展示 头部信息 **/
    var fansGroupInfoVO: FansRankHeadInfo? = null,
    /** 粉丝团列表 **/
    var fansInfos: ArrayList<FansListInfo>? = null,
    /** 粉丝团名称 **/
    var groupName: String = "",
    /** 本人粉丝信息 **/
    var self: UserFansInfo? = null,
    /** 粉丝牌子是否激活 **/
    var signActive: Boolean = false,
    /** 是否登录 **/
    var login: Boolean = false,
    /** 成员人数 **/
    var groupSize: Int = 0,
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 加入粉丝团花费 **/
    var price: Long = 0,
    /**免费倒计时**/
    var deadline: Long = 0,
    //4.19新增参数
    /**亲密度获取情况**/
    var intimateList: ArrayList<Intimate>? = null,
    /**特权信息**/
    var privilegeList: ArrayList<Privilege>? = null,
    /**是否粉丝团成员**/
    var groupMember: Boolean = false,
    /**用户昵称**/
    var nickname: String = "",
    /**用户头像**/
    var headPic: String = "",
    /**亲密度**/
    var intimate: String = "",
    /**粉丝等级**/
    var fansLevel: Int = 0,
    /**下个粉丝等级**/
    var nextFansLevel: Int = 0,
    /**当前等级增加亲密度**/
    var nowLevelAddIntimate: Long = 0,
    /**升级亲密度差值**/
    var diffIntimate: Long = 0,
    //4.20新增字段
    /**当前是否获取了新特权**/
    var gainNewPrivilege: Boolean? = null,
    //4.21新增字段
    /**是否还有更多数据**/
    var hasMore: Boolean = false,

    //自定义参数
    /**
     * @see BusiConstant.FansTabName
     * 页面类型常量类型
     */
    var tabType: String = ""
) : Serializable

/**
 * 粉丝团头部信息
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/09/29
 * @iterativeVersion 4.19
 */
class FansRankHeadInfo(
    /** 头像 **/
    var headPic: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 显示小主回馈图标 **/
    var showStock: Boolean? = null,
    //4.19 新增参数
    /** 周榜排名 **/
    var weekRank: Long = -1,
    /** 粉丝团类型 Common 普通团、Thirty 30人团 and Fifty 50人团**/
    var fansGroupType: String = ""
) : Serializable

/**
 * 粉丝团列表信息
 * @date 2019/11/18
 * @since 4.21
 * @detail 新增字段
 */
class FansListInfo(
    /** 粉丝牌子是否激活 **/
    var signActive: Boolean = false,
    /** 粉丝团名称 **/
    var groupName: String = "",
    /** 亲密度 **/
    var intimate: String = "",
    /** 头像 **/
    var headPic: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 粉丝等级 **/
    var fansLevel: Int = 0,
    /** 排名 **/
    var rank: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 亲密度色值 **/
    var color: String = "",

    //4.17新增字段
    /** 显示对应浮动箭头 **/
    var increase: Boolean? = null,
    //4.21新增字段
    /** 节目id **/
    var programId: Long = 0,
    /** 用户id **/
    var userId: Long = 0,

    //自定义字段
    /** 成员人数 **/
    var fansMember: String = ""
) : Serializable {

    override fun hashCode(): Int {
        if (this.userId != 0L) {
            return this.userId.hashCode()
        }
        if (this.programId != 0L) {
            return this.programId.hashCode()
        }
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is FansListInfo) {
            if (other.userId != 0L && this.userId != 0L) {
                return other.userId == this.userId
            }
            if (other.programId != 0L && this.programId != 0L) {
                return other.programId == this.programId
            }
        }
        return false
    }
}

/**
 * 当前粉丝信息
 */
class UserFansInfo(
    /** 是否是该粉丝团成员 **/
    var groupMember: Boolean = false,
    /** 头像 **/
    var headPic: String = "",
    /** 亲密度 **/
    var intimate: String = "",
    /** 昵称 **/
    var nickname: String = "",
    /** 排名 **/
    var rank: String = "",
    /** 粉丝等级 **/
    var fansLevel: Int = 0,
    /** 粉丝牌是否点亮 **/
    var signActive: Boolean = false,
    /** 粉丝牌名称 **/
    var groupName: String = "",
    /** 粉丝勋章地址 **/
    var fansPic: String = "",
    /** 亲密度色值 **/
    var color: String = "",

    //4.17新增字段
    /** 显示对应浮动箭头 **/
    var increase: Boolean? = null
) : Serializable

/**
 * 粉丝团特权信息
 * @createAuthor WanZhiYuan
 * @createDate 2019/10/10
 * @iterativeVersion 4.19
 */
class FansPrivilegeInfo : Serializable {
    /** 说明框背景色 **/
    var color: String = ""

    /** 当前粉丝团类型(可选项：Common、Thirty、Fifty) **/
    var fansGroupType: String = ""

    /** 粉丝团类型与特权关系 **/
    var fansGroupTypes: ArrayList<FansPrivilegeItemInfo>? = null

    /** 粉丝团特权详情 **/
    var privilegeTypes: ArrayList<FansPrivilegeItemInfo>? = null
}

/**
 * 粉丝团特权item信息
 * @createAuthor WanZhiYuan
 * @createDate 2019/10/11
 * @iterativeVersion 4.19
 */
class FansPrivilegeItemInfo : Serializable {
    /** 当前粉丝团类型(可选项：Common、Thirty、Fifty) **/
    var fansGroupType: String = ""

    /** tab标签名称 **/
    var groupTypeDesc: String = ""

    /** 粉丝团类型对应开放特权 **/
    var privileges: ArrayList<String>? = null

    //特权说明相关信息
    /** 特权类型 **/
    var code: String = ""

    /** 特权名称 **/
    var desc: String = ""

    /** 特权说明和文字颜色 **/
    var remarks: ArrayList<Remarks>? = null

    //自定义参数
    //是否标记高亮显示
    var isLight: Boolean = false

    //是否拥有此特权
    var isOwner: Boolean? = null
}

class JoinFansResult {
    var salvationInfo: PkMicSalvationStartInfo? = null
    var isFree: Boolean? = null
    var price: Long? = null
}

class Remarks : Serializable {
    var color: String = ""
    var remark: String = ""
}
/**
 * 粉丝特权相关实体类
 */
data class FansPrerogative(
    //升级亲密度差值
    var diffIntimate: Int = 0,
    //粉丝团名称
    var fansGroupName: String = "",
    //当前粉丝等级
    var fansLevel: Int = 0,
    //下一粉丝等级
    var nextFansLevel: Int = 0,
    //用户头像
    var headPic: String = "",
    //亲密度
    var intimate: String = "",
    //今日亲密度获取情况
    var intimateList: MutableList<Intimate> = mutableListOf(),
    //用户昵称
    var nickname: String = "",
    //当前等级增加亲密度
    var nowLevelAddIntimate: Int = 0,
    //特权信息
    var privilegeList: MutableList<Privilege> = mutableListOf(),
    //粉丝牌子图片
    var fansSignPic: String = "",
    //粉丝牌子是否佩戴
    var active: Boolean = false
) : Serializable

/**
 * 亲密度相关实体类
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/09
 * @iterativeVersion 4.19
 * @updateDetail 继承特权实例
 */
data class Intimate(
    //是否已完成
    var achieved: Boolean = false,
    //完成情况
    var achievedInfo: String = "",
    //颜色是否高亮
    var fontColor: Boolean = false,
    //4.19新增字段
    var intimateType: String = ""
) : Privilege()

/**
 * 特权信息实体类
 * @iterativeAuthor WanZhiYuan
 * @updateDate 2019/10/12
 * @iterativeVersion 4.19
 * @updateDetail 特权实例作为亲密度的父类使用
 */
open class Privilege(
    //描述
    var description: String = "",
    //图片
    var pic: String = ""
) : Serializable
/**
 * 小主回馈Item相关信息
 * @createDate 2019/08/20
 * @createAuthor WanZhiYuan
 * @iterativeVersion 4.17
 */
data class FansFeedbackItemInfo(
    /** 库存数量 **/
    var count: Int = 0,
    /** 库存名称 **/
    var name: String = "",
    /** 库存图标 **/
    var pic: String = "",
    /** 回馈数量 **/
    var awardCount: Int = 0,
    /** 回馈礼物名称 **/
    var awardName: String = "",
    /** 回馈时间 **/
    var createTime: String = "",
    /** 回馈玩家昵称 **/
    var userInfo: String = ""
) : Serializable