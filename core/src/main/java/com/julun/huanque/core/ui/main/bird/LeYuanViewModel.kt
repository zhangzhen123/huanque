package com.julun.huanque.core.ui.main.bird

import androidx.lifecycle.*
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.BuyBirdForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.bean.forms.UserOnlineHeartForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.net.services.LeYuanService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ToastUtils
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.util.concurrent.TimeUnit

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class LeYuanViewModel : BaseViewModel() {


    private val service: LeYuanService by lazy {
        Requests.create(LeYuanService::class.java)
    }
    val homeInfo: MutableLiveData<ReactiveData<BirdHomeInfo>> by lazy { MutableLiveData<ReactiveData<BirdHomeInfo>>() }
    val buyResult: MutableLiveData<ReactiveData<BuyBirdResult>> by lazy { MutableLiveData<ReactiveData<BuyBirdResult>>() }

    val totalCoin: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }
    val coinsPerSec: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }
    var programId: Long? = null
    private var currentInfo: BirdHomeInfo? = null
    fun queryHome() {
        viewModelScope.launch {
            request({
                val result = service.birdHome(ProgramIdForm(programId)).dataConvert()
                currentInfo = result
                homeInfo.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                startProcessCoins()
            }, error = {
                it.printStackTrace()
                homeInfo.value = it.convertError()
            })

        }

    }

    fun buyBird() {
        viewModelScope.launch {
            request({
                val level = currentInfo?.unlockUpgrade?.upgradeLevel ?: return@request
                val result = service.buyBird(BuyBirdForm(programId, level)).dataConvert()

                buyResult.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
            }, error = {
                it.printStackTrace()
                buyResult.value = it.convertError()
            })

        }

    }

    /**
     * 开始每秒产生金币
     */
    private var processDispose: Disposable? = null
    fun startProcessCoins() {
        processDispose?.dispose()
        processDispose = Observable.interval(1, TimeUnit.SECONDS).subscribe {
            val ps = coinsPerSec.value
            if (ps != null && totalCoin.value != null) {
                totalCoin.value = totalCoin.value?.add(ps)
            }

        }
    }

}