package com.julun.huanque.core.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.coverError
import com.julun.huanque.common.suger.coverRtData
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class HomeViewModel : BaseViewModel() {

    private val userService: HomeService by lazy {
        Requests.create(HomeService::class.java)
    }

    //协程请求示例
    val tabList: LiveData<ReactiveData<ArrayList<String>>> = queryState.switchMap {
        liveData {
            request({
//                    val user = userService.queryUserDetailInfo(SessionForm()).dataConvert()
//                    emit(user)
                val tabTitles: ArrayList<String> = arrayListOf("交友"/*, "推荐"*/)
                emit(tabTitles.coverRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.coverError())
            }, final = {
                logger("最终返回")
            }, needLoadState = true)
        }

    }

}