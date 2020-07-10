package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * Created by WanZhiYuan on 2018/07/09 0009.
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/06/26
 * @iterativeVersion 4.15
 * @iterativeDetail 迭代详情：新增字段godName、defaultGodName和godBadge
 */
class UserDetailInfo : Serializable {
    var nickname: String? = null//用户昵称
    var userId: Int = 0//用户ID
    var headPic: String? = null//用户头像
    var beans: Long = 0//用户萌豆数量
    var sex: String? = null//用户性别(可选项：Male、Female、Unknow)
    var mySign: String? = null//用户签名
    var userLevel: Int = 0//用户等级
    var royalStatus: String = ""//贵族等级状态 不是贵族、正在保级、保级成功\无需保级(可选项：None、Ing、Done)
    var royalLevel: Int = 0//贵族等级
    var royalLevelHtml: String = ""
    var royalDiffValue: Int = 0//下一贵族需要金额
    var nextRoyalName: String = ""//下一贵族名称
    var sjNeedExp: Long = 0//用户等级升级还需经验值
    var isTopLevel: Boolean = false//用户等级是否顶级(可选项：True、False)
    var isTopRoyalLevel: Boolean = false//贵族等级是否顶级(可选项：True、False)

    //4.15新增字段 godName和defaultGodName只会存在一个，defaultGodName存在时表示可以编辑上神昵称
    //上神名称
    var godName: String? = null

    //上神默认名称
    var defaultGodName: String? = null

    //4.15后统一使用这个字段获取贵族勋章
    var royalPic: String = ""

    //上神编辑次数
    var changeCnt: Int = 0

    //4.16新增字段
    //贵族月卡是否显示领取福利
    var showMonthCardWelfare: Boolean = false

    //是否显示贵族月卡整个栏目
    var showMonthCard: Boolean = false

    //是否显示地理位置
    var showLocation: Boolean = true

    //4.17新增字段
    //是否显示福利中心入口
    var showWelfare: Boolean = false

    //4.20.0新增字段
    var usableRoyalCard: Boolean = false
    var gainRoyalCard: Boolean = false

    //4.21.0 福利中心tag信息
    var welfareTag: String? = null

    //4.24.0 贵族热度
    var royalHeat: Long = 0L
    var royalHeatHtml: String = ""
    var showRoyalHeat: Boolean = false

    //4.25新增字段
    //卡券是否有新获得
    var cardAndTicketNew: Boolean = false

    //是否显示卡券入口
    var showCardAndTicket: Boolean = false
    override fun toString(): String {
        return "UserDetailInfo(nickname=$nickname, userId=$userId, headPic=$headPic, beans=$beans, sex=$sex, mySign=$mySign, userLevel=$userLevel, royalStatus='$royalStatus', royalLevel=$royalLevel, royalLevelHtml='$royalLevelHtml', royalDiffValue=$royalDiffValue, nextRoyalName='$nextRoyalName', sjNeedExp=$sjNeedExp, isTopLevel=$isTopLevel, isTopRoyalLevel=$isTopRoyalLevel, godName=$godName, defaultGodName=$defaultGodName, royalPic='$royalPic', changeCnt=$changeCnt, showMonthCardWelfare=$showMonthCardWelfare, showMonthCard=$showMonthCard, showLocation=$showLocation, showWelfare=$showWelfare, usableRoyalCard=$usableRoyalCard, gainRoyalCard=$gainRoyalCard, welfareTag=$welfareTag, royalHeat=$royalHeat, royalHeatHtml='$royalHeatHtml', showRoyalHeat=$showRoyalHeat, cardAndTicketNew=$cardAndTicketNew, showCardAndTicket=$showCardAndTicket)"
    }

}

data class UserLevelInfo(
    var needExp: Long = 0,
    var userExp: Long = 0,
    var userLevel: Int = 0
)

data class ImTokenBean(
    //聊天token
    var imToken: String = ""
)

