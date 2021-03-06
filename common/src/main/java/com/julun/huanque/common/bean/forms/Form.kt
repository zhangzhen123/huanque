package com.julun.huanque.common.bean.forms

import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/7/6 16:06
 *@描述 用于存储请求的form
 */

//空的form
class EmptyForm()

class FriendIdForm(var friendId: Long)

class TargetIdForm(var targetUserId: Long)

class SendChatGiftForm(
    var friendId: Long,
    var chatGiftId: Int,
    var count: Int,
    var fateId: String? = null
)

/**
 * 创建语音通话的form
 */
class CreateCommunicationForm(var friendId: Long, var confirmed: String? = null)

class NetcallIdForm(var callId: Long)

class NetcallCancelForm(var callId: Long, var cancelType: String)

/**
 * 接收匿名邀请的form
 */
class InviteUserIdForm(var inviteUserId: Long = 0)

/**
 * 挂断通话的form
 */
class NetcallHangUpForm(var callId: Long = 0, var duration: Long = 0) : Serializable

/**
 * 发送消息 form
 * @param friendId 发送的好友ID
 * @param content 消息内容
 */
class SendMsgForm(var friendId: Long = 0, var content: String = "", var fateId: String? = null)

class ReportForm {
    // 举报用户
    var userId: Long = 0
    var reportType: String = ""
    var detail: String = ""
    var pics: String? = null
}


/**
 * 联系人使用的form
 */
class ContactsForm(var userDataTabType: String = "", var offset: Int = 0)

/**
 * 更新用户消息使用的form
 */
class SettingForm(
    //非好友私信费用
    var privateMsgFee: Int? = null,
    //折叠非好友 密友消息
    var foldMsg: String? = null,
    //接听语音
    var answer: String? = null,
    //私信提醒
    var privateMsgRemind: String? = null,
    //关注提醒
    var followRemind: String? = null
)

/**
 * 节目关注列表使用的form
 */
class LiveRemindForm(
    var offset: Int? = null,
    var pushOpen: String? = null,
    var programId: Long? = null
)

class FindNewsForm {
    var startAdsVersion: String? = null
}

class BindForm(var code: String)
class BindPhoneForm(var mobile: String, var code: String)
class WithdrawApplyForm(var tplId: Int, var type: String)

class WithdrawHistoryForm(var lastId: Long? = null)

/**
 * 校验nickname使用
 */
class NicknameForm(var nickname: String, var checkNameType: String? = null)

class UpdateVoiceForm(var voiceUrl: String, var length: Long)

/**
 * 保存定位信息
 */
class SaveLocationForm(
    var lat: String,
    var lng: String,
    var city: String,
    var province: String? = null,
    var district: String? = null
)


/**
 * 用户名片
 */
class UserProgramForm(
    var programId: Long = -1,
    var targetUserId: Long = -1
) : Serializable

/**
 * 敏感词过滤form
 */
class ValidateForm(var content: String = "", var programId: Long = 0)

/**
 * 发送传送门的form
 */
class SendRoomForm(var friendId: Long, var programId: Long, var fateId: String? = null)

/**
 * 性别的Form
 */
class SexForm(var sexType: String = "")

/**
 * 需要使用offset的form
 */
class OffsetForm(var offset: Int = 0)

/**
 *
 */
class SaveTeachVideoForm(var videoId: Long = 0)


/**
 *
 */
class QuickAccostForm(var userIds: String = "")

/**
 * 新增搭讪常用语的form
 */
class AddWordsForm(var words: String) : Serializable

/**
 * 修改搭讪常用语的form
 */
class UpdateWordsForm(var words: String, var wordsId: Long)

/**
 * 删除搭讪常用语
 */
class WordsIdForm(var wordsId: Long)

/**
 * 状态的form
 */
class StatusForm(var status: String = "")

/**
 * 用户ID的form
 */
class UserIdForm(val userId: Long? = null) : Serializable

/**
 * 评价Form
 */
class EvaluateForm(var friendId: Long, var content: String) : Serializable

class GroupIdForm(var groupId: Long = 0) : Serializable

/**
 * 查询圈子的Form（关注还是推荐）
 */
class CircleGroupTypeForm(
    var offset: Int = 0, var postType: String = ""
    //已加入的圈子数量
    , var groupJoinNum: Int? = null
) : Serializable

/**
 * 动态分享的form
 */
class PostShareForm(
    //分享方式
    var shareType: String? = null,
    //动态Id
    var postId: Long = 0,
    //评论ID
    var commentId: Long? = null,
    //用户ID  zhjan
    var friendId: Long? = null
) : Serializable

/**
 * 评论动态的form
 */
class PostCommentForm(
    //动态ID
    var postId: Long = 0,
    //父评论
    var parentCommentId: Long? = null,
    //评论内容
    var content: String = ""
) : Serializable

/**
 * 获取更多评论的form
 */
data class CommentListForm(
    //动态ID
    var postId: Long = 0L,
    var offset: Int = 0,
    //倒叙字段
    var order: String = ""
) : Serializable

/**
 * 2级评论更多鹅form
 */
data class SecondCommentList(
    //动态ID
    var postId: Long = 0,
    //父评论的ID
    var parentCommentId: Long = 0,
    var offset: Int = 0
) : Serializable

/**
 * 评论ID form
 */
data class CommentIdForm(
    //评论Id
    var commentId: Long = 0
) : Serializable

/**
 * 创建分身账号的form
 */
data class CreateAccountForm(
    //昵称
    var nickname: String,
    //头像
    var headPic: String,
    //生日
    var birthday: String
) : Serializable

//统计
data class StatisticForm(
    var data: String
) : Serializable

data class StatisticItem(
    //事件类型：
    //Click（点击）只记录点击次数
    //Scan（浏览）需要记录浏览时间
    var eventType: String = "",
    //事件代码
    var eventCode: String = "",
    //点击次数
    var clickNum: Int = 0,
    var enterTime: Long? = null,
    var leaveTime: Long? = null
) : Serializable

/**
 * 标签form
 */
data class TagIdsForm(var tagIds: String = "") : Serializable

/**
 * 社交意愿form
 */
data class SocialWishIdForm(var wishCodes: String = "") : Serializable

/**
 * 更新用户信息的form
 */
data class UpdateUserInfoForm(
    //用户昵称
    var nickname: String? = null,
    //生日
    var birthday: String? = null,
    //签名
    var mySign: String? = null,
    //身高
    var height: Int? = null,
    //体重
    var weight: Int? = null,
    //省名称
    var provinceName: String? = null,
    //市名称
    var cityName: String? = null
) : Serializable

/**
 * 家乡版本form
 */
data class HomeTownVersionForm(
    //省市 版本号
    var version: Int? = null
) : Serializable

/**
 * 城市ID form
 */
data class CityIdForm(
    //城市ID
    var cityId: Int = 0
) : Serializable

/**
 * 人文数据更新form
 */
data class CultureUpdateForm(
    //城市ID
    var cityId: Int = 0,
    //人文 Id
    var cultureIds: String = ""
) : Serializable

/**
 * 星座form
 */
data class ConstellationForm(var constellationType: String? = null) : Serializable

/**
 * 学校模糊搜索对象
 */
data class SchoolForm(var school: String = "", var educationType: String? = null) : Serializable

/**
 * 保存学校form
 */
data class SaveSchoolForm(
    //学历
    var education: String? = null,
    //学校ID
    var schoolId: Int? = null,
    //入学年份
    var startYear: String? = null
) : Serializable

/**
 * 保存职业form
 */
data class SaveProfessionForm(
    //职业ID （如果单独保存或者修改 收入或者职业特性时，记得带上当前职业ID，否则会按照默认来处理）
    var professionId: Int? = null,
    var income: String? = null,
    //职业特性code 多个逗号隔开
    var professionFeatureCodes: String? = null
) : Serializable

/**
 *
 */
data class SaveCoverForm(
    //职业ID （如果单独保存或者修改 收入或者职业特性时，记得带上当前职业ID，否则会按照默认来处理）
    var logId: Long? = null,
    var coverPic: String? = null
)

/**
 *
 */
data class UserUpdateHeadForm(
    var headPic: String,
    var check: String
)

/**
 * 保存编辑页面  图片顺序form
 */
data class LogIdsForm(var logIds: String = "") : Serializable

/**
 * 邀请完善资料的form
 */
data class InviteCompleteForm(
    //好友ID
    var friendId: Long = 0,
    //完善类型
    var completeType: String = ""
) : Serializable {
    companion object {
        //个人资料
        const val Information = "Information"

        //认证标签
        const val AuthTag = "AuthTag"

        //喜欢标签
        const val LikeTag = "LikeTag"
    }

}

/**
 * 观看历史的form
 */
data class WatchForm(
    //访问类型
    var visitType: String = "",
    //分页
    var offset: Int = 0
) : Serializable {
    companion object {
        //谁看过我
        const val SeenMe = "SeenMe"

        //我看过谁
        const val HaveSeen = "HaveSeen"
    }

}

/**
 * 心动form
 */
data class HeartBeanForm(var heartTouchType: String = "", var offset: Int = 0) : Serializable {
    companion object {
        //对我心动
        const val HeartTouchToMe = "HeartTouchToMe"

        //我心动的
        const val MyHeartTouch = "MyHeartTouch"
    }
}

/**
 * 刷新引导信息的form
 */
data class GuideInfoForm(var type: String) : Serializable {
    companion object {
        //心动
        const val HeartTouch = "HeartTouch"

        //访客
        const val Visit = "Visit"
    }

}

/**
 * 解锁form
 */
data class UnlockForm(
    var logId: Long = 0,
    var userId: Long = 0
) : Serializable