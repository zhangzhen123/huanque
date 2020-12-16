package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ProgramListInfo
import com.julun.huanque.common.bean.beans.ProgramTab
import com.julun.huanque.common.bean.forms.ProgramListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.HomeTabType
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.net.Requests
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

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }
    val dataList: MutableLiveData<ReactiveData<ProgramListInfo>> by lazy { MutableLiveData<ReactiveData<ProgramListInfo>>() }


    private var offsetHot = 0
    fun requestProgramList(queryType: QueryType, typeCode:String?) {

        viewModelScope.launch {
            if (queryType != QueryType.LOAD_MORE) {
                offsetHot = 0
            }

            request({
                val result =
                    programService.programList(ProgramListForm(offset = offsetHot,typeCode = typeCode)).dataConvert()
                offsetHot += result.programList.size
                result.isPull = queryType != QueryType.LOAD_MORE
                dataList.value = result.convertRtData()
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            },needLoadState = queryType== QueryType.INIT)
        }
    }

}