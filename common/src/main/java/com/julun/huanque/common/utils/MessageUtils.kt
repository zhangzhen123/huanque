package com.julun.huanque.common.utils

import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.manager.RongCloudManager
import io.rong.imlib.model.Message

/**
 *@创建者   dong
 *@创建时间 2020/9/1 11:20
 *@描述 存放消息相关的通用处理
 */
object MessageUtils {

    /**
     * 设置动画已经播放过
     */
    fun setAnimationStarted(msg: Message) {
        try {
            val extra = JsonUtil.serializeAsString(GlobalUtils.addExtra(msg.extra ?: "", ParamConstant.MSG_ANIMATION_STARTED, true))
            msg.extra = extra

            if (msg.messageId > 0) {
                //数据库修改
                RongCloudManager.updateMessageExtra(msg.messageId, extra)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取动画是否播放标识位
     */
    fun getAnimationStarted(msg: Message): Boolean {
        var map: Map<String, Any>? = null
        try {
            if (msg.extra?.isNotEmpty() == true) {
                map = JsonUtil.toJsonMap(msg.extra)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map?.get(ParamConstant.MSG_ANIMATION_STARTED) == true
    }

}