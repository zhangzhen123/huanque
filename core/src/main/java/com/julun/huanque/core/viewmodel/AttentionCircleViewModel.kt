package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.MyGroupInfo
import com.julun.huanque.common.bean.forms.GroupIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/11/24 13:48
 *@描述  我的圈子 ViewModel
 */
class AttentionCircleViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //我的圈子数据
    val myGroupData: MutableLiveData<MyGroupInfo> by lazy { MutableLiveData<MyGroupInfo>() }

    /**
     * 获取我的圈子 基础数据
     */
    fun getCircleGroupInfo() {
        viewModelScope.launch {
            request({
                val groupData = socialService.myGroup().dataConvert()
                groupData.myGroup.forEach {
                    it.joined = BusiConstant.True
                }
                myGroupData.value = groupData
            }, {}, needLoadState = true)
        }
    }

    /**
     * 加入圈子
     */
    fun groupJoin(groupId: Long) {
        viewModelScope.launch {
            request({
                socialService.groupJoin(GroupIdForm(groupId)).dataConvert()
                ToastUtils.show("圈子加入成功")
                //加入成功，重新获取数据
                getCircleGroupInfo()
            }, {})
        }

    }
}