package com.julun.huanque.common.manager

import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.utils.SessionUtils

/**
 * Created by djp on 2016/12/9.
 * @author djp
 */
object SessionManager  {


//    private val service: AppService by lazy { Requests.create(AppService::class.java) }

    //是否验证过session
    var isCheckSession = false

    // 创建一个游客的身份
    fun doCreateGuestSession(callback: (Boolean) -> Unit) {
//        if (RongCloudUtils.isGuest()) {
//            // 如果当前是游客身份，就直接调用completeBlock
//            callback(true)
//            return
//        }
//
//        newService.getOrCreateSession().handleResponse(makeSubscriber<NewSession> {
//            SessionUtils.saveSession(it)
//            callback(true)
//        }.ifError { callback(false) }
//                .withSpecifiedCodes(1103))//融云Token获取失败

    }


    /**
     * 登录成功
     */
    fun loginSuccess(it: Session) {
        SessionUtils.setSession(it)
        //通知登录成功
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.loginSuccess(it)

    }

    /**
     * 退出登录
     */
    fun doLogout(callback: () -> Unit) {
//        service.logout(SessionForm()).handleResponse(makeSubscriber {
//            SharedPreferencesUtils.commitBoolean(ParamConstant.JUVENILES_PROTECT_CHECK, false)
//            SessionUtils.deleteSession {
//                //推出登录，清空之前设置的未成年密码
//                if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)
//                    RongCloudManager.logout(callback)
//                else {
//                    callback()
//                }
//            }
//            loginOrLogOut(false)
//            EventBus.getDefault().post(LoginEvent(BusiConstant.EXIT_LOGIN_RESULT_CODE))
////            ZegoApiManager.getInstance().refreshUserInfo()
////            val tagAliasBean = com.julun.huanque.lmcore.lmapp.jpush.TagAliasOperatorHelper.TagAliasBean()
////            tagAliasBean.action = com.julun.huanque.lmcore.lmapp.jpush.TagAliasOperatorHelper.ACTION_DELETE
////            tagAliasBean.isAliasAction = true
////            com.julun.huanque.lmcore.lmapp.jpush.TagAliasOperatorHelper.getInstance().handleAction(tagAliasBean)
////            CommonInit.getInstance().getCommonListener()?.logout()
//
//        })
    }

}