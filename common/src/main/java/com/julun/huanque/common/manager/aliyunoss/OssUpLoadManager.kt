package com.julun.huanque.common.manager.aliyunoss

import android.content.Context
import android.util.LruCache
import android.util.SparseArray
import android.view.View
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.VideoUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

/**
 *
 *@author zhangzhen
 *@data 2019/2/16
 * oss上传管理器
 *
 **/
object OssUpLoadManager {
    private val logger = ULog.getLogger("OssUpLoadManager")

    //OSS的上传下载
    private lateinit var mService: OssService
//    private lateinit var mVideoService: OssService //视频上传位置


    const val REPORT_USER_POSITION = "user/report"// 举报用户的位置
    const val COVER_POSITION = "user/cover"// 封面路径
    const val VOICE_POSITION = "user/voice"// 语音文件
    const val HEAD_POSITION = "user/head"// 头像路径
    const val MESSAGE_PIC = "user/message"// im图片路径
    const val POST_POSITION = "social/post"//动态路径

    const val APPLY_POSITION = "user/apply/pic"//标签图片


    var isUploading = false

    const val CODE_SUCCESS = 1
    const val CODE_ERROR = 0

    /**
     * 这里新增一个缓存 专门缓存已经上传OSS成功的的文件 以提高上传有效率  因为经常业务逻辑的错误会需要重复上传相同的文件 导致浪费
     */
    private val ossFileCache: LruCache<String, String> = LruCache<String, String>(100)

    /**
     * 必须初始化
     */
    fun initOss(applicationContext: Context) {
        val credentialProvider: OSSCredentialProvider
        //使用自己的获取STSToken的类
        logger.info("STS_SERVER_URL:${OssConfig.STS_SERVER_URL}")
        credentialProvider = OSSAuthCredentialsProvider(OssConfig.STS_SERVER_URL)
        val conf = ClientConfiguration()
        conf.connectionTimeout = 2 * 60 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 2 * 60 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 3 // 最大并发请求书，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
        val oss = OSSClient(applicationContext, OssConfig.OSS_ENDPOINT, credentialProvider, conf)
        mService = OssService(oss, OssConfig.BUCKET_NAME)
//        mVideoService = OssService(oss, OssConfig.BUCKET_VIDEO_NAME)
    }

    /**
     * [fileList] 文件本地地址 [position]图片存储位置 [callback]上传反馈
     *
     */
    fun uploadFiles(fileList: MutableList<String>, position: String, callback: (code: Int, list: MutableList<String>?) -> Unit) {
//        isUploading = true
        val observable = Observable.fromIterable(fileList)
        val map = mutableMapOf<String, String>()
        observable.flatMap { file ->
            logger.info("开始任务队列：${Thread.currentThread()}")
            Observable.just(file).observeOn(Schedulers.computation()).map { f ->
                logger.info("当前的文件：" + f + "当前的线程：${Thread.currentThread()}")
                val name = "$position/${StringHelper.uuid2()}.${FileUtils.getFilextension(f)}"
                var curresult = ""
                if (ossFileCache.get(f) != null) {
                    curresult = ossFileCache.get(f)
                    map[f] = curresult
                    logger.info("有缓存直接使用：$f----$curresult")
                } else {
                    val success = mService.syncPutImage(name, f, null)
                    if (success) {
                        curresult = name
                        map[f] = curresult
                        //
                        ossFileCache.put(f, curresult)
                    }
                }

                curresult
            }
        }.toList().toObservable()/*.subscribeOn(Schedulers.io())*/
            .observeOn(AndroidSchedulers.mainThread()).subscribe({

                if (it.size == fileList.size) {
                    //重新排序
                    val resultList = mutableListOf<String>()
                    fileList.forEach { item ->
                        val value = map[item]
                        if (value != null && value.isNotEmpty()) {
                            resultList.add(value)
                        }
                    }
                    logger.info("结果：$resultList ")
                    if (resultList.size == fileList.size) {
                        callback(CODE_SUCCESS, resultList)
                    } else {
                        callback(CODE_ERROR, null)
                    }
                } else {
                    callback(CODE_ERROR, null)
                }
//            isUploading = false
            }, {
                //            isUploading = false
                callback(CODE_ERROR, null)
                it.printStackTrace()
            })


    }

    /**
     *  上传单个文件
     *  [path] 文件地址
     *  [position]文件存储位置
     *  [callback]上传反馈带进度条
     */

    fun uploadFile(path: String, position: String, callback: FileUploadCallback? = null) {

        Observable.just(path).observeOn(Schedulers.io()).map { f ->
            logger.info("当前的文件：$f")
            val name = "$position/${StringHelper.uuid2()}.${FileUtils.getFilextension(f)}"
            var result = ""
            if (ossFileCache.get(f) != null) {
                result = ossFileCache.get(f)
                logger.info("有缓存直接使用：$f----$result")
            } else {
                val success = mService.syncPutImage(name, f, object : OssCallback<PutObjectRequest, PutObjectResult> {
                    override fun onProgress(request: PutObjectRequest?, currentSize: Long, totalSize: Long) {
                        Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
                            callback?.onProgress(currentSize, totalSize)
                        }.subscribe()
                    }

                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientException: ClientException?,
                        serviceException: ServiceException?
                    ) {

                    }
                })
                if (success) {
                    result = name
                    ossFileCache.put(f, result)
                }

            }
            result
        }.subscribe({
            if (it.isNotEmpty()) {
                callback?.onResult(CODE_SUCCESS, it)
            } else {
                callback?.onResult(CODE_ERROR)
            }
        }, {
            callback?.onResult(CODE_ERROR)
        })


    }

    var currentVideoPic: String? = null

    /**
     * [videoPath]本地视频文件地址
     * [position]要上传的视频存储目录
     * [ImagePosition]要上传的预览图目录
     * [callback]回调
     * [highImage]是否需要高质量预览图
     */
    fun uploadVideo(
        videoPath: String,
        position: String,
        ImagePosition: String,
        callback: VideoUploadCallback? = null,
        highImage: Boolean = false
    ) {
        isUploading = true
        val bitmap = if (highImage) {
            VideoUtils.getVideoFirstFrame(File(videoPath))
        } else {
            VideoUtils.getVideoThumbnail(videoPath)
        }
        val vf = File(videoPath)
        val bFile = FileUtils.bitmap2File(
            bitmap
                ?: return, vf.nameWithoutExtension, CommonInit.getInstance().getApp()
        ) ?: return
        currentVideoPic = bFile.path
        val sWidth = bitmap.width
        val sHigh = bitmap.height
        val list = mutableListOf<String>().apply {
            add(videoPath)
            add(bFile.path)
        }
        var resultVideoPath = ""
        var resultPicPath = ""
        val observable = Observable.fromIterable(list)
        observable.flatMap { file ->
            //代表是缩略图
            if (file.contains(".png")) {
                Observable.just(file).observeOn(Schedulers.io()).map { f ->
                    logger.info("当前的文件：$f")
                    val name =
                        "$ImagePosition/${StringHelper.uuid2()}.${FileUtils.getFilextension(
                            file
                        )}"
                    var curResult = ""
                    if (ossFileCache.get(f) != null) {
                        curResult = ossFileCache.get(f)
                    } else {
                        val success = mService.syncPutImage(name, f, null)
                        if (success) {
                            curResult = name
                            resultPicPath = curResult
                            ossFileCache.put(f, curResult)
                        }
                    }
                    curResult
                }
            } else {
                Observable.just(file).observeOn(Schedulers.io()).map { f ->
                    logger.info("当前的文件：$f")
//                    val name = "$VIDEO_POSITION/${SessionUtils.getUserId()}/${StringHelper.uuid2()}.${FileUtils.getFilextension(file)}"
                    val name = "$position/${StringHelper.uuid2()}.${FileUtils.getFilextension(file)}"
                    var curResult = ""
                    if (ossFileCache.get(f) != null) {
                        curResult = ossFileCache.get(f)
                    } else {
                        val success = mService.syncPutImage(name, f, object : OssCallback<PutObjectRequest, PutObjectResult> {
                            override fun onProgress(request: PutObjectRequest?, currentSize: Long, totalSize: Long) {
                                Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
                                    callback?.onProgress(currentSize, totalSize)
                                }.subscribe()
                            }

                            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                            }

                            override fun onFailure(
                                request: PutObjectRequest?,
                                clientException: ClientException?,
                                serviceException: ServiceException?
                            ) {
//                            Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete({
//                                callback.onResult(CODE_ERROR)
//                            }).subscribe()
                            }
                        })
                        if (success) {
                            curResult = name
                            resultVideoPath = curResult
                            ossFileCache.put(f, curResult)
                        }
                    }

                    curResult
                }
            }
        }.toList().toObservable()/*.subscribeOn(Schedulers.io())*/
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                val filterList = it.filter { str -> str.isNotEmpty() }
                logger.info("结果：$filterList")
                if (filterList.size == 2) {
                    callback?.onResult(CODE_SUCCESS, resultVideoPath, resultPicPath, sWidth, sHigh)
                } else {
                    callback?.onResult(CODE_ERROR)
                }
//            currentVideoPic=null
                isUploading = false
            }, {
                //            currentVideoPic=null
                isUploading = false
                callback?.onResult(CODE_ERROR)
                it.printStackTrace()
            })
    }

    interface VideoUploadCallback {
        fun onProgress(currentSize: Long, totalSize: Long)

        //最后两个时缩略图的宽高
        fun onResult(code: Int, videoPath: String = "", imgPath: String = "", width: Int = 0, height: Int = 0)
    }


    interface FileUploadCallback {
        fun onProgress(currentSize: Long, totalSize: Long)

        fun onResult(code: Int, imgPath: String? = null)
    }

}