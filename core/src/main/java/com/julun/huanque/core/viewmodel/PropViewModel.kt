package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2019/5/5 16:02
 *@描述 道具相关ViewModel，神秘人倒计时
 */
class PropViewModel : BaseViewModel() {

    private var myExpDisposable: Disposable? = null
    private var mysteriousDisposable: Disposable? = null
    //倒计时剩余时间
    val mysteriousTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    //炫彩发言剩余时间
    private var colorfulTime = 0L
    //是否是神秘人
    val mysteriousState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //炫彩发言状态
    val colorfulState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //经验卡倒计时剩余时间
    val expTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    //经验卡状态 没值/0代表失效
    val expRatioState: MutableLiveData<Double> by lazy { MutableLiveData<Double>() }

    /**
     * 开始神秘人倒计时
     */
    fun startMysteriousCountDown(count: Long) {
        if (count <= 0) {
            return
        }
        mysteriousDisposable?.dispose()
        mysteriousDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribe({
                    mysteriousTime.postValue(count - it)
                    if (count == it) {
                        mysteriousDisposable?.dispose()
                        mysteriousState.postValue(null)
                    }
                }, { it.printStackTrace() })
    }

    var expRatioTtl = 0L//当前的经验总时间
    fun startExpStateCountDown(count: Long) {
        if (count <= 0) return
        expRatioTtl = count
        myExpDisposable?.dispose()
        myExpDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribe {
            expTime.postValue(expRatioTtl--)
            if (expRatioTtl <= 0L) {
                expRatioState.postValue(null)
                myExpDisposable?.dispose()
            }
        }
    }

    /**
     * 设置炫彩发言时间
     */
    fun setColorfulTime(time: Long) {
        colorfulTime = time
        if (time > 0 && mysteriousState.value != true) {
            //炫彩发言状态开启
            colorfulState.postValue(true)
        } else {
            //炫彩发言状态关闭
            colorfulState.postValue(false)
        }
    }

    /**
     * 重置炫彩发言
     */
    fun resetColorfulTime() {
        setColorfulTime(colorfulTime)
    }

    override fun onCleared() {
        super.onCleared()
        mysteriousDisposable?.dispose()
        myExpDisposable?.dispose()
    }
}