package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.StatusForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/10/21 10:42
 *@描述 通用设置ViewModel
 */
class CommonSettingViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //隐藏位置状态
    val hideLocationState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    /**
     * 获取当前位置状态
     */
    fun getHideLocationState() {
        viewModelScope.launch {
            request({
                val result = userService.hideLocation().dataConvert()
                hideLocationState.value = result.status
            })
        }
    }


    /**
     * 保存隐藏状态
     */
    fun saveHideLocation(status: String) {
        viewModelScope.launch {
            request({
                userService.saveHideLocation(StatusForm(status))
                hideLocationState.value = status
            })
        }

    }

}