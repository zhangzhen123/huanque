package com.julun.huanque.common.database.table

/**
 * Created by my on 2018/07/09 0009.
 * @alter WanZhiYuan
 * @since 4.29
 * @date 2020/04/09
 */
data class Session(
    //第三方openid
    var openId: String = "",
    //用户sessionId
    var sessionId: String = "",
    //登陆用户ID
    var userId: Long = 0,
    //登陆用户昵称
    var nickname: String = "",
    //头像
    var headPic: String = "",
    //用户类型(可选项：Viewer(注册用户)、Anchor(主播)、Manager(运管)、Visitor(游客)、Customer(客服))
    var userType: String = "",
    //聊天token
    var imToken: String = "",
    //声网token
    var voiceToken: String = "",
    //是否是新用户
    var newUser: Boolean = false,
    //数据是否完整的标记
    var regComplete: Boolean = false,
    //是否为注册用户
    var regUser: Boolean = false,
    //性别
    var sex: String = "",
    //是否同意了协议
    var agreeUp: String = ""

)