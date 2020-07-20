package com.julun.huanque.support

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.app.HuanQueApp
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.OrderInfo
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.forms.BindForm
import com.julun.huanque.common.bean.forms.ShareWay
import com.julun.huanque.common.constant.ByteConstants
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.net.SocketTimeoutException


/**
 * Created by djp on 2016/11/26.
 */
object WXApiManager {

    //来自于动态页的分享
    private var mIsFromDynamic: Boolean = false

    private fun checkWXInstalled(context: Activity, checkPay: Boolean = false): Boolean {
        if (HuanQueApp.wxApi?.isWXAppInstalled == true) {
            if (checkPay) {
                if ((HuanQueApp.wxApi?.wxAppSupportAPI
                                ?: 0) >= Build.PAY_SUPPORTED_SDK_INT) {
                    //判断微信当前版本是否支持微信支付
                    return true
                }
            } else if ((HuanQueApp.wxApi?.wxAppSupportAPI
                            ?: 0) >= Build.TIMELINE_SUPPORTED_SDK_INT) {
                //判断微信当前版本是否过低
                return true
            }
            ToastUtils.show(context.getString(R.string.weixin_version_is_low))
        } else {
            ToastUtils.show(context.getString(R.string.weixin_isnot_installed))
        }
        return false
    }

    /**
     * 微信组件初始化

     * @param context
     * *
     * @param wx_app_id
     * *
     * @return 微信组件api对象
     */
    fun initWeiXin(context: Context, wx_app_id: String): IWXAPI {
        if (TextUtils.isEmpty(wx_app_id)) {
            ToastUtils.show("app_id不能为空")
        }
        val api = WXAPIFactory.createWXAPI(context, wx_app_id, false)
        api.registerApp(wx_app_id)
        return api
    }

    // 微信授权登录
    fun doLogin(context: Activity) {
        if (checkWXInstalled(context)) {
            //发送授权登录信息，来获取code
            val req = SendAuth.Req()
            //应用作用域，获取个人信息
            req.scope = "snsapi_userinfo"
            /**
             * 用于保持请求和回调的状态，授权请求后原样带回给第三方。
             * 该参数可用于防止csrf攻击（跨站请求伪造攻击），
             * 建议第三方带上该参数，可设置为简单的随机数加session进行校验
             */
            val sessionId = SessionUtils.getSessionId()
            val randomStr = Math.round(Math.random() * 1000000).toString()
            if (TextUtils.isEmpty(sessionId)) {
                req.state = System.currentTimeMillis().toString() + "" + randomStr
            } else {
                req.state = sessionId + "" + randomStr
            }
            println("登录微信，发送数据：" + req.state)
//            SessionUtils.setWeiXinUse(true)
            HuanQueApp.wxApi?.sendReq(req)
        }
    }

    // 微信支付
    fun doPay(context: Activity, orderInfo: OrderInfo) {
        if (checkWXInstalled(context, true)) {
            val request = PayReq()
            request.appId = orderInfo.appid
            request.partnerId = orderInfo.partnerid
            request.prepayId = orderInfo.prepayid
            request.packageValue = orderInfo.`package`
            request.nonceStr = orderInfo.noncestr
            request.timeStamp = orderInfo.timestamp
            request.sign = orderInfo.sign
//            SessionUtils.setWeiXinUse(true)
            HuanQueApp.wxApi?.sendReq(request)
        }
    }

    // 微信分享
    fun doShare(context: Activity, scene: Int, shareObj: ShareObject) {
        if (checkWXInstalled(context)) {
            shareObject = shareObj
            if (scene == SendMessageToWX.Req.WXSceneSession) {
                shareObject?.shareWay = ShareWay.WXFriends.name
            } else {
                shareObject?.shareWay = ShareWay.WXTimeline.name
            }
            val webpage = WXWebpageObject()
            webpage.webpageUrl = shareObj.shareUrl

            val msg = WXMediaMessage(webpage)
            msg.title = shareObj.shareTitle
            msg.description = shareObj.shareContent
//            SessionUtils.setWeiXinUse(true)
            // 没有给分享图片，就用app icon
            if (TextUtils.isEmpty(shareObj.sharePic)) {
                loadLocalImg(context, msg, scene)
            } else {
                loadNetworkImg(context, msg, scene, shareObj.sharePic ?: "")
            }
        }
    }

    /**
     * 分享小程序
     * 小程序目前只支持发送到微信
     */
    fun shareToMini(context: Activity, shareObj: ShareObject) {
        if (shareObj.touchType != "Mini") {
            //做一次兼容，分享到微信
            doShare(context, SendMessageToWX.Req.WXSceneSession, shareObj)
            return
        }
        //小程序
        if (checkWXInstalled(context)) {
            shareObject = shareObj
            shareObject?.shareWay = ShareWay.WXFriends.name
            val miniProgramObj = WXMiniProgramObject()
            // 兼容低版本的网页链接
            miniProgramObj.webpageUrl = shareObj.shareUrl
            // 正式版:0，测试版:1，体验版:2
            miniProgramObj.miniprogramType = if (BuildConfig.DEBUG) {
                WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW
            } else {
                WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
            }
            miniProgramObj.withShareTicket = true
            // 小程序原始id
            miniProgramObj.userName = shareObj.miniUserName
            //小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
            miniProgramObj.path = shareObj.touchValue2
//            miniProgramObj.path = if (GlobalUtils.checkLoginNoJump()) {
//                "${shareObj.touchValue}?roomId=${shareObj.programId}&dd=${SessionUtils.getUserId()}"
//            } else {
//                "${shareObj.touchValue}?roomId=${shareObj.programId}"
//            }
            val msg = WXMediaMessage(miniProgramObj)
            // 小程序消息title
            msg.title = shareObj.shareTitle
            // 小程序消息desc
            msg.description = shareObj.shareContent
            // 小程序消息封面图片，小于128k
            ImageUtils.requestImageForBitmap(shareObj.shareBigPic, {
                if (it != null && !it.isRecycled) {
                    msg.thumbData = BitmapUtil.bitmap2AssignBytes(it, ByteConstants.BYTE_KB_FORM_128)
                }
                val req = SendMessageToWX.Req()
                req.transaction = "miniProgram"
                req.message = msg
                req.scene = SendMessageToWX.Req.WXSceneSession
                if (HuanQueApp.wxApi?.sendReq(req) == false) {
                    ToastUtils.showErrorMessage(context.getString(R.string.not_open_wx_share))
                }
            }, R.mipmap.ic_launcher)
        }
    }

    /**
     * 唤起小程序
     * @author WanZhiYuan
     * @param miniId 小程序id
     * @param path 小程序路径
     */
    fun awakenMini(context: Activity, miniId: String, path: String) {
        if (checkWXInstalled(context)) {
            val req = WXLaunchMiniProgram.Req()
            // 填小程序原始id
            req.userName = miniId
            //拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
            req.path = path
            // 可选打开 开发版，体验版和正式版
            req.miniprogramType = if (BuildConfig.DEBUG) {
                WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW
            } else {
                WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
            }
            HuanQueApp.wxApi?.sendReq(req)
        }
    }

    private fun loadLocalImg(context: Context, msg: WXMediaMessage, scene: Int) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        msg.thumbData = BitmapUtil.bitmap2Bytes(bitmap)
        bitmap.recycle()
        sendToWX(context, msg, scene)
    }

    private fun loadNetworkImg(context: Context, msg: WXMediaMessage, scene: Int, url: String) {
        ImageUtils.requestImageForBitmap(url, {
            //如果btimap 被回收了,那么可能造成的是发送的消息里没有图片缩略图
            if (it != null && !it.isRecycled) {      //不为空,还不能是回收掉的
                msg.thumbData = BitmapUtil.bitmap2Bytes(it)
//                it.recycle()      //这里不需要手工回收,参考调用的方法 ImageUtils.requestImageForBitmap 内部的注释说明
            }
            sendToWX(context, msg, scene)
        }, R.mipmap.ic_launcher)

    }

    private fun sendToWX(context: Context, msg: WXMediaMessage, scene: Int) {
        val req = SendMessageToWX.Req()
        req.message = msg
        req.scene = scene
        if (HuanQueApp.wxApi?.sendReq(req) == false) {
            ToastUtils.showErrorMessage(context.getString(R.string.not_open_wx_share))
        }
    }

    private var shareObject: ShareObject? = null

    fun getShareObject(): ShareObject {
        return shareObject ?: ShareObject()
    }
}