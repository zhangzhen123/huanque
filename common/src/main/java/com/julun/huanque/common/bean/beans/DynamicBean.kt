package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.constant.BusiConstant
import com.luck.picture.lib.entity.LocalMedia
import java.io.Serializable

data class DynamicItemBean(
    var age: Int = 0,
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
    var userAnonymous: Boolean = false,
    var deleteAuth: Boolean = false,
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
    var heatValue: Long = 0,
    var join: Boolean = false,
    var postNum: Long = 0L
)

class HomeDynamicListInfo {
    var postList: MutableList<DynamicItemBean> = mutableListOf()
    var hasMore: Boolean = false
    var groupList: MutableList<DynamicGroup> = mutableListOf()
    var isPull: Boolean = false
    var recom: Boolean = false
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
    //是否具有删除权限
    var deleteAuth: String = "",
    //评论人是否是楼主
    var originalPoster: String = "",
    //被回复人是否是楼主
    var originalReply: String = "",
    var shareNum: Int = 0,
    var commentNum: Int = 0,
    var hasMore: Boolean = false,
    var secondComments: MutableList<DynamicComment> = mutableListOf(),
    //1级评论的ID  有该字段，表示为2级评论  没有该字段  表示为1级评论（本地字段）
    var firstCommentId: Long = 0
)

/**
 * 消息列表使用的评论消息(点赞和评论消息对象)
 */
data class SingleActionInMessage(
    //消息发送者Id（系统）
    var senderId: String = "",
    //时间
    var createTime: Long = 0,
    //性别
    var sex: String = "",
    //昵称
    var nickname: String = "",
    //动态Id
    var postId: Long = 0,
    //动态第一张图片
    var pic: String = "",
    //点赞用户头像
    var headPic: String = "",
    //真人图片
    var authMark: String = "",
    //动态文本内容
    var content: String = "",
    //点赞用户ID
    var targetUserId: Long = 0,
    //消息接收者用户ID
    var userId: Long = 0,
    //用户是否匿名
    var userAnonymous: String = "",
    //是否是楼主
    var originalPoster: String = "",
    //评论内容，有此参数证明是评论点赞
    var comment: String = "",
    //年龄
    var age: Int = 0
) : Serializable


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

class WatchListInfo<T> : RootListData<T>() {
    var extData: WatchListExt? = null
}


/**
 * 观看历史数据
 */
class WatchListExt(
    //能访问的最大数量
    var viewNum: Int = 0,
    //添加喜欢的标签数＞3即可知道谁访问了你哦
    var guideText: String = "",
    //跳转的类型 同消息touchType
    var touchType: String = ""
) : Serializable

/**
 * 点赞变化 分享变化  评论变化 如果是true +1  false则-1 null不处理
 */
class DynamicChangeResult(
    var postId: Long,
    //是否删除
    var hasDelete: Boolean = false,
    var praise: Boolean? = null,
//    var share: Int? = null,
    var comment: Long? = null
)


/**
 * 心动列表数据
 */
class HeartListInfo<T> : RootListData<T>() {
    //引导文案
    var guideText: String = ""

    //跳转类型
    var touchType: String = ""

    //跳转按钮的文案
    var touchText: String = ""

    //剩余解锁次数（-1 无限制）
    var remainUnlockTimes: Int = 0

    var viewNum : Int = 0

}


/**
 * 单个心动对象
 */
data class SingleHeartBean(
    var age: Int = 0,
    var area: String = "",
    var coverPic: String = "",
    var distance: Int = 0,
    //互动文案
    var interactTips: String = "",
    //心动记录ID
    var logId: Long = 0,
    //是否匹配（相互心动），用于判断是否显示互相心动图标
    var matched: String = "",
    var nickname: String = "",
    var sameCity: String = "",
    var sex: String = "",
    //是否解锁
    var unLock: String = "",
    var userId: Long = 0
) : Serializable

/**
 * 分享接口结果
 */
class StatusResult(
    var status: String = ""
) : Serializable

/**
 * 分身消息数据
 */
class RelateAccountMsg(var msgList: MutableList<SingleAccountMsg> = mutableListOf()) : Serializable

class SingleAccountMsg(
    //显示的内容
    var content: String = "",
    //头像
    var headPic: String = "",
    //消息数量
    var msgCnt: Int = 0,
    //消息时间戳， 如果最近没有新消息，则时间戳为空
    var msgTime: Long = 0,
    //昵称
    var nickname: String = "",
    //用户Id
    var userId: Long = 0
) : Serializable

