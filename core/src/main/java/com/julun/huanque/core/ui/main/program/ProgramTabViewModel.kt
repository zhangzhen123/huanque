package com.julun.huanque.core.ui.main.program

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ProgramListInfo
import com.julun.huanque.common.bean.forms.ProgramListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.convertListError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date 2019/7/16 19:29
 *
 *@Description 查询直播间关注列表
 *
 */
class ProgramTabViewModel : BaseViewModel() {

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }
    val dataList: MutableLiveData<ReactiveData<ProgramListInfo>> by lazy { MutableLiveData<ReactiveData<ProgramListInfo>>() }


    private var offsetHot = 0
    fun requestProgramList(queryType: QueryType,typeCode:String?) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
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
            },needLoadState = queryType==QueryType.INIT)
        }
    }

}