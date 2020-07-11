package com.julun.huanque.common.bean.beans

import java.io.Serializable
import java.math.BigDecimal

data class UserDetailInfo(
    var tools: MutableList<UserTool> = mutableListOf(),
    var userBasic: UserBasic = UserBasic(),
    var userDataTabList: MutableList<UserDataTab> = mutableListOf()
) : Serializable

data class UserBasic(
    var anchorExp: Long = 0L,
    var anchorLevel: Int = 0,
    var beans: Long = 0L,
    var cash: BigDecimal = BigDecimal.ZERO,
    var headPic: String = "",
    var headRealPeople: Boolean = false,
    var mySign: String = "",
    var nickname: String = "",
    var royalBjExp: Long = 0L,
    var royalExp: Long = 0L,
    var royalLevel: Int = 0,
    var sex: String = "",
    var userExp: Long = 0L,
    var userId: Long = 0L,
    var userLevel: Int = 0,
    var userType: String = ""
)

data class UserTool(
    var icon: String = "",
    var name: String = "",
    var toolType: String = ""
)

data class UserLevelInfo(
    var needExp: Long = 0,
    var userExp: Long = 0,
    var userLevel: Int = 0
)

data class ImTokenBean(
    //聊天token
    var imToken: String = ""
)

