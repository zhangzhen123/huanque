package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.basic.RootListData

data class DynamicItemBean(
    var age: Int = 0,
    var anonymous: Boolean = false,// 是否匿名
    var auditStatus: String = "",// 审核状态 Wait 等待审核，Reject 被拒绝，Pass 审核通过
    var city: String = "",
    var commentNum: Int = 0,
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
    var praiseNum: Int = 0,
    var sex: String = "",
    var shareNum: Int = 0,
    var userId: Long = 0L,
    var userType: String = "",
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
    var authMark: Boolean = false,
    var commentId: Int = 0,
    var content: String = "",
    var createTime: String = "",
    var headPic: String = "",
    var headRealPeople: Boolean = false,
    var nickname: String = "",
    var parentCommentId: Int = 0,
    var praiseNum: Int = 0,
    var replyNickname: String = "",
    var userId: Long = 0,
    var hasPraise: Boolean = false,
    var originalPoster: Boolean = false,
    var shareNum: Int = 0,
    var commentNum: Int = 0,
    var hasMore: Boolean = false,
    var secondComments: MutableList<DynamicComment> = mutableListOf()
)
