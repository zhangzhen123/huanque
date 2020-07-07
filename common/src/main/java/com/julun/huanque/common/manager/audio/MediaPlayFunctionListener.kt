package com.julun.huanque.common.manager.audio

interface MediaPlayFunctionListener {
    // 准备完成
    fun prepared()

    // 开始播放
    fun start()

    // 暂停
    fun pause()

    // 停止播放
    fun stop()

    //重置
    fun reset()
}