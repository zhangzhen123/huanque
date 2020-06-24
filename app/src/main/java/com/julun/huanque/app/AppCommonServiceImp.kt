package com.julun.huanque.app

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.helper.UnExpectedCrashExceptionForhuanqueApp
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.constant.ARouterConstant

@Route(path = ARouterConstant.APP_COMMON_SERVICE)
class AppCommonServiceImpl : AppCommonService {
    //todo
    override fun umengCallBack(resume: Boolean, context: Context) {
    }

    override fun crashCallBack(e: UnExpectedCrashExceptionForhuanqueApp) {
    }

    override fun activityResultCallBack(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun getExternalChannel(): String {
        return "channel"
    }

    override fun getInnerChannel(): String {
        return "channel"
    }

    override fun loginSuccess(session: Any) {
    }

    override fun logout() {
    }

    override fun init(context: Context?) {
    }

}