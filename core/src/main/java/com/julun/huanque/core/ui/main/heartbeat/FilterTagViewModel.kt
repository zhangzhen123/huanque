package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.FilterTagBean
import com.julun.huanque.common.bean.beans.NearbyListData
import com.julun.huanque.common.bean.beans.NearbyUserBean
import com.julun.huanque.common.bean.beans.ProgramListInfo
import com.julun.huanque.common.bean.forms.NearbyForm
import com.julun.huanque.common.bean.forms.ProgramListForm
import com.julun.huanque.common.bean.forms.SaveSearchConfigForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.net.services.TagService
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
class FilterTagViewModel : BaseViewModel() {

    private val service: TagService by lazy { Requests.create(TagService::class.java) }
    val filterTagBean: MutableLiveData<ReactiveData<FilterTagBean>> by lazy { MutableLiveData<ReactiveData<FilterTagBean>>() }

    val saveResult: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }


    fun requestInfo(queryType: QueryType) {

        viewModelScope.launch {

            request({
                val result = service.searchConfig().dataConvert()
                filterTagBean.value = result.convertRtData()
            }, error = {
                filterTagBean.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

    fun saveSearchConfig(
        sexType: String,
        minAge: Int, maxAge: Int,
        distance: Long,
        wishes: String,
        tagIds: String
    ) {
        viewModelScope.launch {

            request({
                val result = service.saveSearchConfig(SaveSearchConfigForm(sexType, minAge, maxAge, distance, wishes, tagIds))
                    .dataConvert()
                saveResult.value = true.convertRtData()
            }, error = {
                saveResult.value = it.convertError()
            })
        }

    }

}