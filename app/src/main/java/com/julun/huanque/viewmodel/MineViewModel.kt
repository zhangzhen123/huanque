package com.julun.huanque.viewmodel

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.core.net.UserService
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/11 14:05
 *
 *@Description: MineViewModel
 *
 */
class MineViewModel : BaseViewModel() {

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    val checkAuthorResult: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }

    //协程请求示例
    val userInfo: LiveData<ReactiveData<UserDetailInfo>> = queryState.switchMap {
        liveData {
            request({
                val user = userService.queryUserDetailInfo().dataConvert()
                BalanceUtils.saveBalance(user.userBasic.beans)
                emit(ReactiveData(NetStateType.SUCCESS, user))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, needLoadState = it == QueryType.INIT)


        }

    }

    fun checkToAnchor() {
        viewModelScope.launch {
            request({
                userService.checkToAnchor().dataConvert(intArrayOf(ErrorCodes.NOT_INFO_COMPLETE,ErrorCodes.NOT_BIND_WECHAT,ErrorCodes.NOT_REAL_NAME))
                checkAuthorResult.value=true.convertRtData()
            }, error = {
                checkAuthorResult.value=it.convertError()
            })
        }


    }

}