package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * 分享实体类
 * Created by djp on 2016/11/16.
 */
open class ShareObject : Serializable {
    var shareUrl: String? = null
    var shareTitle: String? = null
    var shareContent: String? = null
    var sharePic: String? = null
    var shareId: String? = null
    var shareKey: String? = null

    // 分享成功后回传后台用
    //直播间ID
    var shareKeyId: String? = null
    var shareWay: String = "Other"
    var shareKeyType: String? = null

    var extJsonCfg: String? = null

    //来源页面
    var isFromPage: String = ""
    var shareMode: String? = null

    //4.27新增字段
    /** 小程序id **/
    var miniUserName: String = ""
    /** 分享到小程序大图 **/
    var shareBigPic: String = ""
    /** 分享类型 **/
    var touchType: String = ""
    /** 分享数据 **/
    var touchValue: String = ""
    //4.28新增字段
    /** 分享数据 **/
    var touchValue2: String = ""

    //自定义字段
    //4.19.1
    /** 保存实例化的配置信息 **/
    var config: Any? = null
    //统计
    /** 来源 **/
    var source: String? = null
    /** 节目id **/
    var programId: String? = null
    /** 分享平台 **/
    var platForm: String? = null
}

class ShareConfig : Serializable {
    var shareWay: ArrayList<String> = arrayListOf()//分享方式集合
    var playSeconds: Int? = null
    //是否启用活动面板
    var popupEnable: Boolean? = null
    //是否自动展开活动面板
    var autoExpand: Boolean? = null
    //活动面板最大高度
    var maxHeight: Int? = 0
    //活动面板H5初始化地址
    var initUrl: String? = null
}