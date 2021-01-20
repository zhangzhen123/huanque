package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2021/1/20 9:09
 *@描述 邀请码ViewModel
 */
class InviteCodeViewModel : BaseViewModel() {

    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

    //邀请码更新成功标记位
    val codeSuccessFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 更新邀请码
     */
    fun updateCard(code: String) {
        viewModelScope.launch {
            request({
                userService.updateCode(UpdateInformationForm(invitationCode = code)).dataConvert()
                ToastUtils.show("填写成功")
                //邀请码填写成功
                codeSuccessFlag.value = true
            }, {})
        }
    }
}