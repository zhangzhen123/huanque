package com.julun.huanque.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.permission.PermissionUtils
import java.net.Inet4Address
import java.net.NetworkInterface.getNetworkInterfaces
import java.net.SocketException


/**
 * Created by my on 2018/07/09 0009.
 */
object NetUtils {
    val NETWORK_NONE = 0 // 没有网络连接
    val NETWORK_WIFI = 1 // wifi连接
    val NETWORK_2G = 2 // 2G
    val NETWORK_3G = 3 // 3G
    val NETWORK_4G = 4 // 4G
    val NETWORK_MOBILE = 5 // 手机流量

    /**
     * 获取当前网络连接的类型
     *
     * @return int
     */
    fun getNetworkState(): Int {
        val context = CommonInit.getInstance().getApp()

        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connManager == null) {// 为空则认为无网络
            return NETWORK_NONE// 获取网络服务
        }
//        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager ?:
//                return NETWORK_NONE
        // 获取网络类型，如果为空，返回无网络
        val activeNetInfo = connManager.activeNetworkInfo
        if (activeNetInfo == null || !activeNetInfo.isAvailable) {
            return NETWORK_NONE
        }
        // 判断是否为WIFI
        val wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (null != wifiInfo) {
            val state = wifiInfo.state
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI.toInt()
                }
            }
        }
        // 若不是WIFI，则去判断是2G、3G、4G网
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = telephonyManager.networkType
        when (networkType) {
            /*
                 GPRS : 2G(2.5) General Packet Radia Service 114kbps
                 EDGE : 2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
                 UMTS : 3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
                 CDMA : 2G 电信 Code Division Multiple Access 码分多址
                 EVDO_0 : 3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
                 EVDO_A : 3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
                 1xRTT : 2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
                 HSDPA : 3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
                 HSUPA : 3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
                 HSPA : 3G (分HSDPA,HSUPA) High Speed Packet Access
                 IDEN : 2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
                 EVDO_B : 3G EV-DO Rev.B 14.7Mbps 下行 3.5G
                 LTE : 4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
                 EHRPD : 3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
                 HSPAP : 3G HSPAP 比 HSDPA 快些
                 */
            // 2G网络
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return NETWORK_2G.toInt()
            // 3G网络
            TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> return NETWORK_3G.toInt()
            // 4G网络
            TelephonyManager.NETWORK_TYPE_LTE -> return NETWORK_4G.toInt()
            else -> return NETWORK_MOBILE
        }

    }

    /**
     * 判断网络是否连接
     *
     * @param context context
     * @return true/false
     */
    fun isNetConnected(): Boolean {
        val context = CommonInit.getInstance().getApp()
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.activeNetworkInfo
            if (info != null && info.isConnected) {
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 判断是否wifi连接
     *
     * @param context context
     * @return true/false
     */
    @Synchronized
    fun isWifiConnected(): Boolean {
        val context = CommonInit.getInstance().getApp()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null) {
                val networkInfoType = networkInfo.type
                if (networkInfoType == ConnectivityManager.TYPE_WIFI || networkInfoType == ConnectivityManager.TYPE_ETHERNET) {
                    return networkInfo.isConnected
                }
            }
        }
        return false
    }

    /**
     * 获取运营商名字
     *
     * @param context context
     * @return int
     */
    fun getOperatorId(): String {
        return getImsi(CommonInit.getInstance().getApp())
    }
    @SuppressLint("WrongConstant", "MissingPermission")
    fun getImei(context: Context): String {
        var imei = ""
        if (PermissionUtils.checkSinglePermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            try {
                val tm = context.getSystemService("phone") as? TelephonyManager
                tm?.let {
                    imei = it.deviceId
                }
            } catch (var3: Exception) {
            }
        }
        return imei
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    fun getImsi(context: Context): String {
        var imsi = ""
        if (PermissionUtils.checkSinglePermission(context, android.Manifest.permission.READ_PHONE_STATE)) {
            //权限已经允许的情况进行获取运营商
            try {
                val tm = context.getSystemService("phone") as? TelephonyManager
                tm?.let {
                    imsi = it.subscriberId
                }
            } catch (var3: Exception) {
            }
        }
        return imsi
    }
    /**
     * 获取设备内网IP地址
     * @author WanZhiYuan
     * @date 2020/04/23
     * @detail 对应获取方式参考：https://www.jianshu.com/p/be244fb85a4e
     */
    fun getDeviceIp(): String {
        if (!isNetConnected()) {
            return ""
        }
        if (isWifiConnected()) {
            //WIFI
            //获取wifi服务
            val wifiManager = CommonInit.getInstance().getApp().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            //判断wifi是否开启
            wifiManager?.let {
                if (it.isWifiEnabled) {
                    val wifiInfo = it.connectionInfo
                    val ipAddress = wifiInfo.ipAddress
                    return intToIp(ipAddress)
                }
            }
        } else {
            //GPRS
            try {
                val en = getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
//                        if (!inetAddress.isLoopbackAddress) {
//                            //获取ipv6的地址
//                            return inetAddress.hostAddress.toString()
//                        }
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            //获取ipv4的地址
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
                reportCrash("获取GPRS的IP地址失败",ex)
            }
        }
        return ""
    }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }

}