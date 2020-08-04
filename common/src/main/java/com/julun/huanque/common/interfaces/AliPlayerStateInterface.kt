package com.julun.huanque.common.interfaces

/**
 *@创建者   dong
 *@创建时间 2019/12/12 19:03
 *@描述 推荐播放页面，使用的监听
 */
interface PlayStateListener {
    fun playState(code: Int)
    //首帧图像渲染显示回调
    fun onFirstRenderListener()
}