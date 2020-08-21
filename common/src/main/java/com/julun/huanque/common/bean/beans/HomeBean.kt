package com.julun.huanque.common.bean.beans

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.basic.RootListData
import java.io.Serializable

class HomeItemBean(var showType: Int, var content: Any) : MultiItemEntity {
    companion object {
        const val HEADER = 1//头部
        const val NORMAL = 2//普通样式
        const val GUIDE_TO_ADD_TAG = 3//引导添加标签
        const val GUIDE_TO_COMPLETE_INFORMATION = 4//引导完善资料
    }

    override val itemType: Int
        get() = showType
//    override fun getItemType(): Int {
//        return showType
//    }
}

/**
 *
 * [url]图片地址 为空代表需要添加
 * [res]加载本地工程资源
 *
 */
class PhotoBean(
    var url: String = "",
    var res: Int = 0

) {
    override fun toString(): String {
        return "PhotoBean(url='$url', res=$res)"
    }
}

data class HeadNavigateInfo(
    var moduleList: List<HeadModule>,
    var taskBar: HomeTaskBar
)

data class HeadModule(
    var num: String = "",
    var type: String = "",
    var hot: Boolean = false
//    var baseInfo: HeadBaseInfo = HeadBaseInfo(),
//    var bgPic: String = "",
//    var moduleName: String = "",
//    var moduleType: String = ""
) {
    companion object {
        const val MaskQueen = "MaskQueen"
        const val AnonymousVoice = "AnonymousVoice"
        const val MagpieParadise = "MagpieParadise"
        const val HotLive = "HotLive"
        const val PlumFlower = "PlumFlower"
    }
}

class HomeListData<T> : RootListData<T>() {
    var modules: List<HeadModule> = arrayListOf()
    var remind: HomeRemind = HomeRemind()
    var taskBar: HomeTaskBar = HomeTaskBar()
    var offset: Int? = null
}

data class HomeRemind(
    var coverRemind: Boolean = false,
    var picList: List<String> = listOf(),
    var tagRemind: Boolean = false
)

data class HomeTaskBar(
    var desc: String = "",
    var type: String = "",
    var label: String = "",
    var myCash: String = ""
)
//data class CoverRemind(
//    var headPic: String = "",
//    var nickname: String = "",
//    var picList: List<String> = listOf(),
//    var userId: Long= 0
//)

data class HomeRecomItem(
    var age: Int = 0,
    var city: String = "",
    var coverPicList: MutableList<String> = mutableListOf(),
    var headPic: String = "",
    var introduceVoice: String = "",
    var anchor: Boolean = false,
    var authMark: String = "",
    var living: Boolean = false,
    var mySign: String = "",
    var nickname: String = "",
    var sex: String = "",
    var tagList: List<String> = listOf(),
    var userId: Long = 0
) {

    //去重需要的重写
    override fun equals(other: Any?): Boolean {
        if (other is HomeRecomItem) {
            return this.userId == other.userId
        }
        return false
    }

    override fun hashCode(): Int {
        return userId.toInt()
    }

    var introduceVoiceLength: Int = 0
        set(value) {
            field = value
            currentPlayProcess = value
        }

    //本地字段 保留音频播放状态
    var isPlay: Boolean = false

    //本地字段 保存当前的播放进度
    var currentPlayProcess: Int = 0

    override fun toString(): String {
        return "HomeRecomItem(anchor=$anchor, authMark='$authMark', living=$living, mySign='$mySign', nickname='$nickname', userId=$userId)"
    }

}

/**
 * 邀请语音消息
 */
data class NetCallReceiveBean(var callId: Long = 0, var userIds: LongArray = longArrayOf(), var callUserId: Long = 0) : Serializable

/**
 * 语音通话开始消息
 */
data class NetCallAcceptBean(
    var callId: Long = 0,
    var startTime: Long = 0,
    var billUserId: Long = 0,
    var userIds: LongArray = longArrayOf()
) :
    Serializable

/**
 * 语音结果
 */
data class NetcallResultBean(
    var callId: Long = 0,
    //付费用户ID
    var billUserId: Long = 0,
    //通话时长(单位秒)
    var duration: Long = 0,
    //通话总鹊币
    var totalBeans: Long = 0,
    //通话结束时间
    var currentMillis: Long = 0
) : Serializable

/**
 * 通话挂断消息
 */
data class NetCallHangUpBean(
    var callId: Long = 0,
    //挂断通话的用户ID
    var hangUpId: Long = 0,
    //付费用户ID
    var billUserId: Long = 0,
    //通话时长(单位秒)
    var duration: Long = 0,
    //通话总鹊币
    var totalBeans: Long = 0,
    //结束类型
    var finishType: String = ""
) : Serializable

/**
 * 语音通话余额不足提醒
 */
data class NetCallBalanceRemindBean(
    //通话ID
    var callId: Long = 0,
    //剩余可通话时长
    var duration: Long = 0,
    //余额是否充足
    var enough: Boolean = false
) : Serializable

data class SendRoomInfo(
    var coverPic: String = "",
    var heatValue: Long = 0,
    var programId: Long = 0,
    var programName: String = "",
    var title: String = ""
) : Serializable

/**
 * 传送门接口返回值
 */
data class SendRoomBean(
    var beans: Long = 0,
    var sendRoomInfo: SendRoomInfo = SendRoomInfo()
) : Serializable


/**
 * 匿名语音基础数据
 */
data class AnonymousBasicInfo(
    //头像数据
    var headPics: MutableList<String> = mutableListOf(),
    //剩余次数
    var surplusTimes: Int = 0,
    //匹配时间，如果大于0  继续匹配状态
    var waitingSeconds: Long = 0,
    //本人头像
    var myHeadPic: String = ""
) : Serializable

/**
 * 检测余额结果
 */
data class CheckBeansData(
    //揭秘费用
    var beans: Long = 0,
    //余额是否足够
    var hasEnoughBeans: String = ""
) : Serializable
