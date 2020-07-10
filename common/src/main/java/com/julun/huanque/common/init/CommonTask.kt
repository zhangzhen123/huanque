package com.julun.huanque.common.init

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.utils.ULog
import com.facebook.cache.common.CacheErrorLogger
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.svga.SVGAHelper
import org.jay.launchstarter.Task
import java.io.File


/**
 * ARouter在Debug初始化时间很长，因为要支持 Instant Run，但是Release初始化时间并不长并且只有首次安装后会加载路由表，之后只会读取缓存
 * 但是还是异步初始化吧，能进一步减少冷启动时间
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/13 0013
 */
class ARouterTask : Task() {
    override fun run() {
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()  // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(mContext as Application) // 尽可能早，推荐在Application中初始化
    }

    override fun needRunAsSoon(): Boolean {
        return true
    }

    override fun needWait(): Boolean {
        return true
    }
}

/**
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/12 0012
 */
class RongTask : Task() {
    override fun run() {
        //蛋疼的融云多次初始化的问题,必须把其他的初始化工作放在融云的初始化工作代码快里面,否则将会执行多次(3次)
        RongCloudManager.rongCloudInit(mContext as Application) {}
    }
}

/**
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/13 0013
 */
class SVGATask : Task() {
    override fun run() {
        //初始化svga播放管理器
        SVGAHelper.init(mContext)
    }
}

/**
 * Fresco安装首次初始化时间会稍长，之后冷启动会读取缓存，初始化时间就短了
 * @author WanZhiYuan
 * @since 4.32
 * @date 2020/05/12 0012
 */
class FrescoTask : Task() {

    override fun needWait(): Boolean {
        return true
    }

    override fun run() {
        ULog.i("initFresco")

        //缓存的试着
        val cacheConfigBuilder: DiskCacheConfig.Builder = DiskCacheConfig.newBuilder(mContext)

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
                .setBaseDirectoryName(frescoCacheDir.name)
                .setBaseDirectoryPathSupplier({ frescoCacheDir })


        //总体的配置
        val config: ImagePipelineConfig = OkHttpImagePipelineConfigFactory
                .newBuilder(mContext, Requests.getImageClient())//设置 使用 okhttp 客户端
//                .setSmallImageDiskCacheConfig(cacheConfigBuilder.build())//设置缓存
                .setMainDiskCacheConfig(cacheConfigBuilder.build())
                .setDownsampleEnabled(true)//设置图片压缩时支持多种类型的图片
                .experiment().setDecodeCancellationEnabled(true)
//                .experiment().setNativeCodeDisabled(true) //是否使用底层去加载
                .build()

        Fresco.initialize(mContext, config)
    }
}

class OssTask : Task() {
    override fun run() {
        OssUpLoadManager.initOss(mContext)
    }
}

/**
 * 实名认证
 */
class RealNameTask : Task() {
    override fun dependsOn(): List<Class<out Task>>? {
        val task = ArrayList<Class<out Task>>()
        task.add(ARouterTask::class.java)
        return task
    }

    override fun run() {
        //初始化实名认证SDK
        ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE).navigation()
    }
}
