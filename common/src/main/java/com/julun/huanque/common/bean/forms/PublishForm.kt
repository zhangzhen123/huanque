package com.julun.huanque.common.bean.forms

import com.luck.picture.lib.entity.LocalMedia


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/25 16:20
 *
 *@Description: PublishForm
 *
 */
open class PublishStateForm  {
    var groupId: Long? = null
    var city: String? = null
    var lng: Double? = null
    var lat: Double? = null
    var anonymous: String = ""
    var content: String = ""//
    var pics: String = ""//图片地址，多个以英文逗号分隔
    var picSize: String? = null //	图片尺寸，宽高以英文逗号分隔 ，一张图时传递值
}

class PublishVideoForm : SessionForm() {
    var content: String = ""//
    var url: String = ""//视频地址
    var pic: String = ""//图片地址，多个以英文逗号分隔
    var picSize: String = "" //封面尺寸，宽高以英文逗号分隔
    var duration: Long = 0 //视频时长 单位 秒
}

class CheckForm(var content: String)
