package com.julun.huanque.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import cn.jiguang.verifysdk.api.JVerificationInterface
import com.ishumei.smantifraud.SmAntiFraud
import com.jakewharton.processphoenix.ProcessPhoenix
import com.julun.huanque.BuildConfig
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.R
import com.julun.huanque.agora.AgoraManager
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.init.HuanQueInit
import com.julun.huanque.ui.cockroach.DebugSafeModeTipActivity
import com.julun.jpushlib.TagAliasOperatorHelper
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.wanjian.cockroach.Cockroach
import com.wanjian.cockroach.CrashLog
import com.wanjian.cockroach.ExceptionHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

open class HuanQueApp : Application() {
    companion object{
        var wxApi: IWXAPI? = null
//        var qqApi: Tencent? = null
    }
    //数美分发的deviceId，用于过滤。（设置一次监听会触发两次onSuccess）
    private var mDeviceId = ""

    override fun onCreate() {
        super.onCreate()
        install()
        val baseUrl = if (BuildConfig.DEBUG) {
            BuildConfig.SERVICE_BASE_URL_DEV
        } else {
            BuildConfig.SERVICE_BASE_URL_PRODUCT
        }
        CommonInit.getInstance().setBaseUrl(baseUrl)
        if (AppHelper.isMainProcess(this)) {
            //bugly初始化(bugly的初始化放在所有初始化的第一个，以免一些日志错过收集)
            initBugly(this)
            //初始化声网
            AgoraManager.initAgora(this)
            TagAliasOperatorHelper.getInstance().init(this)
//            /*一键登录相关*/
            JVerificationInterface.setDebugMode(BuildConfig.DEBUG)
            JVerificationInterface.init(this)
            try {
                initShumei(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if(AppHelper.isMainProcess(this)){
            CommonInit.getInstance().inSDK=false
            HuanQueInit.getInstance().init(this)
            CommonInit.getInstance().taskDispatcher
                ?.addTask(JPushTask())
                ?.addTask(TencentTask())

            CommonInit.getInstance().taskDispatcher?.start()
            CommonInit.getInstance().taskDispatcher?.await()
            CommonInit.getInstance().taskDispatcher = null
        }

    }

    private fun initBugly(app: Application) {
        val applicationContext = app.applicationContext
        val strategy = CrashReport.UserStrategy(applicationContext)
        //设置channel相关
//        val channel = LMUtils.getAppMetaData(BusiConstant.MetaKey.DOWNLOAD_CHANNEL)
//        if (!TextUtils.isEmpty(channel)) {
//            Log.i("channel", "buglug: $channel")
//            strategy.appChannel = "main"
//        }

//        strategy.appVersion = String.format("%d.%d.%d", BuildConfig.MAJOR_VERSION, BuildConfig.MINOR_VERSION, BuildConfig.PATCH_VERSION)
        strategy.appVersion = BuildConfig.VERSION_NAME
//        strategy.appVersion = "1.0.0"
        //                CrashReport.initCrashReport(applicationContext, strategy)
        CrashReport.initCrashReport(applicationContext, "381c225d86", false, strategy)

        CrashReport.setIsDevelopmentDevice(applicationContext, BuildConfig.DEBUG)

    }

//    /**
//     * 获取当前的buglyId
//     */
//    private fun getBuglyId(): String {
//        return if (BuildConfig.DEBUG) {
//            when (SPUtils.getProduct()) {
//                1 -> {
//                    BuildConfig.TENCENT_BUGLY_APP_ID_PRODUCT
//                }
//                2 -> {
//                    BuildConfig.TENCENT_BUGLY_APP_ID_DEV
//                }
//                else -> {
//                    //没有
//                    BuildConfig.TENCENT_BUGLY_APP_ID_DEV
//                }
//            }
//        } else {
//            BuildConfig.TENCENT_BUGLY_APP_ID_PRODUCT
//        }
//    }

    /**
     * 初始化Crash捕获相关
     */
    private fun install() {
//        val sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler()
        val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
//        DebugSafeModeUI.init(this)
        Cockroach.install(this, object : ExceptionHandler() {
            override fun onUncaughtExceptionHappened(thread: Thread, throwable: Throwable) {
                Log.e("Cockroach", "--->onUncaughtExceptionHappened:$thread<---", throwable)
                CrashLog.saveCrashLog(applicationContext, throwable)
                reportCrash(throwable)
                if (BuildConfig.DEBUG) {
                    Handler(Looper.getMainLooper()).post {
                        toast.setText("捕获到导致崩溃的异常")
                        toast.show()
                    }
                }

            }

            override fun onBandageExceptionHappened(throwable: Throwable) {
                throwable.printStackTrace()//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                if (BuildConfig.DEBUG) {
                    toast.setText("Cockroach Worked")
                    toast.show()
                }
            }

            override fun onEnterSafeMode() {
                if (BuildConfig.DEBUG) {

                    val tips = "已经进入安全模式"
                    Toast.makeText(this@HuanQueApp, tips, Toast.LENGTH_LONG).show()
//                DebugSafeModeUI.showSafeModeUI()

                    val intent = Intent(this@HuanQueApp, DebugSafeModeTipActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (ForceUtils.activityMatch(intent)) {
                        startActivity(intent)
                    }
                }
            }

            @SuppressLint("CheckResult")
            override fun onMayBeBlackScreen(e: Throwable) {
                val thread = Looper.getMainLooper().thread
                Log.e("Cockroach", "--->onMayBeBlackScreen:$thread<---", e)
                //黑屏时建议直接杀死app
//                sysExcepHandle
// r.uncaughtException(thread, RuntimeException("black screen"))
                Handler(Looper.getMainLooper()).post {
                    toast.setText(resources.getString(R.string.attention_restart))
                    toast.show()
                }
//                ToastUtils.show(R.string.attention_restart)
                //重启重启 延迟是为了commit执行完
                Observable.timer(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val nextIntent = Intent(this@HuanQueApp, MainActivity::class.java)
                        ProcessPhoenix.triggerRebirth(this@HuanQueApp, nextIntent)
                    }
            }

        })
    }

    /**
     * 初始化数美
     */
    private fun initShumei(application: Application) {
        val option = SmAntiFraud.SmOption()
        // 数美提供的公司唯一标识码
        val org = "HvjKuOJMi7FlcXA8DbFx"
        option.organization = org
        option.appId = "default"
        //渠道代码 //如果是首次启动 App，设置为 true，否则设置为 false 或不设置。
//        val channelId = OpenInstallManager.getChannelId()
//        option.channel = channelId
        option.serverIdCallback = object : SmAntiFraud.IServerSmidCallback {
            override fun onSuccess(serverId: String?) {
//                ULog.i("DXC  serverId = $serverId")
                //分发成功
                if (serverId?.isNotEmpty() == true && serverId != mDeviceId) {
                    //获取到新的serverId,更新给服务端
                    mDeviceId = serverId
//                    EventBus.getDefault().postSticky(DeviceEvent())
                }
            }

            override fun onError(errorCode: Int) {

            }
        }
        SmAntiFraud.create(application, option)
    }
}