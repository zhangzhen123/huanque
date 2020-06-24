package com.julun.huanque.common.net.converter


import com.julun.huanque.common.bean.Root
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ToastUtils
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.SocketTimeoutException

/**
 * Created by nirack on 16-10-22.
 */

internal class FastJsonResponseBodyConverter<T>(private val type: Type) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        try {
            val jsonString: String = value.string()
//            println("解析数据  类型: $type , 字符串为:  $jsonString")
            var obj = JsonUtil.deserializeAsObject<T>(jsonString, type)
            if(obj != null && obj is Root<*>){
                if(type is ParameterizedType){
                    val rawType: Type = type.actualTypeArguments[0]
                    obj.setTypeParameter(rawType)
                }
            }
//            println("转换：${JsonUtil.seriazileAsString(obj)}")
            return obj
        } catch (e: Exception) {
//            e.printStackTrace()
//            ToastHelper.showLong(huanqueApp.getApp(), "转换json错误" + e.message)
            if (e is SocketTimeoutException) {
                ToastUtils.showErrorMessage("当前网络状况不太好哦~_~")
            }
            e.printStackTrace()
//            Log.i("DXC","转换json错误 要转换的 字符串 ==>>> ${value.string()} , ResponseBody对象 ==>>> $value , ${e.printStackTrace()}")
            reportCrash("转换json错误 要转换的 字符串 ==>>> ${value.string()} , ResponseBody对象 ==>>> $value , i", e)
            throw  e

        } finally {
            value.close()
        }
    }
}