package com.julun.huanque.app

import android.app.Application
import com.julun.huanque.BuildConfig
import com.julun.huanque.common.BuildConfig.SERVICE_BASE_URL_PRODUCT
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.core.net.init.HuanQueInit

open class HuanQueApp : Application() {
    override fun onCreate() {
        val baseUrl = if (BuildConfig.DEBUG) {
            BuildConfig.SERVICE_BASE_URL_DEV
        } else {
            BuildConfig.SERVICE_BASE_URL_PRODUCT
        }
        CommonInit.getInstance().setBaseUrl(baseUrl)
        HuanQueInit.getInstance().init(this)
        super.onCreate()
    }
}