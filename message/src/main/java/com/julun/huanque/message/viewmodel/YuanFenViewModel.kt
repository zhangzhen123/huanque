package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.FateWeekInfo
import com.julun.huanque.common.bean.forms.OffsetForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
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
    //派单7日周数据
    val fateWeekInfoBean : MutableLiveData<FateWeekInfo> by lazy { MutableLiveData<FateWeekInfo>() }

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
                offset += data.list.size
                data.isPull = it
                emit(data)
            }, needLoadState = true)
        }
    }

    /**
     * 开始倒计时
     */
    fun countDown() {
        mDisposable?.dispose()
        mDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribe({
                var change = false
                val list = result.value?.list ?: return@subscribe
                list.forEach {
                    val ttl = it.ttl
                    if (ttl > 0) {
                        it.ttl = ttl - 1
                        change = true
                    }
                }
                if (change) {
                    refreshFlag.postValue(true)
                } else {
                    mDisposable?.dispose()
                }
            }, {})
    }

    /**
     * 更新列表数据
     */
    fun updateFate(map: HashMap<String, String>) {
        var change = false
        val keyList = map.keys
        if (keyList.isEmpty()) {
            return
        }
        result.value?.list?.forEach {
            if (keyList.contains(it.fateId)) {
                it.status = map[it.fateId] ?: ""
                change = true
            }
        }
        refreshFlag.value = change
    }

    /**
     * 获取派单近一周的数据
     */
    fun getFateDetail() {
        viewModelScope.launch {
            request({
                val result = socialService.fateStat().dataConvert()
                fateWeekInfoBean.value = result
            })
        }
    }


    override fun onCleared() {
        super.onCleared()
        mDisposable?.dispose()
    }
}