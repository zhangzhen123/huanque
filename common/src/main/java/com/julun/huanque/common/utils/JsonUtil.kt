package com.julun.huanque.common.utils

/**
 * Created by nirack on 16-10-27.
 */

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor
import com.alibaba.fastjson.serializer.SerializerFeature
import java.lang.reflect.Type


/**
 * Created by nirack on 16-10-22.
 */
object JsonUtil {

    internal val STANDARD_FEATURES = arrayOf(SerializerFeature.DisableCircularReferenceDetect)

    private val FJU = object {

        private val styleParamProcessor: ExtraProcessor = ExtraProcessor { bean, key, value ->
//            if (bean is TplBean) {
//                val string: String? = value?.toString()
//                if ("styleParams" == key && StringHelper.isNotEmpty(string)) {
//                    val list: List<StyleParam> =
//                            JsonUtil.deserializeAsObject<List<StyleParam>>(string!!, ReflectUtil.list(StyleParam::class.java))
//                    bean.styleParamMap = hashMapOf<String, StyleParam>().apply {
//                        putAll(list.map { it.el to it })
//                    }
//                }
//            }
//            if (bean is PayResultInfo && "content" == key) {
//                if (value is String) {
//                    bean.alipayOrderInfo = value
//                } else if (value is Map<*, *>) {
//                    bean.wxOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.seriazileAsString(value), OrderInfo::class.java)
//                    bean.oppoOrderInfo = JsonUtil.deserializeAsObject(JsonUtil.seriazileAsString(value), OppoPayInfo::class.java)
//                }
//            }
        }

        /**
         * java-object as json-string
         * @param obj
         * *
         * @return
         */
        fun serializeAsString(obj: Any?, vararg features: SerializerFeature): String {
            if (obj == null) {
                return ""
            }
            try {
                return JSON.toJSONString(obj, *features)
            } catch (ex: Exception) {
                throw Exception("Could not write JSON: " + ex.message, ex)
            }

        }

        /**
         * json-string to java-object

         * @param jsonString
         * *
         * @param clazz
         * *
         * @return
         */
        fun <T> deserializeAsObject(jsonString: String?, clazz: Type?): T? {
            if (jsonString == null || clazz == null) {
                return null
            }
            try {
                return JSON.parseObject<T>(jsonString, clazz/*, styleParamProcessor*/)
            } catch (ex: Exception) {
                throw Exception("Could not write JSON: " + ex.message, ex)
            }

        }
    }

    /**
     * TODO 目前因为fastjson丢复杂的json时，使用了"$ref"无法正常解析，因此这里禁用了这个优化
     * 如果fastjson修复了，则应该立即启用

     * @param obj
     * *
     * @return
     */
    fun serializeAsString(obj: Any?): String {
        return FJU.serializeAsString(obj, *STANDARD_FEATURES)
    }

    /**
     * 标准的序列化接口
     */
    fun serializeAsStringWithStandard(obj: Any): String {
        return FJU.serializeAsString(obj, *STANDARD_FEATURES)
    }

    fun <T> deserializeAsObject(jsonString: String, clazz: Type): T {
        return FJU.deserializeAsObject<T>(jsonString, clazz)!!
    }

    //解析数组的
    fun <T> deserializeAsObjectList(jsonString: String?, type: Class<T>): List<T>? {
        if (jsonString == null) {
            return null
        }
        try {
            return JSON.parseArray<T>(jsonString, type)
        } catch (ex: Exception) {
            throw Exception("Could not write JSON: " + ex.message, ex)
        }

    }

    /**
     * 将一个对象，转换成对象

     * @param obj 只能是Object或者Map，不能是数组类型
     * *
     * @return
     */
    fun toJsonMap(obj: Any?): Map<String, Any>? {
        if (obj == null)
            return null
        val objString = JSON.toJSONString(obj)
        return JSON.parseObject<Map<String, Any>>(objString, getType())
    }

    fun toJsonMap(s: String?): Map<String, Any>? {
        if (s == null)
            return null
        return JSON.parseObject<Map<String, Any>>(s, object : TypeReference<Map<String, Any>>() {})
    }

    private fun getType(): Type {
        return object : TypeReference<Map<String, Any>>() {

        }.type
    }



}