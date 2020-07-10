package com.julun.huanque.common.interfaces.routerservice

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.template.IProvider
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.helper.UnExpectedCrashExceptionForhuanqueApp

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/22 15:10
 *
 *@Description: 一般common模块需要从app模块获取的信息
 *
 */
interface AppCommonService : IProvider {

    /**
     * umen注册回调
     */
    fun umengCallBack(resume: Boolean, context: Context)

    /**
     * 日志异常上报
     */
    fun crashCallBack(e: UnExpectedCrashExceptionForhuanqueApp)

    /**
     * 页面返回回调
     */
    fun activityResultCallBack(requestCode: Int, resultCode: Int, data: Intent?)


    /**
     * 外部渠道号
     */
    fun getExternalChannel(): String

    /**
     * 内部渠道号
     */
    fun getInnerChannel(): String

    /**
     * 登录成功
     */
    fun loginSuccess(session : Session)

    /**
     * 退出登录
     */
    fun logout()
}