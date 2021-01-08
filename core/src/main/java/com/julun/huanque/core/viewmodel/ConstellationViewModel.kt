package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.ConstellationInfo
import com.julun.huanque.common.bean.forms.ConstellationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2021/1/5 17:10
 *@描述 星座数据
 */
class ConstellationViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }
    val constellationData: MutableLiveData<ConstellationInfo> by lazy { MutableLiveData<ConstellationInfo>() }

    /**
     * 查询星座数据
     */
    fun queryConstellation(type: String) {
        viewModelScope.launch {
            request({
                constellationData.value = userService.constellation(ConstellationForm(type)).dataConvert()
            }, {})
        }
    }
}