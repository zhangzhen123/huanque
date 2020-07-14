package com.julun.huanque.core.ui.recharge

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.RechargeRespDto
import com.julun.huanque.common.bean.forms.RechargeRuleQueryForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.coverError
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.core.net.RechargeService


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/13 10:34
 *
 *@Description: RechargeFragmentViewModel
 *
 */
class RechargeFragmentViewModel : BaseViewModel() {

    private val service: RechargeService by lazy { Requests.create(RechargeService::class.java) }
    var curTargetUserId: Long = -1L
    val mRechargeRespDto: LiveData<ReactiveData<RechargeRespDto>> = queryState.switchMap {
        liveData {
            request({
                val form = RechargeRuleQueryForm()
                if (curTargetUserId != -1L) {
                    form.targetUserId = curTargetUserId
                }
                val info = service.queryChannelRule(form)
                    .dataConvert()
                emit(ReactiveData(NetStateType.SUCCESS, info))
            }, error = { e ->
                logger("报错了：$e")
                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
            }, final = {
                logger("最终返回")
            }, needLoadState = it == QueryType.INIT)


        }

    }


    //余额数据
    val balance: LiveData<Long> by lazy { BalanceUtils.getBalance() }


}