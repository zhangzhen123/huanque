package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.EditHomeTownBean
import com.julun.huanque.common.bean.beans.SingleCulture
import com.julun.huanque.common.bean.forms.HomeTownVersionForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SharedPreferencesUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/30 20:03
 *@描述 编辑页面家乡数据
 */
class HomeTownEditViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //家乡数据
    val homeTownData: MutableLiveData<EditHomeTownBean> by lazy { MutableLiveData<EditHomeTownBean>() }

    //美食相关数据
    val foodCultureData: MutableLiveData<SingleCulture> by lazy { MutableLiveData<SingleCulture>() }

    //景点数据
    val placeCultureData: MutableLiveData<SingleCulture> by lazy { MutableLiveData<SingleCulture>() }

    /**
     * 获取家乡数据
     */
    fun queryHomeTownInfo() {
        viewModelScope.launch {
            request({
                val version = SharedPreferencesUtils.getInt(SPParamKey.Province_City_Version, 1)
                val form = if (version == 0) {
                    HomeTownVersionForm()
                } else {
                    HomeTownVersionForm(version)
                }
                val homeData = userService.initHomeTown(form).dataConvert()
                homeData.cultureList.forEach {
                    if (it.cultureType == "Food") {
                        //美食数据
                        foodCultureData.value = it
                    }
                    if (it.cultureType == "Place") {
                        //景点
                        placeCultureData.value = it
                    }
                }
                homeTownData.value = homeData
            }, {})
        }
    }


}