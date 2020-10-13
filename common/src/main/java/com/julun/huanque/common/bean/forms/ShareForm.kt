package com.julun.huanque.common.bean.forms


import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.constant.BooleanType
import java.io.Serializable

/**
 * Created by djp on 2016/11/24.
 */
class ShareForm : SessionForm {
    //分享平台类型(WeChat、FriendCircle、Sina)
    var shareType: String = ""

    //分享目标类型(Banner、Room、InviteFriend、Other)
    var shareKeyType: String? = ""

    //分享目标Id ，如果是直播间，则填直播间ID, 如果是邀友，则填邀友海报ID
    var shareKeyId: String? = ""

    //分享URL
    var shareUrl: String? = ""

//    // 分享途径，微信好友，微信朋友圈， QQ好友，QQ空间等
//    // 微信好友WX_FRIENDS，微信朋友圈WX_PYQ， QQ好友: QQ_FRIENDS，QQ空间:QQ_ZONE
//    var shareWay: String = ""


    constructor(shareKey: String, shareKeyId: String?, shareType: String) {
        this.shareKeyType = shareKey
        this.shareKeyId = shareKeyId
        this.shareType = shareType
    }

    constructor(shareObject: ShareObject) {
        this.shareKeyType = shareObject.shareKeyType
        this.shareKeyId = shareObject.shareKeyId
        //ShareObject中的platForm 为此处的shareType
        this.shareType = shareObject.platForm ?: ""
        this.shareKeyId = shareObject.shareKeyId
    }
}

/**
 * 	适用模块，Invite：邀友(可选项：Invite)
 */
class SharePosterQueryForm(var applyModule: String)

/**
 * 传个图片地址 生成一个二维码返回
 */
class SharePosterImageForm(var programId: Long)

/**
 *
 */
class ShareLiveForm(var programId: Long)

enum class ShareWay {
    WXFriends,
    WXTimeline,
    QQFriends,
    QQZone,
    WXMini,
}