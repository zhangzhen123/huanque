package com.julun.rnlib

import android.app.Application
import com.BV.LinearGradient.LinearGradientPackage
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.LifecycleState
import com.facebook.react.shell.MainReactPackage
import com.julun.huanque.common.net.interceptors.HeaderInfoHelper
import com.julun.rnlib.reactpackage.OpenPageReactPackage
import com.julun.rnlib.reactpackage.RequestInfoReactPackage
import com.julun.rnlib.reactpackage.ToastReactPackage
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage
import com.th3rdwave.safeareacontext.SafeAreaContextPackage
import com.zmxv.RNSound.RNSoundPackage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.reactnative.maskedview.RNCMaskedViewPackage

object RnManager {
    //上传方法的标识
    const val uploadPhotos = "uploadPhotos"
    private var mReactInstanceManager: ReactInstanceManager? = null

    var curActivity: RNPageActivity? = null

    var promiseMap: MutableMap<String, Promise> = mutableMapOf()

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
                .addPackage(OpenPageReactPackage())
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

    fun getHeaderInfo(): WritableMap {
        val writableMap = Arguments.createMap()
        val mapInfo = HeaderInfoHelper.getMobileDeviceInfo()
        mapInfo.iterator().forEach { value ->
            writableMap.putString(value.key, value.value)
        }
        return writableMap
    }

    fun closeRnPager() {
        Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).subscribe {
            curActivity?.finish()
        }

    }

    /**
     * 打开上传图片功能
     */
    fun uploadPhotos(max: Int) {
        curActivity?.openPhotoSelect(max)
    }

    fun clearPromiseMap() {
        promiseMap.forEach { it ->
            when (it.key) {
                uploadPhotos -> {
                    it.value.reject("-1", "图片上传功能 页面关闭了 通知rn回调")
                }
                else->{
                    it.value.reject("-1", "其他功能 通知rn回调")
                }
            }

        }
    }
}