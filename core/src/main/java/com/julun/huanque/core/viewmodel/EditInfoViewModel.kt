package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.EditPagerInfo
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.bean.beans.UserHeadChangeBean
import com.julun.huanque.common.bean.beans.UserProcessBean
import com.julun.huanque.common.bean.forms.SaveCoverForm
import com.julun.huanque.common.bean.forms.SocialWishIdForm
import com.julun.huanque.common.bean.forms.UpdateHeadForm
import com.julun.huanque.common.bean.forms.UserUpdateHeadForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
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
    val basicInfo: MutableLiveData<EditPagerInfo> by lazy { MutableLiveData<EditPagerInfo>() }

    //社交意愿数据
    val wishData: MutableLiveData<MutableList<SocialWishBean>> by lazy { MutableLiveData<MutableList<SocialWishBean>>() }

    //保存社交意愿成功标记
    val socialSuccessData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    val updateHeadResult: MutableLiveData<ReactiveData<UserHeadChangeBean>> by lazy { MutableLiveData<ReactiveData<UserHeadChangeBean>>() }

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
            })
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
            })
        }
    }

    /**
     * 新增，删除，更换封面
     */
    fun updateCover(logId: Long?, coverPic: String?) {
        viewModelScope.launch {
            request({
                val result = userService.updateCover(SaveCoverForm(logId, coverPic)).dataConvert()
                processData.value = result
                //更新操作后 全部刷新
                getBasicInfo()
            })
        }
    }

    var currentHeadPic: String = ""

    /**
     * 更换头像封面
     */
    fun updateHeadPic(headPic: String, check: String) {
        currentHeadPic = headPic
        viewModelScope.launch {
            request({
                val result = userService.updateHeadPic(UserUpdateHeadForm(headPic, check)).dataConvert(intArrayOf(ErrorCodes.USER_LOSE_REAL_HEAD))
                updateHeadResult.value=result.convertRtData()
            }, error = {
                updateHeadResult.value=it.convertError()
            })
        }
    }

}