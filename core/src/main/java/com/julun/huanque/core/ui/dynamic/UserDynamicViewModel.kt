package com.julun.huanque.core.ui.dynamic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.DynamicListInfo
import com.julun.huanque.common.bean.forms.PostListsForm
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
 *@Date: 2020/11/24 14:29
 *
 *@Description: DynamicDetailViewModel
 *
 */
class UserDynamicViewModel : BaseViewModel() {


    private val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }

    val dataList: MutableLiveData<ReactiveData<DynamicListInfo<DynamicItemBean>>> by lazy { MutableLiveData<ReactiveData<DynamicListInfo<DynamicItemBean>>>() }


    private var offsetHot = 0

    fun requestPostList(queryType: QueryType, userId: Long?) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offsetHot = 0
            }

            request({
                val result =
                    service.queryUserPosts(PostListsForm(offset = offsetHot, userId = userId)).dataConvert()
                //
                offsetHot += result.list.size
                result.isPull = queryType != QueryType.LOAD_MORE
                dataList.value = result.convertRtData()
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }


}