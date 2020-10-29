package com.julun.huanque.common.bean.beans

import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.File
import java.io.Serializable

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/17 14:21
 *
 *@Description: FindNews
 *
 */
data class FindNewsResult(
    var version: CheckVersionResult? = null
    , var refreshStartAds: Boolean = true
    //首页标签信息  有变化时会返回
    , var homeCategory: HomePageTab? = null
) : Serializable

class CheckVersionResult(

    var localFile: File? = null
    , var startDownload: Boolean = false
    //安装地址
    , var installUrl: String? = null
    //MD5码
    , var md5Code: String? = null
    //更新内容
    , var updateContent: String? = null
    //新的版本号
    , var newVersion: String = ""
    , var updateType: String = "None"
) : Serializable

/**
 * 首页节目栏标签
 */
class HomePageTab:Serializable {
    var homeCategories: List<ProgramTab> = arrayListOf()
    var latestHomeCategoryVersion: String = ""
}
data class ProgramTab(
    var typeName: String = "",
    var typeCode: String = "") : Serializable

data class OnlineInfo(
    var onlineId: String = ""
)