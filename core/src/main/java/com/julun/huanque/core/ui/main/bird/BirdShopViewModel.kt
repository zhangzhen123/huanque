package com.julun.huanque.core.ui.main.bird

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.BirdShopInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LeYuanService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description:
 *
 */
class BirdShopViewModel : BaseViewModel() {

    private val service: LeYuanService by lazy {
        Requests.create(LeYuanService::class.java)
    }

    val shopInfo: MutableLiveData<ReactiveData<BirdShopInfo>> by lazy { MutableLiveData<ReactiveData<BirdShopInfo>>() }
    fun queryShop(queryType: QueryType = QueryType.INIT) {
        viewModelScope.launch {
            request({
                val result = service.shop().dataConvert()
                shopInfo.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
                shopInfo.value = it.convertError()
            }, needLoadState = queryType == QueryType.INIT)

        }

    }

}