package com.julun.huanque.common.bean.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by my on 2018/07/09 0009.
 * @alter WanZhiYuan
 * @since 4.29
 * @date 2020/04/09
 */
@Entity
data class Session(
        var headPic: String = "",//头像
        var imToken: String = "",//聊天token
        var nickname: String = "",//登陆用户昵称
        var openId: String = "",//第三方openid
        var sessionId: String = "",//用户sessionId
        @PrimaryKey
        var userId: Int = 0,//登陆用户ID
        var userType: String = "",//用户类型(可选项：Viewer(注册用户)、Anchor(主播)、Manager(运管)、Visitor(游客)、Customer(客服))
        var isLogin: Boolean = true,
        var hasPushPermis: String = "",//是否拥有直播权限(可选项：True、False、None（false）)
        var newUser: Boolean = false,
        var userLevel:Int = 0,
        var royalLevel:Int = 0,
        //4.29新增字段
        //是否同意隐私协议
        var agreeUp:Boolean = false
):Serializable{
        constructor() : this("")
}