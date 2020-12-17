package com.julun.huanque.core.ui.dynamic

import androidx.lifecycle.*
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.DynamicGroup
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.bean.forms.GroupPostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.SquareTabType
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/27 15:08
 *
 *@Description: CircleDynamicViewModel
 *
 */
class CircleDynamicViewModel : BaseViewModel() {

    private val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }

    val dynamicDetailInfo: MutableLiveData<ReactiveData<DynamicGroup>> by lazy { MutableLiveData<ReactiveData<DynamicGroup>>() }
    //协程请求示例
    val tabList: LiveData<ReactiveData<ArrayList<PagerTab>>> = queryState.switchMap {
        liveData {
            request({

                val tabObj = StorageHelper.getProgramTabObj()
                val tabTitles: ArrayList<PagerTab> = arrayListOf()
                tabTitles.add(PagerTab("热门", typeCode = SquareTabType.HOT))
                tabTitles.add(PagerTab("最新", typeCode = SquareTabType.NEW))
                emit(tabTitles.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = true)
        }

    }

    fun queryGroupPostDetail(groupId:Long?=null){
        viewModelScope.launch {
            request({
                val result = service.groupPostDetail(GroupPostForm(groupId = groupId)).dataConvert()
                dynamicDetailInfo.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
                dynamicDetailInfo.value = it.convertError()
            })

        }

    }

}