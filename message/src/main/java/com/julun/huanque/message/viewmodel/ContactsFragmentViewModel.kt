package com.julun.huanque.message.viewmodel

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.bean.beans.UserInfoChangeResult
import com.julun.huanque.common.bean.forms.ContactsForm
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/14 17:33
 *@描述 联系人Fragment使用
 */
class ContactsFragmentViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }

    //关注状态
//    val followStatusData: MutableLiveData<UserInfoChangeResult> by lazy { MutableLiveData<UserInfoChangeResult>() }

    var mType = ""

    var offset = 0


    val stateList: LiveData<ReactiveData<RootListData<SocialUserInfo>>> = queryState.switchMap { type ->
        liveData {
            if (type != QueryType.LOAD_MORE) {
                offset = 0
            }

            request({
                val homeListData = service.socialList(ContactsForm(mType, offset)).dataConvert()
                val list = homeListData.linkList
                val rList = RootListData(isPull = type != QueryType.LOAD_MORE, list = list, hasMore = homeListData.hasMore)
                offset += list.size
                emit(ReactiveData(NetStateType.SUCCESS, rList))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)

        }

    }

    /**
     * 关注
     */
    fun follow(type: String, userId: Long, formerFollow: String) {
        viewModelScope.launch {
            request({
                val follow = service.follow(FriendIdForm(userId)).dataConvert()
                val followBean = UserInfoChangeResult(userId = userId, follow = follow.follow, formerFollow = formerFollow)
//                followStatusData.value = followBean
                HuanViewModelManager.huanQueViewModel.userInfoStatusChange.value = followBean.convertRtData()
//                EventBus.getDefault().post(UserInfoChangeEvent(userId, follow.stranger))
            }, {
            })
        }
    }

    /**
     * 取消关注
     */
    fun unFollow(type: String, userId: Long, formerFollow: String) {
        viewModelScope.launch {
            request({
                val follow = service.unFollow(FriendIdForm(userId)).dataConvert()
                val followBean = UserInfoChangeResult(userId = userId, follow = FollowStatus.False, formerFollow = formerFollow)
//                followStatusData.value = followBean
                HuanViewModelManager.huanQueViewModel.userInfoStatusChange.value = followBean.convertRtData()
//                EventBus.getDefault().post(UserInfoChangeEvent(userId, follow.stranger))
            }, {
            })
        }
    }
}