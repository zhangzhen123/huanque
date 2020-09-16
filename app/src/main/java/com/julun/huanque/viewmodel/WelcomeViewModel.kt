package com.julun.huanque.viewmodel

import android.annotation.SuppressLint
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.UUidHelper
import com.julun.huanque.common.utils.SharedPreferencesUtils
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/9/10 19:51
 *@描述 欢迎页使用的ViewModel
 */
class WelcomeViewModel : BaseViewModel() {

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
                SharedPreferencesUtils.commitString(ParamConstant.UUID, it)
            }, { it.printStackTrace() }, {})


    }
}