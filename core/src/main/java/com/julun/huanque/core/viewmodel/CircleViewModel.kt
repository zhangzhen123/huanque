package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/11/25 16:16
 *@描述 动态页面使用
 */
class CircleViewModel : BaseViewModel() {
    var mType = ""

    //选择推荐的标记位
    val selectRecom: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}