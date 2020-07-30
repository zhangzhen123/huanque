package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2019/8/26 11:57
 *@描述 直播间页面设置相关的ViewModel
 */
class OrientationViewModel : BaseViewModel() {
    //横屏的标识符   true表示为横屏状态，其它为竖屏状态 默认为竖屏
    val horizonState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //主播端的横竖屏状态
    val screenTypeData : MutableLiveData<String> by lazy { MutableLiveData<String>() }

}