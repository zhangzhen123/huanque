package com.julun.huanque.app

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.helper.UnExpectedCrashExceptionForhuanqueApp
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.logger
import com.julun.jpushlib.TagAliasOperatorHelper

@Route(path = ARouterConstant.APP_COMMON_SERVICE)
class AppCommonServiceImpl : AppCommonService {
    //todo
    override fun umengCallBack(resume: Boolean, context: Context) {
    }

    override fun crashCallBack(e: UnExpectedCrashExceptionForhuanqueApp) {
    }

    override fun activityResultCallBack(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun loginSuccess(session: Session) {
        logger("登录成功的通知回调 id=${session.userId}")
        //绑定极光
        val userId = session.userId.toString()
        val tagAliasBean = TagAliasOperatorHelper.TagAliasBean()
        tagAliasBean.action = TagAliasOperatorHelper.ACTION_SET
        tagAliasBean.isAliasAction = true
        tagAliasBean.alias = userId
        TagAliasOperatorHelper.getInstance().handleAction(tagAliasBean)
    }

    override fun logout() {
    }

    override fun init(context: Context?) {
    }

}