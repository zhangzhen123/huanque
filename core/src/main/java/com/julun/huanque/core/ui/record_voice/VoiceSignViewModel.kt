package com.julun.huanque.core.ui.record_voice

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.InfoPerfection
import com.julun.huanque.common.bean.beans.VoiceSignPointBean
import com.julun.huanque.common.bean.forms.UpdateVoiceForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.net.UserService
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/21 20:49
 *
 *@Description: VoiceSignViewModel
 *
 */
class VoiceSignViewModel : BaseViewModel() {


    private val service: UserService by lazy { Requests.create(UserService::class.java) }

    val updateVoiceResult: MutableLiveData<ReactiveData<InfoPerfection>> by lazy { MutableLiveData<ReactiveData<InfoPerfection>>() }

    val signPoints: LiveData<ReactiveData<VoiceSignPointBean>> = queryState.switchMap {
        liveData {
            request({
                val result = service.getVoiceSignPoint().dataConvert()
                emit(result.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, needLoadState = it == QueryType.INIT)


        }
    }


    fun updateVoice(voiceUrl: String, length: Long) {
        viewModelScope.launch {
            request({
                val result = service.updateVoice(UpdateVoiceForm(voiceUrl, length)).dataConvert()
                updateVoiceResult.postValue(result.convertRtData())
            }, error = {
                updateVoiceResult.value = it.convertError()
            })
        }

    }


}