package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/12/9 10:18
 *@描述
 */
class ScrollStateViewModel : BaseViewModel() {
    val scrollState : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}