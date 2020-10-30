package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.bean.beans.RecommendInfo
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Created by dong on 2018/6/7.
 */
class AnchorNoLiveViewModel : BaseViewModel() {
    private val liveRoomService: LiveRoomService by lazy { Requests.create(LiveRoomService::class.java) }

    //进入直播间获取的基础信息
    val baseData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }

    //推荐的主播信息
    val recommendProgram: MutableLiveData<RecommendInfo> by lazy { MutableLiveData<RecommendInfo>() }

    //显示推荐主播弹窗
    val showRecommendProgram: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //倒计时
    val countDownNumber: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    /**
     * 获取推荐主播信息
     */
    fun findRecommendPrograms() {
        logger("findRecommendPrograms 开始获取新节目")
        viewModelScope.launch {
            request({
                val result = liveRoomService.recommend().dataConvert()
                recommendProgram.value = result
            }, {})
        }
    }

    private var mDisposable: Disposable? = null

    /**
     * 倒计时
     */
    fun startCountDown() {
        mDisposable?.dispose()
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(6)
            .map { 5 - it }
            .subscribe({
                countDownNumber.postValue(it.toInt())
            }, {})
    }

    /**
     * 停止倒计时
     */
    fun stopCountDown(){
        mDisposable?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        mDisposable?.dispose()
    }
}