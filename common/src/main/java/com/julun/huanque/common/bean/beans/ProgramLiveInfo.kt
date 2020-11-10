package com.julun.huanque.common.bean.beans

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.alibaba.fastjson.annotation.JSONField
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.basic.RootListData
import java.io.Serializable

/**
 * Created by djp on 2016/11/2.
 */
open class ProgramLiveInfo : Serializable {
    /** 节目封面 **/
    var coverPic: String = ""

    /** 是否直播中 **/
    @JSONField(name = "isLiving")
    var isLiving: Boolean = false
//    /** 主播昵称 **/
//    var nickname: String = ""
    /** 在线人数 **/
    var onlineUserNum: Long = 0

    var heatValue: Long = 0

    /** 节目ID **/
    var programId: Long = 0

    /** 节目名称 **/
    var programName: String = ""

    /** 头像 **/
    var headPic: String = ""

    /** 是否PC 推流(可选项：True、False) **/
    @JSONField(name = "isPcLive")
    var isPcLive: Boolean = false

    var city: String = ""

    var lastShowTime: String = ""

    var anchorLevel: Int = 0

    var anchorLevelPic: String = ""

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

    //直播广场使用
    var rightTopTag: String = ""
    //左上角标
    var leftTopTag: String = ""
    //播放数据
    var playInfo: PlayInfo? = null

    //本地字段
//    var isVideoPlaying:Boolean =false
    //去重需要
    override fun equals(other: Any?): Boolean {
        if (other is ProgramLiveInfo) {
            return this.programId == other.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.toInt()
    }
}

/**
 * 关注标签所有
 */
class AuthorFollowBean : Serializable {
    /** 节目id  **/
    var programId: Long = 0

    /** 主播等级 默认-1代表不需要显示**/
    var anchorLevel: Int = -1

    /** 封面 **/
    var anchorPic: String = ""

    /** 开播状态 **/
    var livingStatus: Boolean = false

    //用户昵称
    var nickname: String = ""

    /** pc直播平台 **/
    var pcLiveStatus: Boolean = false

    /** 节目名称 **/
    var programName: String = ""

    /** 剩余时间 **/
    var surplusDay: Int = 0

    /** 最后直播时间 **/
    var lastShowTime: String = ""

    /** 观众人数 **/
    var onlineUserNum: Int = 0

    /** 最后直播格式化时间 **/
    var lastStopTime: String = ""

    //4.15新增参数
    /** 粉丝团亲密度 **/
    var intimate: Long = 0L

    /**
     * 是否是守护  本地添加属性
     */
    var guard = false

    /**
     * 4.30.0新增推送开关
     */
    var pushOpen: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is AuthorFollowBean) {
            return programId == other.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.hashCode()
    }
}


class SingleGame(var gameCode: String = "", var pic: String = "", var gameUrl: String = "", var extJsonCfg: String? = null) :
    Serializable
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
    //城市
    var city: String = ""

    //封面
    var coverPic: String = ""

    //热度值
    var heatValue: Long = 0
    var nickname: String = ""
    var programId: Long = 0
    var programName: String = ""

    //播放数据
    var playInfo: PlayInfo? = null
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
    var leafComment: RootListData<SingleComment> = RootListData<SingleComment>()
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
    var programId: Long = 0,
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
    var programId: Long = 0,
    var redId: Int = 0
) : Serializable

/**
 * 附近的人
 */
data class NearbyData(
    var delaySeconds: Long = 0,
    var keepSeconds: Long = 0,
    var onlineUserNum: Int = 0,
    var programId: Long = 0,
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
    var sign: String = "",
    //4.21 新增参数
    /** 协议ID **/
    var agreenebtId: Int? = null,
    /** 协议url **/
    var contentValue: String? = null,
    /** 协议标题 **/
    var title: String? = null
) : Serializable

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
    var tips: String = ""
) : Serializable

/**
 * 更新砸蛋模式，返回的数据
 */
data class EggUpdateBean(
    var luckyHitEgg: Boolean = false
    , var refreshGifts: List<SingleTipsUpdate> = mutableListOf()
) : Serializable

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


/**
 * 节目关注列表
 * @author WanZhiYuan
 * @date 2020/07/15
 * @since 1.0.0
 */
data class LiveRemindBeans(
    //主播id
    var anchorId: Int = 0,
    //主播头像
    var anchorPic: String = "",
    //直播中
    var livingStatus: Boolean = false,
    //PC直播中
    var pcLiveStatus: Boolean = false,
    //开启通知
    var pushOpen: Boolean = false,
    //最后直播时间
    var lastShowTime: String = "",
    //签名
    var mySign: String = "",
    //昵称
    var nickname: String = "",
    //在线人数
    var onlineUserNum: Int = 0,
    //节目id
    var programId: Long = 0,
    //节目名称
    var programName: String = ""
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (other is LiveRemindBeans) {
            return other.programId == this.programId
        }
        return false
    }

    override fun hashCode(): Int {
        return programId.hashCode()
    }
}

data class ProgramListInfo(
    var adList: MutableList<AdInfoBean>? = null,
    var defaultCategory: String = "",
    var hasMore: Boolean = false,
    var programList: MutableList<ProgramLiveInfo> = mutableListOf(),
    var isPull: Boolean = false
)

data class FollowProgramInfo(
    var recomList: MutableList<ProgramLiveInfo>? = null,
    var hasMore: Boolean = false,
    var followList: MutableList<ProgramLiveInfo> = mutableListOf(),
    var isPull: Boolean = false
)


/**
 * 通用的MultiItemEntity
 */
class MultiBean(override var itemType: Int, var content: Any) : MultiItemEntity

/**
 * 主页使用的性格数据
 */
data class CharacterTag(
    //性格
    var characterTagList: MutableList<String> = mutableListOf(),
    //我喜欢的列表
    var favoriteTagList: MutableList<String> = mutableListOf()
) : Serializable

data class CityBean(
    //城市ID
    var cityId: Long = 0,
    //城市名称
    var cityName: String = "",
    //城市类型（故乡，足迹，常驻地）
    var cityType: String = "",
    //省名称
    var provinceName: String = ""
) : Serializable {
    companion object {
        //故乡
        const val Home = "Home"

        //常驻地
        const val Resident = "Resident"

        //足迹
        const val Foot = "Foot"
    }
}

/**
 * 主页中的语音数据
 */
data class VoiceBean(
    //语音时长
    var length: Int = 0,
    //语音状态
    var voiceStatus: String = "",
    //语音连接
    var voiceUrl: String = "",
    //是否点赞
    var like: String = "",
    //点赞数量
    var likeCount: Int = 0


) : Serializable {
    companion object {
        //等待审核
        const val Wait = "Wait"

        //被拒绝
        const val Reject = "Reject"

        //审核通过
        const val Pass = "Pass"
    }
}

/**
 * 评论的对象
 */
data class AppraiseBean(
    //同意的数量
    var agreeNum: Int = 0,
    //评论内容
    var content: String = "",
    //评价ID
    var recordId: Long = 0
) : Serializable

/**
 * 直播信息对象
 */
data class ProgramInfoHomePage(
    //直播间ID
    var programId: Long = 0,
    //直播间封面
    var programCover: String = "",
    //是否开播
    var living: String = ""
) : Serializable

/**
 * 主页相关数据
 */
data class HomePageInfo(
    //年龄
    var age: Int = 0,
    //评价列表
    var appraiseList: MutableList<AppraiseBean> = mutableListOf(),
    //真人标识
    var authMark: String = "",
    //生日
    var birthday: String = "",
    //主播的直播信息,如果不是主播则不返回
    var programInfo: ProgramInfoHomePage? = null,
    //是否拉黑
    var black: String = "",
    //性格数据
    var characterTag: CharacterTag = CharacterTag(),
    //城市列表
    var cityList: MutableList<CityBean> = mutableListOf(),
    //星座
    var constellation: String = "",
    //当前城市
    var currentCity: String = "",
    //是否关注
    var follow: String = "",
    //头像
    var headPic: String = "",
    //是否是真人
    var headRealPeople: String = "",
    //身高
    var height: Int = 0,
    //是否是密友
    var intimate: String = "",
    //职业ID
    var jobId: Int = 0,
    //职业名称
    var jobName: String = "",
    //我的性格列表(相同点判断)
    var myCharacterTag: MutableList<String> = mutableListOf(),
    //我的城市列表
    var myCityList: MutableList<CityBean> = mutableListOf(),
    //我的签名
    var mySign: String = "签名不足以表达我自己，找我私聊吧~",
    //昵称
    var nickname: String = "",
    //勋章列表
    var officeMedal: MutableList<String> = mutableListOf(),
    //乐园是否开启
    var paradiseSwitch: String = "",
    //封面列表
    var picList: MutableList<String> = mutableListOf(),
    //贵族等级
    var royalLevel: Int = 0,
    //性别
    var sex: String = "",
    //用户ID
    var userId: Long = 0L,
    //用户等级
    var userLevel: Int = 0,
    //用户类型
    var userType: String = "",
    //语音签名对象
    var voice: VoiceBean = VoiceBean(),
    //体重
    var weight: Int = 0,
    //密友评价是否还有更多
    var hasMore: String = "",
    //资料完成度 百分比
    var perfection: Int = 0,
    //主播等级
    var anchorLevel: Int = 0,
    //贵族等级图标
    var royalPic: String = "",
    //是否显示亲密知己
    var showCloseConfidant: String = "",
    //亲密知己对象
    var closeConfidant: IntimBeanFriendBean = IntimBeanFriendBean(),
    //亲密榜数据
    var closeConfidantRank: CloseConfidantRankBean = CloseConfidantRankBean(),
    //座驾数据
    var carInfo: HomePageCarInfo = HomePageCarInfo(),
    //tab相关数据
    var userDataTabList: MutableList<UserDataTab> = mutableListOf(),
    //播放数据
    var playProgram: HomePageProgram = HomePageProgram(),
    //养鹊相关数据
    var playParadise: HomePagePlayParadise = HomePagePlayParadise(),
    //足迹使用的对象
    var homeCity: HomeCity = HomeCity(),
    //勋章数据
    var iconList: MutableList<String> = mutableListOf()

) : Serializable

/**
 * 我的足迹使用的对象
 */
data class HomeCity(
    //访问人的常驻地
    var curryCityName: String = "",
    //访问人的头像
    var curryHeadPic: String = "",
    //距离
    var distance: Int = 0,
    //主页用户常驻地
    var homeCityName: String = "",
    //主页用户头像
    var homeHeadPic: String = ""

) : Serializable

/**
 * 单个小鹊
 */
data class SingleMagpie(
    //小鹊图标
    var upgradeIcon: String = "",
    //小鹊等级
    var upgradeLevel: Int = 0
) : Serializable

/**
 * 主页养鹊数据
 */
data class HomePagePlayParadise(
    //小鹊列表
    var magpieList: MutableList<SingleMagpie> = mutableListOf(),
    //显示的文案
    var showText: String = ""
) : Serializable

/**
 * 主页相关 直播数据
 */
data class HomePageProgram(
    //直播状态
    var living: String = "",
    //在线人数
    var onlineUserNum: Int = 0,
    //封面
    var programCover: String = "",
    //直播间ID
    var programId: Long = 0
) : Serializable


/**
 * 主页座驾数据
 */
data class HomePageCarInfo(
    //座驾名称
    var carName: String = "",
    //座驾图标
    var carPic: String = "",
    //座驾类型
    var carType: String = "",
    //动效地址
    var dynamicUrl: String = "",
    //商品ID
    var goodsId: Long = 0,
    //佩戴所需最低贵族等级
    var royalLevel: Int = 0,
    //佩戴所需贵族名称
    var royalName: String = "",
    //显示的文案
    var showMsg: String = ""
) : Serializable

/**
 * 亲密知己对象
 */
data class IntimBeanFriendBean(
    //用户ID
    var userId: Long = 0,
    //用户头像
    var headPic: String = "",
    //差值
    var diffValue: Long = 0
) : Serializable

/**
 * 亲密榜单数据
 */
data class CloseConfidantRankBean(
    //前几名头像
    var rankList: MutableList<String> = mutableListOf(),
    //榜单显示总数
    var rankNum: Int = 0
) : Serializable


/**
 * 评价列表
 */
data class EvaluateTags(
    //最大评论数量
    var maxEvaluateCnt: Int = 0,
    //我的评论列表
    var myEvaluateTagList: MutableList<String> = mutableListOf(),
    //推荐评论列表
    var recomTagList: MutableList<String> = mutableListOf()
) : Serializable

/**
 * 主页图片对象
 */
data class HomePagePicBean(
    //图片地址
    var pic: String = "",
    //真人水印
    var realPic: String = "",
    //是否选中
    var selected: String = ""
) : Serializable

/**
 * 亲密榜单个对象
 */
data class CloseConfidantBean(
    //头像
    var headPic: String = "",
    //昵称
    var nickname: String = "",
    //分数
    var score: Long = 0,
    //用户ID
    var userId: Long = 0
) : Serializable

/**
 * 私信页面展示直播信息
 */
data class ProgramInfoInConversation(
    //封面
    var coverPic: String = "",
    //是否开播
//    var living: String = "",
    //直播间ID
    var programId: Long = 0,
    //是否需要查看更多(查看推荐列表)
    var viewMore: String = ""
) : Serializable

/**
 * 私信页面  单个直播间对象
 */
data class SingleProgramInConversation(
    //直播间ID
    var programId: Long = 0,
    //直播标题
    var programName: String = "",
    //封面
    var coverPic: String = "",
    //是否开播
//    var living: String = "",
    //所在城市
    var city: String = "",
    //热度值
    var heatValue: Long = 0
) : Serializable
