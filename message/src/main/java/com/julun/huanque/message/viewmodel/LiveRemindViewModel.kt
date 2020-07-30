package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.forms.LiveRemindForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRemindService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15
 */
class LiveRemindViewModel : BaseViewModel() {

    val queryData = MutableLiveData<Boolean>()
    val success: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private var offset = 0

    val result = queryData.switchMap {
        liveData {
            it ?: return@liveData
            if (it) {
                offset = 0
            }
            request({
                val data =
                    Requests.create(LiveRemindService::class.java)
                        .followList(LiveRemindForm(offset = offset))
                        .dataConvert()
                offset += data.list.size
                data.isPull = it
                emit(data)
            })
        }
    }

    fun updatePush(push: String, programId: Int) {
        viewModelScope.launch {
            request({
                Requests.create(LiveRemindService::class.java)
                    .changeFollowPush(LiveRemindForm(pushOpen = push, programId = programId))
                    .dataConvert()
                success.value = true
            }, error = { e ->
                if (e !is ResponseError) {
                    ToastUtils.show("网络异常，请重试~！")
                }
            }, needLoadState = false)
        }
    }

}