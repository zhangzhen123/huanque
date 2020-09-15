package com.julun.huanque.core.ui.main.bird

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.RandomRoomForm
import com.julun.huanque.common.bean.forms.TaskBirdActiveReceive
import com.julun.huanque.common.bean.forms.TaskBirdReceive
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LeYuanService
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.BalanceUtils
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/1 17:10
 *
 *@Description: 养鸟任务
 *
 */
class BirdTaskViewModel : BaseViewModel() {

    private val service: LeYuanService by lazy {
        Requests.create(LeYuanService::class.java)
    }
    private val liveService: LiveRoomService by lazy {
        Requests.create(LiveRoomService::class.java)
    }
    val receiveTaskResult: MutableLiveData<ReactiveData<BirdTaskReceiveResult>> by lazy { MutableLiveData<ReactiveData<BirdTaskReceiveResult>>() }

    val receiveActiveAward: MutableLiveData<ReactiveData<VoidResult>> by lazy { MutableLiveData<ReactiveData<VoidResult>>() }

    val mRandomRoom: MutableLiveData<ReactiveData<ProgramRoomBean>> by lazy { MutableLiveData<ReactiveData<ProgramRoomBean>>() }
    val taskInfo: LiveData<ReactiveData<BirdTaskInfo>> = queryState.switchMap {
        liveData {
            request({
                val user = service.taskList().dataConvert()
                emit(ReactiveData(NetStateType.SUCCESS, user))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, needLoadState = it == QueryType.INIT)


        }

    }

    fun receiveTask(code: String) {
        viewModelScope.launch {
            request({
                val result = service.receiveTaskAward(TaskBirdReceive(code)).dataConvert()
                receiveTaskResult.value = result.convertRtData()
            }, error = {
                receiveTaskResult.value = it.convertError()
            })

        }

    }

    fun receiveAward(code: String) {
        viewModelScope.launch {
            request({
                val result = service.receiveActiveAward(TaskBirdActiveReceive(code)).dataConvert()
                receiveActiveAward.value = result.convertRtData()
            }, error = {
                receiveActiveAward.value = it.convertError()
            })

        }
    }
    fun randomLive(){
        viewModelScope.launch {
            request({
                val result = liveService.randomRoom(RandomRoomForm("Magpie")).dataConvert()
                mRandomRoom.value = result.convertRtData()
            }, error = {
                mRandomRoom.value = it.convertError()
            })

        }

    }

}