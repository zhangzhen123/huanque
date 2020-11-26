package com.julun.huanque.common.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.FateQuickMatchBean
import com.julun.huanque.common.bean.beans.UserInfoChangeResult
import com.julun.huanque.common.bean.events.SendRNEvent
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.commonviewmodel.BaseApplicationViewModel
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.constant.RNMessageConst
import com.julun.huanque.common.manager.GlobalDialogManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/9/21 15:27
 *@描述 和app生命周期绑定的ViewModel
 */
class HuanQueViewModel(application: Application) : BaseApplicationViewModel(application) {

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //派单的对象
    val fateQuickMatchData: MutableLiveData<FateQuickMatchBean> by lazy { MutableLiveData<FateQuickMatchBean>() }

    //派单的
    val fateQuickMatchTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    private var mPaidanDisposable: Disposable? = null


    val userInfoStatusChange: MutableLiveData<ReactiveData<UserInfoChangeResult>> by lazy { MutableLiveData<ReactiveData<UserInfoChangeResult>>() }

    /**
     * 设置派单数据
     */
    fun setFateData(bean: FateQuickMatchBean) {
        fateQuickMatchData.value = bean
        mPaidanDisposable?.dispose()
        val count = (bean.expTime - System.currentTimeMillis()) / 1000
        mPaidanDisposable = Observable.intervalRange(0, count + 1, 0, 1, TimeUnit.SECONDS)
            .map { count - it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fateQuickMatchTime.value = it
            }, { it.printStackTrace() }, {
                fateQuickMatchData.postValue(null)
                fateQuickMatchTime.postValue(null)
                GlobalDialogManager.closeDialog()
            })
    }

    /**
     * 清空派单数据
     */
    fun clearFateData() {
        mPaidanDisposable?.dispose()
        fateQuickMatchData.value = null
        fateQuickMatchTime.value = null
        GlobalDialogManager.closeDialog()
    }



    //关注操作做成全局模式的 方便很多地方的数据同步 不再使用eventBus通知

    /**
     * 关注
     */
    fun follow(userId: Long) {
        viewModelScope.launch {
            request({
                val follow = socialService.follow(FriendIdForm(userId)).dataConvert()
                val followBean = UserInfoChangeResult(follow = follow.follow, userId = userId,stranger = follow.stranger)
                userInfoStatusChange.value = followBean.convertRtData()
//                EventBus.getDefault().post(UserInfoChangeEvent(userId, follow.stranger, follow.follow))
                EventBus.getDefault()
                    .post(SendRNEvent(RNMessageConst.FollowUserChange, hashMapOf("userId" to userId, "isFollowed" to true)))
            }, {
                userInfoStatusChange.value = it.convertError()
            })
        }
    }

    /**
     * 取消关注
     */
    fun unFollow(userId: Long) {
        viewModelScope.launch {
            request({
                val follow = socialService.unFollow(FriendIdForm(userId)).dataConvert()
                val followBean = UserInfoChangeResult(follow = FollowStatus.False, userId = userId)
                userInfoStatusChange.value = followBean.convertRtData()
//                EventBus.getDefault().post(UserInfoChangeEvent(userId, follow.stranger, follow.follow))
                EventBus.getDefault()
                    .post(SendRNEvent(RNMessageConst.FollowUserChange, hashMapOf("userId" to userId, "isFollowed" to false)))
            }, {
                userInfoStatusChange.value = it.convertError()
            })
        }
    }
}