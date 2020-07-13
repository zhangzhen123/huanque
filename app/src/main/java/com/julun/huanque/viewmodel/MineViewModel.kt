package com.julun.huanque.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.coverError
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.net.service.UserService

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

    //协程请求示例
    val userInfo: LiveData<ReactiveData<UserDetailInfo>> = queryState.switchMap {
        liveData {
            request({
                val user = userService.queryUserDetailInfo().dataConvert()
                emit(ReactiveData(NetStateType.SUCCESS, user))
            }, error = { e ->
                logger("报错了：$e")
                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
            }, final = {
                logger("最终返回")
            }, needLoadState = it == QueryType.INIT)


        }

    }


}