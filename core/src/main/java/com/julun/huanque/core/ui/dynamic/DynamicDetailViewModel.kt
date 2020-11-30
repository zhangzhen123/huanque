package com.julun.huanque.core.ui.dynamic

import androidx.lifecycle.*
import com.facebook.react.bridge.Dynamic
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.CommentOrderType
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch
import kotlin.math.max

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
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    val dynamicDetailInfo: MutableLiveData<ReactiveData<DynamicDetailInfo>> by lazy { MutableLiveData<ReactiveData<DynamicDetailInfo>>() }

    val dynamicInfo: MutableLiveData<DynamicDetailInfo> by lazy { MutableLiveData<DynamicDetailInfo>() }

    //评论成功返回的评论对象
    val commentSuccessData: MutableLiveData<DynamicComment> by lazy { MutableLiveData<DynamicComment>() }

    //评论列表
    val commentListResult: MutableLiveData<RootListData<DynamicComment>> by lazy { MutableLiveData<RootListData<DynamicComment>>() }

    //二级评论列表
    val secondCommentListResult: MutableLiveData<RootListData<DynamicComment>> by lazy { MutableLiveData<RootListData<DynamicComment>>() }

    //删除动态标识位
    val deleteFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //动态已删除的标识位
    val deletedData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //评论点赞结果
    val commentPraiseResult: MutableLiveData<DynamicComment> by lazy { MutableLiveData<DynamicComment>() }

    //评论的排序方式（默认为热度）
    var commentType = CommentOrderType.Heat

    //正在恢复的评论
    var replyingComment: DynamicComment? = null

    //动态Id
    var mPostId = 0L

    var mOffset = 0

    fun queryDetail(postId: Long, type: QueryType) {
        viewModelScope.launch {
            request({
                val result = service.queryPostDetail(PostDetailForm(postId)).dataConvert()
                //todo
//                result.comments.clear()
                dynamicInfo.value = result
                mOffset += result.comments.size
                dynamicDetailInfo.value = result.convertRtData()
            }, error = {
                it.printStackTrace()
                dynamicDetailInfo.value = it.convertError()
            }, needLoadState = type == QueryType.INIT)

        }
    }

    /**
     * 评论
     */
    fun comment(content: String) {
        viewModelScope.launch {
            request({
                val result = socialService.postComment(PostCommentForm(mPostId, replyingComment?.commentId, content)).dataConvert()
                commentSuccessData.value = result.apply { firstCommentId = replyingComment?.firstCommentId ?: 0 }
                //回复成功，清空正在回复的评论对象
                replyingComment = null
                ToastUtils.show("评论发表成功")
            }, {})
        }
    }

    /**
     * 评论 1级
     */
    fun commentList() {
        viewModelScope.launch {
            request({
                val result = socialService.commentList(CommentListForm(mPostId, mOffset, commentType)).dataConvert()
                result.isPull = mOffset == 0
                mOffset += result.list.size
                commentListResult.value = result
            }, {})
        }
    }

    /**
     * 更多评论（2级）
     */
    fun secondCommentList(parentCommentId: Long, offset: Int) {
        viewModelScope.launch {
            request({
                secondCommentListResult.value = socialService.secondCommentList(SecondCommentList(mPostId, parentCommentId, offset)).dataConvert()
            }, {})
        }
    }

    /**
     * 删除评论
     */
    fun deletePost() {
        viewModelScope.launch {
            request({
                val result = socialService.deletePost(PostForm(mPostId)).dataConvert()
                deletedData.value = true
            }, {})
        }
    }

    /**
     * 点赞评论
     */
    fun praiseComment(comment: DynamicComment) {
        viewModelScope.launch {
            service.commentPraise(CommentIdForm(comment.commentId)).dataConvert()
            commentPraiseResult.value = comment.apply {
                hasPraise = true
                praiseNum += 1
            }
        }
    }

    /**
     * 取消评论点赞
     */
    fun cancelPraiseComment(comment: DynamicComment) {
        viewModelScope.launch {
            service.cancelCommentPraise(CommentIdForm(comment.commentId)).dataConvert()
            commentPraiseResult.value = comment.apply {
                hasPraise = false
                praiseNum = max(0, praiseNum - 1)
            }
        }
    }

}