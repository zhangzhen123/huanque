package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.FirstRechargeInfo
import com.julun.huanque.common.bean.beans.OppoPayInfo
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.bean.beans.PayResultInfo
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.RechargeService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.utils.ToastUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/10/22 17:10
 *@描述 首充ViewModel
 */
class FirstRechargeViewModel : BaseViewModel() {
    private val service: RechargeService by lazy { Requests.create(RechargeService::class.java) }

    private val mUserService: UserService by lazy { Requests.create(UserService::class.java) }

    private var mDisposable: Disposable? = null

    val showTime: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val payTypeFlag: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val payValue: MutableLiveData<PayResultInfo> by lazy { MutableLiveData<PayResultInfo>() }

    //首充数据
    val firstRechargeBean: MutableLiveData<FirstRechargeInfo> by lazy { MutableLiveData<FirstRechargeInfo>() }

    //是否有首充的标识
    val firstRechargeFlag : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    fun startCountDown() {
        mDisposable?.dispose()

        val count = computeTimeToNextDay()

        mDisposable = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
            .map {
                //明天00：00 的时间
                val realCount = count - it * 100
                if (realCount < 0) {
                    return@map ""
                }
                val time = TimeUtils.countDownTimeFormat(realCount / 1000)
                //获取小数
                val count = realCount % 1000 / 100
                return@map "${time}.$count"
            }
            .subscribe({
                if (it.isNotEmpty()) {
                    showTime.postValue(it)
                } else {
                    mDisposable?.dispose()
                }
            }, {})

    }

    /**
     * 计算当前时间到明天0点的时间差
     */
    private fun computeTimeToNextDay(): Long {
        val nextDayCalendar = Calendar.getInstance()
        nextDayCalendar.add(Calendar.DAY_OF_MONTH, 1)
        nextDayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        nextDayCalendar.set(Calendar.MINUTE, 0)
        nextDayCalendar.set(Calendar.SECOND, 0)
        nextDayCalendar.set(Calendar.MILLISECOND, 0)
        val nextDate = nextDayCalendar.time.time

        //当前时间
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time.time
        //当前时间到第二天0点的时间差
        return nextDate - currentDate
    }

    /**
     * 获取首充礼包数据
     */
    fun getFirstRechargeData() {
        viewModelScope.launch {
            request({
                val result = mUserService.firstChargeBag().dataConvert()
                firstRechargeBean.value = result
            })
        }
    }

    private var loadPay = false

    /**
     * 获取新接口的创建充值订单
     */
    fun queryNewCreateAppPay(pay: PayForm) {
        if (loadPay) {
            return
        }
        loadPay = true
        viewModelScope.launch {
            request({
                val result = service.createAppPay(pay).dataConvert(intArrayOf(ErrorCodes.MONTH_TICKETS_NOT_ENOUGH))
                //这里处理可变类型的解析不再放在jsonUtils统一处理 因为只此一处使用 没必要每次都判断
                val value = result.content
                if (value is String) {
                    result.alipayOrderInfo = value
                } else if (value is Map<*, *>) {
                    result.wxOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.serializeAsString(value), OrderInfo::class.java)
                    result.oppoOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.serializeAsString(value), OppoPayInfo::class.java)
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
                delay(300L)
                loadPay = false
            })

        }
    }


    override fun onCleared() {
        super.onCleared()
        mDisposable?.dispose()
    }
}