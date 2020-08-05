package com.julun.huanque.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDLocation
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.bean.forms.SessionForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.core.net.UserService
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.beans.UserLevelInfo
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.NetcallIdForm
import com.julun.huanque.common.bean.forms.SaveLocationForm
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {
    //    val userInfo: LiveData<UserDetailInfo> = liveData {
//        val user = queryUserDetailInfo()
//        emit(user)
//    }
    //当前的fragment下标
    val indexData: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //未读消息数量
    val unreadMsgCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    private val getInfo = MutableLiveData<Boolean>()//这个作为开关标识

    //协程请求示例
    val userInfo: LiveData<UserDetailInfo> = getInfo.switchMap {
        liveData<UserDetailInfo> {
            if (it) {
                request({
                    val user = userService.queryUserDetailInfo().dataConvert()
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
     * 获取语音会话详情
     */
    fun getVoiceCallInfo(callId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.voiceCallInfo(NetcallIdForm(callId)).dataConvert()
                val bundle = Bundle()
                bundle.putString(ParamConstant.TYPE, ConmmunicationUserType.CALLED)
                bundle.putSerializable(ParamConstant.NetCallBean, result)
                ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()

            })
        }
    }

    /**
     * 获取对方数据以及亲密度数据
     */
    fun insertMessage(userId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.userInfo(FriendIdForm(userId)).dataConvert()
                //插入模拟消息
                val targetUser = TargetUserObj()

                val chatExtra = RoomUserChatExtra()

                val sId = "$userId"
                targetUser.apply {
                    headPic = SessionUtils.getHeaderPic()
                    nickname = SessionUtils.getNickName()
                    meetStatus = result.meetStatus
                    this.userId = SessionUtils.getUserId()
                    sex = SessionUtils.getSex()
                    intimateLevel = result.intimateLevel
                    stranger = result.stranger
                }

                chatExtra.apply {
                    headPic = result.headPic
                    senderId = result.userId
                    nickname = result.nickname
                    sex = result.sex
                    targetUserObj = targetUser
                    userAbcd = AppHelper.getMD5(sId)
                }

                val bean = VoiceConmmunicationSimulate(type = VoiceResultType.MINE_REFUSE)

                RongCloudManager.sendSimulateMessage(
                    "$userId",
                    sId,
                    chatExtra,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Voice_Conmmunication_Simulate,
                    bean
                )
            }, {})
        }
    }

    /**
     * 被叫忙
     */
    fun busy(callId: Long) {
        viewModelScope.launch {
            request({
                socialService.voiceBusy(NetcallIdForm(callId)).dataConvert()
            }, {})
        }
    }

    /**
     * 保存定位地址
     */
    fun saveLocation(form: SaveLocationForm) {
        viewModelScope.launch {
            request({
                userService.saveLocation(form = form).dataConvert()
            })
        }
    }

    /**
     * 获取未读数
     */
    fun getUnreadCount() {
        RongCloudManager.queryPMessage {
            unreadMsgCount.postValue(it)
        }
    }

}