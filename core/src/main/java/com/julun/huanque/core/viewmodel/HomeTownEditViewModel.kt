package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.EditHomeTownBean
import com.julun.huanque.common.bean.beans.SingleCulture
import com.julun.huanque.common.bean.beans.UserProcessBean
import com.julun.huanque.common.bean.forms.CityIdForm
import com.julun.huanque.common.bean.forms.CultureUpdateForm
import com.julun.huanque.common.bean.forms.HomeTownVersionForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
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
    companion object {
        //美食
        const val Food = "Food"

        //景点
        const val Place = "Place"
    }

    //当前进度（首次编辑的时候使用）
    var index = -1

    //跳转列表
    var jumpList = mutableListOf<String>()

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //家乡数据
    val homeTownData: MutableLiveData<EditHomeTownBean> by lazy { MutableLiveData<EditHomeTownBean>() }

    //原始的家乡ID
    var oldHownTownId = 0

    //原始的人文ID
    val oldCultureIds = mutableListOf<Long>()

    //美食相关数据
    val foodCultureData: MutableLiveData<SingleCulture> by lazy { MutableLiveData<SingleCulture>() }

    //景点数据
    val placeCultureData: MutableLiveData<SingleCulture> by lazy { MutableLiveData<SingleCulture>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    //省份 名称
    var provinceName: String = ""

    //城市名称
    var cityName: String = ""


    //人文弹窗的类型（美食or景点）
    var markFragmentType = ""

    /**
     * 获取家乡数据
     */
    fun queryHomeTownInfo() {
        viewModelScope.launch {
            request({
                val version = SharedPreferencesUtils.getInt(SPParamKey.Province_City_Version, 0)
                val form = if (version == 0) {
                    HomeTownVersionForm()
                } else {
                    HomeTownVersionForm(version)
                }
                val homeData = userService.initHomeTown(form).dataConvert()
                homeData.cultureList.forEach {
                    if (it.cultureType == Food) {
                        //美食数据
                        foodCultureData.value = it
                        it.cultureConfigList.forEach { scc ->
                            if (scc.mark == BusiConstant.True) {
                                oldCultureIds.add(scc.logId)
                            }
                        }
                    }
                    if (it.cultureType == Place) {
                        //景点
                        placeCultureData.value = it
                        it.cultureConfigList.forEach { scc ->
                            if (scc.mark == BusiConstant.True) {
                                oldCultureIds.add(scc.logId)
                            }
                        }
                    }
                }
                oldCultureIds.sort()
                homeTownData.value = homeData
                oldHownTownId = homeData.homeTownId


            }, {})
        }
    }

    /**
     * 获取城市人文数据
     */
    fun getCityCulture(cityId: Int) {
        viewModelScope.launch {
            request({
                val homeData = userService.getCityCulture(CityIdForm(cityId)).dataConvert()
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
            }, {})
        }
    }

    /**
     * 保存人文数据
     * @param cityId 城市ID
     * @param cultureIds 人文Id
     */
    fun saveHomeTown(cityId: Int, cultureIds: String) {
        viewModelScope.launch {
            request({
                processData.value = userService.saveHomeTown(CultureUpdateForm(cityId, cultureIds)).dataConvert()
            }, {})
        }
    }


}