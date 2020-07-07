package com.julun.huanque.common.bean.beans

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.basic.RootListData

class HomeItemBean(var showType: Int, var content: Any) : MultiItemEntity {
    companion object {
        const val HEADER = 1//头部
        const val NORMAL = 2//普通样式
        const val GUIDE_TO_ADD_TAG = 3//引导添加标签
        const val GUIDE_TO_COMPLETE_INFORMATION = 4//引导完善资料
    }

    override fun getItemType(): Int {
        return showType
    }
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
    var moduleList: List<HeadModule> = listOf(),
    var myCash: Int = 0
)

data class HeadModule(
    var baseInfo: HeadBaseInfo = HeadBaseInfo(),
    var bgPic: String = "",
    var moduleName: String = "",
    var moduleType: String = ""
) {
    companion object {
        const val MaskQueen = "MaskQueen"
        const val AnonymousVoice = "AnonymousVoice"
        const val MagpieParadise = "MagpieParadise"
        const val HotLive = "HotLive"
        const val PlumFlower = "PlumFlower"
    }
}

data class HeadBaseInfo(
    var headPic: String = "",
    var hotValue: Int = 0,
    var joinNum: Int = 0,
    var nickname: String = "",
    var programList: List<Any> = listOf(),
    var remainTimes: Int = 0
)

//class HomeListData(isPull: Boolean = false, hasMore: Boolean = false, extDataJson: String? = null) :
//    RootListData<HomeRecomItem>(isPull, arrayListOf(), hasMore, extDataJson) {
//    var moduleList: List<HeadModule> = arrayListOf()
//}
class HomeListData<T> : RootListData<T>() {
    var moduleList: List<HeadModule> = arrayListOf()
}

data class HomeRecomItem(
    var age: Int = 0,
    var city: String = "",
    var coverPicList: List<String> = listOf(),
    var headPic: String = "",
    var introduceVoice: String = "",
    var introduceVoiceLength: Int = 0,
    var anchor: Boolean = false,
    var authMark:String="",
    var living: Boolean = false,
    var mySign: String = "",
    var nickname: String = "",
    var sex: String = "",
    var tagList: List<String> = listOf(),
    var userId: Int = 0
) {
    //本地字段 保留音频播放状态
    var isPlay: Boolean = false
    //本地字段 保存当前的播放进度
    var currentPlayProcess: Int = introduceVoiceLength
}
