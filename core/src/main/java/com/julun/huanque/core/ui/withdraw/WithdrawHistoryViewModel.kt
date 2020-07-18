package com.julun.huanque.core.ui.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.net.RechargeService
import kotlinx.coroutines.delay

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 11:46
 *
 *@Description: WithdrawViewModel
 *
 */
class WithdrawHistoryViewModel : BaseViewModel() {


    private val service: RechargeService by lazy { Requests.create(RechargeService::class.java) }

    private var offset = 0

    val historyData: LiveData<ReactiveData<RootListData<Any>>> = queryState.switchMap { type ->
        liveData {
            if (type != QueryType.LOAD_MORE) {
                offset = 0
            }
            request({
//                val result = service.queryWithdrawInfo().dataConvert()
                //todo
                delay(500)
                val list = mutableListOf<Any>()
                repeat(10) {
                    list.add(Any())
                }
                val rl = RootListData(isPull = type != QueryType.LOAD_MORE, list = list, hasMore = true)
                emit(rl.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)


        }

    }


}