package com.julun.huanque.core.ui.main.dynamic_square

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.HomeDynamicListInfo
import com.julun.huanque.common.bean.forms.GroupPostForm
import com.julun.huanque.common.bean.forms.HomePostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.suger.convertListError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date 2019/7/16 19:29
 *
 *@Description 查询直播间关注列表
 *
 */
class DynamicTabViewModel : BaseViewModel() {

    private val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }
    val dataList: MutableLiveData<ReactiveData<HomeDynamicListInfo>> by lazy { MutableLiveData<ReactiveData<HomeDynamicListInfo>>() }

    val postList: MutableLiveData<ReactiveData<RootListData<DynamicItemBean>>> by lazy { MutableLiveData<ReactiveData<RootListData<DynamicItemBean>>>() }
    private var offsetHot = 0
    fun requestPostList(queryType: QueryType, postType: String?) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offsetHot = 0
            }

            request({
                val result =
                    service.queryHomePost(HomePostForm(offset = offsetHot, postType = postType)).dataConvert()
//                result.hasMore = false
                //
                offsetHot += result.postList.size
                result.isPull = queryType != QueryType.LOAD_MORE
                dataList.value = result.convertRtData()
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

    fun requestGroupPostList(queryType: QueryType, groupId: Long?, orderType: String?) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offsetHot = 0
            }

            request({
                val result =
                    service.groupPostList(GroupPostForm(offset = offsetHot, orderType = orderType, groupId = groupId))
                        .dataConvert()
                //
                offsetHot += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                postList.value = result.convertRtData()
            }, error = {
                postList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }


}