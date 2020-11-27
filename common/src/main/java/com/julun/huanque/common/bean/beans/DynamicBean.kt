package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.basic.RootListData
import com.luck.picture.lib.entity.LocalMedia
import java.io.Serializable

data class DynamicItemBean(
    var age: Int = 0,
    var anonymous: Boolean = false,// 是否匿名
    var auditStatus: String = "",// 审核状态 Wait 等待审核，Reject 被拒绝，Pass 审核通过
    var city: String = "",
    var commentNum: Long = 0,
    var content: String = "",
    var hasPraise: Boolean = false, // 当前访问者是否点赞
    var group: DynamicGroup? = null,
    var headPic: String = "",
    var headRealPeople: Boolean = false, // 是否真人头像
    var authMark: String = "",
    var heatValue: Long = 0L,
    var nickname: String = "",
    var postId: Long = 0L,
    var postTime: String = "",
    var praiseNum: Long = 0,
    var sex: String = "",
    var shareNum: Long = 0,
    var userId: Long = 0L,
    var userType: String = "",
    var follow: Boolean = false,
    var pics: MutableList<String> = mutableListOf(),
    var watermark: String = "",
    //本地字段
    var hasEllipsis: Boolean? = null

) {
    override fun equals(other: Any?): Boolean {
        if (other is DynamicItemBean) {
            return this.postId == other.postId
        }
        return false
    }

    override fun hashCode(): Int {
        return postId.hashCode()
    }

}

data class DynamicGroup(
    var groupDesc: String = "",
    var groupId: Long = 0,
    var groupName: String = "",
    var groupPic: String = "",
    var hasNewPost: Boolean = false,
    var heatValue: Long = 0
)

class HomeDynamicListInfo {
    var postList: MutableList<DynamicItemBean> = mutableListOf()
    var hasMore: Boolean = false
    var groupList: MutableList<DynamicGroup> = mutableListOf()
    var isPull: Boolean = false
}

class DynamicDetailInfo() {
    var comments: MutableList<DynamicComment> = mutableListOf()
    var post: DynamicItemBean? = null
    var hasMore: Boolean = false
}

data class DynamicComment(
    //真人标识
    var authMark: String = "",
    var commentId: Long = 0,
    var content: String = "",
    var createTime: String = "",
    var headPic: String = "",
    var headRealPeople: Boolean = false,
    var nickname: String = "",
    var parentCommentId: Long = 0,
    var praiseNum: Int = 0,
    var replyNickname: String = "",
    var userId: Long = 0,
    var hasPraise: Boolean = false,
    var originalPoster: Boolean = false,
    var shareNum: Int = 0,
    var commentNum: Int = 0,
    var hasMore: Boolean = false,
    var secondComments: MutableList<DynamicComment> = mutableListOf(),
    //1级评论的ID  有该字段，表示为2级评论  没有该字段  表示为1级评论（本地字段）
    var firstCommentId: Long = 0
)

data class PublishDynamicResult(
    var failPicList: List<String> = listOf(),
    var failText: List<String> = listOf(),
    var message: String = "",
    var result: Boolean = false
)

/**
 * 保存发布动态的草稿
 */
class PublishDynamicCache {
    var groupName: String? = null
    var groupId: Long? = null
    var anonymous: String = ""
    var content: String = ""//
    var selectList: MutableList<LocalMedia> = mutableListOf()
}


class DynamicListInfo<T> : RootListData<T>() {
    var extData: DynamicListExt? = null
}


data class DynamicListExt(
    var follow: Boolean = false
) : Serializable

/**
 * 点赞变化 分享变化  评论变化 如果是true +1  false则-1 null不处理
 */
class DynamicChangeResult(
    var postId: Long = 0L,
    var praise: Boolean? = null,
    var share: Boolean? = null,
    var comment: Boolean? = null
)

