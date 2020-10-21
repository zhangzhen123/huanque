package com.julun.huanque.common.bean.beans

import java.io.Serializable
import java.math.BigDecimal

data class UserDetailInfo(
    var adList: MutableList<RechargeAdInfo>? = null,
    var tools: MutableList<UserTool> = mutableListOf(),
    var userBasic: UserBasic = UserBasic(),
    var userDataTabList: MutableList<UserDataTab> = mutableListOf(),
    //客服地址
    var customerUrl: String = ""
) : Serializable

data class UserBasic(
    var anchorExp: Long = 0L,
    var anchorLevel: Int = -1,
    var beans: Long = 0L,
    var cash: String = "",
    var headPic: String = "",
    var headRealPeople: Boolean = false,
    var mySign: String = "",
    var nickname: String = "",
    var royalBjExp: Long = 0L,
    var royalExp: Long = 0L,
    var royalLevel: Int = 0,
    var royalPic: String = "",
    var sex: String = "",
    var userExp: Long = 0L,
    var userId: Long = 0L,
    var userLevel: Int = 0,
    var userType: String = "",
    var age: Int = 0,
    //真人图片地址
    var authMark: String = ""
)

data class UserTool(
    var icon: String = "",
    var name: String = "",
    var toolType: String = ""
)

data class UserLevelInfo(
    var needExp: Long = 0,
    var userExp: Long = 0,
    var userLevel: Int = 0,
    //红点数量
    var messageCount: Int = 0
)

/**
 * 更新性别后返回的数据
 */
data class UpdateSexBean(
    //聊天token
    var imToken: String = "",
    var sex: String = "",
    //用户ID
    var userId: Long = 0L,
    //昵称
    var nickname: String = "",
    //用户头像
    var headPic: String = "",
    //生日
    var birthday: String = "",
    //邀请码
    var invitationCode: String = ""
)

data class VoiceSignPointBean(
    var points: List<SignPoint> = listOf()
)

data class SignPoint(
    var voiceContent: String = "",
    var voiceTitle: String = ""
)

/**
 * 资料完成度
 */
data class InfoPerfection(val perfection: Int = 0)

