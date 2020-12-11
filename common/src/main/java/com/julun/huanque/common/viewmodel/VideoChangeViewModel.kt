package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.MicAnchor
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.suger.logger


/**
 *
 *@author zhangzhen
 *@data 2018/9/26
 * 管理新增拉流 移除拉流
 *
 **/

class VideoChangeViewModel : BaseViewModel() {


    //
    val addPlayerDatas: MutableLiveData<List<MicAnchor>> by lazy { MutableLiveData<List<MicAnchor>>() }

    val rmPlayerDatas: MutableLiveData<List<MicAnchor>> by lazy { MutableLiveData<List<MicAnchor>>() }

    val refreshLayout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val showVideoInfo: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val curPlayerList = hashSetOf<MicAnchor>()

    fun addPlayer(playerList: List<MicAnchor>) {
        playerList.forEach {
            it.streamID = "${it.programId}"
            curPlayerList.add(it)
        }
        addPlayerDatas.value = playerList
        refreshLayout.value = true
    }
    //每次add前检测是否还存在未移除的老连麦
    //该操作已经放在livePlayerFragment的handleStreamAdded 这样的好处是操作安全可靠 不会有传值不达以及顺序错乱的问题
    fun checkExit(){
        if(curPlayerList.isNotEmpty()){
            val list= arrayListOf<MicAnchor>()
            list.addAll(curPlayerList)
            rmPlayerDatas.value = list
            curPlayerList.clear()
        }
    }
    fun removePlayer(playerList: List<MicAnchor>) {
        playerList.forEach {
            curPlayerList.remove(it)
            it.streamID = "${it.programId}"
        }
        rmPlayerDatas.value = playerList
        refreshLayout.value = true
    }

    /**
     * 判断是不是单主播
     */
    fun isSingleAnchor(): Boolean {
        logger("当前的拉流数目：" + curPlayerList.size)
        return curPlayerList.isEmpty()
    }

    fun resetPlayerCount() {
        curPlayerList.clear()
        addPlayerDatas.value=null
        rmPlayerDatas.value=null
    }


}