package com.julun.huanque.core.ui.main.bird

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.BirdCombineForm
import com.julun.huanque.common.bean.forms.BuyBirdForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.bean.forms.RecycleBirdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LeYuanService
import com.julun.huanque.common.suger.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
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

    companion object {
        const val MAX_BIRDS = 12
    }

    private val service: LeYuanService by lazy {
        Requests.create(LeYuanService::class.java)
    }
    val homeInfo: MutableLiveData<ReactiveData<BirdHomeInfo>> by lazy { MutableLiveData<ReactiveData<BirdHomeInfo>>() }
    val buyResult: MutableLiveData<ReactiveData<BuyBirdResult>> by lazy { MutableLiveData<ReactiveData<BuyBirdResult>>() }

    val combineResult: MutableLiveData<ReactiveData<CombineResult>> by lazy { MutableLiveData<ReactiveData<CombineResult>>() }

    val recycleResult: MutableLiveData<ReactiveData<RecycleResult>> by lazy { MutableLiveData<ReactiveData<RecycleResult>>() }

    val totalCoin: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }
    val coinsPerSec: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }
    var programId: Long? = null
    private var currentInfo: BirdHomeInfo? = null
    fun queryHome() {
        viewModelScope.launch {
            request({
                val result = service.birdHome(ProgramIdForm(programId)).dataConvert()
                currentInfo = result
                //处理棋盘 先默认填充12个
                val totalList = mutableListOf<UpgradeBirdBean>()
                repeat(MAX_BIRDS) {
                    totalList.add(UpgradeBirdBean(upgradePos = it))
                }

                result.upgradeList.forEach {
                    if (it.upgradePos < totalList.size) {
                        totalList[it.upgradePos] = it
                    }

                }
                result.upgradeList = totalList
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
                startProcessCoins()
            }, error = {
                it.printStackTrace()
                buyResult.value = it.convertError()
            })

        }

    }

    /**
     * 合体操作
     * 合成升级、移动位置、互换位置都调用该接口
    合并升级：两只同等级的升级鹊合并，升级至下一级，如果已达到最高等级，则产生功能鹊(所有参数不能为空)
    互换位置：两只不同等级的升级鹊合并，互换位置(所有参数不能为空)
    移动位置：一只升级鹊移动到其他空白处(upgradeId2设置为空)
     *
     */
    fun combineBird(
        upgradeId1: Long? = null,
        upgradeId2: Long? = null,
        upgradePos1: Int? = null,
        upgradePos2: Int? = null
    ) {
        viewModelScope.launch {
            request({
                val result = service.combine(BirdCombineForm(upgradeId1, upgradeId2, upgradePos1, upgradePos2)).dataConvert()
                if (result.functionInfo != null) {
                    //刷新整个页面
                    queryHome()
                }
                combineResult.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
            })

        }

    }

    /**
     * 回收鸟
     */
    fun recycleBird(upgradeId: Long) {
        viewModelScope.launch {
            request({
                val result = service.recovery(RecycleBirdForm(programId, upgradeId)).dataConvert()
                recycleResult.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                startProcessCoins()
            }, error = {
                it.printStackTrace()
            })

        }

    }

    /**
     * 开始每秒产生金币
     */
    private var processDispose: Disposable? = null
    private fun startProcessCoins() {
        processDispose?.dispose()
        processDispose = Observable.interval(1, TimeUnit.SECONDS).subscribe {
            val ps = coinsPerSec.value
            if (ps != null && totalCoin.value != null) {
                totalCoin.postValue(totalCoin.value?.add(ps))
            }

        }
    }

}