package com.julun.huanque.common.interfaces.routerservice

import android.app.Activity
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/09
 */
interface IRealNameService : IProvider {
    //开始实名认证
    fun startRealName(
        activity: Activity,
        realName: String,
        realIdCard: String,
        callback: RealNameCallback
    )
    //开始头像认证
    fun startRealHead(activity: Activity, callback: RealNameCallback)

    /**
     * 检查是否要打开头像认证
     */
    fun checkRealHead()

    //销毁
    fun release()
}

interface RealNameCallback {
    /**
     * @param status 结果状态 参考[com.julun.huanque.common.constant.RealNameConstants]
     * @param des 结果文案
     * @param percent 资料完整度 头像认证才会返回
     */
    fun onCallback(status: String, des: String, percent: Int? = null)
}