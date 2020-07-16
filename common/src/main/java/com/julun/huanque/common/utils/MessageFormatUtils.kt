package com.julun.huanque.common.utils

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.beans.FriendBean
import com.julun.huanque.common.bean.beans.SysMsgContent
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.constant.MessageConstants
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.rd.utils.DensityUtils

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


    /**
     * 解析TextMessage消息
     */
    fun <T> parseJsonFromTextMessage(clazz: Class<T>, content: String): T? {
        try {
            val baseList = JsonUtil.deserializeAsObjectList(content, BaseData::class.java)
            if (baseList?.isNotEmpty() == true) {
                val jsonObject = baseList.first().data as JSONObject
                val json = jsonObject.toJSONString()
                return JsonUtil.deserializeAsObject<T>(
                    json,
                    clazz
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun renderImage(sdstv: DraweeSpanTextView, result: FriendBean) {
        val list = arrayListOf<TIBean>()
        val text = TIBean()
        text.type = TIBean.TEXT
        text.textColor = "#999999"
        text.textSize = DensityUtils.dpToPx(14)
        list.add(text)
        when (result.relationChangeType) {
            MessageConstants.FRIEDN -> {
                //成为好友
                text.text = "和你成了鹊友"
            }
            MessageConstants.FRIEDN_FOLLOW -> {
                //关注
                text.text = "关注了你"
            }
            MessageConstants.FRIEDN_INTIMATE -> {
                //成为密友
                text.text = "和你成为了密友"
            }
            else -> {
                //亲密度等级提升
                text.text = "和你升级为了"
                val image = TIBean()
                image.type = TIBean.IMAGE
                image.imgRes = R.mipmap.icon_logo_avatar_yellow
                image.height = DensityUtils.dpToPx(16)
                image.width = DensityUtils.dpToPx(46)
                list.add(image)
            }
        }
        ImageUtils.renderTextAndImage(list)?.let {
            sdstv.renderBaseText(it)
        }
    }

}