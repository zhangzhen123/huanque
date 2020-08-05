package com.julun.huanque.common.bean.beans

import android.graphics.Bitmap
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.constant.ShareFromModule
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

    //大图分享
    var shareImage: Bitmap? = null

    //多图分享
    var imageList: MutableList<String> = mutableListOf()
    var videoUrl: String = ""

    // 分享成功后回传后台用
    //直播间ID
    var shareKeyId: String? = null

    var shareWay: String = "Other"

    //分享的类型 图片 文本 还是音乐 视频  小程序
    var shareType: String = ""
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

data class SharePosterInfo(
    var inviteCode: String = "",//邀请码
    var posterList: MutableList<SharePoster> = mutableListOf()
)


data class SharePoster(
    var applyModule: String = "",
    var paramsStyle: String = "",
    var posterId: Int = 0,
    var posterPic: String = "",//背景图片
    var posterTitle: String = "",
    var qrCode: String = ""//二维码图片
) : MultiItemEntity {
    var inviteCode: String = ""
    //本地字段
    var authorName:String=""
    //
    var qrBitmap:Bitmap?=null
    override val itemType: Int
        get() = when (applyModule) {
            ShareFromModule.Invite -> 1
            ShareFromModule.Program -> 2
            ShareFromModule.Magpie -> 3
            else -> 1
        }
}

class ShareType {
    var type: String = ""
    var title: String = ""
    var url: String = ""
    var res: Int = -1
}
