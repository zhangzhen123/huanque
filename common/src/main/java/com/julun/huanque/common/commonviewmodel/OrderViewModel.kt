package com.julun.huanque.common.commonviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.julun.huanque.common.base.BaseDialogFragment

/**
 *@创建者   dong
 *@创建时间 2020/1/15 14:48
 *@描述 activity内弹窗排序使用的ViewModel
 */
class OrderViewModel : ViewModel() {
    /**
     * 分发状态 true表示处于分发状态中，false表示未处于分发当中
     */
    var mDispatchState = false

    /**
     * 弹出下一个数据的标识位
     */
    val popState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 弹出下一个数据的标识位
     */
    val dialog: MutableLiveData<BaseDialogFragment> by lazy { MutableLiveData<BaseDialogFragment>() }

}