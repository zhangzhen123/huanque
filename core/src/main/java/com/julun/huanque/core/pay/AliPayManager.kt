package com.julun.huanque.core.pay

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Message
import com.alipay.sdk.app.PayTask
import com.julun.huanque.common.bean.beans.alipay.AliPayResult
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.PayResult
import com.julun.huanque.common.constant.PayType
import com.julun.huanque.common.helper.MixedHelper
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

    fun payOrder(activity: Activity, orderInfo: String) {
        val payRunnable = Runnable {
            val alipay = PayTask(activity)
            val result = alipay.payV2(orderInfo, true)
            val msg = Message()
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
        }
    }

}