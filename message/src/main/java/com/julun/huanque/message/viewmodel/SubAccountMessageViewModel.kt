package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.RelateAccountMsg
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/3 14:32
 *@描述 分身账号ViewModel
 */
class SubAccountMessageViewModel : BaseViewModel() {
    private val socialString: SocialService by lazy { Requests.create(SocialService::class.java) }

    val msgList: MutableLiveData<RelateAccountMsg> by lazy { MutableLiveData<RelateAccountMsg>() }

    fun getMsgList() {
        viewModelScope.launch {
            request({
                msgList.value = socialString.relateAccountMsg().dataConvert()
            }, {})
        }
    }
}