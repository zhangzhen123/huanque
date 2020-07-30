package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.LiveFollowListData
import com.julun.huanque.common.bean.forms.LiveFollowForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.handleResponse


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date 2019/7/16 19:29
 *
 *@Description 查询直播间关注列表
 *
 */
class LiveFollowListViewModel : BaseViewModel() {

    val queryDataList: MutableLiveData<LiveFollowListData> by lazy { MutableLiveData<LiveFollowListData>() }

//    val queryError: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val finalState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var offset = 0

    fun requestFollowLivingList(programId: Int, isPull: Boolean) {

        if (isPull) {
            offset = 0
        }

        val form = LiveFollowForm(offset,programId)
        Requests.create(ProgramService::class.java)
                .followLivingList(form)
                .handleResponse(makeSubscriber<LiveFollowListData> {
                    offset += it.list.size
                    it.isPull = isPull
                    queryDataList.value = it
                }.ifError {
//                    queryError.value = true
                }.withFinalCall {
                    finalState.value = true
                })
    }
}