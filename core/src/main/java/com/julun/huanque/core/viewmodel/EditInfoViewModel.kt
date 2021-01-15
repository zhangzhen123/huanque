package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
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
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/30 11:44
 *@描述 编辑资料的ViewModel
 */
class EditInfoViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //原始的封面列表
    private var originConverPicIdList = mutableListOf<Long>()

    //基础数据
    val basicInfo: MutableLiveData<EditPagerInfo> by lazy { MutableLiveData<EditPagerInfo>() }

    //社交意愿数据
    val wishData: MutableLiveData<MutableList<SocialWishBean>> by lazy { MutableLiveData<MutableList<SocialWishBean>>() }

    //保存社交意愿成功标记
    val socialSuccessData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    //封面数据变动
    val homePagePicChangeBean: MutableLiveData<HomePagePicBean> by lazy { MutableLiveData<HomePagePicBean>() }

    val updateHeadResult: MutableLiveData<ReactiveData<UserHeadChangeBean>> by lazy { MutableLiveData<ReactiveData<UserHeadChangeBean>>() }

    //封面顺序保存成功标识
    val coverOrderSaveFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否需要请求接口，刷新数据
    var needFresh = false

    /**
     * 获取最初的图片ID（增加和移除同步更新）
     */
    fun getOriginPicStr(): String {
        val str = StringBuilder()

        originConverPicIdList.forEach {
            if (str.isNotEmpty()) {
                str.append(",")
            }
            str.append(it)
        }
        return str.toString()
    }

    /**
     * 获取基础信息
     */
    fun getBasicInfo() {
        viewModelScope.launch {
            request({
                val data = userService.editBasic().dataConvert()
                basicInfo.value = data
                wishData.value = data.wishList
                val idSb = StringBuilder()
                data.picList.forEach {
                    originConverPicIdList.add(it.logId)
                }
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
                val picBean = HomePagePicBean()
                picBean.coverPic = coverPic ?: ""
                if (result.logId != 0L) {
                    picBean.logId = result.logId
                } else {
                    picBean.logId = logId ?: 0
                }
                if (picBean.logId > 0L) {
                    //需要更新数据
                    homePagePicChangeBean.value = picBean
                }
                if (coverPic == null && logId != null) {
                    //删除图片
                    originConverPicIdList.remove(logId)
                }
                if (result.logId != 0L) {
                    //新增图片
                    originConverPicIdList.add(result.logId)
                }
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
                updateHeadResult.value = result.convertRtData()
            }, error = {
                updateHeadResult.value = it.convertError()
            })
        }
    }

    /**
     * 保存图像顺序
     */
    fun saveConverOrder(ids: String) {
        viewModelScope.launch {
            request({
                userService.coverOrder(LogIdsForm(ids)).dataConvert()
                coverOrderSaveFlag.value = true
            }, {})
        }
    }


}