package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.RankingsResult
import com.julun.huanque.common.bean.forms.ScoreRankResultForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
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
 *@Date: 2020/8/1 14:21
 *
 *@Description: ScoreViewModel
 *
 */
class ScoreViewModel : BaseViewModel() {
    val refreshData: MutableLiveData<ReactiveData<RootListData<RankingsResult>>> by lazy { MutableLiveData<ReactiveData<RootListData<RankingsResult>>>() }

    private val liveRoomService: LiveRoomService by lazy {
        Requests.create(LiveRoomService::class.java)
    }
    private var offset = 0

    /**
     * 本月贡献榜
     */
    fun queryRankingsResultBtMonth(programId: Long, queryType: QueryType) {

        if (queryType != QueryType.LOAD_MORE) {
            offset = 0
        }
        viewModelScope.launch {
            request({
                val form = ScoreRankResultForm(programId, offset)
                val result = liveRoomService.queryScoreByMonth(form).dataConvert()
                offset += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                refreshData.value = result.convertRtData()
            }, error = {
                refreshData.value = it.convertListError(RootListData(isPull = queryType != QueryType.LOAD_MORE))
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

    /**
     * 本周贡献榜
     */
    fun queryRankingsResultBtWeek(programId: Long, queryType: QueryType) {
        if (queryType != QueryType.LOAD_MORE) {
            offset = 0
        }
        viewModelScope.launch {
            request({
                val form = ScoreRankResultForm(programId, offset)
                val result = liveRoomService.queryScoreByWeek(form).dataConvert()
                offset += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                refreshData.value = result.convertRtData()
            }, error = {
                refreshData.value = it.convertListError(RootListData(isPull = queryType != QueryType.LOAD_MORE))
            }, needLoadState = queryType == QueryType.INIT)
        }

    }

    /**
     * 本场贡献榜
     */
    fun queryRankingsResultBtThis(programId: Long, queryType: QueryType) {
        if (queryType != QueryType.LOAD_MORE) {
            offset = 0
        }
        viewModelScope.launch {
            request({
                val form = ScoreRankResultForm(programId, offset)
                val result = liveRoomService.queryScoreByThis(form).dataConvert()
                offset += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                refreshData.value = result.convertRtData()
            }, error = {
                refreshData.value = it.convertListError(RootListData(isPull = queryType != QueryType.LOAD_MORE))
            }, needLoadState = queryType == QueryType.INIT)
        }

    }

}
