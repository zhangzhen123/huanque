package com.julun.huanque.core.net

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.database.table.Session
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/23 10:11
 *
 *@Description: UserService
 * 测试网络接口
 *
 */
interface UserService {


    /************************************** 个人中心 **********************************************/

    /**
     * 查询用户基本信息
     */
    @POST("user/acct/info/myInfo")
    suspend fun queryUserDetailInfo(@Body form: EmptyForm = EmptyForm()): Root<UserDetailInfo>

    /**
     * 查询用户等级信息
     */
    @POST("user/acct/info/levelInfo")
    fun queryUserLevelInfoBasic(@Body form: SessionForm): Observable<Root<UserLevelInfo>>


    /**
     * 手机号登录
     */
    @POST("user/acct/login/mobile")
    suspend fun mobileLogin(@Body form: MobileLoginForm): Root<Session>

    /**
     * 微信登录
     */
    @POST("user/acct/login/weixin")
    suspend fun weiXinLogin(@Body form: WeiXinForm): Root<Session>


    /**
     * 退出登录
     */
    @POST("user/acct/login/logout")
    suspend fun logout(@Body form: EmptyForm = EmptyForm()): Root<VoidResult>

    /**
     * 手机号一键登录
     */
    @POST("user/acct/login/mobileQuick")
    suspend fun mobileQuick(@Body form: MobileQuickForm): Root<Session>

    /**
     * 更新用户数据
     */
    @POST("user/acct/data/initInfo")
    suspend fun updateInformation(@Body form: UpdateInformationForm): Root<UpdateHeaderBean>

    /**
     * 开始获取验证码
     */
    @POST("user/acct/login/getSmsCode")
    suspend fun startGetValidCode(@Body form: GetValidCode): Root<JSONObject>

    /**
     * 同意协议
     */
    @POST("user/acct/info/agreement")
    suspend fun agreement(@Body form: AgreementResultForm): Root<Void>

//    /**
//     * 修改头像
//     */
//    @POST("user/acct/data/initHeadPic")
//    suspend fun updateHeadPic(@Body form: UpdateHeadForm): Root<UpdateHeaderBean>

    /**
     * 举报
     */
    @POST("social/friend/relation/report")
    suspend fun report(@Body form: ReportForm): Root<VoidResult>

    /**
     * 举报
     */
    @POST("social/friend/relation/reportTypeList")
    suspend fun reportTypeList(): Root<ArrayList<ManagerInfo>>

    /**
     * 获取语音文本提示信息
     */
    @POST("user/acct/data/getVoiceSignPoint")
    suspend fun getVoiceSignPoint(@Body form: EmptyForm = EmptyForm()): Root<VoiceSignPointBean>

    /**
     * 修改语音签名
     */
    @POST("user/acct/data/updateVoice")
    suspend fun updateVoice(@Body form: UpdateVoiceForm): Root<InfoPerfection>

    @POST("user/acct/data/checkNickName")
    suspend fun checkNickName(@Body form: NicknameForm): Root<VoidResult>


    /**
     * 绑定手机
     */
    @POST("user/acct/info/bindMobile")
    suspend fun bindMobile(@Body form: BindPhoneForm): Root<VoidResult>

    /**
     * 获取支付宝登录授权信息
     */
    @POST("user/acct/login/alipayAuthInfo")
    suspend fun getAlipayAuthInfo(@Body form: EmptyForm = EmptyForm()): Root<AliAuthInfo>


    /**
     * 绑定支付宝
     */
    @POST("user/acct/info/bindAlipay")
    suspend fun bindAliPay(@Body form: BindForm): Root<BindResultBean>

    /**
     * 绑定微信
     */
    @POST("user/acct/info/bindWeixin")
    suspend fun bindWeiXin(@Body form: BindForm): Root<BindResultBean>


}