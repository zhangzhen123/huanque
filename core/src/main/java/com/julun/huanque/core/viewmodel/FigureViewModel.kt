package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.FigureBean
import com.julun.huanque.common.bean.beans.FigureConfig
import com.julun.huanque.common.bean.beans.UserProcessBean
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2021/1/8 10:27
 *@描述 身材ViewModel
 */
class FigureViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    var index = -1

    //原始身高
    var originHeight = 0

    //原始体重
    var originWeight = 0

    //当前身高
    val currentHeight: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //当前体重
    val currentWeight: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //身材数据
    val figureData: MutableLiveData<FigureBean> by lazy { MutableLiveData<FigureBean>() }

    //当前体质
    val currentFigureConfig: MutableLiveData<FigureConfig> by lazy { MutableLiveData<FigureConfig>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    //身材配置列表
    var figureList = mutableListOf<FigureConfig>()

    /**
     * 初始化身材数据
     */
    fun initFigure() {
        viewModelScope.launch {
            request({
                val bean = userService.initFigure().dataConvert()
                figureList.addAll(bean.configList)
                figureData.value = bean
            }, {})
        }
    }

    /**
     * 更新身高体重
     * @param height 身高
     * @param weight 体重
     */
    fun updateFigure(height: Int, weight: Int) {
        viewModelScope.launch {
            request({
                val bean = userService.updateUserInfo(UpdateUserInfoForm(height = height, weight = weight)).dataConvert()
                val fb = FigureBean(height, weight)
                EventBus.getDefault().post(fb)
                processData.value = bean
            }, {})
        }
    }

}