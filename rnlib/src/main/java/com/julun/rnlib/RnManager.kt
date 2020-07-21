package com.julun.rnlib

import android.app.Application
import com.BV.LinearGradient.LinearGradientPackage
import com.brentvatne.react.ReactVideoPackage
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactInstanceManager.ReactInstanceEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.LifecycleState
import com.facebook.react.shell.MainReactPackage
import com.horcrux.svg.SvgPackage
import com.julun.huanque.common.net.interceptors.HeaderInfoHelper
import com.julun.huanque.common.suger.logger
import com.julun.rnlib.reactpackage.AppMessageReactPackage
import com.julun.rnlib.reactpackage.OpenPageReactPackage
import com.julun.rnlib.reactpackage.RequestInfoReactPackage
import com.luck.picture.lib.config.PictureConfig
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage
import com.swmansion.reanimated.ReanimatedPackage
import com.th3rdwave.safeareacontext.SafeAreaContextPackage
import com.zmxv.RNSound.RNSoundPackage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import org.reactnative.maskedview.RNCMaskedViewPackage


object RnManager {
    //上传图片方法的标识
    const val uploadPhotos = "uploadPhotos"
    //上传视频方法的标识
    const val uploadVideo = "uploadVideo"
    private var mReactInstanceManager: ReactInstanceManager? = null

    var curActivity: RNPageActivity? = null

    var promiseMap: MutableMap<String, Promise> = mutableMapOf()

    /**
     * 创建ReactInstanceManager 全局复用唯一
     */
    fun createReactInstanceManager(application: Application): ReactInstanceManager {
        logger("createReactInstanceManager 初始化")
        if (mReactInstanceManager == null) {
            mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(application)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(MainReactPackage()) // 背景色渐变：需放到application中
                .addPackage(LinearGradientPackage()) // 请求头数据：需放到application中
                .addPackage(RequestInfoReactPackage())
                .addPackage(AppMessageReactPackage())//新增统一的消息通信模块
                .addPackage(OpenPageReactPackage())
                .addPackage(RNGestureHandlerPackage())
                .addPackage(SafeAreaContextPackage())
                .addPackage(RNCMaskedViewPackage())
                .addPackage(RNSoundPackage())
                .addPackage(SvgPackage())
                .addPackage(ReanimatedPackage())
                .addPackage(ReactVideoPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.BEFORE_CREATE)
                .setNativeModuleCallExceptionHandler {
                    it.printStackTrace()
                }
                .build()
//            mReactInstanceManager!!.createReactContextInBackground()
            if (!mReactInstanceManager!!.hasStartedCreatingInitialContext()) {
                mReactInstanceManager!!.addReactInstanceEventListener(object : ReactInstanceEventListener {
                    override fun onReactContextInitialized(context: ReactContext) {
                        mReactInstanceManager!!.removeReactInstanceEventListener(this)
                        logger("ReactInstanceManager 加载完成了")
                    }
                })
                mReactInstanceManager!!.createReactContextInBackground()
            }
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
        Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
            curActivity?.finish()
        }.subscribe()
    }

    /**
     * 打开上传图片功能
     */
    fun uploadPhotos(max: Int) {
        curActivity?.openPhotoSelect(max,PictureConfig.TYPE_IMAGE)
    }

    /**
     * 打开上传视频功能
     */
    fun uploadVideo(max: Int) {
        curActivity?.openPhotoSelect(max,PictureConfig.TYPE_VIDEO)
    }
    fun clearPromiseMap() {
//        promiseMap.forEach { it ->
//            when (it.key) {
//                uploadPhotos -> {
//                    it.value.reject("-1", "图片上传功能 页面关闭了 通知rn回调")
//                }
//                else->{
//                    it.value.reject("-1", "其他功能 通知rn回调")
//                }
//            }
//
//        }
        promiseMap.clear()
    }
}