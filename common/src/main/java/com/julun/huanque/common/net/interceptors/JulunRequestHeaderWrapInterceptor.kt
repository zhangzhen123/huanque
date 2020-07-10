package com.julun.huanque.common.net.interceptors

import com.julun.huanque.common.R
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.MD5Util
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.NetUtils
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import java.io.IOException
import java.nio.charset.Charset

/**
 * 请求前添加固定的head
 * Created by nirack on 16-10-22.
 */
class JulunRequestHeaderWrapInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

//        if (!NetUtils.isNetConnected()) {
//            ToastUtils.show(CommonInit.getInstance().getApp().resources.getString(R.string.network_not_available))
//        }
        //new request
        var request: Request = chain.request()
        val body = request.body()
        var strBody = ""

        if (body is MultipartBody) {//文件流body不处理

        } else {
            body?.let {
                val buffer = Buffer()
                it.writeTo(buffer)

                var charset: Charset? = Charset.forName("UTF-8")
                val contentType = it.contentType()
                if (contentType != null) {
                    charset = contentType!!.charset(charset)
                }
                strBody = buffer.readString(charset!!)
                strBody = HeaderInfoHelper.parseBodyString(strBody)
            }
        }

        val lmInfoParams = HeaderInfoHelper.getMobileDeviceInfoToJson(false)
        val httpUrl: HttpUrl = request.url().newBuilder()
                .build()
        val value = "$lmInfoParams#$strBody#${BusiConstant.API_KEY}"
        val info = MD5Util.EncodePassword(value)
        request = request.newBuilder()
                .addHeader(BusiConstant.REQUEST_HEADER_FLAG, "G=$info&$lmInfoParams")
//                .addHeader("JVer", huanqueApp.APP_VERSION)
//                .addHeader("JCTYPE", "ANDROID")
//                .addHeader("JSID", SessionUtils.getSessionId())
//                .addHeader("JPatch", "${SessionUtils.getPatch()}")
                .apply {
                    //                    val cookies = OpenInstallManager.getChannelParams()
//                    if (cookies.isNotBlank()) {
//                        addHeader("cookie", cookies)
//                    }


                }
                .url(httpUrl).build()

        var proceed: Response? = null
        try {
            proceed = chain.proceed(request)
        } catch (e: Exception) {
//            e.printStackTrace()
//            reportCrash("处理网络请求的时候发生了错误,请求url ==> ${request.url()}",e)
            //此处网络无法链接,应该提示链接网络
//            ToastUtils.show("亲，网络不给力呢!")
//            throw ConfigException("网络异常$request", e)
            //不抛出异常,上报错误
            return Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(408)
                    .message("Unsatisfiable Request ${e.message}")
                    .body(object : ResponseBody() {
                        override fun contentType(): MediaType? = null

                        override fun contentLength(): Long = 0

                        override fun source(): BufferedSource = Buffer()
                    })
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build()

        }
        return proceed!!
    }
}