package com.julun.huanque.common.init

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.R
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import org.jay.launchstarter.TaskDispatcher
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
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

    private var urlTest = "http://office.katule.cn:9205/"

    //保存的全局application
    private lateinit var mContext: Application
    var inSDK = true

//    private var libraryListener: CommonListener? = null
//
//    fun setCommonListener(listener: CommonListener) {
//        this.libraryListener = listener
//    }
//
//    fun getCommonListener() = libraryListener
    //当前处于活动状态的Activity
    private var mActivityReference: WeakReference<Activity>? = null


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

    fun init(application: Application) {
        mContext = application
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
                isAppOnForeground = false
            }

            override fun onActivityResumed(activity: Activity) {
                setCurrentActivity(activity)
                isAppOnForeground = true

                if (activity != null && (activity.localClassName == "com.cmic.sso.sdk.activity.LoginAuthActivity" || activity.localClassName == "cn.jiguang.verifysdk.CtLoginActivity")) {
                    val contentView = activity.window?.peekDecorView()?.findViewById<View>(android.R.id.content)
                        ?: return
                    //获取号码栏
                    val phoneView = getMatchTextView(contentView, "****") ?: return
                    val viewContent = phoneView.text.toString().trim()
                    if (viewContent.contains("本机号码")) {
                        return
                    }
                    val realContent = "本机号码：$viewContent"
                    phoneView.text = realContent
                    phoneView.backgroundResource = R.drawable.bg_owner_phone_number
//                    phoneView.backgroundColor = GlobalUtils.getColor(R.color.black_333)
                    //设置宽高
                    val params = phoneView.layoutParams
                    params.height = activity.dip(50)
                    val phoneNumWidthPx = ScreenUtils.getScreenWidth() - activity.dip(38) * 2
                    params.width = phoneNumWidthPx
                    phoneView.layoutParams = params
                    //获取隐私栏
//                    val privacyView = getMatchTextView(contentView, "登录即为同意羚萌") ?: return
//                    val privacyText = privacyView.text
//                    if(privacyText is SpannableString){
//                        privacyText.getSpans()
//                    }
                }

            }

            override fun onActivityStarted(activity: Activity) {
                logger("onActivityStopped:$activity")
                setCurrentActivity(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                ActivitiesManager.removeActivities(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity) {
                logger("onActivityStopped:$activity")
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivitiesManager.push(activity)
            }

        })
//        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
//            ARouter.openLog()     // 打印日志
//            ARouter.openDebug()  // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
//        }
//        ARouter.init(application) // 尽可能早，推荐在Application中初始化
//        PartyLibraryInit.getInstance().initComponent(application)
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
        SharedPreferencesUtils.init(application)
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
}