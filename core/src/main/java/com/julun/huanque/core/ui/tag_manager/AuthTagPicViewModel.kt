package com.julun.huanque.core.ui.tag_manager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.AuthPic
import com.julun.huanque.common.bean.beans.AuthTagPicInfo
import com.julun.huanque.common.bean.beans.TagDetailBean
import com.julun.huanque.common.bean.beans.TagUserPicListBean
import com.julun.huanque.common.bean.forms.LogForm
import com.julun.huanque.common.bean.forms.TagApplyForm
import com.julun.huanque.common.bean.forms.TagForm
import com.julun.huanque.common.bean.forms.TagUserForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.TagService
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
class AuthTagPicViewModel : BaseViewModel() {

    private val service: TagService by lazy { Requests.create(TagService::class.java) }

    val tagDetail: MutableLiveData<ReactiveData<AuthTagPicInfo>> by lazy { MutableLiveData<ReactiveData<AuthTagPicInfo>>() }

    val applyResult: MutableLiveData<ReactiveData<AuthPic>> by lazy { MutableLiveData<ReactiveData<AuthPic>>() }

    val deleteResult: MutableLiveData<ReactiveData<Int>> by lazy { MutableLiveData<ReactiveData<Int>>() }
//    val tagPraise: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }

    fun authDetail(queryType: QueryType, tagId: Int/*, friendId: Long*/) {

        viewModelScope.launch {


            request({
                val form = TagForm(tagId/*, friendId = friendId*/)
                val result =
                    service.authDetail(form).dataConvert()
                tagDetail.value = result.convertRtData()
            }, error = {
                tagDetail.value = it.convertError()
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

    fun applyTagPic(
        tagId: Int,
        applyPic: String,
        logId: Int? = null
    ) {

        viewModelScope.launch {

            request({
                val result = service.tagApply(TagApplyForm(tagId, applyPic, logId)).dataConvert()
                if (logId != null) {
                    result.deleteLogId = logId
                }
                applyResult.value = result.convertRtData()
            }, error = {
                applyResult.value = it.convertError()
            })
        }
    }

    fun deleteTagPic(logId: Int) {

        viewModelScope.launch {

            request({
                val result = service.deleteAuthPic(LogForm(logId)).dataConvert()
                deleteResult.value = logId.convertRtData()
            }, error = {
                deleteResult.value = it.convertError()
            })
        }
    }
//
//    fun tagPraise(tagId: Int, friendId: Long) {
//        viewModelScope.launch {
//
//            request({
//                val result = service.tagPraise(TagUserForm(tagId, friendId)).dataConvert()
//                tagPraise.value = true.convertRtData()
//            }, error = {
//                tagPraise.value = it.convertError()
//            })
//        }
//
//    }
//
//    fun tagCancelPraise(tagId: Int, friendId: Long) {
//        viewModelScope.launch {
//
//            request({
//                val result = service.tagCancelPraise(TagUserForm(tagId, friendId)).dataConvert()
//                tagPraise.value = false.convertRtData()
//            }, error = {
//                tagPraise.value = it.convertError()
//            })
//        }
//
//    }

}