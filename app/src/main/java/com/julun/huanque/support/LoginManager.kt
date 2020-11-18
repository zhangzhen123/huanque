package com.julun.huanque.support

import com.alibaba.android.arouter.launcher.ARouter
import com.ishumei.smantifraud.SmAntiFraud
import com.julun.huanque.BuildConfig
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.forms.MobileLoginForm
import com.julun.huanque.common.bean.forms.MobileQuickForm
import com.julun.huanque.common.bean.forms.WeiXinForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.net.NError
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.utils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.rong.imlib.RongIMClient
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2017/8/24
 *
 * 用于管理所有的登录 把微信登录 qq登录 以后可能加入手机登录等等全部整合统一回调
 *
 **/
object LoginManager {
    val logger = ULog.getLogger("LoginManager")
    const val WECHAT_LOGIN = 0
    const val MOBILE_LOGIN = 1
    const val MOBILE_FAST_LOGIN = 2
    const val QQ_LOGIN = 3
    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

//    /**
//     * 对第三方登录成功回调后 统一请求后台和回调
//     */
//    suspend fun callbackSuccess(type: Int, code: String, loginSuccess: (Session) -> Unit = {}) {
//        when (type) {
//            WECHAT_LOGIN -> {
//                doLoginByWinXin(code) {
//                    delayToConnectRong()
//                    loginSuccess(it)
//                }
//            }
//            QQ_LOGIN -> {
//                //增加一个小延时 qq授权后可能无法后台立即刷新权限 延时再去获取试试 todo
////                Observable.timer(200, TimeUnit.MILLISECONDS)
////                    .observeOn(AndroidSchedulers.mainThread())
////                    .subscribe {
////                        doLoginByQQ(code) {
////                            delayToConnectRong()
////                            loginSuccess()
////                        }
////                    }
//            }
//        }
//    }


    //微信登录接口是否处于请求中
    private var isLogging = false

    // 微信授权登录
    suspend fun doLoginByWinXin(code: String, success: (Session) -> Unit) {
        if (isLogging) {
            ToastUtils.show("当前正在登录中，请不要重复操作")
            return
        }
//        GlobalScope.launch {
        try {
            isLogging = true
            val deviceID = SmAntiFraud.getDeviceId() ?: ""
            val result = userService.weiXinLogin(WeiXinForm(code, BuildConfig.WX_APP_ID, deviceID)).dataConvert()
            loginSuccess(result, WECHAT_LOGIN, success)

        } catch (e: Exception) {
            e.printStackTrace()
//            var message = "${e.message}"
//            if (e is SocketTimeoutException) {
//                message = "连接超时,可能是网络不太好,您可以稍后重试"
//            }
//            if (e is ResponseError) {
//                message = e.busiMessage
//            }
//            ToastUtils.show("登陆出错!\n $message ")
        } finally {
            isLogging = false
        }
//        }
    }

    //手机登录
    suspend fun loginByMobile(phoneNum: String, code: String, success: (Session) -> Unit, error: NError? = null) {
        if (isLogging) {
            ToastUtils.show("当前正在登录中，请不要重复操作")
            return
        }
        try {
            isLogging = true
            val result =
                userService.mobileLogin(MobileLoginForm(phoneNum, code, shuMeiDeviceId = SmAntiFraud.getDeviceId() ?: ""))
                    .dataConvert()
            loginSuccess(result, MOBILE_LOGIN, success)

        } catch (e: Exception) {
            error?.invoke(e)
        } finally {
            isLogging = false
        }
    }

    /**
     * 手机号一键登录
     */
    suspend fun fastLogin(jToken: String, success: (Session) -> Unit, error: NError? = null) {
        if (isLogging) {
            ToastUtils.show("当前正在登录中，请不要重复操作")
            return
        }
        try {
            isLogging = true
            val result = userService.mobileQuick(MobileQuickForm(jToken, SmAntiFraud.getDeviceId() ?: "")).dataConvert()
            loginSuccess(result, MOBILE_FAST_LOGIN, success)

        } catch (e: Exception) {
            error?.invoke(e)
        } finally {
            isLogging = false
        }
    }

    //处理融云
    private fun processRongYun(result: Session, success: (Session) -> Unit) {
        if (result.regComplete) {
            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                RongCloudManager.logout {
                    delayToConnectRong()
                    success(result)
                }
            } else {
                delayToConnectRong()
                success(result)
            }

        } else {
            success(result)
        }

    }

    /**
     * 登录成功 可能没有注册完成
     */
    private fun loginSuccess(
        session: Session,
        type: Int = 0,
        success: (Session) -> Unit
    ) {
        SessionUtils.setSession(session)
        //如果登录的账号是注册完整的 就执行完整的登录后续操作（心跳，融云等等）
        SPUtils.commitString(SPParamKey.AgreeUp,session.agreeUp)
        StorageHelper.setDefaultHomeTab(session.defaultHomeTab)
        StorageHelper.setHideSocialTab(session.hideSocialTab)
        if (session.regComplete) {
            (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
                .navigation() as? AppCommonService)?.loginSuccess(session)
            EventBus.getDefault().post(LoginEvent(true))
            LoginStatusUtils.loginSuccess()
            UserHeartManager.startCheckOnline(true)
            processRongYun(session, success)
        } else {
            //如果没有注册完成标志 也通知回调 让登录页做后续操作
            success(session)
        }


    }

    /**
     * 完善注册后 代表真正的成功
     */
    fun loginSuccessComplete() {
        SessionUtils.setRegComplete(true)
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.loginSuccess(SessionUtils.getSession())
        EventBus.getDefault().post(LoginEvent(true))
        LoginStatusUtils.loginSuccess()
        //连接融云
        RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
        UserHeartManager.startCheckOnline(true)
    }

    fun doLoginOut(success: () -> Unit, error: NError? = null) {
        GlobalScope.launch {
            kotlin.runCatching {
                /*  val result = */userService.logout().dataConvert()
                withContext(Dispatchers.Main) {
                    loginOutSuccess(success, error)
                }

            }.onFailure {
                it.printStackTrace()
                withContext(Dispatchers.Main) {
                    error?.invoke(it)
                }
            }
        }

    }

    /**
     * 退出登录成功 清除数据 断开心跳 以及断开融云
     * 注销成功也是执行这个
     */
    fun loginOutSuccess(success: () -> Unit, error: NError? = null) {
        try {
            SessionUtils.clearSession()
            UserHeartManager.stopBeat()
            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)
                RongCloudManager.logout { success() }
            else {
                success()
            }
            LoginStatusUtils.logout()
            EventBus.getDefault().post(LoginEvent(false))
        } catch (e: Exception) {
            error?.invoke(e)
        }
    }

    /**
     * 之所以加延迟 是为了确保融云已经断开 如果直接重连可能会失败 融云会有延迟
     */
    private fun delayToConnectRong() {
        RongCloudManager.clearRoomList()
        Observable.timer(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //后台获取ImToken成功   连接融云
                ULog.i("DXC 登录成功  连接融云")
                RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
            }
    }

    //qq授权登录
    fun doLoginByQQ(code: String, callback: () -> Unit = {}) {
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


}