package com.julun.huanque.common.bean.forms

import com.julun.huanque.common.constant.ProdType
import java.io.Serializable


open class ProgramIdForm(var programId: Long? = null) : SessionForm()

//查询玩家列表
class QueryRoomUserForm(val programId: Long, val offset: Int) : SessionForm()

//查询房管
class QueryManagerForm(val programId: Long, val offset: Int) : SessionForm()

//查询守护
class QueryGuardForm(val programId: Long, val offset: Int) : SessionForm()

//获取直播间次要信息Form   dataType 不传值为查询所有数据，传值表示查询单个数据
class EnterExForm(var programId: Long? = null, var dataType: String? = null) : Serializable {
    companion object {
        //dataType 取值
        //私信体验相关
        const val ExperienceSms = "ExperienceSms"

        //快捷发言
        const val QuickWord = "QuickWord"

        //首充
        const val FirstRecharge = "FirstRecharge"

        //主播头条
        const val Headline = "headlineInfo"

        //体验守护
        const val ExperienceGuard = "ExperienceGuard"
    }
}

class UserEnterRoomForm(
    programId: Long,
    var positionIndex: Int? = null,
    var fromType: String? = null,
    var shareUserId: String = ""
) : ProgramIdForm(programId)

/**
 * 查询礼物规则的form
 */
class GiftRuleForm(var giftId: Long, var prodType: String = ProdType.Gift) : Serializable

class RecommendProgramForm(var programId: Long? = null) : SessionForm()

class ShareProgramForm(var programId: Long? = null) : SessionForm()

class ConsumeForm(
    programId: Long,
    /** 数量  */
    var count: Int = -1,
    /** 物品id  */
    var giftId: Int = -1,
    //是否从背包赠送
    var fromBag: String,
    //物品类型
    var prodType: String = "",
    //分享用户的ID
    var shareUserId: String = ""
) : ProgramIdForm(programId)

///**
// * 送礼form
// */
//class NewSendGiftForm(
//    programId: Long
//    , count: Int = -1
//    , giftId: Int = -1,
//    fromBag: String
//) : ConsumeForm(programId, count, giftId) {
//
//    /** 赠送对象类型  默认是用户  */
//
//}


class MallBuyForm(
    /** 物品id  */
    var goodId: Int = -1
    /**守护的节目编号 */
    , var programId: Long = -1
) : SessionForm()


/**
 * 主播名片权限
 */
class AnchorProgramForm(
    /** 节目ID 必传 **/
    var programId: Long = -1,
    /** 封禁时间：单位（s）必传 **/
    var seconds: Int = -1,
    /** 截图：多个以逗号隔开 **/
    var pics: String = "",
    /** 举报类型值 **/
    var reportTypeValue: String = "",
    /** 节目监控类型(可选项：TmpMonitor、ImpMonitor) **/
    var monitorType: String = "",
    /** 理由 **/
    var reason: String = ""
) : Serializable

class LiveGiftForm(var programId: Long? = null) : SessionForm()


class UserServiceForm(
    var serviceCode: String = "",
    var programId: Long = -1,
    var userId: Long = -1,
    var nickname: String = "",
    //New
    var targetuserId: Long = -1,
    /** 禁言时间 **/
    var seconds: Int = -1,
    /** 理由 **/
    var reason: String = ""
) : Serializable


class AliveForm(var programId: Long = -1, var themeSessionId: Int? = null) : Serializable

class QureyRankResultForm(
    var RankingsId: Int = -1,
    var recordId: Int? = null,
    var programId: Long? = null,
    var preCycle: Boolean = false,
    var offset: Int = 0,
    var limit: Int = 20
) : Serializable

class CloseDownForm(var programId: Long, var userId: Long?, var stopAction: String) : SessionForm()

class RecommendForm(var programId: Long, var sortOrder: Long) : SessionForm()

class WarnAuthorForm(var programId: Long, var warnMsg: String) : SessionForm()

class StartLivingForm(var programId: Long) : SessionForm()

class NewLiveHeartForm(var programId: Long) : SessionForm()

class NewStopLiveForm(var programId: Long) : SessionForm()

class TipOffUserForm(
    // 举报用户
    var targetuserId: Long = 0,
    // 节目编号
    var programId: Long = 0,
    //举报类型值 -> BusiConstant.ReportType
    var reportTypeValue: String = ""
) : Serializable

/**
 *
programId	int	否	节目ID
targetUserId	long	是	被操作用户id
reportTypeValue	string	是	举报类型值（数据字典表中的item_value值）
reason	string	否	举报详情
pics	string	否	图片，多张用,隔开
 */
class SaveReportForm(
    // 举报用户
    var targetuserId: Long = 0,
    // 节目编号
    var programId: Long? = null,
    //举报类型值 -> BusiConstant.ReportType
    var reportTypeValue: String = "",
    var reason: String? = null,
    var pics: String? = null
) : Serializable

//举报节目的表单
class SaveReportProgramForm(
    // 节目编号
    var programId: Long = 0,
    //举报类型值 -> BusiConstant.ReportType
    var reportTypeValue: String = "",
    var reason: String? = null,
    var pics: String? = null
) : Serializable

/**
 * 弹幕.
 *
 * type 可选 danmu , broadcast
 */
class DanmuForm(
    var programId: Long,
    var content: String,
    var dmlevel: Int,
    var acceptprogramId: Long? = null
) : SessionForm()

class RechargeChannelQueryForm(
    var programId: Long,
    var message: String,
    var type: String = "danmu"
) : SessionForm()

/**
 * 红包
 */
class UserAndProgramForm(
    var redPacketId: Int = -1,
    var userId: Long = -1,
    var programId: Long = -1
) : SessionForm()

/**
 * 贡献榜请求body
 */
class ScoreRankResultForm(
    var programId: Long? = null,
    //数据索引位置，从0开始
    var offset: Int = 0
) : Serializable
/**
 * PK贡献榜请求body
 */
class PKScoreRankResultForm(
    var programId: Long? = null,
    var pkId:Long?=null/*,
    //数据索引位置，从0开始
    var offset: Int = 0*/
) : Serializable

/**
 * 直播间侧滑关注列表
 */
class LiveFollowForm(var offset: Int? = null, var programId: Long = 0)

/**
 * 上下切换表单
 * @param fromType 来源类型 可选项：Social、Follow、Guard、Manager、Watched
 * @param programId
 * @param typeCode 分类
 */
class SwitchForm(var fromType: String? = null, var programId: Long, var typeCode: String = "")

/**
 * 查询某一关卡的信息 [level]要查的关卡  [programId]直播间id
 */
class PassLevelForm(var level: Int? = null, var programId: Long)

/**
 * 一键送礼[level]当前的关卡  [programId]直播间id [gifts]礼物信息 id:count [passLevel]是不是一键送礼True,False
 */
class OneKeySendGiftForm(
    var level: Int,
    var programId: Long,
    var gifts: String,
    var passLevel: String
)

/**
 * 横竖屏切换的Form
 */
class SwitchScreenForm(var programId: Long, var screenType: String) : Serializable

/**
 * [dateType]时间维度(可选项：Today、Yesterday)
 */
data class QueryPassRankForm(
    var dateType: String = "",
    var offset: Int = 0
)

data class DiscountForm(
    var offset: Int? = null,
    //4.25新增参数，用于我的卡券列表 True or False
    var sameDayMerge: String? = null
)

data class ReportRescueForm(
    var salvationId: Int, var programId: Long
)

/**
 * PK段位赛
 */
data class PkRankForm(
    var programId: Long? = null,
    var offset: Int? = null
)

/**
 * 在线列表
 */
data class OnLineForm(
    var programId: Long? = null,
    var offset: Int? = null
)


/**
 * 验证动态码form
 */
data class VerifyCodeForm(var authToken: String = "", var code: String = "") : Serializable


/**
 * 请求直播列表 typeCode	string	否	节目分类代码(取热门列表返回的typeList中的typeCode，PC端取首页返回的programType中的typeCode,查询推荐节目 typeCode=Recom)
offset	int	否	分页索引(起始值0)(取值范围：0-2147483647)
limit	int	否	每页显示条数(取值范围：2-50)
programId	int	否	当前直播间ID PC端直播间查询推荐节目用到此字段
 */
data class ProgramListForm(
    var offset: Int? = null,
    var programId: Long? = null,
    var limit: Int? = null,
    var typeCode: String? = null

)

/**
 * 管理form
 */
data class CardManagerForm(
    var programId: Long = 0, var targetUserId: Long = 0, var mangeType: String? = null,
    var itemValue: String? = null, var deviceUuid: String? = null
)

/**
 * 随机获取一个直播间
 */
class RandomRoomForm(
    var programId: Long? = null,
    var sourceType: String? = null,
    var dataType: String? = null//dataType: 数据类型，如果sourceType为Magpie，则该值传任务的代码(taskCode)
)

//搜索参数
class KeyWordForm(var searchKey: String = "")