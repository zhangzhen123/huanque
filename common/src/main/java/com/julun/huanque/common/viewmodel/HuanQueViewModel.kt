package com.julun.huanque.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.FateQuickMatchBean
import com.julun.huanque.common.manager.GlobalDialogManager
import com.julun.huanque.common.suger.logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/9/21 15:27
 *@描述 和app生命周期绑定的ViewModel
 */
class HuanQueViewModel(application: Application) : AndroidViewModel(application) {

    //派单的对象
    val fateQuickMatchData: MutableLiveData<FateQuickMatchBean> by lazy { MutableLiveData<FateQuickMatchBean>() }

    //派单的
    val fateQuickMatchTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    private var mPaidanDisposable: Disposable? = null

    /**
     * 设置派单数据
     */
    fun setFateData(bean: FateQuickMatchBean) {
        fateQuickMatchData.value = bean
        mPaidanDisposable?.dispose()
        val count = (bean.expTime - System.currentTimeMillis()) / 1000
        mPaidanDisposable = Observable.intervalRange(0, count + 1, 0, 1, TimeUnit.SECONDS)
            .map { count - it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fateQuickMatchTime.value = it
            }, { it.printStackTrace() }, {
                fateQuickMatchData.postValue(null)
                fateQuickMatchTime.postValue(null)
                GlobalDialogManager.closeDialog()
            })
    }

    /**
     * 清空派单数据
     */
    fun clearFateData() {
        mPaidanDisposable?.dispose()
        fateQuickMatchData.value = null
        fateQuickMatchTime.value = null
        GlobalDialogManager.closeDialog()
    }


}