package com.julun.huanque.core.ui.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.net.RechargeService

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


    private val service: RechargeService by lazy { Requests.create(RechargeService::class.java) }

    val withdrawData: LiveData<ReactiveData<WithdrawInfo>> = queryState.switchMap {
        liveData {
            request({
                val result = service.queryWithdrawInfo().dataConvert()
                emit(result.coverRtData())
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.coverError())
            }, final = {
                logger("最终返回")
            }, needLoadState = it == QueryType.INIT)


        }

    }



}