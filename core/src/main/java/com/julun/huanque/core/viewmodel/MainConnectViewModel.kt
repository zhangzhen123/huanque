package com.julun.huanque.core.viewmodel

import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.forms.SaveLocationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.init.HuanQueInit
import com.julun.maplib.LocationService
import kotlinx.coroutines.launch

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

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }

    //
    val heartBeatSwitch: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val refreshNearby: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val locationTag: MutableLiveData<BDLocation?> by lazy { MutableLiveData<BDLocation?>() }

    //百度地图监听的Listener
    private var mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            logger("location error=${location?.locTypeDescription}")
            if (null != location && location.locType != BDLocation.TypeServerError && location.locType != BDLocation.TypeCriteriaException) {
                logger("location=${location.addrStr}")
                //获得一次结果，就结束定位
                stopLocation()

                locationTag.value = location

                saveLocation(
                    SaveLocationForm(
                        "${location.latitude}",
                        "${location.longitude}",
                        location.city ?: "",
                        location.province,
                        location.district
                    )
                )
            } else {
                locationTag.value=null
//                ToastUtils.show("无法获取定位 请确保已打开定位开关")
            }
        }
    }
    private val mLocationService: LocationService by lazy {
        LocationService(CommonInit.getInstance().getApp()).apply {
            registerListener(mLocationListener)
        }
    }
    init {
        mLocationService.setLocationOption(mLocationService.defaultLocationClientOption.apply {
//            this.setScanSpan(0)
            this.isOpenGps = true
        })
    }
    fun startLocation() {
        mLocationService.start()
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        mLocationService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        mLocationService.unregisterListener(mLocationListener)
    }

    var hasLocation: Boolean = false

    /**
     * 保存定位地址
     */
    fun saveLocation(form: SaveLocationForm) {
        if (hasLocation) {
            return
        }
        viewModelScope.launch {
            request({
                userService.saveLocation(form = form).dataConvert()
                hasLocation = true
            })
        }
    }
}