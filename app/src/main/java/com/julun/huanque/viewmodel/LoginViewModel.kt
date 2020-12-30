package com.julun.huanque.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.LoginStatusUtils
import com.julun.huanque.support.LoginManager
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/6 14:23
 *@描述 登录页面使用的微信
 */
class LoginViewModel : BaseViewModel() {

    companion object {
        //登录弹窗初始化状态
        const val Fragment_State_Init = "Fragment_State_Init"

        //登录弹窗手机号验证码页面
        const val Fragment_State_Phone = "Fragment_State_Phone"

        //输入验证码页面
        const val Fragment_State_Code = "Fragment_State_Code"

    }

    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

    //登录数据
    val loginData: MutableLiveData<Session> by lazy { MutableLiveData<Session>() }

    //登录状态
    val loginStatus: LiveData<Boolean> by lazy { LoginStatusUtils.getLoginStatus() }

    //当前登录弹窗的状态
    val currentFragmentState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val finishState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //点击微信登录的标记位
    val weixinLoginFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //开始粘贴
    val pasteFlag : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 手机号一键登录
     */
    fun fastLogin(jToken: String) {
        viewModelScope.launch {
            LoginManager.fastLogin(jToken, success = { result ->
                loginData.postValue(result)
//                EventBus.getDefault().post(result)
            }, error = {
//                if (it is ResponseError) {
//                    ToastUtils.show(it.busiMessage)
//                }
            })
        }
    }

    fun weiXinLogin(code: String) {
        logger("weiXinLogin的线程：${Thread.currentThread().name}")
        viewModelScope.launch {
            LoginManager.doLoginByWinXin(code) {
                logger("weiXinLogin处理结果的线程：${Thread.currentThread().name}")
                loginData.postValue(it)
//                EventBus.getDefault().post(it)
            }

        }
        logger("weiXinLogin后当前的线程：${Thread.currentThread().name}")
    }
}