package com.julun.huanque.common.bean.forms

/**
 *@创建者   dong
 *@创建时间 2020/7/3 15:27
 *@描述 登录的form文件
 */

/**
 * 手机号登录的form
 */
class MobileLoginForm(var mobile: String = "", var code: String = "", var programId: Int? = null, var deviceId: String? = null)