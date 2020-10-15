package com.julun.huanque.common.bean

import androidx.room.Entity
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.model.Conversation
import java.io.Serializable

@Entity(primaryKeys = ["userId", "mineUserId"])
data class ChatUser(
    //头像
    var headPic: String = "",
    //个性签名
    var mySign: String = "",
    //昵称
    var nickname: String = "",
    //用户ID
    var userId: Long = 0,
    //用户性别
    var sex: String = "",
    //登录用户ID，用于切换账号时使用
    var mineUserId: Long = SessionUtils.getUserId(),

    //亲密等级
    var intimateLevel: Int = 0,
    //欢遇标识
    var meetStatus: String = "",
    //是否是陌生人
    var stranger: Boolean = false,
    //聊天背景
    var backPic: String = "",
    //用户类型
    var userType: String = ""
) : Serializable {


    override fun toString(): String {
        return "ChatUser(headPic='$headPic', nickname='$nickname', userId=$userId, intimateLevel=$intimateLevel, meetStatus='$meetStatus', sex='$sex', mineUserId=$mineUserId, stranger=$stranger, backPic='$backPic',userType='$userType')"
    }

}

/**
 *@创建者   dong
 *@创建时间 2019/5/22 15:55
 *@描述  chat使用的对象文件
 */
data class InteractMsgBean(
    var friendRequestCount: Int = 0,
    var praiseCount: Int = 0,
    var replyCount: Int = 0,
    var sysMsgCount: Int = 0
) : Serializable

/**
 * 消息中心查询各类消息的数量和最近的内容
 */
data class MessageInfoBean(
    var actMsgContent: String = "",
    var actMsgCount: Int = 0,
    var actMsgTime: Long = 0,
    var chatRoomCount: Int = 0,
    var interactContent: String = "",
    var interactCount: Int = 0,
    var interactTime: Long = 0,
    var sysMsgContent: String = "",
    var sysMsgCount: Int = 0,
    var sysMsgTime: Long = 0
) : Serializable

/**
 * 互动对象
 */
data class InteractBean(
    var hasMore: Boolean = false, var list: List<SingleInteractBean> = arrayListOf()
) : Serializable

/**
 * 好友请求数
 */
data class FriendRequestCountBean(
    var friendRequestCount: Int = 0
)

data class ReplayAndPraiseCountBean(
    var praiseCount: Int = 0, var replyCount: Int = 0
)

/**
 * 单个的互动消息
 */
data class SingleInteractBean(
    var createTime: Long = 0,
    var friendOptType: String = "",
    var headPic: String = "",
    var msg: String = "",
    var nickname: String = "",
    var noticeId: Long = 0,
    var userId: Long = 0,
    var objectId: Long = 0,
    var socialType: String = ""
) : Serializable

/**
 * 是否可以私聊，私聊字数
 */
data class ChatInfoDetail(
    //是否可以关注
    var canFollow: Boolean = false,
    //是否直播中
    var living: Boolean = false,
    //直播间ID
    var programId: Long = 0,
    //是否可以私聊
    var canPrivateChat: Boolean = false,
    //私聊字数
    var wordCount: Int = 0,
    //提示文案
    var toast: String = ""
) : Serializable

/**
 * 包含用户信息的Conversation
 */
data class LocalConversation(
    //私聊的时候显示的对方用户信息
    var showUserInfo: ChatUser? = null,
    //Conversation
    var conversation: Conversation = Conversation(),
    //陌生人item使用
    var strangerInfo: HashMap<String, String> = hashMapOf()
) {
    companion object {
        //陌生人会话使用key
        //时间
        const val TIME = "TIME"

        //昵称
        const val NICKNAME = "NICKNAME"

        //未读数量
        const val UNREADCOUNT = "UNREADCOUNT"
    }
}


/**
 * 分页对象
 */
open class BaseOffsetBean<T>(
    //是否还有更多
    var hasMore: Boolean = false,
    //返回的数据集合
    var list: ArrayList<T> = arrayListOf<T>()
) : Serializable

/**
 * 携带参数的分页对象 评论列表使用
 */
data class ExtroBaseOffsetBean<T>(
    var extData: ExtBean = ExtBean()
) : BaseOffsetBean<T>(), Serializable

data class ExtBean(var total: Long = 0) : Serializable

/**
 * 聊天室设置相关用到的实体bean
 */
open class SettingBean(
    //聊天室名称
    var chatRoomName: String = "",
    //聊天室开放状态
    var openStatus: Boolean = false,
    //聊天室密码
    var password: String = "",
    //名称修改次数
    var modifyNum: Int = 0
) : Serializable


/**
 * 连送返回的对象
 */
data class ContinueBean(var continueNum: Int = 0) : Serializable

/**
 * 聊天室名称修改次数
 */
data class ModifyCountBean(var modifyNum: Int = 0) : Serializable

/**
 * 聊天室祝福榜单信息
 * @createAuthor WanZhiYuan
 * @createVersion 4.15
 * @createDate 2019/07/01
 */
data class WishRankInfo(
    var rankList: List<WishRankItemInfo> = arrayListOf(),
    var godInfo: WishRankItemInfo? = null,
    var hasMore: Boolean = false,
    //自定义参数
    var isPull: Boolean = false
) : Serializable

/**
 * 聊天室祝福榜单单人信息
 * @createAuthor WanZhiYuan
 * @createVersion 4.15
 * @createDate 2019/07/01
 */
data class WishRankItemInfo(
    //头像
    var headPic: String = "",
    //昵称
    var nickname: String = "",
    //排名
    var orderNum: String = "",
    //显示分值
    var score: Long = 0,
    //用户id
    var userId: Long = 0,
    //贵族等级
    var royalLevel: Int = 0,
    //用户等级
    var userLevel: Int = 0,
    //贵族图标
    var royalPic: String = ""
) : Serializable

/**
 * 创建聊天室信息
 * @createAuthor WanZhiYuan
 * @createVersion 4.15
 * @createDate 2019/07/03
 */
data class CreateChatInfo(
    var open: Boolean = false,
    var roomId: Long = 0,
    var roomName: String = "",
    var hasPermis: Boolean? = null,
    var royalLevelUrl: String? = null
) : Serializable

/**
 * 聊天室开启数据
 */
data class OpenChatRoomBean(var closeMillSeconds: Long = 0L) : Serializable

data class MsgCountBean(var newCount: Int = 0) : Serializable

data class MessageHeaderBean(
    //头像
    var headRes: Int = 0,
    var headPic: String = "",
    var title: String = "",
    var content: String = "",
    var messageCount: Int = 0,
    var time: Long = 0L
)

