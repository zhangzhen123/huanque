package com.julun.huanque.common.bean.beans

import android.graphics.Bitmap
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.constant.ShareFromModule
import java.io.Serializable

/**
 * 分享实体类
 * Created by djp on 2016/11/16.
 *  注意shareWay shareType 必须赋值 不然不知道分享类型 和 分享方式
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

    /** 分享到小程序/微博大图 **/
    var bigPic: String = ""

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
    var userId: String? = null

    /** 分享平台 (本地传递使用)**/
    var platForm: String? = null

    //动态ID(分享动态时使用)
    var postId: Long? = null

    //评论Id(分享动态时使用)
    var commentId: Long? = null

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
    var qrCode: String = "",//二维码图片
    var qrCodeBase64: String = ""//新版使用直接返回的二维码图片实体
) : MultiItemEntity {
    var inviteCode: String = ""

    //本地字段
    var authorName: String = ""

    //
    var qrBitmap: Bitmap? = null
    override val itemType: Int
        get() = when (applyModule) {
            ShareFromModule.Invite -> 1
            ShareFromModule.Program -> 2
            ShareFromModule.Magpie -> 3
            else -> 1
        }
}

/**
 * 动态分享对象
 */
data class PostShareBean(
    //内容
    var content: String = "",
    //头像
    var headPic: String = "",
    //昵称
    var nickname: String = "",
    //图片
    var pic: String = "",
    //动态Id
    var postId: Long = 0,
    //二维码
    var qrCode: String = "",
    //分享的链接
    var shareUrl: String = "",
    //用户Id
    var userId: Long = 0,
    //分享类型(本地字段)
    var shareType: String = "",


//评论特色数据
    //评论内容
    var commentContent: String = "",
    //发表评论的用户昵称
    var commentUserName: String = "",
    //二维码bitmap
    var qrBitmap: Bitmap? = null
) : Serializable

class ShareType {
    var type: String = ""
    var title: String = ""
    var url: String = ""
    var res: Int = -1
}
