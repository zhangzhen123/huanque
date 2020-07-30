package com.julun.huanque.common.bean.beans

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

/**
 * Created by djp on 2016/11/2.
 */
open class ProgramLiveInfo : Serializable {
    /** 节目封面 **/
    var coverPic: String = ""

    /** 是否直播中 **/
    var isLiving: String = ""
//    /** 主播昵称 **/
//    var nickname: String = ""
    /** 在线人数 **/
    var onlineUserNum: Int? = null

    /** 节目ID **/
    var programId: Int = 0

    /** 节目名称 **/
    var programName: String = ""

    /** 主播id **/
    var anchorId: Int = -1

    /** 头像 **/
    var headPic: String = ""

    /** 是否PC 推流(可选项：True、False) **/
    var isPcLive: String = ""

    /** 角标图片或文字 **/
    var tagContentTpl: String = ""

    //新增的标签
    var bodyTagContentTpl: String = ""

    /** 连星数量 **/
    var starCount: Int = 0

    /** PK或红包图标地址**/
    var statePic: String = ""

    /*图标列表*/
    var iconList: MutableList<String> = mutableListOf()

    //4.23新增字段
    /** 右上角标签 **/
    var upRightInfo: String = ""

    /** 边框图标 **/
    var coverFramePic: String = ""

    //4.24新增字段
    var positionIndex: Int? = null

    //播放数据
    var playInfo: PlayInfo? = null
}

class ProgramLiveIndexInfo : ProgramLiveInfo() {
    //主播等级
    var anchorLevel: Int = 0

    //粉丝数
    var fansNum: Long = 0L

    /** 最后直播时间 **/
    var lastShowTime: String = ""
}


/**
 * 新人礼包tag类
 */
class AwardTag(var tag: Int = 1)

class SingleGame(var gameCode: String = "", var pic: String = "", var gameUrl: String = "", var extJsonCfg: String? = null) : Serializable
/**
 * 直播间显示控件
 */
//class LiveAction(var actionCode:String="",var actionName:String="",var pic:String="",var res:Int=-1,var tag:String?=null):Serializable
/************************************* 首页新接口 JavaBeans ***************************************/

class StartAdsResult : Serializable {
    var startAds: List<RoomBanner> = arrayListOf()
    var latestStartAdsVersion: String = ""
}

class RecommendInfo : Serializable {
    var anchorId: Int = 0
    var defaultPlaySeconds: Int = 8
    var headPic: String = ""
    var nickname: String = ""
    var programId: Int = 0

    /**新增的标签**/
    var bodyTagContentTpl: String = ""

    //播放数据
    var playInfo: PlayInfo? = null
}


/**
 * 动态
 */
open class DynamicInfo : Serializable {
    /** 节目id **/
    var programId: Int = 0

    /** 帖子id **/
    var postId: Long = 0

    /** 头像 **/
    var headPic: String = ""

    /** 昵称 **/
    var nickname: String = ""

    /** 描述 **/
    var content: String = ""

    /** 用户等级 **/
    var anchorLevel: Int = 0

    /** 图片 **/
    var pic: List<String?> = ArrayList()

    /** 发布时间 **/
    var createTime: String = ""

    /** 审核状态 待审核(W)、通过(P)、审核不通过(R)**/
    var status: String = ""

    /** 违规描述 **/
    var remark: String = ""

    /** 分享数量 **/
    var shareNum: Long = 0

    /**评论数量**/
    var commentNum: Long = 0

    /** 点赞数量 **/
    var praiseNum: Long = 0

    /** 是否点赞 **/
    var praiseStatus: Boolean = false

    /** 是否正在直播 **/
    var isLiving: Boolean = false

    //用户ID
    var userId: Long = 0

    //4.28新增参数
    /** 关注状态 **/
    var followStatus: Boolean = false

    /** 帖子类型 Post -> 动态帖子，Video -> 视频帖子 **/
    var type: String = ""

    /** 封面尺寸，宽高以英文逗号分隔 **/
    var picSize: String = ""

    /** 视频地址 type=Video时有值 **/
    var videoUrl: String = ""

    /** 封面宽高比例 **/
    var videoScale: Float = 0f

    /** 性别：男(Male)、女(Female) **/
    var sex: String = ""

    //本地字段
    /** 存放一个对象 **/
    var obj: Any? = null

    override fun equals(other: Any?): Boolean {
        if (other is DynamicInfo) {
            return postId == other.postId
        }
        return false
    }

    override fun hashCode(): Int {
        return postId.hashCode()
    }
}


/**
 * 单个评论 （二级）
 */
open class SingleComment() : Parcelable {
    var comment: String = ""
    var commentId: Long = 0
    var commentNum: Long = 0
    var createTime: Long = 0
    var isAuthor: Boolean = false
    var myComment: Boolean = false
    var nickname: String = ""
    var objectId: Long = 0
    var praiseNum: Long = 0
    var praised: Boolean = false
    var headPic: String = ""

    //回复的时候使用
    var toNickname: String = ""

    //回复的ID
    var replyId: Long = 0

    constructor(parcel: Parcel) : this() {
        comment = parcel.readString() ?: ""
        commentId = parcel.readLong()
        commentNum = parcel.readLong()
        createTime = parcel.readLong()
        isAuthor = parcel.readByte() != 0.toByte()
        myComment = parcel.readByte() != 0.toByte()
        nickname = parcel.readString() ?: ""
        objectId = parcel.readLong()
        praiseNum = parcel.readLong()
        praised = parcel.readByte() != 0.toByte()
        headPic = parcel.readString() ?: ""
        toNickname = parcel.readString() ?: ""
        replyId = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeLong(commentId)
        parcel.writeLong(commentNum)
        parcel.writeLong(createTime)
        parcel.writeByte(if (isAuthor) 1 else 0)
        parcel.writeByte(if (myComment) 1 else 0)
        parcel.writeString(nickname)
        parcel.writeLong(objectId)
        parcel.writeLong(praiseNum)
        parcel.writeByte(if (praised) 1 else 0)
        parcel.writeString(headPic)
        parcel.writeString(toNickname)
        parcel.writeLong(replyId)
    }

    override fun describeContents(): Int {
        return 0
    }

    @SuppressLint("ParcelCreator")
    companion object CREATOR : Parcelable.Creator<SingleComment> {
        override fun createFromParcel(parcel: Parcel): SingleComment {
            return SingleComment(parcel)
        }

        override fun newArray(size: Int): Array<SingleComment?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 单个评论,一级
 */
class FirstLevelComment : SingleComment(), Serializable {
    var leafComment: RootListLiveData<SingleComment> = RootListLiveData<SingleComment>()
}


/**
 * 刷新回调
 */
data class RefreshResult(
        //是否刷新用户中心
        var refreshUserCenter: Boolean = false,
        //是否删除主播视频
        var deleteAnchorVideo: Boolean = false,
        var objectId: Long = 0
) : Serializable

/**
 * 猜你喜欢结果
 */
data class GuessULikeBean(
        var coverPic: String = "",
        var hasULike: Boolean = false,
        var headPic: String = "",
        var nickname: String = "",
        var programId: Int = 0,
        var isShow: Boolean = false,
        //播放数据
        var playInfo: PlayInfo? = null
) : Serializable

/**
 * 首页提示弹窗
 */
data class HomeHitBean(
    var redPacket: RedPacketBean? = null,
    var guessULike: GuessULikeBean? = null,
    var type: String? = null
) : Serializable

/**
 * 首页红包提示弹窗
 */
data class RedPacketBean(
        var actionShowTitle: String = "",
        @JSONField(name = "isShow")
        var isShow: Boolean = false,
        var logoPic: String = "",
        var subTitle: String = "",
        var title: String = "",
        var programId: Int = 0,
        var redId: Int = 0
) : Serializable

/**
 * 附近的人
 */
data class NearbyData(
        var delaySeconds: Long = 0,
        var keepSeconds: Long = 0,
        var onlineUserNum: Int = 0,
        var programId: Int = 0,
        var userPics: List<String> = listOf()
)

/**
 * 存储一个键值对 值可空键不可空
 */
data class KeyValue(var key: String, var value: String?)

/**
 * 检查协议接口返回的对象
 * @param agreementCode 协议代码
 * @param sign 用户是否已经同意该协议
 * @author WanZhiYuan
 * @since 4.21
 * @update 2019/11/15
 */
data class AgreementData(
        var agreementCode: String = "",
        var sign: Boolean = false,
        //4.21 新增参数
        /** 协议ID **/
        var agreenebtId: Int? = null,
        /** 协议url **/
        var contentValue: String? = null,
        /** 协议标题 **/
        var title: String? = null) : Serializable

/**
 * 更新位置信息使用的Bean
 */
data class UpdateLocationBean(
        var showLocation: Boolean = false
) : Serializable {}

/**
 * 更新砸蛋模式，返回的需要刷新的tip和礼物ID
 */
data class SingleTipsUpdate(
        var giftId: Int = 0,
        var tips: String = "") : Serializable

/**
 * 更新砸蛋模式，返回的数据
 */
data class EggUpdateBean(
        var luckyHitEgg: Boolean = false
        , var refreshGifts: List<SingleTipsUpdate> = mutableListOf()) : Serializable

data class OrdinaryActivitiesBean(
        var rechargeWheelVO: RechargeWheelVO? = null
)

data class RechargeWheelVO(
        var canRotate: Boolean = false,
        var url: String = "",
        var actPic: String = ""
)

data class AdolescentBean(
    var headPic: String = "",
    var nickname: String = "",
    var programs: ArrayList<AdolescentProgramBean> = arrayListOf(),
    var popWin: Boolean = false
) : Serializable

data class AdolescentProgramBean(
        var videoCover: String = "",
        var videoName: String = "",
        var videoUrl: String = ""
) : Serializable