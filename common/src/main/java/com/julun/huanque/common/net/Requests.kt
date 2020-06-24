package com.julun.huanque.common.net

import android.util.Log
import com.julun.huanque.common.net.interceptors.JulunRequestHeaderWrapInterceptor
import com.julun.huanque.common.utils.ULog
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.converter.FastJsonConverterFactory
import com.julun.huanque.common.helper.AppHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Requests {
    private val RETROFIT: Retrofit by lazy {
        Retrofit.Builder()
            .client(getClient())//设置客户端
            .baseUrl(CommonInit.getInstance().getBaseUrl())//请求url
//            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(FastJsonConverterFactory.create())//json解析
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())//rxjava支持
            .apply { ULog.i("RETROFIT 初始化了:${CommonInit.getInstance().getBaseUrl()}") }
            .build()
    }


    fun <T> create(targetClass: Class<T>): T = RETROFIT.create(targetClass)


    //默认的请求超时时间  2019/4/26 素素要求  接口超时时间修改为15秒
    private const val DEFAULT_TIMEOUT = 15L

    //图片的默认请求时长
    private const val IMG_DEFAULT_TIMEOUT = 15L

    //3秒请求超时时间
    private const val DEFAULT_SHORT_TIMEOUT = 1L

    //http数据日志工具
    private val logger: HttpLogger by lazy { HttpLogger() }

    fun getClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(JulunRequestHeaderWrapInterceptor())    //自定义的拦截器
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor(logger).setLevel(HttpLoggingInterceptor.Level.BODY))           //日志拦截器
                }
                if (AppHelper.isStethoPresent()) {
                    addNetworkInterceptor(StethoInterceptor())
                }
            }

            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build()

    fun getShortClient() = OkHttpClient.Builder()
        .addInterceptor(JulunRequestHeaderWrapInterceptor())    //自定义的拦截器
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))           //日志拦截器
            }
            if (AppHelper.isStethoPresent()) {
                addNetworkInterceptor(StethoInterceptor())
            }
        }
        .connectTimeout(DEFAULT_SHORT_TIMEOUT, TimeUnit.SECONDS).build()

    fun getImageClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(IMG_DEFAULT_TIMEOUT, TimeUnit.SECONDS).build()
}

class HttpLogger : HttpLoggingInterceptor.Logger {
    override fun log(message: String?) {
        println("OkHttp", message ?: "")
    }

    private fun println(tag: String, msg: String) {
        var msg = msg
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //把4*1024的MAX字节打印长度改为2001字符数
        val max_str_length = 3 * 1024 - tag.length
        //大于4000时
        while (msg.length > max_str_length) {
            Log.d(tag, msg.substring(0, max_str_length))
            msg = msg.substring(max_str_length)
        }
        //剩余部分
        Log.d(tag, msg)
    }
}