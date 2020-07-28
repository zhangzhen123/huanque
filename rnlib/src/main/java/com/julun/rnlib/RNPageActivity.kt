package com.julun.rnlib

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.infer.annotation.Assertions
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.devsupport.DoubleTapReloadRecognizer
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.bean.events.AliAuthCodeEvent
import com.julun.huanque.common.bean.events.RPVerifyResult
import com.julun.huanque.common.bean.events.VoiceSignEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.NetUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Android 通过Activity打开RN页面
 */

class RNPageActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {

    companion object {
        /**
         * 通过fragment去加载一个rn页面 入口
         * [moduleName]需要打开的rn模块
         */
        fun start(activity: ComponentActivity, moduleName: String, initialProperties: Bundle? = null) {
            val intent = Intent(activity, RNPageActivity::class.java)
            intent.putExtra(RnConstant.MODULE_NAME, moduleName)
            intent.putExtra(RnConstant.INITIAL_PROPERTIES, initialProperties)
            activity.startActivity(intent)
        }
    }

    private lateinit var mReactRootView: ReactRootView
    private var mReactInstanceManager: ReactInstanceManager? = null
    private val mDeveloperSupport = true
    private var mDoubleTapReloadRecognizer: DoubleTapReloadRecognizer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            EventBus.getDefault().register(this)
            val intent = intent
            val moduleName = intent.getStringExtra(RnConstant.MODULE_NAME)
            val initialProperties = intent.getBundleExtra(RnConstant.INITIAL_PROPERTIES)
            RnManager.curActivity = this

            mReactRootView = ReactRootView(this)
            mReactInstanceManager = RnManager.createReactInstanceManager(application)
            fun mustRun() {
                // 这个"App1"名字一定要和我们在index.js中注册的名字保持一致AppRegistry.registerComponent()
                mReactRootView.startReactApplication(mReactInstanceManager, moduleName, initialProperties)
                mDoubleTapReloadRecognizer = DoubleTapReloadRecognizer()

                setContentView(mReactRootView)
                mReactRootView.setEventListener {
                    logger("RNPageActivity 我已加载完成")
                }
            }
            if (!mReactInstanceManager!!.hasStartedCreatingInitialContext()) {
                mReactInstanceManager!!.addReactInstanceEventListener(object :
                    ReactInstanceManager.ReactInstanceEventListener {
                    override fun onReactContextInitialized(context: ReactContext) {
                        mReactInstanceManager!!.removeReactInstanceEventListener(this)
                        logger("ReactInstanceManager 刚刚加载完成了")
                        mustRun()
                    }
                })
            } else {
                logger("ReactInstanceManager 已经加载完成了")
                mustRun()
            }


        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show("加载rn模块出错了")
            finish()
        }
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (useDeveloperSupport) {
            if (keyCode == KeyEvent.KEYCODE_MENU) { //Ctrl + M 打开RN开发者菜单
                mReactInstanceManager!!.showDevOptionsDialog()
                return true
            }
            val didDoubleTapR = Assertions.assertNotNull(mDoubleTapReloadRecognizer)
                .didDoubleTapR(keyCode, currentFocus)
            if (didDoubleTapR) { //双击R 重新加载JS
                mReactInstanceManager!!.devSupportManager.handleReloadJS()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private val useDeveloperSupport: Boolean
        private get() = mReactInstanceManager != null && mDeveloperSupport

    override fun onPause() {
        super.onPause()
        mReactInstanceManager?.onHostPause(this)

    }

    override fun onResume() {
        super.onResume()
        mReactInstanceManager?.onHostResume(this, this)

    }

    override fun onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager!!.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mReactInstanceManager?.onHostDestroy(this)
        mReactRootView.unmountReactApplication()
        RnManager.curActivity = null
        RnManager.clearPromiseMap()
        EventBus.getDefault().unregister(this)
    }

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            for (media in selectList) {
                Log.i("图片-----》", media.path)
            }
            startUploadImages(selectList)
        } else if (requestCode == 1002) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            logger("收到视频=$selectList")
            for (media in selectList) {
                Log.i("视频-----》", media.path)
            }
            startUploadVideo(currentRootPath, currentImagePath, selectList)


        }

    }

    private fun startUploadImages(selectList: List<LocalMedia>) {
        try {
            if (selectList.isNotEmpty()) {
                val pathList = mutableListOf<String>()
                selectList.forEach {
                    val isGif = PictureMimeType.isGif(it.pictureType)
                    //动图直接上传原图
                    when {
                        isGif -> {
                            pathList.add(it.path)
                        }
                        it.isCompressed -> {
                            pathList.add(it.compressPath)
                        }
                        else -> {
                            pathList.add(it.path)
                        }
                    }
                }
                if (!mLoadingDialog.isShowing) {
                    mLoadingDialog.showDialog()
                }
                OssUpLoadManager.uploadFiles(pathList, OssUpLoadManager.COVER_POSITION) { code, list ->
                    if (code == OssUpLoadManager.CODE_SUCCESS) {
                        logger("上传oss成功结果的：$list")
                        val writeArrayList = Arguments.createArray()
                        list?.forEach {
                            writeArrayList.pushString(it)
                        }
                        RnManager.promiseMap[RnManager.uploadPhotos]?.resolve(writeArrayList)
                        currentRootPath = ""
                        currentImagePath = ""
                    } else {
                        ToastUtils.show("上传失败，请稍后重试")
                        RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 上传失败，请稍后重试 通知rn回调")
                    }
                    if (mLoadingDialog.isShowing) {
                        mLoadingDialog.dismiss()
                    }

                }

            } else {
                RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 没有选择 通知rn回调")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 图片返回出错了 通知rn回调")
            logger("图片返回出错了")
        }

    }

    private fun startUploadVideo(position: String, imagePosition: String, selectList: List<LocalMedia>) {
        try {
            if (!NetUtils.isNetConnected()) {
                ToastUtils.show(this.resources.getString(R.string.upload_no_network))
                RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 网络异常 通知rn回调")
                return
            }

            if (selectList.isNotEmpty()) {
                val media = selectList[0]
                //视频要求 大于15秒
                if (currentMinTime * 1000L > media.duration) {
                    ToastUtils.show(resources.getString(R.string.video_duration_is_out))
                    RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 选择的视频时长不符合要求 通知rn回调")
                    return
                }
                //大于100M的不给上传
                val size = FileUtils.getFileOrFilesSize(media.path, FileUtils.SIZETYPE_KB)//单位kb
                logger("当前视频的大小：${size}kb")
                if (currentMaxSize < size) {
                    ToastUtils.show(resources.getString(R.string.video_size_is_out))
                    RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 选择的视频大小不符合要求 通知rn回调")
                    return
                }
                if (!mLoadingDialog.isShowing) {
                    mLoadingDialog.showDialog()
                }
                OssUpLoadManager.uploadVideo(media.path, position, imagePosition, object : OssUpLoadManager.VideoUploadCallback {
                    override fun onProgress(currentSize: Long, totalSize: Long) {
                        logger("视频上传进度条：$currentSize / $totalSize")
                    }

                    override fun onResult(code: Int, videoPath: String, imgPath: String, width: Int, height: Int) {
                        if (mLoadingDialog.isShowing) {
                            mLoadingDialog.dismiss()
                        }
                        if (code == OssUpLoadManager.CODE_SUCCESS) {
                            logger("视频上传成功 ：${Thread.currentThread()}")
                            val map = Arguments.createMap()
                            map.putString("videoURL", videoPath)
                            map.putString("imageURL", imgPath)
                            map.putInt("size", size.toInt())
                            map.putInt("time", (media.duration / 1000).toInt())
                            RnManager.promiseMap[RnManager.uploadVideo]?.resolve(map)
                            currentRootPath = ""
                            currentImagePath = ""
                        } else {
                            RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 上传失败 通知rn回调")
                        }

                    }
                })

            } else {
                RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 没有选择 通知rn回调")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RnManager.promiseMap[RnManager.uploadVideo]?.reject("-1", "视频上传功能 视频返回出错了 通知rn回调")
        }

    }

    private fun checkPhotoPermissions(max: Int, type: Int) {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger("获取权限成功")
                        goToPictureSelectPager(max, type)
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    /**
     *打开相册选择
     */
    private fun goToPictureSelectPager(max: Int, type: Int) {
        if (type == PictureConfig.TYPE_IMAGE) {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .minSelectNum(1)// 最小选择数量
                .maxSelectNum(max)
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)// 是否可预览图片
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                .enableCrop(true)// 是否裁剪
                .compress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.compressSavePath(getPath())//压缩图片保存地址
                .glideOverride(150, 150)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .forResult(1001)

        } else if (type == PictureConfig.TYPE_VIDEO) {
            //只传单视频
            PictureSelector.create(this).openGallery(PictureMimeType.ofVideo())
                .theme(com.julun.huanque.common.R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.SINGLE)
                .previewVideo(true)// 是否可预览视频
                .isCamera(true)// 是否显示拍照按钮
                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                .compress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.compressSavePath(getPath())//压缩图片保存地址
                .glideOverride(150, 150)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
//                .selectionMedia(selectList)// 是否传入已选图片
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                //.rotateEnabled(true) // 裁剪是否可旋转图片
                //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                //.videoQuality()// 视频录制质量 0 or 1
                //.videoSecond()//显示多少秒以内的视频or音频也可适用
                //.recordVideoSecond()//录制视频秒数 默认60s
                .forResult(1002)
        }

        //结果回调onActivityResult code
    }

    //当前的需要存储的根目录
    var currentRootPath: String = ""
    var currentImagePath: String = ""

    //当前的最大文件限制
    var currentMaxSize: Int = 100 * 1024

    //当前的最小视频时长
    var currentMinTime: Int = 15

    /**
     * [type]打开相册操作分类
     * [PictureConfig.TYPE_IMAGE]图片 [PictureConfig.TYPE_VIDEO]视频 [PictureConfig.TYPE_AUDIO]音频
     */
    fun openPhotoSelect(
        type: Int,
        max: Int = 1,
        rootPath: String = "",
        imagePath: String = "",
        maxSize: Int = 100 * 1024,
        minTime: Int = 15
    ) {

        if (type == PictureConfig.TYPE_VIDEO) {
            currentRootPath = rootPath
            currentImagePath = imagePath
        } else if (type == PictureConfig.TYPE_IMAGE) {
            currentRootPath = rootPath
        }
        if (maxSize != 0) {
            currentMaxSize = maxSize
        }
        if (minTime != 0) {
            currentMinTime = minTime
        }

        runOnUiThread {
            checkPhotoPermissions(max, type)
        }
    }

    /**
     * 打开页面操作
     */
    fun openPager(pageName: String, params: ReadableMap? = null) {
        runOnUiThread {
            when (pageName) {
                RnConstant.PRIVATE_MESSAGE_PAGE -> {
                    val id = params!!.getString("userId")
                    if (id != null) {
                        val userId = id.toLong()
                        val bundle = Bundle()
                        bundle.putLong(ParamConstant.TARGET_USER_ID, userId)
                        //                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle)
                            .navigation(this)
                    }
                }
                RnConstant.TELEPHONE_CALL_PAGE -> {
                    val id = params!!.getString("userId")
                    if (id != null) {
                        val rxPermissions = RxPermissions(this)
                        rxPermissions.requestEachCombined(Manifest.permission.RECORD_AUDIO)
                            .subscribe { permission ->
                                when {
                                    permission.granted -> {
                                        logger("获取权限成功")
                                        val userId = id.toLong()
                                        val bundle = Bundle()
                                        bundle.putLong(ParamConstant.UserId, userId)
                                        bundle.putString(ParamConstant.TYPE, ConmmunicationUserType.CALLING)
                                        //                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
//                                        bundle.putString(ParamConstant.OPERATION, OperationType.CALL_PHONE)
                                        ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle)
                                            .navigation(this)
                                    }
                                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                                        ToastUtils.show("权限无法获取")
                                    else -> {
                                        logger("获取权限被永久拒绝")
                                        val message = "无法获取到录音权限，请手动到设置中开启"
                                        ToastUtils.show(message)
                                    }
                                }

                            }

                    }
                }
                RnConstant.SEND_GIFT_PAGE -> {
                    val id = params!!.getString("userId")
                    if (id != null) {
                        val userId = id.toLong()
                        val bundle = Bundle()
                        bundle.putLong(ParamConstant.TARGET_USER_ID, userId)
                        //                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
                        bundle.putString(ParamConstant.OPERATION, OperationType.OPEN_GIFT)
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle)
                            .navigation(this)
                    }
                }
                RnConstant.RECORD_VOICE_PAGE -> {
                    ARouter.getInstance().build(ARouterConstant.VOICE_SIGN_ACTIVITY).navigation()
                }
                RnConstant.REPORT_USER_PAGE -> {
                    val id = params!!.getString("userId")
                    if (id != null) {
                        val userId = id.toLong()
                        val extra = Bundle()
                        extra.putLong(ParamConstant.TARGET_USER_ID, userId)
                        extra.putInt(ParamConstant.REPORT_TYPE, 0)
                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
                    }
                }
                RnConstant.REAL_NAME_AUTH_PAGE -> {
                    ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY).navigation()
                }
                RnConstant.SHARE_INVITE_PAGE->{
                    ARouter.getInstance().build(ARouterConstant.INVITE_SHARE_ACTIVITY).withString(IntentParamKey.TYPE.name,"Invite").navigation()
                }
            }

        }

    }


    /**
     ******************************************* 以下是收到各种刷新广播的Rn处理****************************************************
     */
    /**
     * 语音签名后结果返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveVoiceSignEvent(event: VoiceSignEvent) {
        logger("收到语音签名修改成功")
        RnManager.promiseMap[RnConstant.RECORD_VOICE_PAGE]?.resolve(true)
    }

    /**
     * 实名认证结果返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveRP(event: RPVerifyResult) {
        logger("收到实名认证结果：${event.result}")
        if (event.result == RealNameConstants.TYPE_SUCCESS) {
            RnManager.promiseMap[RnConstant.REAL_NAME_AUTH_PAGE]?.resolve(true)
        } else {
            RnManager.promiseMap[RnConstant.REAL_NAME_AUTH_PAGE]?.resolve(false)
        }
    }
}