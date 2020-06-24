/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.julun.huanuqe

import android.content.Context
import android.os.SystemClock

import com.facebook.stetho.Stetho
import com.julun.huanque.app.HuanQueApp
import com.julun.huanque.common.suger.logger


class SampleDebugApplication : HuanQueApp() {

    override fun onCreate() {
        super.onCreate()
//        loadLib()
        val startTime = SystemClock.elapsedRealtime()
        initializeStetho(this)
        val elapsed = SystemClock.elapsedRealtime() - startTime
        logger( "Stetho initialized in $elapsed ms")
//        if (AppInitUtils.isMainProcess(this)) {
//            BlockCanary.install(this, AppBlockCanaryContext()).start()
//        }
    }

    /**
     * 手动加载libsqlite.so库
     */
    private fun loadLib() {

//        val file = File("${applicationInfo.nativeLibraryDir}/libsqlite.so")
//        System.load(file.absolutePath)
        System.loadLibrary("sqlite")
    }

    private fun initializeStetho(context: Context) {
        // See also: Stetho.initializeWithDefaults(Context)
        //初始化Stetho
        Stetho.initializeWithDefaults(context)
    }

    companion object {
        private val TAG = "SampleDebugApplication"
    }
}
