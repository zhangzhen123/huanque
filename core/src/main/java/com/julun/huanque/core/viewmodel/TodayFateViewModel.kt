package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.TodayFateInfo
import com.julun.huanque.common.bean.beans.TodayFateItem
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/27 16:38
 *
 *@Description: 今日缘分
 *
 */
class TodayFateViewModel : BaseViewModel() {

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }
    val matchesInfo: MutableLiveData<ReactiveData<TodayFateInfo<TodayFateItem>>> by lazy { MutableLiveData<ReactiveData<TodayFateInfo<TodayFateItem>>>() }


    fun requestInfo() {

        viewModelScope.launch {

            request({
//                val result =
//                    programService.squareHeatList(ProgramListForm(offset = offsetHot, programId = programId)).dataConvert()
//                result.isPull = queryType != QueryType.LOAD_MORE
                //todo
                val info=TodayFateInfo<TodayFateItem>().apply {
                    repeat(4){
                        this.list.add(TodayFateItem())
                    }

                }
                matchesInfo.value = info.convertRtData()
            }, error = {
                matchesInfo.value=it.convertError()
            })
        }
    }
}