package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.AuthorFollowBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.bean.forms.LiveFollowForm
import com.julun.huanque.common.bean.forms.ProgramListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.convertListError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.delay
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
class LiveSquareViewModel : BaseViewModel() {

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }
    val followList: MutableLiveData<ReactiveData<RootListData<AuthorFollowBean>>> by lazy { MutableLiveData<ReactiveData<RootListData<AuthorFollowBean>>>() }
    val hotDataList: MutableLiveData<ReactiveData<RootListData<ProgramLiveInfo>>> by lazy { MutableLiveData<ReactiveData<RootListData<ProgramLiveInfo>>>() }

    private var offset1 = 0

    fun requestFollowList(isPull: Boolean) {


        viewModelScope.launch {
            if (isPull) {
                offset1 = 0
            }
            request({
                val result = programService.squareFollowList(LiveFollowForm(offset1)).dataConvert()
                offset1 += result.list.size
                result.isPull = isPull
                followList.value = result.convertRtData()
            })
        }
    }

    private var offsetHot = 0
    fun requestHotList(queryType: QueryType, programId: Int) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offsetHot = 0
            }

            request({
                val result =
                    programService.squareHeatList(ProgramListForm(offset = offsetHot, programId = programId)).dataConvert()
                offsetHot += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                hotDataList.value = result.convertRtData()
            }, error = {
                hotDataList.value = it.convertListError(RootListData(isPull = queryType != QueryType.LOAD_MORE))
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}