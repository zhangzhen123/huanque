package com.julun.rnlib

import android.app.Application
import android.content.Context
import com.BV.LinearGradient.LinearGradientPackage
import com.facebook.react.ReactInstanceManager
import com.facebook.react.common.LifecycleState
import com.facebook.react.shell.MainReactPackage
import com.julun.rnlib.reactpackage.OpenActivityReactPackage
import com.julun.rnlib.reactpackage.RequestInfoReactPackage
import com.julun.rnlib.reactpackage.ToastReactPackage
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage
import com.th3rdwave.safeareacontext.SafeAreaContextPackage
import com.zmxv.RNSound.RNSoundPackage
import org.reactnative.maskedview.RNCMaskedViewPackage

object RnManager {

    private var mReactInstanceManager: ReactInstanceManager? = null

    /**
     * 创建ReactInstanceManager 全局复用唯一
     */
    fun createReactInstanceManager(application: Application): ReactInstanceManager {
        if (mReactInstanceManager == null) {
            mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(application)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(MainReactPackage()) // 背景色渐变：需放到application中
                .addPackage(LinearGradientPackage()) // 请求头数据：需放到application中
                .addPackage(RequestInfoReactPackage())
                .addPackage(ToastReactPackage())
                .addPackage(OpenActivityReactPackage())
                .addPackage(RNGestureHandlerPackage())
                .addPackage(SafeAreaContextPackage())
                .addPackage(RNCMaskedViewPackage())
                .addPackage(RNSoundPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.BEFORE_CREATE)
                .build()

        }
        return mReactInstanceManager!!

    }
}