package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/1/6 16:33
 *
 *@Description: MainConnectViewModel 用于MainActivity通信
 *
 */
class MainConnectViewModel : BaseViewModel() {

    //
    val heartBeatSwitch: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val refreshNearby: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}