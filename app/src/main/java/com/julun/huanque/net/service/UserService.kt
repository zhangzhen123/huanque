package com.julun.huanque.net.service

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.bean.beans.UpdateHeaderBean
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
import com.julun.huanque.common.bean.forms.*
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
    suspend fun logout(@Body form: EmptyForm = EmptyForm()): Root<Session>

    /**
     * 手机号一键登录
     */
    @POST("user/acct/login/mobileQuick")
    suspend fun mobileQuick(@Body form: MobileQuickForm): Root<Session>

    /**
     * 更新用户数据
     */
    @POST("user/acct/data/update")
    suspend fun updateInformation(@Body form: UpdateInformationForm): Root<VoidResult>

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

    /**
     * 修改头像
     */
    @POST("user/acct/data/updateHeadPic")
    suspend fun updateHeadPic(@Body form: UpdateHeadForm): Root<UpdateHeaderBean>

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


}