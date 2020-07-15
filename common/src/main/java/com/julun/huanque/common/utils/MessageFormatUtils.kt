package com.julun.huanque.common.utils

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.beans.SysMsgContent

/**
 *@创建者   dong
 *@创建时间 2020/7/15 10:25
 *@描述 系统消息和鹊友消息解析类
 */
object MessageFormatUtils {

    fun formatSysMsgContent(str: String): SysMsgContent? {
        val content = parseJson(str)
        try {
            return JsonUtil.deserializeAsObject<SysMsgContent>(
                content,
                SysMsgContent::class.java
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    private fun parseJson(content: String): String {
        try {
            val baseList = JsonUtil.deserializeAsObjectList(content, BaseData::class.java)
            if (baseList?.isNotEmpty() == true) {
                val jsonObject = baseList.first().data as JSONObject
                return jsonObject.toJSONString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

}