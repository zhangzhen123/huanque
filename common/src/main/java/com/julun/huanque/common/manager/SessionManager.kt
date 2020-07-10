package com.julun.huanque.common.manager

import com.alibaba.android.arouter.launcher.ARouter
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

    //微信登录接口是否处于请求中
    private var wxLogining = false

    // 微信授权登录
    fun doLoginByWinXin(code: String, programId: Int? = null, callback: () -> Unit = {}) {
//        if (wxLogining) {
//            return
//        }
//        wxLogining = true
//        val deviceID = (ProviderPoolManager.getService(ARouterConstant.SHUMEI_SERVICE) as? ShuMeiDeviceIdService)?.getDeviceId() ?: ""
//        service.loginByWeixin(NewWeixinOpenLoginForm(code, programId, deviceID)).handleResponse(makeSubscriber<NewSession> {
//            loginSuccess(it, "微信")
//            // 当前连接着融云，先退出，因为token变了，需要退出重连
//            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
//                RongCloudManager.logout(callback)
//            } else {
//                callback()
//            }
//        }.ifError {
//            var message = "${it.message}"
//            if (it is SocketTimeoutException) {
//                message = "连接超时,可能是网络不太好,您可以稍后重试"
//            }
//            if (it is ResponseError) {
//                message = it.busiMessage
//                EventBus.getDefault().post(LoginErrorEvent(it.busiCode))
//            }
//            showErrorAlertView(0, "登陆出错!\n $message ")
//        }.withSpecifiedCodes(-1).withFinalCall { wxLogining = false })

    }

    //快捷登录接口是否处于请求中
    private var mFastLogining = false

    /**
     * 手机号快捷登录
     */
    fun doLoginByFastLogin(loginToken: String, programId: Int? = null, callback: () -> Unit = {}, errCallback: () -> Unit) {
//        val deviceID = (ProviderPoolManager.getService(ARouterConstant.SHUMEI_SERVICE) as? ShuMeiDeviceIdService)?.getDeviceId() ?: ""
//        service.mobileQuickLogin(MobileFastLoginForm(loginToken, programId, deviceID))
//                .handleResponse(makeSubscriber<NewSession> {
//                    loginSuccess(it, "本机号码")
//                    // 当前连接着融云，先退出，因为token变了，需要退出重连
//                    if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
//                        RongCloudManager.logout(callback)
//                    } else {
//                        callback()
//                    }
//                }.ifError {
//                    var message = "${it.message}"
//                    if (it is SocketTimeoutException) {
//                        message = "连接超时,可能是网络不太好,您可以稍后重试"
//                    }
//                    if (it is ResponseError) {
//                        EventBus.getDefault().post(LoginErrorEvent(it.busiCode))
//                        when (it.busiCode) {
//                            5015, 5011, 5012, 5010, 501 -> {
//                                ToastUtils.show(it.busiMessage)
//                            }
//                            else -> {
//                                ToastUtils.show(message)
//                            }
//                        }
//                    }
//                    errCallback()
//                }.withSpecifiedCodes(5015, 5011, 5012, 5010, 501))
    }

    //QQ登录接口是否处于请求中
    private var qqLogining = false

    //qq授权登录
    fun doLoginByQQ(code: String, programId: Int? = null, callback: () -> Unit = {}) {
        // 这里如果本地存在游客sessionId，
        // 就传入游客的sessionId进行回收
//        if (qqLogining) {
//            return
//        }
//        qqLogining = true
//        val deviceID = (ProviderPoolManager.getService(ARouterConstant.SHUMEI_SERVICE) as? ShuMeiDeviceIdService)?.getDeviceId() ?: ""
//        service.loginByQQ(NewQQOpenLoginForm(code, programId, deviceID)).handleResponse(makeSubscriber<NewSession> {
//            loginSuccess(it, "QQ")
//            // 当前连接着融云，先退出，因为token变了，需要退出重连
//            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
//                RongCloudManager.logout(callback)
//            } else {
//                callback()
//            }
//        }.ifError {
//            var message = "${it.message}"
//            if (it is SocketTimeoutException) {
//                message = "连接超时,可能是网络不太好,您可以稍后重试"
//            }
//            if (it is ResponseError) {
//                message = it.busiMessage
//                EventBus.getDefault().post(LoginErrorEvent(it.busiCode))
//            }
//            showErrorAlertView(0, "登陆出错!\n $message ")
//        }.withSpecifiedCodes(-1).withFinalCall { qqLogining = false })

    }


//    //心聊用户登录
//    fun doLoginByXinLiao(nickname: String, headPic: String, openId: String, sex: String, callback: () -> Unit = {}) {
//        // 这里如果本地存在游客sessionId，
//        // 就传入游客的sessionId进行回收
//        GIODataPool.setLoginType(GioSourceLogin.SDK)
//        service.loginByHeartChat(HeartChatLoginForm(nickname, headPic, openId, sex)).handleResponse(makeSubscriber<NewSession> {
//            loginSuccess(it)
//            // 当前连接着融云，先退出，因为token变了，需要退出重连
//            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
//                RongCloudManager.logout(callback)
//            } else {
//                callback()
//            }
////            huanqueInit.getInstance().gethuanqueListener()?.loginCallBack(BusiConstant.ErrorCodes.RIGHT)
//        }.ifError {
//            var message = "${it.message}"
//            if (it is SocketTimeoutException) {
//                message = "连接超时,可能是网络不太好,您可以稍后重试"
//            }
////            if (it is ResponseError) {
////                message = it.busiMessage
////                huanqueInit.getInstance().gethuanqueListener()?.loginCallBack(it.busiCode)
////            } else {
////                huanqueInit.getInstance().gethuanqueListener()?.loginCallBack(10000)
////            }
////            if (!JuLunLibrary.getInstance().inSDK) {
////                showErrorAlertView(0, "登陆出错!\n $message ")
////            }
//            it.printStackTrace()
//        }.withSpecifiedCodes(-1))
//
//    }

    //手机登录
    fun loginByMobile(mobile: String, code: String, programId: Int? = null, callback: () -> Unit = {}, errCallback: () -> Unit) {
        // 这里如果本地存在游客sessionId，
        // 就传入游客的sessionId进行回收
//        val deviceID = (ProviderPoolManager.getService(ARouterConstant.SHUMEI_SERVICE) as? ShuMeiDeviceIdService)?.getDeviceId() ?: ""
//        service.loginByMobile(MobileLoginForm(mobile, code, programId, deviceID)).handleResponse(makeSubscriber<NewSession> {
//            loginSuccess(it, "其他手机")
//            // 当前连接着融云，先退出，因为token变了，需要退出重连
//            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
//                RongCloudManager.logout(callback)
//            } else {
//                callback()
//            }
//        }.ifError {
//            var message = "${it.message}"
//            if (it is SocketTimeoutException) {
//                message = "连接超时,可能是网络不太好,您可以稍后重试"
//            }
//            if (it is ResponseError) {
//                EventBus.getDefault().post(LoginErrorEvent(it.busiCode))
//                when (it.busiCode) {
//                    5015, 5011, 5012, 5010, 501 -> {
//                        ToastUtils.show(it.busiMessage)
//                    }
//                    else -> {
//                        ToastUtils.show(message)
//                    }
//                }
//            }
//            errCallback()
//        }.withSpecifiedCodes(5015, 5011, 5012, 5010, 501))
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