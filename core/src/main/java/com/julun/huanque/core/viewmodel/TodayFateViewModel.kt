package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.QuickAccostResult
import com.julun.huanque.common.bean.beans.TodayFateInfo
import com.julun.huanque.common.bean.beans.TodayFateItem
import com.julun.huanque.common.bean.forms.QuickAccostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.constant.MessageFailType
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


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

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    val matchesInfo: MutableLiveData<ReactiveData<TodayFateInfo>> by lazy { MutableLiveData<ReactiveData<TodayFateInfo>>() }

    val quickAccostResult: MutableLiveData<QuickAccostResult> by lazy { MutableLiveData<QuickAccostResult>() }

    val quickAccostError: MutableLiveData<Throwable> by lazy { MutableLiveData<Throwable>() }

    val closeFateDialog: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //关闭弹窗标记
    val closeFateDialogTag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //展示今日缘分弹窗
    val showFateDialog: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //是否已经触发过今日缘分
    var hasShowTodayFate: Boolean = false
    //今日缘分是否已经完成 没有完成 每次关闭时 显示拖拽图标
    var isComplete: Boolean = false

    fun requestInfo() {

        viewModelScope.launch {

            request({
                val result = socialService.todayFate().dataConvert()
                matchesInfo.value = result.convertRtData()
            }, error = {
                matchesInfo.value = it.convertError()
            })
        }
    }

    //一键快速搭讪
    fun quickAccost(userIds: String) {

        viewModelScope.launch {

            request({
                val result = socialService.quickAccost(QuickAccostForm(userIds)).dataConvert(intArrayOf(ErrorCodes.BALANCE_NOT_ENOUGH))
                quickAccostResult.value = result
                isComplete=true
                closeFateDialog.value = true
            }, error = {
//                quickAccostResult.value = it.convertError()
                quickAccostError.value=it
            })
        }
    }
}