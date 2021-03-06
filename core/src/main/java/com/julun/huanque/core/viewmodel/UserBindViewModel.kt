package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.AliAuthInfo
import com.julun.huanque.common.bean.beans.BindResultBean
import com.julun.huanque.common.bean.forms.BindForm
import com.julun.huanque.common.bean.forms.BindPhoneForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.net.services.UserService
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/20 15:30
 *
 *@Description: UserBindViewModel 用户绑定手机以及微信和支付宝操作
 *
 */
class UserBindViewModel : BaseViewModel() {
    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

    //绑定微信结果
    val bindWinXinData: MutableLiveData<ReactiveData<BindResultBean>> by lazy { MutableLiveData<ReactiveData<BindResultBean>>() }

    //获取支付宝信息
    val aliPayAuth: MutableLiveData<ReactiveData<AliAuthInfo>> by lazy { MutableLiveData<ReactiveData<AliAuthInfo>>() }


    //绑定支付宝结果
    val bindAliData: MutableLiveData<ReactiveData<BindResultBean>> by lazy { MutableLiveData<ReactiveData<BindResultBean>>() }


    //绑定手机结果
    val bindPhoneData: MutableLiveData<ReactiveData<VoidResult>> by lazy { MutableLiveData<ReactiveData<VoidResult>>() }

    // 微信绑定操作
    fun bindWinXin(code: String) {
        viewModelScope.launch {
            request({
                val result = userService.bindWeiXin(BindForm(code)).dataConvert(intArrayOf(ErrorCodes.HAS_BIND_OTHER))
                bindWinXinData.postValue(result.convertRtData())
            }, error = {
                bindWinXinData.postValue(it.convertError())
            })
        }


    }

    // 获取支付宝授权信息
    fun getAliPayAuthInfo() {
        viewModelScope.launch {
            request({
                val result = userService.getAlipayAuthInfo().dataConvert()
                aliPayAuth.postValue(result.convertRtData())
            }, error = {
                aliPayAuth.postValue(it.convertError())
            })

        }


    }

    // 支付宝绑定操作
    fun bindAliPay(code: String) {
        viewModelScope.launch {
            request({
                val result = userService.bindAliPay(BindForm(code)).dataConvert(intArrayOf(ErrorCodes.HAS_BIND_OTHER))
                bindAliData.postValue(result.convertRtData())
            }, error = {
                bindAliData.postValue(it.convertError())
            })

        }


    }

    // 手机绑定操作
    fun bindPhone(phone: String, code: String) {
        viewModelScope.launch {
            request({
                val result = userService.bindMobile(BindPhoneForm(phone, code)).dataConvert()
                bindPhoneData.postValue(result.convertRtData())
            }, error = {
                bindPhoneData.postValue(it.convertError())
            })

        }


    }
}