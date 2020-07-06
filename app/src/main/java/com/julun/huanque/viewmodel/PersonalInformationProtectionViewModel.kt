package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.AgreementResultForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.request
import com.julun.huanque.net.service.UserService
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/4 19:42
 *@描述 协议ViewModel
 */
class PersonalInformationProtectionViewModel : BaseViewModel() {
    val agreeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    /**
     * 同意协议
     * @param code 协议code
     */
    fun agree(code: String) {
        viewModelScope.launch {
            request({
                userService.agreement(AgreementResultForm(agreementCode = code))
                agreeState.value = true
            })

        }
    }

}