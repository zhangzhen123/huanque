package com.julun.huanque.app

import cn.jpush.android.api.JPushInterface
import com.julun.huanque.BuildConfig
import com.julun.huanque.support.WXApiManager
import org.jay.launchstarter.Task

//class MSATask : Task() {
//    override fun dependsOn(): List<Class<out Task>>? {
//        val task = ArrayList<Class<out Task>>()
//        task.add(ARouterTask::class.java)
//        return task
//    }
//
//    override fun run() {
//        //初始化MSA服务
//        LingMengService.getService(IMSAService::class.java)?.initSDK(mContext)
//    }
//}

/**
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/12 0012
 * @detail
 */
//class BuglyTask : MainTask() {
//    override fun run() {
//        val applicationContext = mContext.applicationContext
//        val strategy = CrashReport.UserStrategy(applicationContext)
//        val channel = LMUtils.getAppMetaData(BusiConstant.MetaKey.DOWNLOAD_CHANNEL)
//        if (!TextUtils.isEmpty(channel)) {
//            Log.i("channel", "buglug: $channel")
//            strategy.appChannel = channel
//        }
//        strategy.appVersion = String.format("%d.%d.%d", com.julun.lingmeng.common.BuildConfig.MAJOR_VERSION, com.julun.lingmeng.common.BuildConfig.MINOR_VERSION, com.julun.lingmeng.common.BuildConfig.PATCH_VERSION)
//        //                CrashReport.initCrashReport(applicationContext, strategy)
//        CrashReport.initCrashReport(applicationContext, AppInitUtils.getBuglyId(), false, strategy)
//
//        CrashReport.setIsDevelopmentDevice(applicationContext, BuildConfig.DEBUG)
//    }
//
//}

/**
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/12 0012
 * @detail
 */
class JPushTask : Task() {
    override fun run() {
        //初始化极光
        JPushInterface.setDebugMode(BuildConfig.DEBUG)    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(mContext.applicationContext)            // 初始化 JPush
        /*一键登录相关*/
//        JVerificationInterface.setDebugMode(BuildConfig.DEBUG)
//        JVerificationInterface.init(mContext)
    }
}

/**
 * @author WanZhiYuan
 * @date 2020/05/14
 * @since 4.32
 */
//class OPPOTask : Task() {
//    override fun dependsOn(): List<Class<out Task>>? {
//        val task = ArrayList<Class<out Task>>()
//        task.add(ARouterTask::class.java)
//        return task
//    }
//
//    override fun run() {
//        //oppo联运
//        val oppoService = ARouter.getInstance().build(ARouterConstant.OPPOPAY_MANAGER).navigation() as? OppoPayService
//        oppoService?.initSDK(mContext)
//    }
//}

/**
 * 实名认证
 * @author WanZhiYuan
 * @date 2020/05/14
 * @since 4.32
// */
//class RealTask : Task() {
//    override fun dependsOn(): List<Class<out Task>>? {
//        val task = ArrayList<Class<out Task>>()
//        task.add(ARouterTask::class.java)
//        return task
//    }
//
//    override fun run() {
//        //初始化实名认证SDK
//        LingMengService.getService(IRealNameService::class.java)?.initSDK(mContext.applicationContext)
//    }
//}

/**
 * @author WanZhiYuan
 * @date 2020/05/14
 * @since 4.32
 */
class TencentTask : Task() {
    override fun run() {
        HuanQueApp.wxApi = WXApiManager.initWeiXin(mContext, BuildConfig.WX_APP_ID)
//        AppInitUtils.qqApi = Tencent.createInstance(BuildConfig.QQ_APP_ID, mContext)
    }
}

/**
 * 友盟放到UI线程执行，一定要提前初始化，要么会丢统计数据
 * @author WanZhiYuan
 * @date 2020/05/14
 * @since 4.32
 */
//class UmengTask : MainTask() {
//
//    override fun run() {
//        OpenInstallManager.initUmengParams()
//    }
//}
