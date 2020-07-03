package com.julun.huanque.viewmodel

import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.MobileLoginForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.net.service.UserService
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/3 14:49
 *@描述 手机号登录使用的ViewModel
 */
class PhoneNumLoginViewModel : BaseViewModel() {

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }


    /**
     * 登录
     */
    fun login(phoneNum: String, code: String) {

        viewModelScope.launch {
            request({ val result = userService.mobileLogin(MobileLoginForm(phoneNum, code)).dataConvert() })
        }

    }
}