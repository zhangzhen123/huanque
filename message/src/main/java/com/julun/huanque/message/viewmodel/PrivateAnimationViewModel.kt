package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/9/2 20:32
 *@描述 私信播放动画使用的ViewModel
 */
class PrivateAnimationViewModel : BaseViewModel() {
    //动画播放成功标识位
    val animationEndFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

}