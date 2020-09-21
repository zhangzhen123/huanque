package com.julun.huanque.support

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.forms.ShareForm
import com.julun.huanque.common.constant.WeiBoShareType
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.nothing
import com.julun.huanque.common.utils.ToastUtils
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.sina.weibo.sdk.share.WbShareCallback
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


object WeiBoApiManager {
    //在微博开发平台为应用申请的App Key
    private const val APP_KEY = "1153419351"

    //在微博开放平台设置的授权回调页
    private const val REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"

    //在微博开放平台为应用申请的高级权限
    private const val SCOPE = ("all")

    private var wbApi: IWBAPI? = null
    private var currentObject: ShareObject? = null
    fun doWeiBoShare(activity: Activity, shareObj: ShareObject) {
        currentObject = shareObj
        wbApi = WBAPIFactory.createWBAPI(activity)
        val authInfo = AuthInfo(activity, APP_KEY, REDIRECT_URL, SCOPE)
        wbApi?.registerApp(activity, authInfo)

        val message = WeiboMultiMessage()
        var mShareClientOnly = false
        when (shareObj.shareType) {
            WeiBoShareType.WbText -> {
                val textObject = TextObject()
                var text = shareObj.shareContent
                // 分享文字
                textObject.text = text
                message.textObject = textObject

            }
            WeiBoShareType.WbImage -> {
                val imageObject = ImageObject()
                val bitmap: Bitmap = shareObj.shareImage ?: return
                imageObject.setImageData(bitmap)
                message.imageObject = imageObject

            }
            WeiBoShareType.WbWeb -> {
                val webObject = WebpageObject()
                webObject.identify = UUID.randomUUID().toString()
                webObject.title = shareObj.shareTitle
                webObject.description = shareObj.shareContent
                val bitmap: Bitmap =
                    BitmapFactory.decodeResource(CommonInit.getInstance().getContext().resources, R.mipmap.ic_launcher)
                var os: ByteArrayOutputStream? = null
                try {
                    os = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os)
                    webObject.thumbData = os.toByteArray()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        os?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                webObject.actionUrl = shareObj.shareUrl
                webObject.defaultText = shareObj.shareTitle
                message.mediaObject = webObject
                mShareClientOnly = true
            }
            WeiBoShareType.WbVideo -> {
                // 分享视频
                val videoObject = VideoSourceObject()
                videoObject.videoPath = Uri.fromFile(File(shareObj.videoUrl))
                message.videoSourceObject = videoObject
                mShareClientOnly = true
            }
            WeiBoShareType.WbMultiImage -> {
                // 分享多图
                val multiImageObject = MultiImageObject()
                val list = arrayListOf<Uri>()
                shareObj.imageList.forEach {
                    list.add(Uri.parse(it))
                }
//                list.add(Uri.fromFile(File(getExternalFilesDir(null).toString() + "/aaa.png")))

                multiImageObject.imageList = list
                message.multiImageObject = multiImageObject
                //多图分享只能客户端
                mShareClientOnly = true
            }
        }
        //Android9.0以上只使用客户端分享
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mShareClientOnly = true
            if (wbApi?.isWBAppInstalled == false) {
                ToastUtils.show("请先安装微博客户端再分享")
                return
            }

        }

        wbApi?.shareMessage(message, mShareClientOnly)


    }

    fun shareResult(data: Intent) {
        wbApi?.doResultIntent(data, object : WbShareCallback {
            override fun onComplete() {
                logger("分享成功")
                ToastUtils.show("分享成功")
                Requests.create(UserService::class.java).shareLog(ShareForm(currentObject ?: return)).nothing()
            }

            override fun onCancel() {
                logger("分享取消")
                ToastUtils.show("微博分享取消")
            }


            override fun onError(p0: UiError?) {
                logger("分享错误")
                ToastUtils.show("分享错误 请检查是否安装了微博客户端")
            }

        });
    }

}