package com.julun.huanque.core.ui.publish_dynamic

import androidx.lifecycle.MutableLiveData
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.DynamicDetailInfo
import com.julun.huanque.common.bean.beans.PublishDynamicResult
import com.julun.huanque.common.bean.forms.PostDetailForm
import com.julun.huanque.common.bean.forms.PublishStateForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/25 11:33
 *
 *@Description: PublishViewModel
 *
 */

class PublishViewModel : BaseViewModel() {

    val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }

    val publisStateResult: MutableLiveData<ReactiveData<PublishDynamicResult>> by lazy { MutableLiveData<ReactiveData<PublishDynamicResult>>() }

    val publisVideoResult: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //发布状态
    fun publishState(form: PublishStateForm) {
        form.content = newLineFilter(form.content)
        viewModelScope.launch {
            request({
                val result = service.publishState(form).dataConvert()
                publisStateResult.value = result.convertRtData()
            }, error = {
                publisStateResult.value = it.convertError()
            })

        }

    }

    //发布视频
//    fun publishVideo(form: PublishVideoForm) {
//        service.publishVideo(form)
//                .handleResponse(makeSubscriber<VoidResult> {
//                    publisVideoResult.value = true
//                }.ifError {
//                    publisVideoResult.value = false
//                })
//
//    }

    /**
     * 过滤换行符,只保留一个
     */
    private fun newLineFilter(content: String): String {
        if (TextUtils.isEmpty(content)) {
            return content
        }
        val buffer: StringBuffer
        if (content.contains("\n")) {
            //字符串是否存在换行符
            val str = content.split("\n")
            if (!str.isEmpty()) {
                buffer = StringBuffer()
                str.forEach {
                    if (!TextUtils.isEmpty(it)) {
                        buffer.append("${it}\n")
                    }
                }
                return buffer.toString()
            }
        }
        return content
    }
}