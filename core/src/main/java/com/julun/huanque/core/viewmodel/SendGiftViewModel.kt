package com.julun.huanque.core.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.GiftDataDto
import com.julun.huanque.common.bean.beans.LiveGiftDto
import com.julun.huanque.common.bean.beans.SendGiftResult
import com.julun.huanque.common.bean.forms.LiveGiftForm
import com.julun.huanque.common.bean.forms.NewSendGiftForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.net.UserService
import kotlinx.coroutines.launch

/**
 * /**
 *
 *@author zhangzhen
 *@data 2017/8/7
 *
**/
 */
class SendGiftViewModel : BaseViewModel() {
    val data: MutableLiveData<GiftDataDto> by lazy { MutableLiveData<GiftDataDto>() }

    //背包数据
    val bagData: MutableLiveData<MutableList<LiveGiftDto>> by lazy { MutableLiveData<MutableList<LiveGiftDto>>() }
    val hideLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val sendGiftSuccess: MutableLiveData<SendGiftResult> by lazy { MutableLiveData<SendGiftResult>() }
    val sendGiftError: MutableLiveData<Throwable> by lazy { MutableLiveData<Throwable>() }
    val sendFinal: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //背包变动标识
    val bagChangeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //刷新数据中
    val isRefreshing: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private val liveRoomService: LiveRoomService by lazy {
        Requests.create(LiveRoomService::class.java)
    }
    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }

    //上一次送礼Form
    private var tempForm: NewSendGiftForm? = null

    /**
     * 加载所有礼物列表
     */
    fun doLoadGiftData(programId: Long) {
        isRefreshing.value = true
        viewModelScope.launch {
            request({
                val result = liveRoomService.giftsInfo(LiveGiftForm(programId)).dataConvert()
                isRefreshing.value = false
                BalanceUtils.saveBalance(result.beans)
                data.value = result
                bagChangeState.value = result.bagChange
            }, {
                hideLoading.value = true
                isRefreshing.value = false
            })
        }
    }

    /**
     * 获取背包数据
     */
    fun getBagData(programId: Long) {
        viewModelScope.launch {
            request({
                val result = liveRoomService.bag(ProgramIdForm(programId)).dataConvert()
                bagData.value = result
            })
        }
    }

    //赠送礼物
    fun sendGift(form: NewSendGiftForm) {
        viewModelScope.launch {
            request({
                val result = liveRoomService.sendGift(form).dataConvert(intArrayOf(ErrorCodes.BALANCE_NOT_ENOUGH))
                result.form = form
                bagChangeState.value = result.bagChange
                BalanceUtils.saveBalance(result.beans)
                sendGiftSuccess.value = result
                if (result.alertMsg.isNotEmpty()) {
                    ToastUtils.show(result.alertMsg)
                }
            }, {
                sendGiftError.value = it
            })
        }
    }
}