package com.julun.huanque.support

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.julun.huanque.R
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.DynamicChangeResult
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.beans.StatusResult
import com.julun.huanque.common.bean.events.ShareSuccessEvent
import com.julun.huanque.common.bean.forms.PostShareForm
import com.julun.huanque.common.bean.forms.ShareForm
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.WeiBoShareType
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.RequestCaller
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.nothing
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.sina.weibo.sdk.share.WbShareCallback
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


object WeiBoApiManager : RequestCaller {
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
                val textMessage = TextObject()
                textMessage.text = shareObj.shareContent
                message.textObject = textMessage

            }
            WeiBoShareType.WbWeb -> {
                val webObject = WebpageObject()
                webObject.identify = UUID.randomUUID().toString()
                webObject.title = shareObj.shareTitle
                webObject.description = shareObj.shareContent
                val bitmap: Bitmap =
                    shareObj.shareImage ?: BitmapFactory.decodeResource(CommonInit.getInstance().getContext().resources, R.mipmap.ic_launcher)
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
//                val textObj = TextObject()
//                textObj.text = shareObj.shareContent
//                message.textObject = textObj
                mShareClientOnly = false
                //单纯分享链接太单调 这里带上相关图片
                if (shareObj.bigPic.isEmpty()) {
                    val imageObject = ImageObject()
                    imageObject.setImageData(bitmap)
                    message.imageObject = imageObject
                } else {
                    loadNetworkImg(shareObj.bigPic, message, mShareClientOnly)
                    return
                }

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

    private fun loadNetworkImg(
        url: String,
        message: WeiboMultiMessage,
        mShareClientOnly: Boolean
    ) {
        var shareClientOnly = mShareClientOnly
        ImageUtils.requestImageForBitmap(url, {
            //如果btimap 被回收了,那么可能造成的是发送的消息里没有图片缩略图
            if (it != null && !it.isRecycled) {
                //不为空,还不能是回收掉的
                val imageObject = ImageObject()
                imageObject.setImageData(it)
                message.imageObject = imageObject

                //Android9.0以上只使用客户端分享
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    shareClientOnly = true
                    if (wbApi?.isWBAppInstalled == false) {
                        ToastUtils.show("请先安装微博客户端再分享")
                        return@requestImageForBitmap
                    }

                }

                wbApi?.shareMessage(message, shareClientOnly)
            }

        }, R.mipmap.ic_launcher)

    }

    fun shareResult(data: Intent) {
        wbApi?.doResultIntent(data, object : WbShareCallback {
            override fun onComplete() {
                logger("分享成功")
                ToastUtils.show("分享成功")
                if (currentObject?.postId != null) {
                    //分享动态或者评论
                    Requests.create(SocialService::class.java)
                        .saveShareLog(PostShareForm(currentObject?.platForm ?: "", currentObject?.postId ?: 0, currentObject?.commentId))
                        .handleResponse(makeSubscriber<StatusResult>() {
                            if (it.status == BusiConstant.True) {
                                EventBus.getDefault().post(ShareSuccessEvent(currentObject?.postId ?: 0, currentObject?.commentId))
                            }
                        })

                } else {
                    Requests.create(UserService::class.java).shareLog(ShareForm(currentObject ?: return)).nothing()
                }

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

    override fun getRequestCallerId() = StringHelper.uuid()

}