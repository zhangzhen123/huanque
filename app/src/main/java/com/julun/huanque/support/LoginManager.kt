package com.julun.huanque.support

import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.SessionManager
import com.julun.huanque.common.utils.ULog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
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
    const val QQ_LOGIN = 1

    /**
     * 对登录成功回调后 统一请求后台和回调
     */
    fun loginSuccess(type: Int, code: String/*, loginSuccess: () -> Unit = {}*/) {
        when (type) {
            WECHAT_LOGIN -> {
                doLoginByWinXin(code) {
//                    SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.WEIXIN)
                    delayCallBack()
                }
            }
            QQ_LOGIN -> {
                //增加一个小延时 qq授权后可能无法后台立即刷新权限 延时再去获取试试
                Observable.timer(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        doLoginByQQ(code) {
//                                SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.QQ)
                            delayCallBack()
                        }
                    }
            }
        }
    }
    /**
     * 之所以加延迟 是为了确保融云已经断开 如果直接重连可能会失败 融云会有延迟
     */
    private fun delayCallBack(loginSuccess: () -> Unit = {}) {
        RongCloudManager.clearRoomList()
        Observable.timer(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //后台获取ImToken成功   连接融云
                ULog.i("DXC 登录成功  连接融云")
                RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
                EventBus.getDefault().post(LoginEvent(true))
                loginSuccess()
            }
    }

    //微信登录接口是否处于请求中
    private var wxLogining = false

    // 微信授权登录
    fun doLoginByWinXin(code: String, callback: () -> Unit = {}) {
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
    fun doLoginByFastLogin(loginToken: String, callback: () -> Unit = {}, errCallback: (ResponseError) -> Unit) {
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

    //手机登录
    fun loginByMobile(mobile: String, code: String, callback: () -> Unit = {}, errCallback: (ResponseError) -> Unit) {
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

}