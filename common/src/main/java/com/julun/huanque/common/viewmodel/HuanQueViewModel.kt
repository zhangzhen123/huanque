package com.julun.huanque.common.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.DynamicChangeResult
import com.julun.huanque.common.bean.beans.FateQuickMatchBean
import com.julun.huanque.common.bean.beans.UserInfoChangeResult
import com.julun.huanque.common.bean.events.SendRNEvent
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.PostForm
import com.julun.huanque.common.commonviewmodel.BaseApplicationViewModel
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.constant.RNMessageConst
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.manager.GlobalDialogManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
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

    private val dynamicService: DynamicService by lazy { Requests.create(DynamicService::class.java) }

    //派单的对象
    val fateQuickMatchData: MutableLiveData<FateQuickMatchBean> by lazy { MutableLiveData<FateQuickMatchBean>() }

    //派单的
    val fateQuickMatchTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    private var mPaidanDisposable: Disposable? = null


    val userInfoStatusChange: MutableLiveData<ReactiveData<UserInfoChangeResult>> by lazy { MutableLiveData<ReactiveData<UserInfoChangeResult>>() }

    val dynamicChangeResult: MutableLiveData<DynamicChangeResult> by lazy { MutableLiveData<DynamicChangeResult>() }

    //代表某个动态已经被删除
//    val deletedDynamic: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

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
                val followBean = UserInfoChangeResult(follow = follow.follow, userId = userId, stranger = follow.stranger)
                userInfoStatusChange.value = followBean.convertRtData()
                if(follow.toastMsg.isNotEmpty()){
                    ToastUtils.show(follow.toastMsg)
                }else{
                    ToastUtils.show("关注成功")
                }

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
                ToastUtils.show("取消关注成功")
                userInfoStatusChange.value = followBean.convertRtData()
                EventBus.getDefault()
                    .post(SendRNEvent(RNMessageConst.FollowUserChange, hashMapOf("userId" to userId, "isFollowed" to false)))
            }, {
                userInfoStatusChange.value = it.convertError()
            })
        }
    }
    //------------------------------------------动态点赞做成全局统一----------------------------------------------------------------
    /**
     * 点赞
     */
    fun praise(postId: Long) {
        viewModelScope.launch {
            request({
                val result = dynamicService.postPraise(PostForm(postId)).dataConvert()
                dynamicChangeResult.value = DynamicChangeResult(postId = postId, praise = true)
            }, {
                if (it is ResponseError) {
//                    ToastUtils.show(it.busiMessage)
                }
            })
        }
    }

    fun cancelPraise(postId: Long) {
        viewModelScope.launch {
            request({
                val result = dynamicService.cancelPraisePost(PostForm(postId)).dataConvert()
                dynamicChangeResult.value = DynamicChangeResult(postId = postId, praise = false)
            }, {
                if (it is ResponseError) {
//                    ToastUtils.show(it.busiMessage)
                }
            })
        }
    }

    /**
     * 删除动态
     */
    fun deletePost(mPostId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.deletePost(PostForm(mPostId)).dataConvert()
//                deletedDynamic.value = mPostId
                dynamicChangeResult.value = DynamicChangeResult(postId = mPostId, hasDelete = true)
                ToastUtils.show("内容删除成功")
//                dynamicChangeFlag = true
            }, {})
        }
    }
}