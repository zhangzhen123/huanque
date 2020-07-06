package com.julun.huanque.common.bean.forms

/**
 *@创建者   dong
 *@创建时间 2020/7/3 15:27
 *@描述 登录的form文件
 */

/**
 * 手机号登录的form
 */
class MobileLoginForm(var mobile: String = "", var code: String = "", var programId: Int? = null, var shuMeiDeviceId: String? = null)

/**
 * 手机号一键登录form
 */
class MobileQuickForm(var loginToken: String = "", var shuMeiDeviceId: String = "")

/**
 * 更新基础数据的form
 */
class UpdateInformationForm(
    var nickname: String? = null,
    var birthday: String? = null,
    var sexType: String? = null,
    var mySign: String? = null,
    var height: Int? = null,
    var weight: Int? = null,
    var jobId: Int? = null,
    //邀请码
    var invitationCode: String? = null
)

/**
 * 获取验证码form
 */
class GetValidCode {
    var mobile: String = ""

    constructor(phone: String) {
        this.mobile = phone
    }
}

/**
 * 同意协议的form
 * @param agreementCode 协议代码
 */
class AgreementResultForm(var result: String = "True", var agreementCode: String = "")

class UpdateHeadForm(var headPic: String )