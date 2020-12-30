package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/12/21 11:24
 *@描述 登录注册 使用的实体类
 */
/**
 * 登录页面，滚动背景数据
 */
class LoginPicBean(
    //图片资源
    var resourceId: Int = 0,
    //年龄
    var age: Int = 0,
    //城市数据
    var city: String = "",
    //标签数据
    var tag: String = ""
) : Serializable

/**
 * 选择tag对象
 */
data class ChooseTagBean(var resourceId: Int = 0, var selected: Boolean = false) : Serializable