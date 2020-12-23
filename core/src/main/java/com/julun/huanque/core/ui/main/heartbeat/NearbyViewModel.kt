package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.NearbyListData
import com.julun.huanque.common.bean.beans.NearbyUserBean
import com.julun.huanque.common.bean.beans.ProgramListInfo
import com.julun.huanque.common.bean.forms.NearbyForm
import com.julun.huanque.common.bean.forms.ProgramListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class NearbyViewModel : BaseViewModel() {

    private val service: HomeService by lazy { Requests.create(HomeService::class.java) }
    val dataList: MutableLiveData<ReactiveData<NearbyListData<NearbyUserBean>>> by lazy { MutableLiveData<ReactiveData<NearbyListData<NearbyUserBean>>>() }


    private var offset = 0
    fun requestNearbyList(
        queryType: QueryType,
        lat: Double,
        lng: Double,
        province: String = "",
        city: String = "",
        districe: String = ""
    ) {

        viewModelScope.launch {
            if (queryType != QueryType.LOAD_MORE) {
                offset = 0
            }

            request({
                val result =
                    service.getNearBy(
                        NearbyForm(
                            offset = offset,
                            lat = lat,
                            lng = lng,
                            province = province,
                            city = city,
                            districe = districe
                        )
                    ).dataConvert()
                offset += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                dataList.value = result.convertRtData()
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}