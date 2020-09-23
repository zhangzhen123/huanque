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
import kotlinx.coroutines.launch
import java.math.BigInteger

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: 乐园首页的相关
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

    val unlockUpgrade: MutableLiveData<UnlockUpgrade> by lazy { MutableLiveData<UnlockUpgrade>() }
    val totalCoin: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }
    val coinsPerSec: MutableLiveData<BigInteger> by lazy { MutableLiveData<BigInteger>() }

    val functionInfo: MutableLiveData<FunctionBird> by lazy { MutableLiveData<FunctionBird>() }

    //功能鹊描述
    val functionBirds: MutableLiveData<MutableList<FunctionBirdDes>> by lazy { MutableLiveData<MutableList<FunctionBirdDes>>() }

    val hasNotReceive: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    private val functionBirdsList = mutableListOf<FunctionBirdDes>()
    var programId: Long? = null
    var currentInfo: BirdHomeInfo? = null
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
                        it.createTime = (System.currentTimeMillis() + it.upgradePos * 200 + 200) / 100
                        totalList[it.upgradePos] = it
//                        logger("createTime ${it.upgradePos}---${it.createTime}")
                    }

                }
                result.upgradeList = totalList
                homeInfo.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                unlockUpgrade.value = result.unlockUpgrade
                hasNotReceive.value =result.hasNotReceive
//                startProcessCoins()
            }, error = {
                it.printStackTrace()
                homeInfo.value = it.convertError()
            })

        }

    }

    fun buyBird() {
        viewModelScope.launch {
            request({
                val level = unlockUpgrade.value?.upgradeLevel ?: return@request
                val result = service.buyBird(BuyBirdForm(programId, level)).dataConvert(intArrayOf(501))
                buyResult.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                if (result.unlockUpgrade != null) {
                    unlockUpgrade.value = result.unlockUpgrade
                }
//                startProcessCoins()
            }, error = {
                it.printStackTrace()
                buyResult.value = it.convertError()
            })

        }

    }

    //供商店用
    fun buyBird(level: Int) {
        viewModelScope.launch {
            request({
                val result = service.buyBird(BuyBirdForm(programId, level)).dataConvert(intArrayOf(501))
                buyResult.value = result.convertRtData()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                if (result.unlockUpgrade != null) {
                    unlockUpgrade.value = result.unlockUpgrade
                }
//                startProcessCoins()
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
//                if (result.functionInfo != null) {
//                    functionInfo.value = result.functionInfo
//                    //刷新整个页面
//                    queryHome()
//                }
                if (result.unlockUpgrade != null) {
                    unlockUpgrade.value = result.unlockUpgrade
                }
                combineResult.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
                combineResult.value = it.convertError()
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
//                startProcessCoins()
            }, error = {
                it.printStackTrace()
                recycleResult.value = it.convertError()
            })

        }

    }

    /**
     * 开始每秒产生金币
     */
//    private var processDispose: Disposable? = null
//    private fun startProcessCoins() {
//        processDispose?.dispose()
//        processDispose = Observable.interval(1, TimeUnit.SECONDS).subscribe {
//            val ps = coinsPerSec.value
//            if (ps != null && totalCoin.value != null) {
//                totalCoin.postValue(totalCoin.value?.add(ps))
//            }
//
//        }
//    }
    fun startProcessCoins(ps: BigInteger) {
        if (totalCoin.value != null) {
            totalCoin.postValue(totalCoin.value?.add(ps))
        }

    }

    fun getFunctionBirdInfo() {
        functionBirdsList.clear()
        val wealth = currentInfo?.functionInfo?.wealth
        val cowherd = currentInfo?.functionInfo?.cowherd
        val mystical = currentInfo?.functionInfo?.mystical
        val redpacket = currentInfo?.functionInfo?.redpacket
        val weaver = currentInfo?.functionInfo?.weaver

        functionBirdsList.add(
            FunctionBirdDes(
                "财神鹊", wealth?.functionIcon,
                bFunction = "放飞后获得888元零钱奖励。",
                source = "两个lv37小鹊合并，有机会获得。\n两个神秘鹊合并，有机会获得。",
                num = wealth?.functionNum?.toIntOrNull(),
                type = "wealth"
            )
        )
        functionBirdsList.add(
            FunctionBirdDes(
                "红包鹊",
                redpacket?.functionIcon,
                bFunction = "放飞后有机会获得1-100元零钱奖励。",
                source = "两个lv37小鹊合并，有机会获得。\n两个神秘鹊合并有机会获得。",
                num = redpacket?.functionNum?.toIntOrNull(),
                type = "redpacket"
            )
        )
        functionBirdsList.add(
            FunctionBirdDes(
                "牛郎鹊",
                cowherd?.functionIcon,
                bFunction = "和织女鹊一起放飞，获得88元零钱奖励。",
                source = "两个lv37小鹊合并，有机会获得。\n两个神秘鹊合并，有机会获得。",
                num = cowherd?.functionNum?.toIntOrNull(),
                type = "cowherd"
            )
        )
        functionBirdsList.add(
            FunctionBirdDes(
                "织女鹊",
                weaver?.functionIcon,
                bFunction = "和牛郎鹊一起放飞，获得88元零钱奖励。",
                source = "两个lv37小鹊合并，有机会获得。\n两个神秘鹊合并，有机会获得。",
                num = weaver?.functionNum?.toIntOrNull(),
                type = "weaver"
            )
        )
        functionBirdsList.add(
            FunctionBirdDes(
                "神秘鹊",
                mystical?.functionIcon,
                bFunction = "两个神秘鹊放飞，随机获得财神鹊、红包鹊、牛郎鹊、织女鹊。",
                source = "两个lv37小鹊合并，有机会获得。",
                num = mystical?.functionNum?.toIntOrNull(),
                type = "mystical"
            )
        )
    }

    /**

    2.财神鹊，显示icon、图片。
    -功能：放飞后获得888元零钱奖励。
    -来源：两个神秘鹊合并，有机会获得。
    3.红包鹊
    -功能：放飞后有机会获得1-100元零钱奖励。
    -来源：两个lv37小鹊合并，有机会获得。两个神秘鹊合并有机会获得。
    4.牛郎鹊
    功能：和织女鹊一起放飞，获得00元零钱奖励。
    来源：两个lv37小鹊合并，有机会获得。两个神秘鹊合并，有机会获得。
    5.织女鹊
    -功能：和牛郎鹊一起放飞，获得88元零钱奖励。
    -来源：两个lv37小鹊合并，有机会获得。两个神秘鹊合并，有机会获得。
    6.神秘鹊
    -功能：两个神秘鹊放飞，随机获得财神鹊、红包鹊、牛郎鹊、织女鹊。
    -来源：两个lv37小鹊合并，有机会获得。
     */
    fun gotFunctionBirdInfos() {
        getFunctionBirdInfo()
        functionBirds.value = functionBirdsList
    }

    fun gotFunctionBirdInfo(type: String): FunctionBirdDes? {
        getFunctionBirdInfo()
        functionBirdsList.forEach {
            if (it.type == type) {
                return it
            }
        }
        return null
    }

    fun refreshCoins(){
        viewModelScope.launch {
            request({
                val result = service.coinsInfo(ProgramIdForm(programId)).dataConvert()
                totalCoin.value = result.totalCoins
                coinsPerSec.value = result.coinsPerSec
                hasNotReceive.value =result.hasNotReceive
            }, error = {
                it.printStackTrace()
                recycleResult.value = it.convertError()
            })

        }

    }
}