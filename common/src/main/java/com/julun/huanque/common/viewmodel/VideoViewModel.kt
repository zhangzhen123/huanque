package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2019/12/3 15:01
 *@描述 播放器使用Viewmodel
 */
class VideoViewModel : BaseViewModel() {
    //是否是声网推流
    var agora = false
    //切换房间
    val checkoutRoom: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    //登录状态
    val loginState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //进入直播间之前的基础信息
    val baseData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }
    //加入直播间成功
    val loginSuccessData: MutableLiveData<UserEnterRoomRespDto> by lazy { MutableLiveData<UserEnterRoomRespDto>() }
    //value为true时 关闭页面
    val finishState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //主播直播间ID
    val anchorProgramId: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //打开相应功能的简单设置（合并一些只需要传Boolean值的设置）
    val actionBeanData: MutableLiveData<BottomActionBean> by lazy { MutableLiveData<BottomActionBean>() }
    //开播的相关操作
    val publishActionBeanData: MutableLiveData<BottomActionBean> by lazy { MutableLiveData<BottomActionBean>() }
    //播放相关数据
    val playInfoData: MutableLiveData<PlayInfo> by lazy { MutableLiveData<PlayInfo>() }
    //主播加入房间成功
    val anchorLoginRoomSuccess: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //清空所有的流 1代表常规关闭 2代表关闭所有（常规和单例播放器）  3代表关闭所有 同时单例播放器断开视图连接
    val stopAllStreamState: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    //主播端是否在推流
    var anchorIsPublishing = false

    //新增播放器viewModel管理播放相关数据 防止在fragment重建时丢失 引起一系列崩溃
    val playerData: MutableLiveData<LiveBean> by lazy { MutableLiveData<LiveBean>() }
    //ZEGO使用
    val logout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //主播开播前的数据
    val guardAgainstData: MutableLiveData<GuardAgainst> by lazy { MutableLiveData<GuardAgainst>() }

    //推流切换需要使用的数据
    val switchPublishData: MutableLiveData<SwitchPublishBaseData> by lazy { MutableLiveData<SwitchPublishBaseData>() }
    //释放AGORA
    val agoraReleaseTag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //释放ZEGO
    val zegoReleaseTag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //接收到切换CDN消息的标识位(单流切换使用)
    val toggleCDNState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    //恢复默认设置标识位
    val recoverDefaultState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}