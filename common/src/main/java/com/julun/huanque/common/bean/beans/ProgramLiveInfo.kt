package com.julun.huanque.common.bean.beans

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.alibaba.fastjson.annotation.JSONField
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.contrarywind.interfaces.IPickerViewData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.constant.BusiConstant
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


class SingleGame(
    var gameCode: String = "",
    var pic: String = "",
    var gameUrl: String = "",
    var extJsonCfg: String? = null
) :
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
    var likeCount: Int = 0,
    //拒绝原因
    var remark: String = ""


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
    //真人标识
    var authMark: String = "",
    //他人主页认证的标签列表
    var authTagList: ArrayList<UserTagBean> = arrayListOf(),
    //我认证的标签
    var myAuthTag: MineTagBean = MineTagBean(),
    //当前访问用户，头像认证状态
    var currHeadRealPeople: String = "",
    //生日
    var birthday: String = "",
    //是否心动
    var heartTouch: String = "",
    //心动文案
    var interactTips: String = "",
    //是否可以互动
    var canInteractive: String = "",
    //我是否拉黑对方
    var black: String = "",
    //对方是否拉黑我
    var targetBlack: String = "",
    //星座code
    var constellationType: String = "",
    //星座名称
    var constellation: String = "",
    //当前登录用户性别
    var currSexType: String = "",
    //当前用户ID
    var currUserId: Long = 0,
    //家乡数据
    var homeTown: HomTownBean = HomTownBean(),
    //距离使用的对象
    var distanceCity: HomeCity = HomeCity(),
    //是否关注
    var follow: String = "",
    //头像
    var headPic: String = "",
    //我的签名
    var mySign: String = "",
    //是否是真人
    var headRealPeople: String = "",
    //是否是密友
    var intimate: String = "",
    //他人主页喜欢的标签列表
    var likeTagList: ArrayList<UserTagBean> = arrayListOf(),
    //我喜欢的标签
    var myLikeTag: MineTagBean = MineTagBean(),
    //昵称
    var nickname: String = "",
    //在线状态
    var online: HomeOnLineBean? = null,
    //资料完成度 百分比
    var perfection: Int = 0,
    //封面列表
    var picList: MutableList<String> = mutableListOf(),
    //动态数量
    var postNum: Int = 0,
    //收礼数据
    var receiveGifts: MutableList<ChatGift> = mutableListOf(),
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
    //身材数据
    var figure: FigureBean = FigureBean(),
    //社交意愿
    var wishList: MutableList<SocialWishBean> = mutableListOf(),
    //所有的社交意愿
    var wishConfigList: MutableList<SocialWishBean> = mutableListOf(),
    //职业相关
    var profession: ProfessionInfo = ProfessionInfo(),
    //学校数据
    var schoolInfo: SchoolInfo = SchoolInfo(),

    //语音签名对象
    var voice: VoiceBean = VoiceBean(),

    //主播等级
    var anchorLevel: Int = 0,
    //贵族等级图标
    var royalPic: String = "",
    //播放数据
    var playProgram: HomePageProgram = HomePageProgram(),
    //直播间内的动态数据
    var post: PostInHomePage = PostInHomePage()
    /*2.0.0新增字段*/

) : Serializable

/**
 * 编辑相关数据  这个类除了picList结构与上面不一样 其他一模一样 至于为什么这样做 问后台
 */
data class EditPagerInfo(
    //星座名称
    var constellation: String = "",
    //年龄
    var age: Int = 0,
    //真人标识
    var authMark: String = "",
    //他人主页认证的标签列表
    var authTagList: MutableList<UserTagBean> = mutableListOf(),
    //我认证的标签
    var myAuthTag: MineTagBean = MineTagBean(),
    //生日
    var birthday: String = "",
    //是否可以互动
    var canInteractive: String = "",
    //是否拉黑
    var black: String = "",
    //星座数据
    var constellationInfo: ConstellationInfo = ConstellationInfo(),
    //当前登录用户性别
    var currSexType: String = "",
    //当前用户ID
    var currUserId: Long = 0,
    //家乡数据
    var homeTown: HomTownBean = HomTownBean(),
    //距离使用的对象
    var distanceCity: HomeCity = HomeCity(),
    //是否关注
    var follow: String = "",
    //头像
    var headPic: String = "",
    //我的签名
    var mySign: String = "",
    //是否是真人
    var headRealPeople: String = "",
    //是否是密友
    var intimate: String = "",
    //他人主页喜欢的标签列表
    var likeTagList: MutableList<UserTagBean> = mutableListOf(),
    //我喜欢的标签
    var myLikeTag: MineTagBean = MineTagBean(),
    //昵称
    var nickname: String = "",
    //在线状态
    var online: HomeOnLineBean? = null,
    //资料完成度 百分比
    var perfection: Int = 0,
    //封面列表
    var picList: MutableList<HomePagePicBean> = mutableListOf(),
    //动态数量
    var postNum: Int = 0,
    //收礼数据
    var receiveGifts: MutableList<ChatGift> = mutableListOf(),
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
    //身材数据
    var figure: FigureBean = FigureBean(),
    //社交意愿
    var wishList: MutableList<SocialWishBean> = mutableListOf(),
    //所有的社交意愿
    var wishConfigList: MutableList<SocialWishBean> = mutableListOf(),
    //职业相关
    var profession: ProfessionInfo = ProfessionInfo(),
    //学校数据
    var schoolInfo: SchoolInfo = SchoolInfo(),

    //语音签名对象
    var voice: VoiceBean = VoiceBean(),

    //主播等级
    var anchorLevel: Int = 0,
    //贵族等级图标
    var royalPic: String = "",
    //播放数据
    var playProgram: HomePageProgram = HomePageProgram(),
    //直播间内的动态数据
    var post: PostInHomePage = PostInHomePage()
    /*2.0.0新增字段*/

) : Serializable

/**
 * 编辑资料页面 家乡数据
 */
data class HomTownBean(
    //城市数据
    var homeTownCity: String = "",
    //家乡ID
    var homeTownId: Int = 0,
    //省份数据
    var homeTownProvince: String = ""
) : Serializable

/**
 * 我的标签数据
 */
data class MineTagBean(
    //标签列表
    var showTagList: MutableList<UserTagBean> = mutableListOf(),
    //分类标签列表
    var typeTagList: MutableList<SingleTypeTag> = mutableListOf(),
    //我 认证/喜欢 的标签数量
    var markTagNum: Int = 0
) : Serializable

/**
 * 单个分类标签
 */
data class SingleTypeTag(
    //tab标签名称
    var tabTagName: String = "",
    //分组标签列表
    var groupTagList: MutableList<UserTagBean> = mutableListOf()
) : Serializable

/**
 * 家乡数据
 */
data class HomeTownInfo(
    //家乡省份名称 没有省份 只显示城市（直辖市）
    var homeTownProvince: String = "",
    //家乡城市名称
    var homeTownCity: String = "",
    //家乡介绍
    var introduce: String = "",
    //人文数据
    var cultureList: MutableList<SingleCulture> = mutableListOf()
) : Serializable

/**
 * 单个人文列表对象
 */
data class SingleCulture(
    //人文类型
    var cultureType: String = "",
    //人文类型文案
    var cultureTypeText: String = "",
    //人文类型数量（总数零）
    var num: Int = 0,
    //选择的数量
    var totalMarkNum: Int = 0,
    //人文列表数据
    var cultureConfigList: MutableList<SingleCultureConfig> = mutableListOf()
) : Serializable

/**
 * 单个人文对象
 */
data class SingleCultureConfig(
    //人文记录ID（编辑 使用）
    var logId: Long = 0,
    //城市ID
    var cityId: Long = 0,
    //城市名称
    var name: String = "",
    //封面
    var coverPic: String = "",
    //去过或者吃过标记
    var mark: String = ""
) : Serializable


/**
 * 职业数据
 */
data class ProfessionInfo(
    //职业ID
    var professionId: Int = 0,
    //行业
    var professionTypeText: String = "",
    //职业名称
    var professionName: String = "",
    //年收入
    var incomeText: String = "",
    //职业特性
    var myFeatureList: MutableList<SingleProfessionFeatureConfig> = mutableListOf()
) : Serializable

/**
 * 学校数据
 */
data class SchoolInfo(
    //学历
    var education: String = "",
    //学历code
    var educationCode: String = "",
    //学校名称
    var school: String = "",
    //入学年份
    var startYear: String = ""
) : Serializable

/**
 * 身材数据
 */
data class FigureBean(
    //身高
    var height: Int = 0,
    //体重
    var weight: Int = 0,
    //身材
    var myFigure: String = "",
    //主页使用
    var figure: String = "",
    //身材图片，主页使用
    var figurePic: String = "",
    //建议
    var suggest: MutableList<String> = mutableListOf(),
    //身材图片（编辑页面使用）
    var myFigurePic: String = "",
    //身材配置列表（编辑页面使用）
    var configList: MutableList<FigureConfig> = mutableListOf()
) : Serializable

/**
 * 身材配置
 */
data class FigureConfig(
    //身材
    var figure: String = "",
    //身材图片
    var figurePic: String = "",
    //最小bmi
    var min: Int = 0,
    //最大bmi
    var max: Int = 0
) : Serializable

/**
 * 星座数据
 */
data class ConstellationInfo(
    //星座名称
    var constellationName: String = "",
    //星座图片
    var constellationPic: String = "",
    //适配文案日期
    var hitText: String = "",
    //最佳配对
    var pairConstellation: String = "",
    //星座描述
    var constellationDesc: String = ""
) : Serializable

/**
 * 社交意愿对象
 */
data class SocialWishBean(
    //社交意愿code
    var wishType: String = "",
    //社交意愿文本
    var wishTypeText: String = "",
    //主页使用的图标
    var icon: String = "",
    //社交意愿说明
    var wishTypeExplain: String = "",
    //是否选中（本地字段）
    var selected: String = ""
) : Serializable

///**
// * 主页tag对象
// */
//data class UserTagBean(
//    //标签ID
//    var tagId: Long = 0,
//    //标签名称
//    var tagName: String = "",
//    //标签Icon
//    var tagIcon: String = "",
//    //标签上显示的图片
//    var tagPic: String = "",
//    //喜欢或者认证的标记
//    var mark: String = "",
//    //点赞数量
//    var praiseNum: Long = 0,
//    //审核通过照片数量
//    var picNum: Int = 0
//) : Serializable

/**
 * 主页在线状态
 */
data class HomeOnLineBean(
    //在线状态
    var onlineStatus: String = "",
    //状态文案
    var onlineStatusText: String = ""
) : Serializable {
    companion object {
        //在线
        const val Online = "Online"
    }

}

/**
 * 主页里面使用的动态数据
 */
data class PostInHomePage(
    //动态数据
    var postType: String = "",
    //最近动态文本 postType = Text 显示
    var lastPostContent: String = "",
    //动态数量
    var postNum: Long = 0,
    //最近动态图片 postType = Pic 显示
    var lastPostPics: MutableList<String> = mutableListOf()
) : Serializable

/**
 * 我的足迹使用的对象
 */
data class HomeCity(
    //距离(单位米)
    var distance: Long = 0,
    //主页用户城市名称
    var curryCityName: String = "",
    //是否同市
    var sameCity: String = ""
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
    var programId: Long = 0,
    //播放数据
    var playInfo: PlayInfo? = null
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
    var coverPic: String = "",
    //真人水印
    var realPic: String = "",
    //是否选中
    var selected: String = "",
    //是否是头像（编辑资料页面使用）
    var headerPic: String = "",
    //是否显示不可移动提示
    var showNoMoveAttention: String = "",
    var logId: Long = 0L
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
    var living: String = "",
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
    var living: String = "",
    //所在城市
    var city: String = "",
    //热度值
    var heatValue: Long = 0,
    //标签
    var rightTopTag: String = ""
) : Serializable

/**
 * 我的圈子对象
 */
data class MyGroupInfo(
    //我的圈子
    var group: RootListData<CircleGroup> = RootListData(),
    //推荐圈子
    var recommendGroup: RootListData<CircleGroup> = RootListData()
) : Serializable

/**
 * 圈子数据
 */
data class CircleGroup(
    //圈子描述
    var groupDesc: String = "",
    //圈子ID
    var groupId: Long = 0,
    //圈子名称
    var groupName: String = "",
    //圈子图片
    var groupPic: String = "",
    //圈子热度值
    var heatValue: Long = 0L,
    //是否已经加入（本地字段）
    var joined: String = "",
    //类型（推荐还是关注 本地字段）
    var type: String = ""
) : Serializable


/**
 * 分身账号的数据
 */
data class AccountBean(
    //是否可以新增分身账号
    var canAdd: String = "",
    //账号列表
    var subList: MutableList<SingleAccount> = mutableListOf(),
    //不能创建账号的原因
    var reason: String = ""
) : Serializable

/**
 * 单个账号
 */
data class SingleAccount(
    //昵称
    var nickname: String = "",
    //头像
    var headPic: String = "",
    //用户ID
    var userId: Long = 0
) : Serializable

/**
 * 更新用户信息之后的返回
 */
data class UserProcessBean(
    //资料完成度
    var perfection: Int = 0,
    //添加封面的时候返回
    var logId: Long = 0
) : Serializable

data class UserHeadChangeBean(
    //资料完成度
    var perfection: Int = 0,
    var headRealPeople: Boolean = false
) : Serializable

/**
 * 编辑页面  家乡数据
 */
data class EditHomeTownBean(
    //家乡Id
    var homeTownId: Int = 0,
    //城市配置合集  需客户端自行存储本地 如果城市配置版本没有变化，此字段不返回
    var cityConfigList: MutableList<EditCityBean> = mutableListOf(),
    //当前城市版本号
    var version: Int = 0,
    //人文数据
    var cultureList: MutableList<SingleCulture> = mutableListOf(),
    //家乡省会
    var homeTownProvince: String = "",
    //家乡城市
    var homeTownCity: String = ""
) : Serializable

/**
 * 城市对象
 */
data class EditCityBean(
    //城市名称
    var city: String = "",
    //城市名称
    var cityId: Int = 0,
    //排序值
    var cityOrderNum: Int = 0,
    //城市拼音
    var cityPinyin: String = "",
    //城市类型
    var cityType: String = "",
    //国家名称
    var country: String = "",
    //纬度
    var lat: String = "",
    //经度
    var lng: String = "",
    //省份名称
    var province: String = "",
    //省份排序值
    var provinceOrderNum: Int = 0
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return city
    }
}

/**
 * 学校接口
 */
data class SchoolBean(
    //学校
    var school: String = "",
    //学历code
    var education: String = "",
    //入学年份
    var startYear: String = "",
    //学历数据
    var configList: MutableList<EducationBean> = mutableListOf(),
    //学历名称（本地字段）
    var educationText: String = ""
) : Serializable

/**
 * 学历对象
 */
data class EducationBean(
    //学历code
    var educationCode: String = "",
    //学历名称
    var educationText: String = ""
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return educationText
    }
}

/**
 * 搜索学校的结果
 */
data class QuerySchoolBean(
    var schoolList: MutableList<SingleSchool> = mutableListOf()
) : Serializable

/**
 * 单个学校
 */
data class SingleSchool(
    //学校ID
    var schoolId: Int = 0,
    //学校名称
    var schoolName: String = ""
) : Serializable

/**
 * 编辑使用的职业对象
 */
data class EditProfessionBean(
    //我的年收入code
    var incomeCode: String = "",
    //我的职业特性
    var myFeatureList: MutableList<SingleProfessionFeatureConfig> = mutableListOf(),
    //职业ID
    var professionId: Int = 0,
    //年收入配置表
    var incomeConfigList: MutableList<SingleIncome> = mutableListOf(),
    //职业特性配置表
    var featureConfigList: MutableList<SingleProfessionFeatureConfig> = mutableListOf(),
    //职业配置表
    var professionConfigList: MutableList<SingleProfessionConfig> = mutableListOf(),
    //职业特性选取数量最大值
    var maxFeature: Int = 0
) : Serializable

/**
 * 单个收入说明
 */
data class SingleIncome(
    //年收入code
    var incomeCode: String = "",
    //年收入文案
    var incomeText: String = ""
) : Serializable

/**
 * 单个职业特性
 */
data class SingleProfessionFeatureConfig(
    //职业特性code
    var professionFeatureCode: String = "",
    //职业特性文案
    var professionFeatureText: String = "",
    //特性分类
    var professionFeatureType: String = "",
    //是否标记（本地字段）
    var mark: String = ""
) : Serializable {
    companion object {
        const val Positive = "Positive"
        const val Middle = "Middle"
        const val Negative = "Negative"
    }
}

/**
 * 单个职业配置
 */
data class SingleProfessionConfig(
    //行业type
    var professionType: String = "",
//行业文案
    var professionTypeText: String = "",
    //职业列表
    var professionList: MutableList<SingleProfession> = mutableListOf()
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return professionTypeText
    }
}

/**
 * 单个职业
 */
data class SingleProfession(
    // 职业ID
    var professionId: Int = 0,
    //职业名称
    var professionName: String = ""
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return professionName
    }
}