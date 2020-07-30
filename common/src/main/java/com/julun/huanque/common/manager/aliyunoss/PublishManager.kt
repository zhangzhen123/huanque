package com.julun.huanque.common.manager.aliyunoss

import com.julun.huanque.common.R
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.forms.PublishVideoForm
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.RequestCaller
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.PublishService
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.utils.NetUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.luck.picture.lib.entity.LocalMedia


/**
 *
 *@author zhangzhen
 *@data 2019/2/18
 *管理发布的 包括上传文件和后台发布 当前只管视频发布
 **/
object PublishManager : RequestCaller {

    private val logger = ULog.getLogger("PublishManager")
    private val UUID = StringHelper.uuid()
    override fun getRequestCallerId(): String = UUID

    val service: PublishService by lazy { Requests.create(PublishService::class.java) }

    const val PUBLISH_SUCCESS = 2//发布成功
    const val PUBLISH_FAIL = -1//发布失败
    const val UPLOAD_FAIL = -2//上传文件失败
    const val PUBLISH_NONE = 0//初始状态
    const val PUBLISH_ING = 1//进行中
    var publish_state = PUBLISH_NONE //记录当前的发布状态

    val total = 100

    private var currentMedia: LocalMedia? = null
    private var currentContent: String? = null

    private var isPublish: Boolean = false

    /**
     * 重式上一次的操作
     */
    fun retryLastUpload() {
        if (currentMedia != null && currentContent != null) {
            publishVideo(currentMedia!!, currentContent!!)
        } else {
            ToastUtils.show(CommonInit.getInstance().getApp().getString(R.string.upload_retry_error))
        }
    }

    /**
     * 返回当前正在上传的视频缩略图文件地址 null代表当前没有视频上传任务
     */
    fun getCurrentVideohumbnail(): String? {
        return OssUpLoadManager.currentVideoPic
    }

    /**
     * 如果当前有失败的任务 此操作是清空掉这个任务
     */
    fun deleteCurrentErrorTask() {
        OssUpLoadManager.currentVideoPic = null
        publish_state = PUBLISH_NONE
        currentMedia = null
        currentContent = null
    }

    /**
     * 返回当前是否正在忙碌
     */
    fun isBusy(): Boolean {
        return OssUpLoadManager.isUploading || isPublish
    }

    var callback: PublishCallback? = null
    fun deleteCallback() {
        callback = null
    }

    fun publishVideo(media: LocalMedia, content: String) {
        if (!NetUtils.isNetConnected()) {
            ToastUtils.show(CommonInit.getInstance().getApp().resources.getString(R.string.upload_no_network))
            return
        }
        if (OssUpLoadManager.isUploading || isPublish) {
            ToastUtils.show(CommonInit.getInstance().getApp().resources.getString(R.string.is_upload_tips))
            return
        }
        publish_state = PUBLISH_ING
        currentMedia = media
        currentContent = content
        OssUpLoadManager.uploadVideo(media.path,"视频目录","缩略图目录", object : OssUpLoadManager.VideoUploadCallback {
            override fun onProgress(currentSize: Long, totalSize: Long) {
//                logger.info("上传进度 current:$currentSize total:$totalSize 线程：${Thread.currentThread()}")
                val current = currentSize / totalSize.toFloat() * total * 90 / 100  //取90%
                callback?.onProgress(current.toInt(), total)
            }

            override fun onResult(code: Int, videoPath: String, imgPath: String, width: Int, height: Int) {
                //支持后台上传 这里界面可能不存在
                if (code == OssUpLoadManager.CODE_SUCCESS) {
//                    logger.info("上传成功 ：${Thread.currentThread()}")
                    var picSize = ""
                    picSize = "$width,$height"
                    val form = PublishVideoForm().apply {
                        this.content = content
                        this.duration = (media.duration / 1000f + 0.5).toLong()
                        this.url = videoPath
                        this.pic = imgPath
                        if (picSize.isNotBlank()) {
                            this.picSize = picSize
                        }
                    }
                    startPublishVideo(form)
                } else {
                    publish_state = UPLOAD_FAIL
                    callback?.onResult(UPLOAD_FAIL)
                    publishFail()
//                    ToastUtils.show(huanqueInit.getInstance().getContext().resources.getString(R.string.upload_video_fail))
                }
            }

        })

    }

    //全局提醒失败
    private fun publishFail() {
        val activity = CommonInit.getInstance().getCurrentActivity()
        //todo
//        val tips = TipShowFragment.newInstance(
//                CommonInit.getInstance().getApp().resources.getString(R.string.upload_fail), false)
//        if (activity != null && activity is AppCompatActivity) {
//            tips.show(activity.supportFragmentManager, "tipsFail")
//        }
        ToastUtils.show("发布失败，请稍后重试")
    }

    private fun publishSuccess() {
        //只有成功后才置空
        OssUpLoadManager.currentVideoPic = null
        val activity = CommonInit.getInstance().getCurrentActivity()
        //todo
//        val tips = TipShowFragment.newInstance(
//                CommonInit.getInstance().getApp().resources.getString(R.string.publish_success), true)
//        if (activity != null && activity is AppCompatActivity) {
//            tips.show(activity.supportFragmentManager, "tipsSuccess")
//        }
        ToastUtils.show("发布成功")
    }

    //发布视频
    private fun startPublishVideo(form: PublishVideoForm) {
        isPublish = true
        service.publishVideo(form)
            .handleResponse(makeSubscriber<VoidResult> {
                //成功后直接设置进度100
                callback?.onProgress(total, total)
                publishSuccess()
                publish_state = PUBLISH_SUCCESS
                callback?.onResult(PUBLISH_SUCCESS)
                currentMedia = null
                currentContent = null
            }.ifError {
//                    ToastUtils.show(huanqueInit.getInstance().getContext().resources.getString(R.string.publish_video_fail))
                publishFail()
                publish_state = PUBLISH_FAIL
                callback?.onResult(PUBLISH_FAIL)
            }.withFinalCall {
                isPublish = false
            })

    }
}

interface PublishCallback {
    fun onProgress(currentSize: Int, totalSize: Int)
    fun onResult(code: Int)
}