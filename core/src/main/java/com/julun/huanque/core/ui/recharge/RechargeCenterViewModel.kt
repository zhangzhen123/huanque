package com.julun.huanque.core.ui.recharge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.OppoPayInfo
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.beans.RechargeRespDto
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.net.RechargeService
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by dong on 2017/6/28.
 */
class RechargeCenterViewModel : BaseViewModel() {
    val payPosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val payValue: MutableLiveData<PayResultInfo> by lazy { MutableLiveData<PayResultInfo>() }

    //只是关闭加载框
    val isShowPayLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val data: MutableLiveData<RechargeRespDto> by lazy { MutableLiveData<RechargeRespDto>() }
    val isRefresh: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val getHelpUrl: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //true  隐藏弹窗
    val hideState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //oppo支付相关
//    var mOppoService: OppoPayService? = null

    //优惠券的回调
//    val couponInfo: MutableLiveData<CouponItemInfo> by lazy { MutableLiveData<CouponItemInfo>() }


    init {
//        mOppoService = ARouter.getInstance().build(ARouterConstant.OPPOPAY_MANAGER).navigation() as? OppoPayService
    }

    private val service: RechargeService by lazy { Requests.create(RechargeService::class.java) }

    /**
     * 获取新接口的创建充值订单
     */
    fun queryNewCreateAppPay(pay: PayForm) {
        isShowPayLoading.value = true
        viewModelScope.launch {
            request({
                val result = service.createAppPay(pay).dataConvert(intArrayOf(ErrorCodes.MONTH_TICKETS_NOT_ENOUGH))
                //todo 这里处理可变类型的解析不再放在jsonUtils统一处理 因为只此一处使用 没必要每次都判断
                val value=result.content
                if (value is String) {
                    result.alipayOrderInfo = value
                } else if (value is Map<*, *>) {
                    result.wxOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.seriazileAsString(value), OrderInfo::class.java)
                    result.oppoOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.seriazileAsString(value), OppoPayInfo::class.java)
                }
                result.payType = pay.payType
                payValue.value = result
            }, error = {
                it.printStackTrace()
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                } else {
                    ToastUtils.show("网络出现了问题")
                }
            }, final = {
                mDelayDispose?.dispose()
                delay(300L)
                isShowPayLoading.value = false
            })

        }
    }

    private var mDelayDispose: Disposable? = null
    override fun onCleared() {
        super.onCleared()
        mDelayDispose?.dispose()
    }

    /**
     * 直播间快捷充值信息
     */
    fun queryRecomPay(pay: PayForm) {
//        service.queryRecomPay(pay)
//            .handleResponse(makeSubscriber<RechargeRespDto> {
//                data.value = it
//            }.ifError {
//                data.value = null
//                if (it is ResponseError) {
//                    when (it.busiCode) {
//                        1101 -> ToastUtils.show(it.busiMessage)
//                    }
//                } else {
//                    ToastUtils.show("网络出现了问题")
//                }
//            }.withSpecifiedCodes(1101))
    }


}