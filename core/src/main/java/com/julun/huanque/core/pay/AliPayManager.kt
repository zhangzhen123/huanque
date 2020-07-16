package com.julun.huanque.core.pay

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import com.alipay.sdk.app.AuthTask
import com.alipay.sdk.app.PayTask
import com.julun.huanque.common.bean.beans.alipay.AliAuthResult
import com.julun.huanque.common.bean.beans.alipay.AliPayResult
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.constant.PayResult
import com.julun.huanque.common.constant.PayType
import com.julun.huanque.common.utils.ULog
import org.greenrobot.eventbus.EventBus


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/13 15:54
 *
 *@Description: AliPayManager
 *
 */
object AliPayManager {

    private val SDK_PAY_FLAG = 1
    private val SDK_AUTH_FLAG = 2

    /**
     * 支付宝支付功能
     */
    fun payOrder(activity: Activity, orderInfo: String) {
        val payRunnable = Runnable {
            val alipay = PayTask(activity)
            val result = alipay.payV2(orderInfo, true)
            val msg = Message()
            msg.obj = result
            msg.what = SDK_PAY_FLAG
            mHandler.sendMessage(msg)
        }
        Thread(payRunnable).start()
    }

    /**
     * 支付宝授权功能
     */
    fun aliAuth(activity: Activity, authInfo: String) {
        val payRunnable = Runnable {
            // 构造AuthTask 对象
            val authTask = AuthTask(activity)
            // 调用授权接口
            // AuthTask#authV2(String info, boolean isShowLoading)，
            // 获取授权结果。
            val result = authTask.authV2(authInfo, true)
            // 将授权结果以 Message 的形式传递给 App 的其它部分处理。
            // 对授权结果的处理逻辑可以参考支付宝 SDK Demo 中的实现。
            val msg = Message()
            msg.what = SDK_AUTH_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }
        Thread(payRunnable).start()
    }

    //支付宝结果处理
    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            ULog.i("handleMessage = $msg")
            if (msg.what == SDK_PAY_FLAG) {
                @SuppressWarnings("unchecked")
                val payResult = AliPayResult((msg.obj as? Map<String, String>) ?: return)
                val resultStatus = payResult.resultStatus
                ULog.i("DXC  payResult = $payResult")
                when (resultStatus) {
                    "9000" -> EventBus.getDefault().post(
                        PayResultEvent(PayResult.PAY_SUCCESS, PayType.AliPayApp)
                    )
                    "6001" -> EventBus.getDefault().post(PayResultEvent(PayResult.PAY_CANCEL, PayType.AliPayApp))
                    "5000" -> EventBus.getDefault().post(PayResultEvent(PayResult.PAY_REPETITION, PayType.AliPayApp))
                    else -> EventBus.getDefault().post(PayResultEvent(PayResult.PAY_FAIL, PayType.AliPayApp))
                }
            } else if (msg.what == SDK_AUTH_FLAG) {
                val authResult = AliAuthResult(msg.obj as Map<String?, String?>, true)
                val resultStatus: String = authResult.resultStatus

                // 判断resultStatus 为“9000”且result_code
                // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档

                // 判断resultStatus 为“9000”且result_code
                // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.resultCode, "200")) {
                    // 获取alipay_open_id，调支付时作为参数extern_token 的value
                    // 传入，则支付账户为该授权账户
                    //授权成功
                } else {

                    //授权失败
                }
            }

        }
    }


}