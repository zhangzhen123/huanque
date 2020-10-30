package com.julun.huanque.core.ui.main.follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.FollowProgramInfo
import com.julun.huanque.common.bean.forms.LiveFollowForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.*
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
class FollowViewModel : BaseViewModel() {

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }
    val followInfo: MutableLiveData<ReactiveData<FollowProgramInfo>> by lazy { MutableLiveData<ReactiveData<FollowProgramInfo>>() }


    private var offset: Int = 0
    fun requestProgramList(queryType: QueryType, isNullOffset: Boolean = false) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offset = 0
            }

            request({
                val form = if (isNullOffset) {
                    LiveFollowForm(offset = null)
                } else {
                    LiveFollowForm(offset = offset)
                }
                val result =
                    programService.followPrograms(form).dataConvert()
                offset += result.followList.size
                result.isPull = queryType != QueryType.LOAD_MORE
                followInfo.value = result.convertRtData()
            }, error = {
                followInfo.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}