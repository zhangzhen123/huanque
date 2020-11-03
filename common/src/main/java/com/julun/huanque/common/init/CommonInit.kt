package com.julun.huanque.common.init

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.cache.common.CacheErrorLogger
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.events.HideFloatingEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.svga.SVGAHelper
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.jay.launchstarter.TaskDispatcher
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import java.io.File
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * common library初始化类，进行第三库初始化之外的工作
 */
class CommonInit {
    companion object {
        @Volatile
        private var singleton: CommonInit? = null

        fun getInstance(): CommonInit {
            if (singleton == null) {
                synchronized(CommonInit::class.java) {
                    if (singleton == null) {
                        singleton = CommonInit()
                    }
                }
            }
            return singleton!!
        }


    }

    //这里记录fresco配置 给其他第三方用
    var frescoConfig: ImagePipelineConfig? = null
    private var urlTest = "http://office.katule.cn:9205/"

    //保存的全局application
    private lateinit var mContext: Application
    var inSDK = true

    /**
     * 一键登录页面
     */
    private var mFastLoginContentView: FrameLayout? = null

    /**
     * 获取一键登录页面
     */
    fun getFastLoginContentView() = mFastLoginContentView

    //    private var libraryListener: CommonListener? = null
//
//    fun setCommonListener(listener: CommonListener) {
//        this.libraryListener = listener
//    }
//
//    fun getCommonListener() = libraryListener
    //当前处于活动状态的Activity
    private var mActivityReference: WeakReference<Activity>? = null

    //当前显示页面数量
    private var mActCount = 0


    //判断app是否在前台
    var isAppOnForeground: Boolean = false

    fun getCurrentActivity(): Activity? {
        return mActivityReference?.get()
    }

    fun setCurrentActivity(activity: Activity?) {
        if (activity != null)
            mActivityReference = WeakReference(activity)
        else
            mActivityReference?.clear()
    }

    //保存首页引用 以供特殊地方使用
    private var mMainActivityReference: WeakReference<Activity>? = null
    fun getMainActivity(): Activity? {
        return mMainActivityReference?.get()
    }

    fun setMainActivity(activity: Activity?) {
        if (activity != null)
            mMainActivityReference = WeakReference(activity)
        else
            mMainActivityReference?.clear()
    }

    fun getContext(): Context {
        return mContext.applicationContext
    }

    fun getApp(): Application {
        return mContext
    }

    fun initContext(application: Application) {
        mContext = application
    }

    fun init(application: Application) {
//        mContext = application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                isAppOnForeground = false
            }

            override fun onActivityResumed(activity: Activity) {
                setCurrentActivity(activity)
                isAppOnForeground = true

                if (activity != null && (activity.localClassName == "com.cmic.sso.sdk.activity.LoginAuthActivity" || activity.localClassName == "cn.jiguang.verifysdk.CtLoginActivity")) {
                    val contentView =
                        activity.window?.peekDecorView()?.findViewById<View>(android.R.id.content)
                            ?: return
                    //获取号码栏
                    val phoneView = getMatchTextView(contentView, "****") ?: return
                    val viewContent = phoneView.text.toString().trim()
                    if (viewContent.contains("一键登录")) {
                        return
                    }
                    val realContent = "$viewContent 一键登录"
                    phoneView.text = realContent
                    phoneView.backgroundResource = R.drawable.bg_phone_number_fast_login
                    //设置宽高
                    val params = phoneView.layoutParams
                    params.height = activity.dip(50)
                    val phoneNumWidthPx = ScreenUtils.getScreenWidth() - activity.dip(38) * 2
                    params.width = phoneNumWidthPx
                    phoneView.layoutParams = params
                    //获取隐私栏
//                    val privacyView = getMatchTextView(contentView, "登录即为同意欢鹊") ?: return
//                    val privacyText = privacyView.text
//                    if(privacyText is SpannableString){
//                        privacyText.getSpans()
//                    }
                }

            }

            override fun onActivityStarted(activity: Activity) {
                logger("onActivityStarted:$activity")
                mActCount++
                setCurrentActivity(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                ActivitiesManager.INSTANCE.removeActivities(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity) {
                logger("onActivityStopped:$activity")
                mActCount--
                if (mActCount == 0) {
                    //从前台退到后台
                    EventBus.getDefault().post(HideFloatingEvent())
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivitiesManager.INSTANCE.push(activity)
            }

        })
        initTask(application)
    }


    /**
     * 获取baseUrl
     */
//    fun getBaseUrl() = if (BuildConfig.DEBUG) {
//        if (SPUtils.getProduct() == 1) {
//            BuildConfig.SERVICE_BASE_URL_PRODUCT
//        } else {
//            if (SPUtils.getUrl().isEmpty()) {
//                BuildConfig.SERVICE_BASE_URL_DEV
//            } else {
//                SPUtils.getUrl()
//            }
//        }
//    } else {
//        BuildConfig.SERVICE_BASE_URL_PRODUCT
//    }
    fun getBaseUrl(): String {
        return mBaseUrl ?: BuildConfig.SERVICE_BASE_URL_PRODUCT
//        return BuildConfig.SERVICE_BASE_URL_PRODUCT
    }

    private var mBaseUrl: String? = null //内部用

    //根据debug模式 设置baseUrl
    fun setBaseUrlByMode(debug: Boolean) {
        when {
            //如果有值 说明直接设置过了
            mBaseUrl != null -> {
            }
            debug -> {
                mBaseUrl = urlTest
            }
            else -> {
                mBaseUrl = BuildConfig.SERVICE_BASE_URL_PRODUCT
            }
        }
    }

    /**
     * 获取拥有对应文案的TextView
     */
    private fun getMatchTextView(view: View, str: String): TextView? {
        if (view is ViewGroup) {
            val childCount = view.childCount
            (0 until childCount).forEach {
                val tempView = view.getChildAt(it)
                if (tempView is TextView) {
                    val viewContent = tempView.text.toString().trim()
                    if (viewContent.contains(str)) {
                        //获取到对应的号码栏
                        return tempView
                    }
                } else {
                    val childTempView = getMatchTextView(tempView, str)
                    if (childTempView != null) {
                        return childTempView
                    }
                }
            }
            return null
        } else {
            return null
        }
    }

    //改变基本域名 自己内部使用
    fun setBaseUrl(baseUrl: String) {
        mBaseUrl = baseUrl
    }

//    //从直播间触发登录的时候使用
//    private var programId: Long? = null
//
//    fun setProgramId(programId: Long?) {
//        this.programId = programId
//    }
//
//    fun getProgramId() = programId
//
//    private var mSdkType = BusiConstant.SDKType.BAOMIHUA
//
//    /**
//     * SDK类型
//     */
//    fun setSdkType(type: String) {
//        mSdkType = type
//    }
//
//    fun getSdkType() = mSdkType

    var taskDispatcher: TaskDispatcher? = null
    fun initTask(application: Application) {
        ToastUtils.init(application)//初始化自定义土司
        RongCloudManager.clearRoomList()
        //手动设置Rx ComputationScheduler线程数目
        //todo
//        System.setProperty(PartyLibraryInit.KEY_MAX_THREADS, "5")
        //debug模式开启webview debug调试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        TaskDispatcher.init(application)
        taskDispatcher = TaskDispatcher.createInstance()
        taskDispatcher?.addTask(ARouterTask())
            ?.addTask(FrescoTask())
            ?.addTask(RongTask())
            ?.addTask(SVGATask())
            ?.addTask(OssTask())
            ?.addTask(RealNameTask())

        if (inSDK) {
            taskDispatcher?.start()
            taskDispatcher?.await()
            taskDispatcher = null
        }
    }

    suspend fun initWithCoroutines(application: Application) {
        val currentTime = System.currentTimeMillis()
        logger("common initWithCoroutines start----${Thread.currentThread()} ")
//        mContext = application
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                isAppOnForeground = false
                if (activity != null && (activity.localClassName == "com.cmic.sso.sdk.activity.LoginAuthActivity" || activity.localClassName == "cn.jiguang.verifysdk.CtLoginActivity")) {
                    mFastLoginContentView = null
                }
            }

            override fun onActivityResumed(activity: Activity) {
                setCurrentActivity(activity)
                isAppOnForeground = true

                if (activity != null && (activity.localClassName == "com.cmic.sso.sdk.activity.LoginAuthActivity" || activity.localClassName == "cn.jiguang.verifysdk.CtLoginActivity")) {
                    val contentView =
                        activity.window?.peekDecorView()?.findViewById<View>(android.R.id.content)
                            ?: return
                    mFastLoginContentView = contentView as? FrameLayout
                    //获取号码栏
                    val phoneView = getMatchTextView(contentView, "****") ?: return
                    val viewContent = phoneView.text.toString().trim()
                    if (viewContent.contains("一键登录")) {
                        return
                    }
                    val realContent = "$viewContent 一键登录"
                    phoneView.text = realContent
                    phoneView.backgroundResource = R.drawable.bg_phone_number_fast_login
                    //设置宽高
                    val params = phoneView.layoutParams
                    params.height = activity.dip(50)
                    val phoneNumWidthPx = ScreenUtils.getScreenWidth() - activity.dip(38) * 2
                    params.width = phoneNumWidthPx
                    phoneView.layoutParams = params
                    //获取隐私栏
//                    val privacyView = getMatchTextView(contentView, "登录即为同意欢鹊") ?: return
//                    val privacyText = privacyView.text
//                    if(privacyText is SpannableString){
//                        privacyText.getSpans()
//                    }
                }

            }

            override fun onActivityStarted(activity: Activity) {
                logger("onActivityStarted:$activity")
                mActCount++
                setCurrentActivity(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                ActivitiesManager.INSTANCE.removeActivities(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity) {
                logger("onActivityStopped:$activity")
                mActCount--
                if (mActCount == 0) {
                    //从前台退到后台
                    EventBus.getDefault().post(HideFloatingEvent())
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivitiesManager.INSTANCE.push(activity)
            }

        })

        coroutineScope {
            val timeC = System.currentTimeMillis()
            logger("Common  coroutineScope start launch----${Thread.currentThread()} ")
            val normal = async {
                val timeN = System.currentTimeMillis()
                ToastUtils.init(application)//初始化自定义土司
                SharedPreferencesUtils.init(application)
                RongCloudManager.clearRoomList()
                //手动设置Rx ComputationScheduler线程数目
//        System.setProperty(PartyLibraryInit.KEY_MAX_THREADS, "5")
                //debug模式开启webview debug调试
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                } catch (e: Exception) {
//                    e.printStackTrace()
                }
                logger("普通内容初始化 end launch----${Thread.currentThread()} duration=${System.currentTimeMillis() - timeN} ")
            }

            val aRouter = async(Dispatchers.Default) {
                val timeN = System.currentTimeMillis()

                if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
                    ARouter.openLog()     // 打印日志
                    ARouter.openDebug()  // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
                }
                ARouter.init(mContext as Application) // 尽可能早，推荐在Application中初始化
                logger("launch ARouter----${Thread.currentThread()} duration=${System.currentTimeMillis() - timeN} ")
                //初始化实名认证SDK
                ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE).navigation()
                ULog.i("launch REALNAME----${Thread.currentThread()} duration=${System.currentTimeMillis() - timeN}")
                //初始化MSA SDK并获取oaid
                ARouter.getInstance().build(ARouterConstant.MSA_SERVICE).navigation()
                ULog.i("launch MSASERVICE----${Thread.currentThread()} duration=${System.currentTimeMillis() - timeN}")
            }
            val fresco = async(Dispatchers.Default) {
                val time = System.currentTimeMillis()
                //缓存的试着
                val cacheConfigBuilder: DiskCacheConfig.Builder =
                    DiskCacheConfig.newBuilder(mContext)
                val cacheDir: File = mContext.cacheDir
                val frescoCacheDir = File(cacheDir, "fresco")
                if (!frescoCacheDir.exists()) {
                    frescoCacheDir.mkdir()
                }

                cacheConfigBuilder.setVersion(BuildConfig.VERSION_CODE)
//                .setMaxCacheSize (1000L)
//                .setMaxCacheSizeOnLowDiskSpace (100L)
//                .setMaxCacheSizeOnVeryLowDiskSpace (10)
                    .setCacheErrorLogger { cacheErrorCategory: CacheErrorLogger.CacheErrorCategory, clazz: Class<*>, _: String, throwable: Throwable? ->
                        throwable?.printStackTrace()
                        println("fresco 缓存错误.... cacheErrorCategory -> $cacheErrorCategory class $clazz ")
                    }
//                .setBaseDirectoryName(frescoCacheDir.name)
                    .setBaseDirectoryPathSupplier { frescoCacheDir }


                //总体的配置
                val config: ImagePipelineConfig = OkHttpImagePipelineConfigFactory
                    .newBuilder(mContext, Requests.getImageClient())//设置 使用 okhttp 客户端
//                .setSmallImageDiskCacheConfig(cacheConfigBuilder.build())//设置缓存
                    .setMainDiskCacheConfig(cacheConfigBuilder.build())
                    .setDownsampleEnabled(true)//设置图片压缩时支持多种类型的图片
                    .experiment().setDecodeCancellationEnabled(true)
//                .experiment().setNativeCodeDisabled(true) //是否使用底层去加载
                    .build()
                CommonInit.getInstance().frescoConfig = config
                Fresco.initialize(mContext, config)
                ULog.i("launch initFresco----${Thread.currentThread()} duration=${System.currentTimeMillis() - time} ")
            }

            val rong = async(Dispatchers.Default) {
                val time = System.currentTimeMillis()
                //蛋疼的融云多次初始化的问题,必须把其他的初始化工作放在融云的初始化工作代码快里面,否则将会执行多次(3次)
                RongCloudManager.rongCloudInit(mContext as Application) {}
                ULog.i("launch RongCloudManager----${Thread.currentThread()} duration=${System.currentTimeMillis() - time}")
            }

            val svga = async(Dispatchers.Default) {
//                val time=System.currentTimeMillis()
                //初始化svga播放管理器
                SVGAHelper.init(mContext)
//                ULog.i("launch SVGAHelper----${Thread.currentThread()} duration=${System.currentTimeMillis() - time} ")
            }
            val oss = async(Dispatchers.Default) {
//                val time=System.currentTimeMillis()
                //初始化svga播放管理器
                OssUpLoadManager.initOss(mContext)
//                ULog.i("launch initOss----${Thread.currentThread()} duration=${System.currentTimeMillis() - time} ")
            }

            normal.await()
            aRouter.await()
            fresco.await()
            rong.await()
            svga.await()
            oss.await()
            logger("Common  coroutineScope  end launch----${Thread.currentThread()} duration=${System.currentTimeMillis() - timeC} ")
        }
        logger("Common initWithCoroutines end----${Thread.currentThread()} duration=${System.currentTimeMillis() - currentTime} ")

    }
}