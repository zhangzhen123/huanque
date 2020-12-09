package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.MyGroupInfo
import com.julun.huanque.common.bean.forms.CircleGroupTypeForm
import com.julun.huanque.common.bean.forms.GroupIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.CircleGroupTabType
import com.julun.huanque.common.constant.CircleGroupType
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

    //加入的圈子ID
    val joinedGroupIdData: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //退出的圈子ID
    val quitGroupIdData: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    var mOffset = 0

    //请求的Type
    var requestType = ""

    /**
     * 获取我的圈子 基础数据
     */
    fun getCircleGroupInfo(queryType: QueryType,joinNum : Int = 0) {
        viewModelScope.launch {
            request({
                if (queryType != QueryType.LOAD_MORE) {
                    mOffset = 0
                }

                val groupData = socialService.groupList(CircleGroupTypeForm(mOffset, requestType, joinNum)).dataConvert()
//                if (mOffset == 0) {
//                    //刷新操作
                groupData.recommendGroup.isPull = queryType != QueryType.LOAD_MORE
                groupData.group.isPull = queryType != QueryType.LOAD_MORE
//                }
                val recommendGroup = groupData.recommendGroup.list
                if (recommendGroup.isNotEmpty()) {
                    //推荐有数据，下次请求，使用推荐的offset
                    mOffset += recommendGroup.size
                }
                recommendGroup.forEach {
                    it.type = CircleGroupTabType.Recom
                }
                val myGroup = groupData.group.list
                if (recommendGroup.isEmpty()) {
                    //推荐为空，使用关注的offset
                    mOffset += myGroup.size
                }
                myGroup.forEach {
                    it.type = CircleGroupTabType.Follow
                }

                myGroupData.value = groupData
            }, {}, needLoadState = true)
        }
    }

    /**
     * 获取选择圈子数据
     */
    fun getChooseCircle(queryType: QueryType) {
        viewModelScope.launch {
            request({
                if (queryType != QueryType.LOAD_MORE) {
                    mOffset = 0
                }
                val groupData = socialService.groupChose(CircleGroupTypeForm(mOffset, requestType)).dataConvert()
//                if (mOffset == 0) {
//                    //刷新操作
                groupData.group.isPull = queryType != QueryType.LOAD_MORE
                val myGroup = groupData.group.list
                myGroup.forEach {
                    it.type = requestType
                }
                if (myGroup.isNotEmpty()) {
                    //推荐有数据，下次请求，使用推荐的offset
                    mOffset += myGroup.size
                }

                myGroupData.value = groupData
            }, needLoadState = true)
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
                joinedGroupIdData.value = groupId
            }, {})
        }
    }

    /**
     * 退出圈子
     */
    fun groupQuit(groupId: Long) {
        viewModelScope.launch {
            request({
                socialService.groupQuit(GroupIdForm(groupId)).dataConvert()
                quitGroupIdData.value = groupId
            }, {})
        }
    }

}