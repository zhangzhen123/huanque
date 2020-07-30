package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.bean.beans.RecommendInfo

/**
 * Created by dong on 2018/6/7.
 */
class AnchorNoLiveViewModel : BaseViewModel() {
    //进入直播间获取的基础信息
    val baseData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }
    //推荐的主播信息
    val recommendProgram: MutableLiveData<RecommendInfo> by lazy { MutableLiveData<RecommendInfo>() }
    //显示推荐主播弹窗
    val showRecommendProgram: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 获取推荐主播信息
     */
    fun findRecommendPrograms() {
        logger("findRecommendPrograms 开始获取新节目")
        //todo
//        Requests.create(LiveRoomService::class.java).findRecommendPrograms(RecommendProgramForm(recommendProgram.value?.programId))
//                .handleResponse(makeSubscriber<RecommendInfo> {
//                    recommendProgram.value = it
//                }.ifError { recommendProgram.value = null }
//                        .withSpecifiedCodes(500))
    }

}