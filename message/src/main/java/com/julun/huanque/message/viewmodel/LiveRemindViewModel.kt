package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.bean.forms.LiveRemindForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15
 */
class LiveRemindViewModel : BaseViewModel() {

    val queryData = MutableLiveData<Boolean>()

    private var offset = 0

    val result = queryData.switchMap {
        liveData {
            it ?: return@liveData
            if (it) {
                offset = 0
            }
            request({
                val data =
                    Requests.create(LiveService::class.java).followList(LiveRemindForm(offset))
                        .dataConvert()
                offset += data.list.size
                data.isPull = it
                emit(data)
            }, error = { e ->
            }, final = {}, needLoadState = true)
        }
    }

}