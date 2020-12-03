package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.AccountBean
import com.julun.huanque.common.bean.forms.UserIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.support.LoginManager
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/2 18:46
 *@描述 分身账号列表ViewModel
 */
class AccountManagerViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //账号数据
    val accountBeanData: MutableLiveData<AccountBean> by lazy { MutableLiveData<AccountBean>() }

    //登录成功标识
    val loginSuccessData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 获取子账号列表
     */
    fun queryAccountList() {
        viewModelScope.launch {
            request({
                accountBeanData.value = userService.subList().dataConvert()
            }, {})
        }
    }

    /**
     * 登录分身账号
     */
    fun accountLogin(userId: Long) {
        if (userId == SessionUtils.getUserId()) {
            //准备登录的账号就是当前登录的账号
            return
        }
        viewModelScope.launch {
            LoginManager.loginBySubAccount(userId) { it ->
                loginSuccessData.postValue(true)
            }
        }
    }
}