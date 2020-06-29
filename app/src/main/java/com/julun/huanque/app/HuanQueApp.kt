package com.julun.huanque.app

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.julun.huanque.BuildConfig
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.core.init.HuanQueInit
import com.tencent.bugly.crashreport.CrashReport

open class HuanQueApp : Application() {
    override fun onCreate() {
        val baseUrl = if (BuildConfig.DEBUG) {
            BuildConfig.SERVICE_BASE_URL_DEV
        } else {
            BuildConfig.SERVICE_BASE_URL_PRODUCT
        }
        CommonInit.getInstance().setBaseUrl(baseUrl)
        if (AppHelper.isMainProcess(this)) {
            //bugly初始化(bugly的初始化放在所有初始化的第一个，以免一些日志错过收集)
            initBugly(this)
        }
        HuanQueInit.getInstance().init(this)
        super.onCreate()
    }

    private fun initBugly(app: Application) {
        val applicationContext = app.applicationContext
        val strategy = CrashReport.UserStrategy(applicationContext)
        //设置channel相关
//        val channel = LMUtils.getAppMetaData(BusiConstant.MetaKey.DOWNLOAD_CHANNEL)
//        if (!TextUtils.isEmpty(channel)) {
//            Log.i("channel", "buglug: $channel")
//            strategy.appChannel = "main"
//        }

//        strategy.appVersion = String.format("%d.%d.%d", BuildConfig.MAJOR_VERSION, BuildConfig.MINOR_VERSION, BuildConfig.PATCH_VERSION)
        strategy.appVersion = BuildConfig.VERSION_NAME
//        strategy.appVersion = "1.0.0"
        //                CrashReport.initCrashReport(applicationContext, strategy)
        CrashReport.initCrashReport(applicationContext, "381c225d86", BuildConfig.DEBUG, strategy)

        CrashReport.setIsDevelopmentDevice(applicationContext, BuildConfig.DEBUG)

    }

//    /**
//     * 获取当前的buglyId
//     */
//    private fun getBuglyId(): String {
//        return if (BuildConfig.DEBUG) {
//            when (SPUtils.getProduct()) {
//                1 -> {
//                    BuildConfig.TENCENT_BUGLY_APP_ID_PRODUCT
//                }
//                2 -> {
//                    BuildConfig.TENCENT_BUGLY_APP_ID_DEV
//                }
//                else -> {
//                    //没有
//                    BuildConfig.TENCENT_BUGLY_APP_ID_DEV
//                }
//            }
//        } else {
//            BuildConfig.TENCENT_BUGLY_APP_ID_PRODUCT
//        }
//    }
}