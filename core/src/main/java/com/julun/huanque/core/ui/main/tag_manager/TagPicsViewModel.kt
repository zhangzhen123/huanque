package com.julun.huanque.core.ui.main.tag_manager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.TagDetailBean
import com.julun.huanque.common.bean.beans.TagPicBean
import com.julun.huanque.common.bean.forms.TagDetailForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
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
 *@Description 关注列表
 *
 */
class TagPicsViewModel : BaseViewModel() {

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    val tagDetail: MutableLiveData<ReactiveData<TagDetailBean<TagPicBean>>> by lazy { MutableLiveData<ReactiveData<TagDetailBean<TagPicBean>>>() }


    private var offset: Int = 0
    fun requestTagList(queryType: QueryType, tagId:Int, friendId: Long) {

        viewModelScope.launch {
            if (queryType != QueryType.LOAD_MORE) {
                offset = 0
            }

            request({

                val form=TagDetailForm(tagId,friendId = null,offset = offset)
                val result =
                    userService.tagDetail(form).dataConvert()
                offset += result.authPage.list.size
                result.authPage.isPull = queryType != QueryType.LOAD_MORE
                tagDetail.value = result.convertRtData()
            }, error = {
                tagDetail.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}