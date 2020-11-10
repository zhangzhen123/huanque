package com.julun.huanque.core.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.MultiBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.bean.forms.KeyWordForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 * Created by dong on 2017/6/28.
 */
class SearchViewModel : BaseViewModel() {
    val queryData: MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>> by lazy { MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>>() }
    val recentData: MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>> by lazy { MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>>() }
    val recommendData: MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>> by lazy { MutableLiveData<ReactiveData<MutableList<ProgramLiveInfo>>>() }

    private val service: ProgramService by lazy { Requests.create(ProgramService::class.java) }

    //搜索主播
    fun searchProgram(key: String) {
        mSearchWord = key
        viewModelScope.launch {
            request({
                val result = service.search(KeyWordForm(key)).dataConvert()
                queryData.value=result.convertRtData()
                //搜索关键字统计
                searchWord(result.isNotEmpty())
            }, error = {
                queryData.value=it.convertError()
            })
        }
    }

    //获取最近主播
    fun queryRecent() {
//        Requests.create(LiveRoomService::class.java)
//                .queryRecent(SessionForm())
//                .handleResponse(makeSubscriber {
//                    recentData.value = it
//                })
    }

    fun getRecommendData() {

//        Requests.create(ProgramService::class.java)
//                .guessLikeList(SessionForm())
//                .handleResponse(makeSubscriber<List<ProgramLiveInfo>> {
//                    val list = arrayListOf<MultipleEntity>()
//                    it.forEach { item ->
//                        list.add(MultipleEntity(ProgramAdapterNew.LIVEITEM, item))
//                    }
//                    recommendData.value = list
//                }.ifError {
//                    recommendData.value = null
//                })

    }

    //------------------------------------ 埋点相关 start ----------------------------------------
    private var mSearchWord: String? = null

    fun searchWord(result: Boolean?) {
//        LingMengService?.getService(IStatistics::class.java)?.onSearchClick(mSearchWord
//                ?: "", when (result) {
//            true -> "有"
//            else -> "无"
//        })
        mSearchWord = null
    }
    //------------------------------------- 埋点相关 end -----------------------------------------
}