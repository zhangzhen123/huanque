package com.julun.huanque.core.ui.main.home

import androidx.lifecycle.*
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.*
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
    private val getInfo = MutableLiveData<Boolean>()//这个作为开关标识

    fun getInfo() {
        getInfo.value = true
    }

    //协程请求示例
    val userInfo: LiveData<UserDetailInfo> = getInfo.switchMap {
        liveData<UserDetailInfo> {
            if (it) {
                request({
                    val user = userService.queryUserDetailInfo(SessionForm()).dataConvert()
                    emit(user)
                }, error = { e ->
                    logger("报错了：$e")
                }, final = {
                    logger("最终返回")
                }, loadState = loadState)
            } else {
                logger("getInfo=false 这里根本不会执行")
            }

        }

    }

}