package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/9/18 19:29
 *@描述 更新用户数据的ViewModel
 */
class UpdateInfoViewModel : BaseViewModel() {
    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

    var birthdayData: Date? = null

    //昵称是否可用标识
    val nicknameEnable: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //昵称是否有变化
    var nicknameChange = false
}