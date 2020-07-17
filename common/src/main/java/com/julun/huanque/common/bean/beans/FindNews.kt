package com.julun.huanque.common.bean.beans

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
