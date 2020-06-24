package org.jay.launchstarter.utils

import android.os.Debug

/**
 * 性能检测工具
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/18
 */
object LaunchRecord {
    private var sStart: Long = 0

    fun startRecord() {
        sStart = System.currentTimeMillis()
    }

    fun endRecord() {
        endRecord("")
    }

    fun endRecord(postion: String) {
        val cost = System.currentTimeMillis() - sStart
        println("===$postion===$cost")
    }

    fun startTrace() {
        Debug.startMethodTracing("cold-start", 20 * 1024 * 1024)
    }

    fun endTrace() {
        Debug.stopMethodTracing()
    }
}