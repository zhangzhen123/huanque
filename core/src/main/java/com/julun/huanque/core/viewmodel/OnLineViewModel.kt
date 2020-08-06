package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.OnlineListData
import com.julun.huanque.common.bean.beans.OnlineUserInfo
import com.julun.huanque.common.bean.forms.OnLineForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.TabTags
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.convertListError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch


class OnLineViewModel : BaseViewModel() {

    val service: LiveRoomService by lazy { Requests.create(LiveRoomService::class.java) }

    val listResult: MutableLiveData<ReactiveData<OnlineListData<OnlineUserInfo>>> by lazy { MutableLiveData<ReactiveData<OnlineListData<OnlineUserInfo>>>() }
    val guardSuccess: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var offset = 0

    private var balanceDisposable: Disposable? = null

    /**
     * 购买守护
     * @param programId 节目id
     * @param guardId 守护id
     */
//    fun buyGuard(programId: Int, guardId: Int) {
//        Requests.create(LiveRoomService::class.java)
//                .openGuard(MallBuyForm(goodId = guardId, programId = programId))
//                .handleResponse(makeSubscriber<VoidResult> {
//                    guardSuccess.value = true
//                    guardStatistics(programId)
//                }.ifError {
//                    if (it is ResponseError) {
//                        errorStatus.value = it
//                        return@ifError
//                    }
//                    ToastUtils.show("网络异常!")
//                }.withFinalCall {
//                    finalStatus.value = true
//                })
//    }
//
//    /**
//     * 购买体验守护
//     * @param programId 节目id
//     */
//    fun buyExperienceGuard(programId: Int) {
//        Requests.create(LiveRoomService::class.java)
//                .openExperienceGuard(MallBuyForm(goodId = 9, programId = programId))
//                .handleResponse(makeSubscriber<VoidResult> {
//                    guardSuccess.value = true
//                }.ifError {
//                    if (it is ResponseError) {
//                        errorStatus.value = it
//                        return@ifError
//                    }
//                    ToastUtils.show("网络异常!")
//                }.withFinalCall {
//                    finalStatus.value = true
//                })
//    }

    fun queryList(tagNames: String, type: QueryType, programId: Long) {

        if (type != QueryType.LOAD_MORE) {
            offset = 0
        }
        viewModelScope.launch {
            request({
                val result = when (tagNames) {
                    TabTags.TAB_TAG_ROYAL -> {
                        service.queryRoyalList(OnLineForm(programId, offset)).dataConvert()
                    }
                    TabTags.TAB_TAG_GUARD -> {
                        service.queryGuardList(OnLineForm(programId, offset)).dataConvert()
                    }
                    TabTags.TAB_TAG_MANAGER -> {
                        service.queryManagerList(OnLineForm(programId, offset)).dataConvert()
                    }
                    else -> {
                        service.queryNormalList(OnLineForm(programId, offset)).dataConvert()
                    }
                }
                //todo test
//                result.list.add(OnlineUserInfo())
                offset += result.list.size
                result.isPull = type != QueryType.LOAD_MORE
                listResult.value = result.convertRtData()

            }, error = {
                listResult.value = it.convertListError(OnlineListData(isPull = type != QueryType.LOAD_MORE))
            },needLoadState = type==QueryType.INIT)
        }
    }


    //------------------------------------ 埋点相关 start ----------------------------------------
    private fun guardStatistics(programId: Int) {
//        LingMengService.getService(IStatistics::class.java)?.onCoreBehavior("守护")
//        LingMengService.getService(IStatistics::class.java)?.onExpend("守护", "1", "守护", "${(guardExpense
//                ?: 100000) * 0.3}", "$programId")
    }
    //------------------------------------- 埋点相关 end -----------------------------------------

}