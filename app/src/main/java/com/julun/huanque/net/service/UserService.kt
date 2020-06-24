package com.julun.huanque.net.service

import com.julun.huanque.common.bean.Root
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
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
    @POST("user/acct/info/basic")
    suspend fun queryUserDetailInfo(@Body form: SessionForm):Root<UserDetailInfo>

    /**
     * 查询用户等级信息
     */
    @POST("user/acct/info/levelInfo")
    fun queryUserLevelInfoBasic(@Body form: SessionForm): Observable<Root<UserLevelInfo>>



}