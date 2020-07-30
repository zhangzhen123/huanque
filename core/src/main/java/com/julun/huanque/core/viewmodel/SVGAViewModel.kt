package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 * Created by dong on 2018/4/11.
 */
class SVGAViewModel : BaseViewModel() {
    //资源地址
    val resource : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    //播放标志
    val startPlay : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //动画播放结束标志
    val animationFinish : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}