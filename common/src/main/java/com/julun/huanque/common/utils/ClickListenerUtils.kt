package com.julun.huanque.common.utils

/**
 *
 *@author zhangzhen
 *@data 2017/8/11
 * 检测是否进行了连续点击 防止重复提交
 *
 **/
object ClickListenerUtils {
    private var lastClickTime: Long = 0
    private val SPACE_TIME = 500//最小的连点间隔
    val isDoubleClick: Boolean
        @Synchronized get() {
            val currentTime = System.currentTimeMillis()
            val isClick2: Boolean
            isClick2 = currentTime - lastClickTime <= SPACE_TIME
            lastClickTime = currentTime
            return isClick2
        }
}

