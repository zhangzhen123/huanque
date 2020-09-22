package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.bean.forms.LiveRemindForm
import com.julun.huanque.common.bean.forms.OffsetForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRemindService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/9/22 14:04
 *@描述 缘分页面使用
 */
class YuanFenViewModel : BaseViewModel() {

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    private var mDisposable: Disposable? = null
    val queryData = MutableLiveData<Boolean>()

    val refreshFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private var offset = 0

    val result = queryData.switchMap {
        liveData {
            it ?: return@liveData
            if (it) {
                offset = 0
            }
            request({
                val data =
                    socialService.fateList(OffsetForm(offset = offset))
                        .dataConvert()
                if (offset == 0) {
                    countDown()
                }
                offset += data.list.size
                data.isPull = it
                emit(data)
            }, needLoadState = true)
        }
    }

    /**
     * 开始倒计时
     */
    private fun countDown() {
        mDisposable?.dispose()
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .map {
                result.value?.list?.forEach {
                    val ttl = it.ttl
                    if (ttl > 0) {
                        it.ttl = ttl - 1
                    }
                }
            }
            .subscribe({
                refreshFlag.postValue(true)
            }, {})
    }

}