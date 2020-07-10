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
    fun startRealName(activity: Activity, realName: String, realIdCard: String,callback: RealNameCallback)

    //开始头像认证
    fun startRealHead(activity: Activity,callback: RealNameCallback)

    //销毁
    fun release()

}

interface RealNameCallback {
    fun onCallback(status: String, des: String)
}