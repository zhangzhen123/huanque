package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.PkUserRankInfo
import com.julun.huanque.common.bean.beans.RankingsResult
import com.julun.huanque.common.bean.forms.PKScoreRankResultForm
import com.julun.huanque.common.bean.forms.ScoreRankResultForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.net.services.PkMicService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/16 14:53
 *
 *@Description: PkScoreViewModel
 *
 */
class PkScoreViewModel : BaseViewModel() {
    val refreshData: MutableLiveData<ReactiveData<PkUserRankInfo>> by lazy { MutableLiveData<ReactiveData<PkUserRankInfo>>() }

    private val liveRoomService: PkMicService by lazy {
        Requests.create(PkMicService::class.java)
    }
    private var offset = 0

    /**
     * 本月贡献榜
     */
    fun queryPkRankingsResult(programId: Long,pkId:Long, queryType: QueryType) {

//        if (queryType != QueryType.LOAD_MORE) {
//            offset = 0
//        }
        viewModelScope.launch {
            request({
                val form = PKScoreRankResultForm(programId, pkId = pkId)
                val result = liveRoomService.queryPkRank(form).dataConvert()
//                offset += result.list.size
//                result.isPull = queryType != QueryType.LOAD_MORE
                refreshData.value = result.convertRtData()
            }, error = {
                refreshData.value = it.convertError()
            }, needLoadState = queryType == QueryType.INIT)
        }
    }



}
