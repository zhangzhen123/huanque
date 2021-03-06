package com.julun.huanque.common.bean.beans

import com.julun.huanque.common.basic.RootListData
import java.io.Serializable

data class ChatDetailBean(
    //拉黑状态
    var blacklistSign: Boolean = false,
    //背景所需要等级
    var chatBackgroundLevel: Int = 0,
    //用户ID
    var friendId: Long = 0,
    //头像
    var headPic: String = "",
    //当前亲密度等级
    var intimateLevel: Int = 0,
    //欢遇状态
    var meetStatus: String = "",
    //昵称
    var nickname: String = "",
    //签名
    var mySign: String = "签名不足以表达我自己，找我私聊吧~",
    var remindSign: String = ""
) :
    Serializable

/**
 * 道具列表
 */
data class PropListBean(
    var propList: MutableList<PrivateProp> = mutableListOf(),
    //聊天券数量
    var chatTicketCnt: Int = 0,
    //语音券数量
    var voiceTicketCnt: Int = 0
) : Serializable

/**
 * 首页对象派单
 */
data class ChatRoomBean(
    //派单未回复数量
    var fateNoReplyNum: Int = 0,
    //在线状态
    var onlineStatus: String = "",
    //显示缘分入口
    var showFate: String = "",
    //广告列表
    var adList: MutableList<AdInfoBean>? = null,
    //是否有分身账号
    var hasSubAccount: String = "",
    //是否有分身账号未读消息
    var hasSubAccountMsg: String = "",
    //心动数量
    var heartTouchCnt: Int = 0,
    //心动的头像列表
    var heartTouchHeadPicList: MutableList<String> = mutableListOf(),
    //新增访客数量
    var visitCnt: Int = 0
) : Serializable {
    companion object {
        //在线
        const val Online = "Online"

        //隐身
        const val Invisible = "Invisible"
    }
}

/**
 * 派单数据
 */
data class FateInfo(
    //年龄
    var age: Int = 0,
    //城市
    var city: String = "",
    //订单Id
    var fateId: String = "",
    //匹配时间
    var matchTime: String = "",
    //用户头像
    var headPic: String = "",
    //用户昵称
    var nickname: String = "",
    //贵族等级
    var royalPic: String = "",
    //性别：男(M)、女(F)(可选项：Male、Female、Unknow)
    var sexType: String = "",
    var userId: Long = 0,
    //用户等级
    var userLevel: Int = 0,
    //当前状态
    var status: String = "",
    //是否是新用户
    var newUser: String = "",
    //用户标签
    var userTag: String = "",
    //是否显示快捷回复
    var quickChat: String = "",
    //倒计时
    var ttl: Int = 0
) : Serializable {
    companion object {
        //等待回复
        const val Wait = "Wait"

        //准时回复
        const val Finish = "Finish"

        //超时回复
        const val Timeout = "Timeout"
    }
}

class FateListInfo<T> : RootListData<T>() {
    var extData: ExtDataBean? = null
}


data class ExtDataBean(
    //规则图片地址
    var ruleUrl: String = ""
) : Serializable

class TodayFateInfo {
    var fateList: MutableList<TodayFateItem> = mutableListOf()
    var originalPrice: Long = 0
    var showTodayFate: Boolean = false
    var unitPrice: Long = 0
    var beans: Long = 0
}

data class TodayFateItem(
    //年龄
    var age: Int = 0,
    //城市
    var city: String = "",
    //订单Id
    var userId: String = "",
    //匹配时间
    var matchTime: String = "",
    //用户头像
    var headPic: String = "",
    //用户昵称
    var nickname: String = "",
    //性别：男(M)、女(F)(可选项：Male、Female、Unknow)
    var sex: String = "Female",

    var authMark: String = "",
    var select: Boolean = true

)

data class QuickAccostResult(
    var msgList: List<AccostMsg> = listOf()
)

data class AccostMsg(
    var consumeBeans: Long = 0L,
    var content: String = "",
    var contentType: String = "",
    var relationId: String = "",
    var stranger: Boolean = false,
    var targetUserInfo: TargetUserObj = TargetUserObj(),
    var gift: ChatGift = ChatGift()
)

data class AccostTargetUserInfo(
    var fee: Long = 0L,
    var headPic: String = "",
    var intimateLevel: Int = 0,
    var meetStatus: String = "",
    var nickname: String = "",
    var sexType: String = "",
    var stranger: Boolean = false,
    var userId: Long = 0L
)

/**
 * 常用语对象
 */
data class SingleUsefulWords(
    //内容
    var words: String = "",
    //常用语ID
    var wordsId: Long = 0
) : Serializable

/**
 * 派单数据  单日
 */
data class FateState(
    //统计日期
    var dateStr: String = "",
    //缘分总次数
    var totalNum: Long = 0,
    //准备回复数
    var replyNum: Long = 0
) : Serializable

/**
 * 派单周数据
 */
data class FateWeekInfo(
    //结果
    var result: String = "",
    //缘分总次数
    var totalNum: Long = 0,
    //准时回复次数
    var replyNum: Long = 0,
    //近7日详情
    var itemList: MutableList<FateState> = mutableListOf()
) : Serializable
