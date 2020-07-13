package com.julun.huanque.common.bean.message

/**
 *@创建者   dong
 *@创建时间 2020/7/11 11:07
 *@描述 自定义使用到的对象
 */

/**
 * 语音会话模拟消息
 * @param type 会话结束类型
 * @param 通话时长
 * @param createUserID 创建者ID
 */
data class VoiceConmmunicationSimulate(var type: String = "", var duration: Long = 0, var createUserID: Long = 0)