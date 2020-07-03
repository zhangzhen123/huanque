package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/7/3 19:29
 *@描述 完善数据页面
 */
class FillInformationViewModel : BaseViewModel() {

    companion object {
        //第一步（昵称，性别，生日）
        const val FIRST = "FIRST"

        //第二步(头像)
        const val SECOND = "SECOND"
    }

    val currentStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

}