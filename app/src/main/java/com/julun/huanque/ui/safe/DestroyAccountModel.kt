package com.julun.huanque.ui.safe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.DestroyAccountResult
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/12/5 11:22
 *
 *@Description: DestroyAccountModel
 *
 */

class DestroyAccountModel : BaseViewModel() {

    val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }


    //注销成功 值为null代表注销正常完成 不是null需要审核等操作
    val applyResult: MutableLiveData<ReactiveData<VoidResult>> by lazy { MutableLiveData<ReactiveData<VoidResult>>() }

    fun destroyAccount() {
        viewModelScope.launch {
            request({
                val result = userService.destroyAccount().dataConvert(intArrayOf(1304,1305))
                applyResult.value = result.convertRtData()
            }, error = {
                applyResult.value = it.convertError()
            })
        }

    }


}