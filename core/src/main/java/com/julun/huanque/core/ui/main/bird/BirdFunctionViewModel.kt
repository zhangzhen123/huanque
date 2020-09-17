package com.julun.huanque.core.ui.main.bird

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.BirdFlyResult
import com.julun.huanque.common.bean.beans.BirdShopInfo
import com.julun.huanque.common.bean.forms.BirdFunctionForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LeYuanService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/10 17:42
 *
 *@Description: BirdFunctionViewModel
 *
 */
class BirdFunctionViewModel : BaseViewModel() {

    private val service: LeYuanService by lazy {
        Requests.create(LeYuanService::class.java)
    }

    val flyResult: MutableLiveData<ReactiveData<BirdFlyResult>> by lazy { MutableLiveData<ReactiveData<BirdFlyResult>>() }
    fun fly(code: String) {
        viewModelScope.launch {
            request({
                val result = service.fly(BirdFunctionForm(code)).dataConvert()
                flyResult.value = result.convertRtData()
            }, error = {
                flyResult.value = it.convertError()
            })

        }

    }

}