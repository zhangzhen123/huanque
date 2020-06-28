package com.julun.huanque.core.init

import android.app.Activity
import android.app.Application
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.init.CommonInit


/**
 * Created by dong on 2018/8/31.
 * 作为SDK开放出去的初始化类
 *
 * 总调用入口
 */
class HuanQueInit private constructor()  {

    companion object {
        @Volatile
        private var singleton: HuanQueInit? = null
        fun getInstance(): HuanQueInit {
            if (singleton == null) {
                synchronized(HuanQueInit::class.java) {
                    if (singleton == null) {
                        singleton = HuanQueInit()
                    }
                }
            }
            return singleton!!
        }

    }
    private lateinit var mContext: Application

    private var debug = false

    /**
     * 控制SDK网络环境 对外使用
     */
    fun setDebug(deBug: Boolean) {
        this.debug = deBug
    }

    /**
     * SDK类型
     */
    fun setSdkType(type: String) {
//        CommonInit.getInstance().setSdkType(type)
    }

    fun getDebug(): Boolean {
        return debug
    }

    var mainActivity: Activity? = null//aliPay用到 防止依赖的activity内存泄漏

    fun init(application: Application) {
        mContext = application
        if (AppHelper.isMainProcess(application)) {
//            CommonInit.getInstance().setBaseUrlByMode(debug)
            CommonInit.getInstance().init(application)
        }
    }

}
