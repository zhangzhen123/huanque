package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 * Created by dong on 2018/7/12.
 */
class PlayerDinosaurViewModel : BaseViewModel() {
    //恐龙砸蛋弹窗隐藏
    val eggResultDismiss: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}