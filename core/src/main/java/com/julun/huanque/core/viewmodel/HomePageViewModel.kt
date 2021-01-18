package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.LikeEvent
import com.julun.huanque.common.bean.forms.EvaluateForm
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.UserIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/9/29 16:04
 *@描述 主页使用的ViewModel
 */
class HomePageViewModel : BaseViewModel() {
    companion object {
        //举报
        val ACTION_REPORT = "ACTION_REPORT"

        //拉黑
        val ACTION_BLACK = "ACTION_BLACK"
    }

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    private val service: HomeService by lazy { Requests.create(HomeService::class.java) }

    //主页数据
    val homeInfoBean: MutableLiveData<HomePageInfo> by lazy { MutableLiveData<HomePageInfo>() }

    //语音点赞成功标记位
    val praiseSuccessState: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //亲密榜数据
    val closeListData: MutableLiveData<MutableList<CloseConfidantBean>> by lazy { MutableLiveData<MutableList<CloseConfidantBean>>() }

    //拉黑状态
    val blackStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //关注状态
    val followStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //操作数据
    val actionData: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //评论tag数据
    val evaluateTagsBean: MutableLiveData<EvaluateTags> by lazy { MutableLiveData<EvaluateTags>() }

    //需要添加的评价
    val evaluateContent: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //评论数据
    val appraiseListData: MutableLiveData<MutableList<AppraiseBean>> by lazy { MutableLiveData<MutableList<AppraiseBean>>() }

    //评价成功标识
    val evaluateFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //心动状态
    val heartStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //真人认证标识为
    val realPeopleState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否有共享元素跳转
    var shareElement = false
//
//    //附近数据
//    val nearByBeanData: MutableLiveData<NearbyUserBean> by lazy { MutableLiveData<NearbyUserBean>() }

    //评论列表
    var targetUserId = 0L

    //我的主页标识位
    var mineHomePage = false

    /**
     * 获取基础信息
     */
    fun homeInfo() {
        viewModelScope.launch {
            request({
                val result = userService.homeInfo(UserIdForm(targetUserId)).dataConvert()
                homeInfoBean.value = result
                blackStatus.value = result.black
                followStatus.value = result.follow
//                heartStatus.value = result.heartTouch
            }, {}, needLoadState = true)
        }
    }

    /**
     * 拉黑
     */
    fun black() {
        viewModelScope.launch {
            request({
                socialService.black(FriendIdForm(targetUserId)).dataConvert()
                blackStatus.value = BusiConstant.True
                homeInfoBean.value?.black = BusiConstant.True
                ToastUtils.show("拉黑成功")
            }, { it.printStackTrace() })
        }
    }

    /**
     * 取消拉黑
     */
    fun recover() {
        viewModelScope.launch {
            request({
                socialService.recover(FriendIdForm(targetUserId)).dataConvert()
                blackStatus.value = BusiConstant.False
                homeInfoBean.value?.black = BusiConstant.False
                ToastUtils.show("取消拉黑成功")
            }, { it.printStackTrace() })
        }
    }

    /**
     * 关注
     */
//    fun follow() {
//        viewModelScope.launch {
//            request({
//                val follow = socialService.follow(FriendIdForm(targetUserId)).dataConvert()
////                val followBean = FollowResultBean(follow = follow.follow, userId = userId)
//                followStatus.value = follow.follow
//                homeInfoBean.value?.follow = follow.follow
//                ToastUtils.show(follow.toastMsg)
//                EventBus.getDefault()
//                    .post(UserInfoChangeEvent(targetUserId, follow.stranger, follow.follow))
//                EventBus.getDefault()
//                    .post(
//                        SendRNEvent(
//                            RNMessageConst.FollowUserChange,
//                            hashMapOf("userId" to targetUserId, "isFollowed" to true)
//                        )
//                    )
//            }, {
//            })
//        }
//    }

    /**
     * 取消关注
     */
//    fun unFollow() {
//        viewModelScope.launch {
//            request({
//                val follow = socialService.unFollow(FriendIdForm(targetUserId)).dataConvert()
////                val followBean = FollowResultBean(follow = FollowStatus.False, userId = userId)
//                followStatus.value = FollowStatus.False
//                homeInfoBean.value?.follow = FollowStatus.False
//                ToastUtils.show("取消关注成功")
//                EventBus.getDefault()
//                    .post(UserInfoChangeEvent(targetUserId, follow.stranger, follow.follow))
//                EventBus.getDefault()
//                    .post(
//                        SendRNEvent(
//                            RNMessageConst.FollowUserChange,
//                            hashMapOf("userId" to targetUserId, "isFollowed" to false)
//                        )
//                    )
//            }, {
////                followStatusData.value = it.convertError()
//            })
//        }
//    }

    /**
     * 获取评价列表
     */
    fun getEvaluateList() {
        viewModelScope.launch {
            request({
                val result = socialService.evaluateTags(FriendIdForm(targetUserId)).dataConvert()
                evaluateTagsBean.value = result
            })

        }
    }


    /**
     * 密友评价
     */
    fun evaluteateFriend(list: MutableList<String>) {

//        viewModelScope.launch {
//            request({
//                val contentStringBuilder = StringBuilder()
//                list.forEach {
//                    if (contentStringBuilder.isNotEmpty()) {
//                        contentStringBuilder.append(",")
//                    }
//                    contentStringBuilder.append(it)
//                }
//                val result = socialService.relationEvaluate(EvaluateForm(targetUserId, contentStringBuilder.toString())).dataConvert()
//                evaluateFlag.value = true
//                val list = result.list
//                appraiseListData.value = list
//                if (list != null) {
//                    homeInfoBean.value?.appraiseList = list
//                }
//                ToastUtils.show("添加评论成功")
//            }, {})
//        }

    }


    /**
     * 语音点赞接口
     */
    fun voicePraise() {
        viewModelScope.launch {
            request({
                socialService.voicePraise(FriendIdForm(targetUserId)).dataConvert()
                homeInfoBean.value?.voice?.apply {
                    like = BusiConstant.True
                    likeCount += 1
                }
                praiseSuccessState.value = BusiConstant.True
            }, {})
        }
    }

    /**
     * 亲密榜列表
     */
    fun closeConfidantRank() {
        viewModelScope.launch {
            request({
                closeListData.value = socialService.closeConfidantRank(UserIdForm(targetUserId)).dataConvert()
            }, {})
        }
    }

    fun like(userId: Long) {
        viewModelScope.launch {
            request({
                val result = service.like(FriendIdForm(userId)).dataConvert()
                heartStatus.value = BusiConstant.True
                homeInfoBean.value?.heartTouch = BusiConstant.True
                EventBus.getDefault().post(LikeEvent(userId, true))
            })
        }

    }

}