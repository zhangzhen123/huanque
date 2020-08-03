package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.Handbook
import com.julun.huanque.common.bean.beans.LastShowGiftStateBean
import com.julun.huanque.common.bean.beans.LiveGiftDto
import com.julun.huanque.common.bean.beans.SingleTipsUpdate
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.core.net.UserService

/**
 *@创建者   dong
 *@创建时间 2019/9/9 9:14
 *@描述 原先：砸蛋设置ViewModel（模式和匿名状态）
 *   礼物说明使用的ViewModel
 */
class EggSettingViewModel : BaseViewModel() {
    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    private val liveRoomService: LiveRoomService by lazy {
        Requests.create(LiveRoomService::class.java)
    }
    //当前送礼面板选中的礼物
    val mSelectGiftData: MutableLiveData<LiveGiftDto> by lazy { MutableLiveData<LiveGiftDto>() }
    //按钮状态 开启还是关闭
    val anonymousState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //砸蛋模式 true表示为幸运模式，false表示为高爆模式
    val eggLuckyState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //需要更新的tips列表
    val updateTipsData: MutableLiveData<List<SingleTipsUpdate>> by lazy { MutableLiveData<List<SingleTipsUpdate>>() }
    //视图位置
    val locationData: MutableLiveData<IntArray> by lazy { MutableLiveData<IntArray>() }
    //折扣券状态（优先折扣券 or 优先背包）
    val discountStatus : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //图鉴数据
    val handbookData: MutableLiveData<MutableList<Handbook>> by lazy { MutableLiveData<MutableList<Handbook>>() }
    //当前礼物上次点击是否显示过说明弹窗
    val mCurrentGiftLastClickShowExplainBean : MutableLiveData<LastShowGiftStateBean> by lazy { MutableLiveData<LastShowGiftStateBean>() }
    /**
     *更新匿名
     */
    fun updateAnonymous() {
//        userService.updateAnonymous(EmptyForm())
//                .handleResponse(makeSubscriber<Boolean> {
//                    anonymousState.value = it
//                }.ifError { anonymousState.value = anonymousState.value })
    }

    /**
     * 更新砸蛋模式
     */
    fun updateEggPattern() {
//        userService.updateLuckyHitEgg()
//                .handleResponse(makeSubscriber {
//                    updateTipsData.value = it.refreshGifts
//                    eggLuckyState.value = it.luckyHitEgg
//                })
    }

    /**
     * 更新折扣券状态
     */
    fun updateDiscountSet() {
//        userService.updateDiscountSet().handleResponse(makeSubscriber<Boolean> {
//            discountStatus.value = it
//        }.ifError {
//            ToastUtils.show("网络异常，请检查网络！")
//        })
    }

    /**
     * 获取图鉴数据
     */
    fun getHandbookData() {
//        liveRoomService.handbook()
//                .handleResponse(makeSubscriber {
//                    handbookData.value = it
//                })
    }

}