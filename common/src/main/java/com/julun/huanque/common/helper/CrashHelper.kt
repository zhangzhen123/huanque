package com.julun.huanque.common.helper

import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.constant.ARouterConstant

class UnExpectedCrashExceptionForhuanqueApp : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

fun reportCrash(extraDescription: String, thr: Throwable) {
    (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.crashCallBack(UnExpectedCrashExceptionForhuanqueApp(extraDescription, thr))
}

fun reportCrash(thr: Throwable) {
    (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.crashCallBack(UnExpectedCrashExceptionForhuanqueApp(/*"${thr.message}",*/thr))
}

fun reportCrash(extraDescription: String) {
    (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.crashCallBack(UnExpectedCrashExceptionForhuanqueApp(extraDescription))
}