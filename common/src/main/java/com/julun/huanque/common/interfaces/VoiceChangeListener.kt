package com.julun.huanque.common.interfaces

/**
 *@创建者   dong
 *@创建时间 2020/11/17 16:07
 *@描述 语音变化监听  状态或者通话时长
 */
interface VoiceChangeListener {
    /**
     * @param state 状态
     * @param duration 通话时长
     */
    fun voiceChange(state: String, duration: Long)

}