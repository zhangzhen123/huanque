package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.bean.beans.ManagerOptionInfo
import com.julun.huanque.common.bean.forms.CardManagerForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/31
 */
class CardManagerViewModel : BaseViewModel() {

    val auditResult by lazy { MutableLiveData<Boolean>() }
    val listResult by lazy { MutableLiveData<ArrayList<ManagerOptionInfo>>() }

    val canShowFlag : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    fun saveManage(info: ManagerInfo, programId: Long, targetId: Long) {
        viewModelScope.launch {
            request({
                Requests.create(LiveRoomService::class.java).saveManage(
                    CardManagerForm(
                        programId = programId,
                        targetUserId = targetId,
                        mangeType = info.mangeType,
                        itemValue = if (info.itemValue.isNullOrEmpty()) {
                            null
                        } else {
                            info.itemValue
                        },
                        deviceUuid = if (info.deviceUuid.isNullOrEmpty()) {
                            null
                        } else {
                            info.deviceUuid
                        }
                    )
                ).dataConvert()
                ToastUtils.show("${info.mangeTypeDesc}成功~!")
                auditResult.value = true
            }, error = { e ->
                if (e !is ResponseError) {
                    ToastUtils.show("网络异常，请重试~！")
                }
            })
        }
    }

    /**
     * @param judgeShow 判断是否可以显示管理弹窗
     */
    fun getManage(programId: Long, targetId: Long, judgeShow: Boolean = false) {
        viewModelScope.launch {
            request({
                val list =
                    Requests.create(LiveRoomService::class.java).getManage(
                        CardManagerForm(
                            programId = programId,
                            targetUserId = targetId
                        )
                    )
                        .dataConvert()
                if(judgeShow){
                    canShowFlag.value = list.isNotEmpty()
                }else{
                    listResult.value = list
                }
            }, error = { e ->
                if (e !is ResponseError) {
                    ToastUtils.show("网络异常，请重试~！")
                }
            }, needLoadState = true)
        }
    }
}