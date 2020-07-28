package com.julun.huanque.common.bean.message

import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/7/11 11:07
 *@描述 自定义使用到的对象
 */

/**
 * 语音会话模拟消息
 * @param type 会话结束类型
 * @param duration 通话时长
 * @param createUserID 创建者ID
 * @param billUserId 付费的用户ID
 * @param totalBeans 总共花费的金额
 */
data class VoiceConmmunicationSimulate(
    var type: String = "", var duration: Long = 0, var createUserID: Long = 0,
    var billUserId: Long = 0, var totalBeans: Long = 0
)

/**
 * 自定义动画消息
 */
data class ExpressionAnimationBean(var name: String = "", var result: String = "") : Serializable