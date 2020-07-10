package com.julun.huanque.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.net.service.UserService
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ConmmunicationUserType
import com.julun.huanque.common.constant.ParamKey
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {
    //    val userInfo: LiveData<UserDetailInfo> = liveData {
//        val user = queryUserDetailInfo()
//        emit(user)
//    }
    //当前的fragment下标
    val indexData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    private val getInfo = MutableLiveData<Boolean>()//这个作为开关标识

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
                }, needLoadState = true)
            } else {
                logger("getInfo=false 这里根本不会执行")
            }

        }

    }

    fun getInfo() {
        getInfo.value = true
    }

    //rxjava3的请求示例
    val userLevelInfo = MutableLiveData<UserLevelInfo>()

    //传统的rx请求模式
    fun getUserLevelByRx2() {
        requestRx(
            { userService.queryUserLevelInfoBasic(SessionForm()) },
            onSuccess = {
                logger("请求成功结果：it")
                userLevelInfo.value = it
            },
            error = {
                logger("请求报错了$it")
            },
            final = {
                logger("最终返回的")
            },
            loadState = loadState
        )
    }


    //传统的rx请求模式
    fun getUserLevelByRx() {
        userService.queryUserLevelInfoBasic(SessionForm()).handleResponse(makeSubscriber<UserLevelInfo> {
            logger("请求成功结果：it")
            userLevelInfo.value = it
        }.ifError {
            logger("请求报错了$it")
        }.withFinalCall {
            logger("最终返回的")
        }.withSpecifiedCodes(1, 2, 3))
    }


    /**
     * 获取用户信息
     */
    suspend fun queryUserDetailInfo(): UserDetailInfo {
//        return withContext(Dispatchers.IO) {
        logger("开始请求网络：${Thread.currentThread().name}")
        try {
            val call = userService.queryUserDetailInfo(SessionForm())
//                val result = call.execute().body()
            logger("请求完成：${Thread.currentThread().name}")
            return call.data!!
        } catch (e: Exception) {
            logger("报错了")
            e.printStackTrace()
        }
        return UserDetailInfo()
//        }

    }

    /**
     * 获取语音会话详情
     */
    fun getVoiceCallInfo(callId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.voiceCallInfo(NetcallIdForm(callId)).dataConvert()
                val bundle = Bundle()
                bundle.putString(ParamKey.TYPE, ConmmunicationUserType.CALLED)
                bundle.putSerializable(ParamKey.CallReceiveBean, result)
                ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()

            })
        }
    }
}