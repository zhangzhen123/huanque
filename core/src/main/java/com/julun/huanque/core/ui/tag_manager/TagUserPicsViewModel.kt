package com.julun.huanque.core.ui.tag_manager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ManagerTagBean
import com.julun.huanque.common.bean.beans.TagUserPicListBean
import com.julun.huanque.common.bean.forms.TagForm
import com.julun.huanque.common.bean.forms.TagUserForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.TagService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/29 16:37
 *
 *@Description: TagUserPicsViewModel
 *
 */
class TagUserPicsViewModel : BaseViewModel() {

    private val service: TagService by lazy { Requests.create(TagService::class.java) }

    val tagUserPics: MutableLiveData<ReactiveData<TagUserPicListBean>> by lazy { MutableLiveData<ReactiveData<TagUserPicListBean>>() }

    val tagPraise: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }

    fun requestTagUserList(queryType: QueryType, tagId: Int, friendId: Long) {

        viewModelScope.launch {


            request({
                val form = TagUserForm(tagId, friendId = friendId)
                val result =
                    service.tagAuthPic(form).dataConvert()
                tagUserPics.value = result.convertRtData()
            }, error = {
                tagUserPics.value = it.convertError()
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

    fun tagPraise(tagId: Int, friendId: Long) {
        viewModelScope.launch {

            request({
                val result = service.tagPraise(TagUserForm(tagId, friendId)).dataConvert()
                tagPraise.value = true.convertRtData()
            }, error = {
                tagPraise.value = it.convertError()
            })
        }

    }

    fun tagCancelPraise(tagId: Int, friendId: Long) {
        viewModelScope.launch {

            request({
                val result = service.tagPraise(TagUserForm(tagId, friendId)).dataConvert()
                tagPraise.value = false.convertRtData()
            }, error = {
                tagPraise.value = it.convertError()
            })
        }

    }

}