package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.HomePageInfo
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.bean.forms.SocialWishIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/30 11:44
 *@描述 编辑资料的ViewModel
 */
class EditInfoViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //基础数据
    val basicInfo: MutableLiveData<HomePageInfo> by lazy { MutableLiveData<HomePageInfo>() }

    //社交意愿数据
    val wishData: MutableLiveData<MutableList<SocialWishBean>> by lazy { MutableLiveData<MutableList<SocialWishBean>>() }

    //保存社交意愿成功标记
    val socialSuccessData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否需要请求接口，刷新数据
    var needFresh = false

    /**
     * 获取基础信息
     */
    fun getBasicInfo() {
        viewModelScope.launch {
            request({
                val data = userService.editBasic().dataConvert()
                basicInfo.value = data
                wishData.value = data.wishList
            }, {})
        }
    }

    /**
     * 保存社交意愿
     */
    fun saveSocialWish(wishIds: String) {
        viewModelScope.launch {
            request({
                userService.socialWish(SocialWishIdForm(wishIds)).dataConvert()
                socialSuccessData.value = true
                val selectList = mutableListOf<SocialWishBean>()
                basicInfo.value?.wishConfigList?.forEach {
                    if (it.selected == BusiConstant.True) {
                        selectList.add(it)
                    }
                }
                basicInfo.value?.wishList = selectList
                wishData.value = selectList
            }, {})
        }
    }


}