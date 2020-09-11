package com.julun.huanque.core.init

import android.app.Application
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.init.CommonInit


/**
 * Created by dong on 2018/8/31.
 * 作为SDK开放出去的初始化类
 *
 * 总调用入口
 */
class HuanQueInit private constructor() {

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

    fun init(application: Application) {
//        mContext = application
        if (AppHelper.isMainProcess(application)) {
//            CommonInit.getInstance().setBaseUrlByMode(debug)
            //使用TaskDispatcher分发进行初始化
            CommonInit.getInstance().init(application)
        }
    }

    fun initContext(application: Application){
        mContext = application
    }

    suspend fun initWithCoroutine(application: Application) {
//        mContext = application
        if (AppHelper.isMainProcess(application)) {
//            CommonInit.getInstance().setBaseUrlByMode(debug)
            //使用协程初始化
            CommonInit.getInstance().initWithCoroutines(application)
        }
    }

}
