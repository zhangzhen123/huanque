package com.julun.huanque.common.net.interceptors

import android.os.Build
import android.text.TextUtils
import android.util.ArrayMap
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.utils.device.DeviceUtils
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.utils.NetUtils
import com.julun.huanque.common.constant.ParamConstant
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

object HeaderInfoHelper {

    /**
     * 头部签名使用的判空，和后端保持一致
     */
    fun isWebEmpty(str: String?): Boolean = (str?.length ?: 0) == 0

    /**
     * 对请求的body进行处理
     */
    fun parseBodyString(bodyString: String): String {
        var str = ""
        if (bodyString.isBlank()) return str
        val result = JSONObject.parseObject(bodyString)
        val listKey = ArrayList(result.keys)
        Collections.sort(listKey)


        var temp: String
        var value: String
        for (key in listKey) {
            value = result.getString(key)
            if (isWebEmpty(value)) {
                continue
            }
            temp = key + "=" + (URLEncoder.encode(value, "UTF-8").replace("+", "%20"))
            if ("" == str) {
                str = temp
            } else {
                str += "&" + temp
            }
        }
        return str
    }

    /**
     * 请求头iminfo转成json
     * @author WanZhiYuan
     * @date 2020/04/20
     */
    fun getMobileDeviceInfoToJson(isToJson: Boolean = true): String {
        val map = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mutableMapOf<String, String>()
        } else {
            ArrayMap<String, String>()
        }
        map["t"] = "A"
        if (SessionUtils.getSessionId().isNotEmpty()) {
            map["s"] = SessionUtils.getSessionId()
        }
        map["h"] = "${System.currentTimeMillis()}"
//        val apptype = BuildConfig.APP_TYPE
//        if (apptype.isNotEmpty()) {
//            map["a"] = apptype
//        }

        if (CommonInit.getInstance().inSDK) {
            map["a"] = "A2"
//            if (CommonInit.getInstance().getSdkType() == BusiConstant.SDKType.BAOMIHUA) {
//                map["c"] = BusiConstant.BaoMiHua
//            } else {
//                map["c"] = BusiConstant.XinLiao
//            }
        } else {
            val jAppChannelCode = (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
                .navigation() as? AppCommonService)?.getInnerChannel()
            if (jAppChannelCode?.isNotBlank() == true) {
                map["c"] = "$jAppChannelCode"
            }
        }

        val jExtraChannelCode = (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.getExternalChannel()
        if (jExtraChannelCode?.isNotBlank() == true) {
            map["e"] = "$jExtraChannelCode"
        }
        val iei = DeviceUtils.getIMEI()
        if (iei.isNotEmpty()) {
            map["x"] = iei
        }
        if (DeviceUtils.getUdid().isNotEmpty()) {
            map["i"] = SharedPreferencesUtils.getString(ParamConstant.UUID, "").toUpperCase()
        }
        val operatorId = NetUtils.getOperatorId()
        val isi = when {
            operatorId.startsWith("46000") || operatorId.startsWith("46002") || operatorId.startsWith(
                "46007"
            ) -> "M"
            operatorId.startsWith("46001") || operatorId.startsWith("46006") -> "U"
            operatorId.startsWith("46003") -> "T"
            else -> ""
        }
        if (isi.isNotEmpty()) {
            map["r"] = isi
        }
        val netStatus = when (NetUtils.getNetworkState()) { //获取当前的网络类型
            NetUtils.NETWORK_2G, NetUtils.NETWORK_MOBILE -> "2G"
            NetUtils.NETWORK_3G -> "3G"
            NetUtils.NETWORK_4G -> "4G"
            NetUtils.NETWORK_NONE -> ""
            else -> "Wifi"
        }
        if (!TextUtils.isEmpty(netStatus)) {
            map["n"] = netStatus
        }
        map["v"] = "${AppHelper.getAppVersionName()}.${BuildConfig.BUILD_NUMBER}"
        if (Build.VERSION.RELEASE.isNotEmpty()) {
            map["o"] = Build.VERSION.RELEASE
        }
        if (Build.MANUFACTURER.isNotEmpty()) {
            map["d"] = URLEncoder.encode(Build.MANUFACTURER, "UTF-8")
        }
        if (Build.MODEL.isNotEmpty()) {
            map["m"] = StringHelper.getValidUA(Build.MODEL)
        }
//        val ip = NetUtils.getDeviceIp()
//        if (!TextUtils.isEmpty(ip)) {
//            map["ip"] = ip
//        }
//        val oaid = huanqueService.getService(IMSAService::class.java)?.getHuaweiOaid()
//        if (!TextUtils.isEmpty(oaid)) {
//            map["w"] = oaid ?: ""
//        }
        return if (isToJson) {
            JSONObject.toJSONString(map)
        } else {
            StringBuilder().apply {
                map.forEach {
                    if (this.isEmpty()) {
                        append("${it.key}=${it.value}")
                    } else {
                        append("&${it.key}=${it.value}")
                    }
                }
            }.toString()
        }
    }
}