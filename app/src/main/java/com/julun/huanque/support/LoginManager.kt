package com.julun.huanque.support

import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.SessionManager
import com.julun.huanque.common.utils.ULog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
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
    val WECHAT_LOGIN = 0
    val QQ_LOGIN = 1

    /**
     * 对登录成功回调后 统一请求后台和回调
     */
    fun loginSuccess(type: Int, code: String/*, loginSuccess: () -> Unit = {}*/) {
        when (type) {
            WECHAT_LOGIN -> {
                SessionManager.doLoginByWinXin(code) {
//                    SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.WEIXIN)
                    delayCallBack()
                }
            }
            QQ_LOGIN -> {
                //增加一个小延时 qq授权后可能无法后台立即刷新权限 延时再去获取试试
                Observable.timer(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        SessionManager.doLoginByQQ(code) {
//                                SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.QQ)
                            delayCallBack()
                        }
                    }
            }
        }
    }

    /**
     * 手机登录/动态码登录
     * authToken和authCode 同时不为空  标识动态码登录
     */
    fun loginForMobile(
        phone: String,
        code: String,
        authToken: String? = null,
        authCode: String? = null,
        loginSuccess: () -> Unit = {},
        loginError: (ResponseError) -> Unit
    ) {
        SessionManager.loginByMobile(phone, code, {
            //登陆成功
            delayCallBack(loginSuccess)
//            SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.PHONE)
        }, loginError)
    }


    /**
     * 手机号快捷登录
     */
    fun loginForFastLogin(loginToken: String, loginSuccess: () -> Unit = {}, errCallback: (ResponseError) -> Unit) {
        SessionManager.doLoginByFastLogin(loginToken, {
//            SharedPreferencesUtils.commitString(ParamConstant.LAST_LOGIN_TYPE, BusiConstant.PHONE)
            delayCallBack(loginSuccess)
        }, errCallback)
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
                //                    EventBus.getDefault().post(LoginEvent(SUCCESS_LOGIN_RESULT_CODE))
                loginSuccess()
            }
    }
}