package com.julun.huanque.common.utils.svga

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.FileUtils
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

import okhttp3.*
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
/**
 *
 * lru内存缓冲池
 *
 * 资源解压缓存目录 File(context.cacheDir.absolutePath + "/" + cacheKey + "/")
 *
 * 网络请求缓存目录 FileUtils.getCachePath(mContext), "svgaCache") 有效期60天
 *
 *
 * 本地资源asset解析使用rxjava线程池  网络资源使用默认okhttp的线程池
 *
 *
 * @author zhangzhen
 * *
 * @data 2017/11/1
 */
object SVGAHelper {

    val logger = ULog.getLogger("SVGAHelper")

    private lateinit var mContext: Context

    //一定要初始化 不然后续都报错
    fun init(context: Context) {
        mContext = context
        resetDownloader()
    }

    private val lruCache: LruCache<String, SVGAVideoEntity> by lazy {
        val size = getMaxCacheSize(mContext)
        object : LruCache<String, SVGAVideoEntity>(size) {
            override fun sizeOf(key: String, value: SVGAVideoEntity): Int {
                //由于对象SVGAVideoEntity内存占用大头是images 这里只计算images 内存占用总和
                return getMapsSize(value.images)
            }
        }
    }
    private val parser: SVGAParser by lazy {
        SVGAParser(context = mContext)
    }

    private fun get(key: String): SVGAVideoEntity? {
//        logger.info("当前的lruCache 情况：${lruCache.size()}/${lruCache.maxSize()}  占比${lruCache.size() * 100/ lruCache.maxSize()}%")
        return lruCache.get(key)
    }

    private fun put(key: String, item: SVGAVideoEntity) {
        lruCache.put(key, item)
    }

    /**
     * 传入的assets
     */
    fun parse(assetsName: String, callback: SVGAParser.ParseCompletion) {
        val key = cacheKey("file:///assets/" + assetsName)
        val item = get(key)
        if (item != null) {
//            logger.info("有缓存 直接读取缓存$assetsName")
            callback.onComplete(item)
        } else {
            Observable.create<SVGAVideoEntity> {
                var svg: SVGAVideoEntity? = null
                mContext.assets.open(assetsName)?.let {
                    svg = parse(it, key)
                }
                if (svg != null)
                    it.onNext(svg!!)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                callback.onComplete(it)
                put(key, it) //将结果存进缓存
//                logger.info("成功获取 当前订阅的的线程：" + Thread.currentThread().name)
            }, {
                callback.onError()
                it.printStackTrace()
            }, {

            })


        }
    }

    /**
     * 这里没办法 私有方法只能反射去获取 再执行
     * 库自带的方法每次都创建线程 太low 这里使用rxjava线程池
     */
    private fun parse(inputStream: InputStream, cacheKey: String): SVGAVideoEntity? {
        val cls = parser!!.javaClass
        //获得类的私有方法
        val method = cls.getDeclaredMethod("parse", InputStream::class.java, String::class.java)
        method.isAccessible = true //没有设置就会报错
        //调用该方法
        val videoItem = method.invoke(parser, inputStream, cacheKey) as? SVGAVideoEntity

//        val videoItem= parser.parse(inputStream,cacheKey)
        return videoItem
    }

    /**
     * 传入的url 网络请求的解析
     */
    fun parse(url: URL, callback: SVGAParser.ParseCompletion) {
        val key = cacheKey(url)
        val item = get(key)
        if (item != null) {
//            logger.info("有缓存 直接读取缓存")
            callback.onComplete(item)
        } else {
            parser.parse(url, object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    put(key, videoItem) //将结果存进缓存
                    callback.onComplete(videoItem)
                }

                override fun onError() {
                    callback.onError()
                }

            })
        }

    }

    /**
     * 传入的url 网络请求的解析 不使用内存缓存 （对于同一个svga同时多个view播放时 不能使用缓存 否则只会有一个播放）
     */
    fun parseNoCache(url: String, callback: SVGAParser.ParseCompletion) {

        parser.parse(URL(url), object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                callback.onComplete(videoItem)
            }
            override fun onError() {
                callback.onError()
            }

        })

    }

    //总的调用入口
    fun startParse(url: String, callback: SVGAParser.ParseCompletion) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            parse(URL(url), callback)
        } else {
            parse(url, callback)
        }
    }

    private fun cacheKey(str: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(str.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        val sb = StringBuffer()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun cacheKey(url: URL): String {
        return cacheKey(url.toString())
    }

    /**
     * 清空缓冲池
     */
    fun clear() {
        lruCache.evictAll()
    }

    /**
     * @param mContext
     * *
     * @return 设置最大的缓存内存值 这里设置成1/9 最大内存
     */

    fun getMaxCacheSize(mContext: Context): Int {
        val mActivityManager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val maxMemory = Math.min(mActivityManager.memoryClass * ByteConstants.MB, Integer.MAX_VALUE)
        if (maxMemory < 32 * ByteConstants.MB) {
            return 4 * ByteConstants.MB
        } else if (maxMemory < 64 * ByteConstants.MB) {
            return 6 * ByteConstants.MB
        } else {
            // We don't want to use more ashmem on Gingerbread for now, since it doesn't respond well to
            // native memory pressure (doesn't throw exceptions, crashes app, crashes phone)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                return 8 * ByteConstants.MB
            } else {
                return maxMemory / 6
            }
        }
    }

    /**
     * @param images
     * *
     * @return 返回该集合所占用内存总和
     */
    fun getMapsSize(images: HashMap<String, Bitmap>): Int {
        val entrySet = images.entries
        var size = 0
        for ((_, value) in entrySet) {
            size += getBitmapSize(value)
        }
        return size
    }

    //计算bitmap大小
    private fun getBitmapSize(value: Bitmap): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //当在4.4以上手机复用的时候 需要通过此函数获得占用内存
            value.allocationByteCount
        } else value.byteCount
    }

    /**
     * 设置下载器，这是一个可选的配置项。
     *
     * @param parser
     */
    private fun resetDownloader() {
        logger.info("开始设置网络请求")
        //缓存文件夹
        val cacheFile = File(FileUtils.getCachePath(mContext), "svgaCache")
        //网络磁盘缓存大小为50M
        val cacheSize = 50L * ByteConstants.MB
        //创建缓存对象
        val cache = Cache(cacheFile, cacheSize)
        val client = OkHttpClient.Builder().cache(cache).build()


        parser.fileDownloader = object : SVGAParser.FileDownloader() {
            override fun resume(url: URL, complete: Function1<InputStream, Unit>, failure: Function1<Exception, Unit>) {

                val cacheBuild = CacheControl.Builder()
                    .maxAge(60, TimeUnit.DAYS).build()
                val request = Request.Builder().cacheControl(
                    cacheBuild
                ).url(url).get().build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        failure(e)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
//                        logger.info("cacheResponse:" + response.cacheResponse() + "networkResponse:" + response.networkResponse())
                        if (response.body() != null)
                            complete(response.body()!!.byteStream())
                    }
                })

            }
        }
    }

}
