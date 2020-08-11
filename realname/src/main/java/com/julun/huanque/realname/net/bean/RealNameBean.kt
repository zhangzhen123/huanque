package com.julun.huanque.realname.net.bean

import java.io.Serializable

/**
 * 实名认证实体类
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/10
 */
data class RealNameBean(
    //扫描页需要token
    var token: String = "",
    //是否需要打开扫描页
    var need: Boolean = true,
    //认证结果
    var result: Boolean = false,
    //资料完整百分比
    var perfection:Int ?=null
) : Serializable