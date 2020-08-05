package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/8/4 20:14
 *@描述 直播间向私聊页面传递数据
 */
class PlayerMessageViewModel : BaseViewModel() {

    //直播间内，主播基础数据
    val anchorData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }
}