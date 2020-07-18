package com.julun.huanque.core.ui.withdraw

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.bean.forms.WithdrawApplyForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.WithdrawErrorCode
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.net.WithDrawService
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 11:46
 *
 *@Description: WithdrawViewModel
 *
 */
class WithdrawViewModel : BaseViewModel() {


    private val service: WithDrawService by lazy { Requests.create(WithDrawService::class.java) }

    val withdrawResult:MutableLiveData<ReactiveData<Any>> by lazy { MutableLiveData<ReactiveData<Any>>() }

    val withdrawData: LiveData<ReactiveData<WithdrawInfo>> = queryState.switchMap {
        liveData {
            request({
                val result = service.queryWithdrawInfo().dataConvert()
                emit(result.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = it == QueryType.INIT)


        }

    }

    fun startApplyWithdraw(tplId:Int,type:String){
        viewModelScope.launch {
            request({
                val form= WithdrawApplyForm(tplId,type)
                val result=service.applyWithdraw(form).dataConvert(intArrayOf(WithdrawErrorCode.NO_VERIFIED,WithdrawErrorCode.NO_BIND_PHONE))
                withdrawResult.value=result.convertRtData()
            },error = {
                withdrawResult.value=it.convertError()
            })
        }

    }


}