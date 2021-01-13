package com.julun.huanque.core.ui.tag_manager

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.beans.ManagerTagTabBean
import com.julun.huanque.common.bean.beans.MyTagListData
import com.julun.huanque.common.bean.forms.MyTagForm
import com.julun.huanque.common.bean.forms.TagForm
import com.julun.huanque.common.bean.forms.TagListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.TagService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/30 16:33
 *
 *@Description: TagManagerViewModel 由于标签的改动很多地方都有 这里做成全局单一对象
 *
 */
class MyTagViewModel : BaseViewModel() {

    private val service: TagService by lazy { Requests.create(TagService::class.java) }


    val myList: MutableLiveData<ReactiveData<MyTagListData>> by lazy { MutableLiveData<ReactiveData<MyTagListData>>() }

    fun queryMyTagList(queryType: QueryType, type: String) {
        viewModelScope.launch {

            request({
                val result = service.myList(MyTagForm(type)).dataConvert()
                myList.value = result.convertRtData()
            }, error = {
                myList.value = it.convertError()
            }, needLoadState = queryType == QueryType.INIT)
        }

    }

}