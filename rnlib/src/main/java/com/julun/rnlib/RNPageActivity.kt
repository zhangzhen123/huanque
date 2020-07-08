package com.julun.rnlib

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.facebook.infer.annotation.Assertions
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.Arguments
import com.facebook.react.devsupport.DoubleTapReloadRecognizer
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType

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
        mReactRootView = ReactRootView(this)
        mReactInstanceManager = RnManager.createReactInstanceManager(application)
        RnManager.curActivity = this
        val intent = intent
        val moduleName = intent.getStringExtra(RnConstant.MODULE_NAME)
        val initialProperties = intent.getBundleExtra(RnConstant.INITIAL_PROPERTIES)
        // 这个"App1"名字一定要和我们在index.js中注册的名字保持一致AppRegistry.registerComponent()
        mReactRootView.startReactApplication(mReactInstanceManager, moduleName, initialProperties)
        mDoubleTapReloadRecognizer = DoubleTapReloadRecognizer()
        mReactRootView.setEventListener {
            Log.d("RNPageFragment", "我已加载完成")
        }
        setContentView(mReactRootView)
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
    }

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {

                val selectList = PictureSelector.obtainMultipleResult(data)
                logger("收到图片=$selectList")
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
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
                            if (mLoadingDialog.isShowing) {
                                mLoadingDialog.dismiss()
                            }
                        } else {
                            ToastUtils.show("上传失败，请稍后重试")
                            RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 上传失败，请稍后重试 通知rn回调")
                            if (mLoadingDialog.isShowing) {
                                mLoadingDialog.dismiss()
                            }
                        }

                    }

                } else {
                    RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 没有选择 通知rn回调")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RnManager.promiseMap[RnManager.uploadPhotos]?.reject("-1", "图片上传功能 图片返回出错了 通知rn回调")
            logger("图片返回出错了")
        }
    }

    private fun checkPermissions(max: Int) {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger("获取权限成功")
                        goToPictureSelectPager(max)
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
    private fun goToPictureSelectPager(max: Int) {
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
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }

    fun openPhotoSelect(max: Int) {
        runOnUiThread {
            checkPermissions(max)
        }
    }


}