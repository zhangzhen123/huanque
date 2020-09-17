package com.julun.huanque.common.bean.beans

import java.io.Serializable



/**
 * 星球霸主CP对象
 */
data class CpInfo(
    var nickname: String = "",
    var headPic: String = "",
    var userNickname: String = "",
    var userHeadPic: String = ""
) : Serializable

/**
 * 星球霸主基础信息
 */
data class PlanetHomeBean(
    //萌豆余额
    var beans: Long = 0L,
    //推荐模式
    var recommend: String = "",
    //是否幸运星球模式
    var isLuckyMode: Boolean = false,
    var cpInfo: CpInfo? = null,
    //榜单数据
    var rankUrl: String = "",
    //规则地址
    var ruleUrl: String = ""

) : Serializable {
    companion object {
        //攻击模式
        const val AttackMode = "AttackMode"

        //休闲模式
        const val CasualMode = "CasualMode"

        //幸运模式
        const val LuckyMode = "LuckyMode"
    }

}


/**
 * 休闲模式当前最高数据玩家
 */
data class TopUserInfo(
    var headPic: String = "",
    var nickname: String = "",
    //分数
    var score: Long = 0L,
    var userId: Long = 0L,
    //攻击模式使用
    var anchorScore: Long = 0L
) : Serializable

/**
 * 休闲模式  基础数据查询
 */
data class PlanetCasualBean(
    //萌豆数量
    var beans: Long = 0L,
    //光线抢卡数量
    var lightGunCardNum: Int = 0,
    //奖励模式
    var preAwardDesc: String = "",
    //奖励图片
    var preAwardPic: String = "",
    var topUser: TopUserInfo? = null
) : Serializable
data class Weapon(
    //价格
    var beans: Long = 0L,
    //商品ID
    var goodsId: Int = 0,
    var pic: String = "",
    //枪名称
    var weaponName: String = "",
    //枪对应的Type
    var weaponType: String = "",
    //武器卡数量
    var bagCnt: Int = 0,
    //最大可中奖金额
    var maxAwardBeans: Long = 0
) : Serializable

/**
 * 攻击模式  基础数据查询
 */
data class PlanetAttackBean(
    //萌豆数量
    var beans: Long = 0L,
    //主播今日积分
    var anchorScore: Long = 0L,
    //模式
    var mode: String = "",
    //幸运模式倒计时
    var ttl: Long = 0,
    //top用户数据
    var currTopUser: TopUserInfo? = null,
    //当前血量（幸运星球）
    var blood: Long = 0L,
    //枪杆数据
    var weaponList: MutableList<Weapon> = mutableListOf(),
    //榜单
    var rankUrl: String = "",
    //缓存的中奖消息
    var msgList: MutableList<String> = mutableListOf()

) : Serializable {
    companion object {
        //攻击模式
        const val AttackMode = "AttackMode"

        //幸运模式
        const val LuckyMode = "LuckyMode"
    }
}

/**
 * 休闲模式 击打过程 返回的数据
 */
data class HitPlanetBean(
    //获取爆能枪卡标识位
    var gotEnergyCard: Boolean = false,
    //增加的分数
    var addScore: Int = 0,
    //是否获得萌币
    var gotMoeCoins: Boolean = false,
    //总分数
    var totalScore: Long = 0,
    //本地使用字段
    var type: String = ""
) : Serializable
/**
 * 星球  奖励
 */
data class PlanetAward(
    //奖励描述
    var desc: String = "",
    //中奖数量
    var count: Int = 0,
    //奖励名称
    var name: String = "",
    //奖励图片
    var pic: String = ""
) : Serializable

/**
 * 休闲模式 结果数据
 */
data class RelaxationResultBean(
    //奖励列表
    var awardList: MutableList<PlanetAward> = mutableListOf(),
    //击败比例
    var beatRate: String = "",
    //本场精准值
    var score: Int = 0
) : Serializable

/**
 * 攻击模式下的奖励
 */
data class PlanetAttackAward(
    var count: Int = 0,
    var pic: String = "",
    var prizeName: String = ""
) : Serializable

/**
 * 发射子弹返回的结果
 */
data class UseWeaponResult(
    var beans: Long = 0,
    //中奖萌豆余额
    var awardBeans: Long = 0,
    var awardList: MutableList<PlanetAttackAward> = mutableListOf(),
    var showType: String = "",
    //剩余的武器卡数量
    var bagCnt: Int = 0,
    //显示类型
    var popupType: String = "",
    //武器名称
    var weaponName: String = "",
    //发射子弹的时间
    var shootTime: Long = 0L
) : Serializable {
    companion object {
        const val Style1 = "Style1"
        const val Style2 = "Style2"
        const val Style3 = "Style3"
        const val Popup = "Popup"

        //不弹窗
        const val None = "None"

        //小奖弹窗
        const val Popup1 = "Popup1"

        //大奖弹窗
        const val Popup2 = "Popup2"
    }
}

/**
 * 武器卡数量 类
 */
data class WeaponCountBean(
    //武器类型
    var type: String = "",
    //武器卡数量
    var count: Int = 0,
    //是否需要刷新
    var refresh: Boolean = false
) : Serializable

/**
 * 星球星球消失消息  返回的数据
 */
data class LuckyPlanetDisappearBean(
    var luckyPlanetTimeOut: String = "",
    //用户ID
    var userId: Long = 0L,
    //奖励数据   所有用户的都在一起
    var luckyPlanetUserInfo: HashMap<String, LuckyPlanetDisappearSingleAward> = hashMapOf()
) : Serializable

/**
 * 幸运星球  消息  当个用户的奖励数据
 */
data class LuckyPlanetDisappearSingleAward(
    //霸道值
    var score: Long = 0L,
    //直播间ID
    var programId: Long = 0L,
    //奖励列表
    var awardValue: MutableList<PlanetAward> = mutableListOf()
) : Serializable

/**
 * 霸主信息变更消息
 */
data class PlanetScoreChangeBean(
    var planetScoreInfo: TopUserInfo? = null
) : Serializable

//单个中奖助手
data class SingleAwardAssistant(
    //奖励范围
    var awardBeans: String = "",
    //奖励数量
    var awardCnt: Int = 0,
    //最近奖励时间
    var lastAwardTime: String = ""
) : Serializable

/**
 * 中奖助手数据
 */
data class AwardAssistantBean(
    //中奖助手记录
    var awardList: MutableList<SingleAwardAssistant> = mutableListOf(),
    //武器类型
    var currWeaponType: String = "",
    //武器列表
    var weaponList: MutableList<Weapon> = mutableListOf()
) : Serializable

