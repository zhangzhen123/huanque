package com.julun.huanque.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.UUidHelper
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/9/10 19:51
 *@描述 欢迎页使用的ViewModel
 */
class WelcomeViewModel : BaseViewModel() {

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    /**
     * 初始化UUID相关
     */
    @SuppressLint("CheckResult")
    fun initUUID() {

        io.reactivex.rxjava3.core.Observable.create<String> {
            val uuId = UUidHelper.getUUID()
            if (uuId.isNotEmpty()) {
                it.onNext(uuId)
            } else {
                var sb = StringBuilder().append("${StringHelper.uuid()}-")
                val random = Random()
                sb.append("${random.nextInt(10)}").append("${random.nextInt(8999999) + 1000000}")
                val str = sb.toString()
                it.onNext(str)
                UUidHelper.saveGlobalUUID(str)
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe({
                SharedPreferencesUtils.commitBoolean(SPParamKey.UUID_Created, true)
                SharedPreferencesUtils.commitString(ParamConstant.UUID, it)
            }, { it.printStackTrace() }, {})

    }

    /**
     * 激活APP
     */
    fun appStart() {
        viewModelScope.launch {
            request({
                userService.appStart().dataConvert()
                SPUtils.commitBoolean(SPParamKey.APP_START, true)
            }, {})
        }
    }
}