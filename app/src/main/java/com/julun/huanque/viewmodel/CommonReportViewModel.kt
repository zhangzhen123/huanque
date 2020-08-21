package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.bean.forms.ReportForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.net.services.UserService
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/3/16 10:17
 *
 *@Description: CommonReportViewModel
 *
 */

class CommonReportViewModel : BaseViewModel() {


    val reportStateResult: MutableLiveData<ReactiveData<VoidResult>> by lazy { MutableLiveData<ReactiveData<VoidResult>>() }

    val reportList: MutableLiveData<ArrayList<ManagerInfo>> by lazy { MutableLiveData<ArrayList<ManagerInfo>>() }

    //    private val liveService: LiveRoomService by lazy { Requests.create(LiveRoomService::class.java) }
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    /**
     * 举报用户
     */
    fun reportLiveUser(form: ReportForm) {
        form.detail = newLineFilter(form.detail ?: "")
        viewModelScope.launch {
            request({
                val result = userService.report(form).dataConvert()
                reportStateResult.value = result.convertRtData()
            },error = {
                reportStateResult.value=it.convertError()
            })
        }
    }

    /**
     * 请求用户举报列表
     */
    fun queryReportList() {
        viewModelScope.launch {
            request({
                val list=userService.reportTypeList().dataConvert()
                reportList.value=list
            })
        }
    }
    /**
     * 举报(广场相关)
     */
//    fun reportDynamic(form: DynamicReportForm) {
//        userService.saveReport(form)
//                .handleResponse(makeSubscriber<VoidResult> {
//                    reportStateResult.value = true
////                    ToastUtils.showSuccessMessage("举报成功")
//                }.ifError {
//                    reportStateResult.value = false
//                }.withFinalCall {
//                    finialState.value=true
//                })
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

    /**
     * 请求主播举报列表
     */
//    fun queryAnchorReportList() {
//        liveService.queryAnchorReportList().handleResponse(makeSubscriber<ArrayList<ManagerInfo>> {
//            reportList.value = it
//        }.ifError {
//            reportList.value = null
//        })
//    }
//    /**
//     * 请求广场用户举报列表
//     */
//    fun querySquareReportList() {
//        userService.queryReportList().handleResponse(makeSubscriber<ArrayList<ManagerInfo>> {
//            reportList.value = it
//        }.ifError {
//            reportList.value = null
//        })
//    }

}