package com.julun.huanque.ui.safe

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.UserSecurityInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.net.services.UserService

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/14 15:14
 *
 *@Description: AccountAndSecurityViewModel
 *
 */
class AccountAndSecurityViewModel : BaseViewModel() {

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    val checkAuthorResult: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }


    //协程请求示例
    val securityInfo: LiveData<ReactiveData<UserSecurityInfo>> = queryState.switchMap {
        liveData {
            request({
                val user = userService.security().dataConvert()
                emit(ReactiveData(NetStateType.SUCCESS, user))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, needLoadState = it == QueryType.INIT)


        }

    }

}