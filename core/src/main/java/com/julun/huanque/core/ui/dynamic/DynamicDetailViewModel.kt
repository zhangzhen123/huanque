package com.julun.huanque.core.ui.dynamic

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.PayForm
import com.julun.huanque.common.bean.forms.PostDetailForm
import com.julun.huanque.common.bean.forms.UpdateVoiceForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.delay
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
class DynamicDetailViewModel : BaseViewModel() {


    private val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }

    val dynamicDetailInfo: MutableLiveData<ReactiveData<DynamicDetailInfo>> by lazy { MutableLiveData<ReactiveData<DynamicDetailInfo>>() }

    fun queryDetail(postId: Long,type: QueryType) {
        viewModelScope.launch {
            request({
                val result = service.queryPostDetail(PostDetailForm(postId)).dataConvert()
                //todo
//                result.comments.clear()
                dynamicDetailInfo.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
                dynamicDetailInfo.value = it.convertError()
            },needLoadState = type==QueryType.INIT)

        }
    }

}