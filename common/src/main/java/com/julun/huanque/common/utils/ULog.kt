package com.julun.huanque.common.utils

import android.util.Log
import com.julun.huanque.common.BuildConfig
import java.util.logging.Level
import java.util.logging.Logger

/**
 *
 * @author zhangzhen
 * @data 2017/2/20
 */
class ULog private constructor() {
    companion object {
        fun getLogger(tag: String?): Logger {
            val logger = Logger.getLogger(tag)
            logger.level = if (BuildConfig.DEBUG) Level.ALL else Level.OFF
            return logger
        }

        var isDebug = BuildConfig.DEBUG //

        //todo
        private const val TAG = "huanque"

        // 下面四个是默认tag的函数
        fun i(msg: String) {
            if (isDebug) Log.i(TAG, msg)
        }

        fun d(msg: String) {
            if (isDebug) Log.d(TAG, msg)
        }

        fun e(msg: String) {
            if (isDebug) Log.e(TAG, msg)
        }

        fun v(msg: String) {
            if (isDebug) Log.v(TAG, msg)
        }

        // 下面是传入自定义tag的函数
        fun i(tag: String?, msg: String) {
            if (isDebug) Log.i(tag, msg)
        }

        fun d(tag: String?, msg: String) {
            if (isDebug) Log.d(tag, msg)
        }

        fun e(tag: String?, msg: String) {
            if (isDebug) Log.e(tag, msg)
        }

        fun v(tag: String?, msg: String) {
            if (isDebug) Log.v(tag, msg)
        }
    }

    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}