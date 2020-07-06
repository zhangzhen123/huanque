package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ishumei.smantifraud.SmAntiFraud
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.Session
import com.julun.huanque.common.bean.forms.MobileQuickForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.net.service.UserService
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/6 14:23
 *@描述 登录页面使用的微信
 */
class LoginViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //登录数据
    val loginData: MutableLiveData<Session> by lazy { MutableLiveData<Session>() }

    /**
     * 手机号一键登录
     */
    fun fastLogin(jToken: String) {
        viewModelScope.launch {
            request({
                val result = userService.mobileQuick(MobileQuickForm(jToken, SmAntiFraud.getDeviceId() ?: "")).dataConvert()
                SessionUtils.setSession(result)
                loginData.postValue(result)
            }, {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            })

        }
    }
}