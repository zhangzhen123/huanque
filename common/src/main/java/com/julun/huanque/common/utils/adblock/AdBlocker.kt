package com.julun.huanque.common.utils.adblock

import android.text.TextUtils
import android.util.Log
import android.webkit.WebResourceResponse
import com.julun.huanque.common.suger.logger
import java.io.ByteArrayInputStream
import java.net.MalformedURLException
import java.util.*

object AdBlocker {
    /**
     * 广告黑名单  国外有专门的收集 国内找不到 汗  暂时只收集这几个
     */
    private val AD_HOSTS = HashSet<String>().apply {
        add("m.4832880.com")
        add("a.ucoz.net")
        add("a.ucoz.ru")
        add("www.9945.com")
        add("www.vip.com")
        add("www.nat123.com")
        add("s.lianmeng.360.cn")
        add("images.sohu.com")
        add("union.sogou.com")
        add("sogou.com")

        add("a.baidu.com")
        add("c.baidu.com")
        add("cbjs.baidu.com")
        add("pos.baidu.com")
        add("cpro.baidu.com")
        add("nsclick.baidu.com")
        add("baidustatic.com")
        add("cnzz.com")


    }
    fun isAd(url: String): Boolean {
        return try {
            val host= UrlUtils.getHost(url)
            logger("当前的域名：$host")
            isAdHost(host)
        } catch (e: MalformedURLException) {
            Log.d("AdBlocker", e.toString())
            false
        }

    }

    private fun isAdHost(host: String): Boolean {
        if (TextUtils.isEmpty(host)) {
            return false
        }
        val index = host.indexOf(".")
        return index >= 0 && (AD_HOSTS.contains(host) || index + 1 < host.length && isAdHost(host.substring(index + 1)))
    }

    fun createEmptyResource(): WebResourceResponse {
        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
    }

}