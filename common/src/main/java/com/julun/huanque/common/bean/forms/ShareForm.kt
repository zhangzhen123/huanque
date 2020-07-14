package com.julun.huanque.common.bean.forms


import com.julun.huanque.common.bean.beans.ShareObject
import java.io.Serializable

/**
 * Created by djp on 2016/11/24.
 */
class ShareForm : SessionForm {
    var shareKeyType: String? = ""
    var shareKeyId: String? = ""
    // 分享途径，微信好友，微信朋友圈， QQ好友，QQ空间等
    // 微信好友WX_FRIENDS，微信朋友圈WX_PYQ， QQ好友: QQ_FRIENDS，QQ空间:QQ_ZONE
    var shareWay: String = ""
    var shareUrl: String? = ""

    constructor(shareKey: String, shareKeyId: String?, shareWay: String) {
        this.shareKeyType = shareKey
        this.shareKeyId = shareKeyId
        this.shareWay = shareWay
    }

    constructor(shareObject: ShareObject) {
        this.shareKeyType = shareObject.shareKeyType
        this.shareKeyId = shareObject.shareKeyId
        this.shareWay = shareObject.shareWay
    }
}


enum class ShareWay {
    WXFriends,
    WXTimeline,
    QQFriends,
    QQZone,
    WXMini
}