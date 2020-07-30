package com.julun.huanque.core.viewmodel
import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel


/**
 * Created by dong on 2018/8/29.
 * 入场消息ViewModel
 */
class EntranceViewModel : BaseViewModel() {
    //是否收到40级入场消息
    val messageState : MutableLiveData<String> by lazy { MutableLiveData<String>() }
    //动画开始播放标识
    val animatorState : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}