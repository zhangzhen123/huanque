package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.FavoriteUserBean
import com.julun.huanque.common.bean.forms.LikeForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class FavoriteTabViewModel : BaseViewModel() {

    private val service: HomeService by lazy { Requests.create(HomeService::class.java) }

    val dataList: MutableLiveData<ReactiveData<MutableList<FavoriteUserBean>>> by lazy { MutableLiveData<ReactiveData<MutableList<FavoriteUserBean>>>() }

    val currentList: MutableList<FavoriteUserBean> = mutableListOf()
    var offset = 0

    fun addFirstList(list: MutableList<FavoriteUserBean>) {
        offset += list.size
        currentList.addAll(list)
        if (currentList.size >= 4) {
            val subList = currentList.sliceFromStart(4,removeSource = true)
            dataList.value = subList.convertRtData()
        }
    }

    fun requestProgramList(queryType: QueryType, typeCode: Int?) {
        //先从本地去取 然后再请求网络
        if (currentList.size >= 4) {
            logger("有缓存队列 直接使用=${currentList.size}")
            val subList = currentList.sliceFromStart(4,true)
            dataList.value = subList.convertRtData()
            return
        }

        viewModelScope.launch {
            if (queryType != QueryType.LOAD_MORE) {
                offset = 0
            }

            request({
                val result =
                    service.getLikeList(LikeForm(offset, typeCode)).dataConvert()
                currentList.addAll(result.list)
                currentList.removeDuplicate()
                offset += result.list.size
//                result.isPull = queryType != QueryType.LOAD_MORE
                if (currentList.size >= 4) {
                    val subList = currentList.sliceFromStart(4,true)
                    dataList.value = subList.convertRtData()
                }
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}