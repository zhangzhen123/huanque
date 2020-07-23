package com.julun.huanque.core.ui.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.WithdrawRecord
import com.julun.huanque.common.bean.forms.WithdrawHistoryForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.net.WithDrawService

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


    private val service: WithDrawService by lazy { Requests.create(WithDrawService::class.java) }

    private var lastId: Long? = null

    val historyData: LiveData<ReactiveData<RootListData<WithdrawRecord>>> = queryState.switchMap { type ->
        liveData {
            if (type != QueryType.LOAD_MORE) {
                lastId = null
            }
            val form = WithdrawHistoryForm(lastId)
            request({
                val result = service.withdrawHistory(form).dataConvert()
                lastId=result.list.lastOrNull()?.orderNo
                result.isPull = type != QueryType.LOAD_MORE
                emit(result.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, needLoadState = type == QueryType.INIT)


        }

    }


}