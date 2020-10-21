package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * pk用户
 */
class PKUser(
    var creator: Boolean = false,
    var nickname: String = "",
    var headPic: String = "",
    var status: String = "",
    var anchorLevel: Int = 0,
    var playInfo: PlayInfo? = null,
    var pushUrl: String = "",
    var sdkParams: SdkParam? = null,
    var winRound: Int = -1,//获胜回合数
    var roundResult:String ="",
    var finalResult:String ="",
    var scoreTime:Long =-1,
    var sdkProvider: String = ""
) : ProgramAnchor() {
    var roundScore: Long? = null
    var propScore: Long? = null
    var prePic: String? = null

    //4.30新增字段
    //是否是地主
    var landlord: Boolean? = null

    //本地字段
    var scoreRatio: Float? = null
    var previousScore: Int? = null
    var previousScoreRatio: Float? = null
    override fun toString(): String {
        return "PKUser(pid=$programId, score=$roundScore,propScore=$propScore nickname=$nickname, scoreRatio=$scoreRatio, previousScore=$previousScore, previousScoreRatio=$previousScoreRatio)"
    }

    fun toStringNoPrevious(): String {
        return "PKUser(pid=$programId, score=$roundScore, nickname=$nickname"
    }

}

/**
 * pk信息对象
 */
class PKInfoBean : Serializable {
    //是否倒计时结束  < 0表示倒计时结束
    var seconds: Int? = null
    var closeSeconds: Int? = null
    var totalScore: Long? = null
    var detailList: ArrayList<PKUser>? = null
    var pkId: Long = 0L
    var currRound: Int = -1 //当前的回合数1,2,3
    var punish:Boolean =false//todo 惩罚标志
    var roundFinish:Boolean =false
    var finalResult:Any? =null//代表最终PK胜负结果
    //新增PK道具字段
    var totalPropScore: Long? = null
    var stageList: ArrayList<PkStage>? = null
    var endTime: Long? = null
    var pkType: String? = null
    var logoPic: String? = null

    var template: PKExtra? = null//额外属性
    var propInfo: PkPropInfo? = null
    var giftTaskInfo: PkGiftTaskInfo? = null
    var scoreTaskInfo: PkScoreTaskInfo? = null

    //用于控制是否显示PK道具入口
    var openPropWindow: Boolean = false
    var programId: Long = 0//直播间ID

    //4.30新增字段
    //平民总分
    var civilianScore: Long = 0

    //地主分
    var landlordScore: Long = 0
    //本地字段
    var needAnim: Boolean = false//是否需要飘星动画的标识
    override fun toString(): String {
        return "PKInfoBean(seconds=$seconds, totalScore=$totalScore, detail=$detailList, endTime=$endTime, pkType=$pkType, template=$template)"
    }

}

data class PkStage(
    var continueWinCnt: Int = 0,
    var icon: String = "",
    var programId: Long = 0,
    var stagePkLevel: Int = 0
)

/***
 * programId 节目id

result PK胜负结果

stagePkLevel 当前段位等级

stageName 段位名称

icon 段位图标

bigIcon 大段位图标：用于展示升级效果

stageChange 段位等级变化: 0不变，1晋升，-1降级

stagePkScore 本次PK获得段位分

stageScore 当前段位分

stageMinScore 当前段位最低分

nextStageScore 下一段位最低分（已是最高时：-1）

continueWinCnt 当前连胜次数

mvpNickName  mvp昵称

mvpHeadPic mvp头像

closeTtl 结果展示倒计时
 */
data class PkStageDetail(
    var programId: Long = 0,
    var result: String = "",
    var stagePkLevel: Int = 0,
    var stageName: String = "",
    var icon: String = "",
    var bigIcon: String = "",
    var stageChange: Int = 0,
    var stagePkScore: Long = 0,
    var stageMinScore: Long? = null,
    var nextStageScore: Long? = null,
    var continueWinCnt: Int = 0,
    var mvpNickName: String = "",
    var mvpHeadPic: String = "",
    var closeTtl: Long = 0L,
    var resultAnimBgUrl: String? = null,
    var resultAnimForeUrl: String? = null,
    //4.34 新增字段
    var pkType: String? = null,
    //是否是地主 斗地主消息才会有此字段
    var landlord: Boolean? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other is PkStageDetail) {
            return other.programId == this.programId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return this.programId.hashCode()
    }
}

data class PkPropInfo(
    var nickname: String = "",
    var programId: Long = 0,
    var propName: String = "",
    var propPic: String = "",
    var propType: String = "",
    var status: String = "",
    var ttl: Long = 0,
    var maxTtl: Long = 0,
    var userId: Long = 0
) {
    override fun toString(): String {
        return "PkPropInfo(nickname='$nickname', programId=$programId, propName='$propName', propPic='$propPic', propType='$propType', status='$status', ttl=$ttl, userId=$userId)"
    }
}

data class PkGiftTaskInfo(
    var awardScore: Long = 0,
    var giftBeans: Long = 0,
    var giftId: Int = 0,
    var giftName: String = "",
    var giftPic: String = "",
    var needCnt: Int = 0,
    var receiveCnt: Int = 0,
    var status: String = "",
    var ttl: Long = 0,
    var maxTtl: Long = 0,
    var multiple: Float = 0f
) {
    override fun toString(): String {
        return "PkGiftTaskInfo(awardScore=$awardScore, giftBeans=$giftBeans, giftId=$giftId, giftName='$giftName', needCnt=$needCnt, receiveCnt=$receiveCnt, status='$status', ttl=$ttl)"
    }
}

//pk积分任务
data class PkScoreTaskInfo(
    var awardScore: Long = 0,
    var needScore: Long = 0,
    var nowScore: Long = 0,
    var status: String = "",
    var ttl: Long = 0,
    var maxTtl: Long = 0,
    var multiple: Float = 0f
)

/**
 * 创建pk信息对象
 */
class CreatePKInfoBean {
    //    var form: PKform? = null
    var optionGift: List<OptionGift>? = null//
    var participators: ArrayList<Participator>? = null
    var optionTime: List<Int>? = null
    var action: String = ""//create   reject cancel   accept   timeout  pking
    var myInfo: MyPk? = null
    var pkInfo: PkCreateInfo? = null // 只有创建成功后才会有
    var pkRuleUrl: String = ""
    var roomPkInfo: PKInfoBean? = null

    //默认PK时长
    var defaultTime: String = ""
}

data class OptionGift(
    var giftId: Int = 0,
    var giftName: String = ""
)

/**
 * 当前的pk创建信息
 */
class PkCreateInfo {
    var pkId: Int = 0
    var status = ""
    var giftMode = ""
    var duration: Int = 0
    var programId: Long = 0
}

class MyPk {
    var creator: Boolean = false //是不是创建者
    var anchorId: Int = 0
    var status: String = ""
    var giftId: Int = 0
    var programId: Long = 0
    var nickname: String = ""
    var picId: String = ""
    var goodsName: String = ""
    var score: Int = 0
}

//发起PK者
class Participator(
    var anchorId: Int = 0,
    var nickname: String = "",
    var headPic: String = "",
    var giftPic: String? = null,
    var status: String = "",
    var programId: Long = 0,
    var score: Int = 0
) {
    //目的是去重 以anchorId为准
    override fun hashCode(): Int {
        return programId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is Participator) && this.programId.equals(other.programId)
    }
}

//PK战绩
class PKHistory(var hasMore: Boolean = false, var list: ArrayList<PKHistoryBean> = arrayListOf())

class PKHistoryBean(
    var startTime: String = "",
    var participators: ArrayList<Participator> = ArrayList()
)

/**
 * PK礼物相关数据
 */
data class PKGiftBalanceBean(
    var beans: Long = 0,
    var groupInfo: MutableList<PKGiftBean> = mutableListOf()
) : Serializable


data class PKGiftBean(
    var gifts: MutableList<LiveGiftDto> = mutableListOf()
) : Serializable