package com.julun.huanque.common.ui.web

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.events.LoginOutEvent
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.common.widgets.ActionBean
import com.julun.huanque.common.widgets.BaseWebView
import com.julun.huanque.common.widgets.WebViewAdapter
import com.julun.huanque.common.widgets.WebViewFileListener
import com.luck.picture.lib.PictureSelectionModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.android.synthetic.main.activity_web.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.io.File

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/17 18:05
 *
 *@Description: WebActivity
 *
 */
@Route(path = ARouterConstant.WEB_ACTIVITY)
class WebActivity : BaseActivity() {
    companion object {
        //        羚萌公约
//        val LM_PUBLIC_RULE = LMUtils.getDomainName(CommonInit.getInstance().getApp().getString(R.string.html_spec))

        fun startWeb(mActivity: Activity, url: String) {
            val extra = Bundle()
            extra.putString(BusiConstant.WEB_URL, url)
//            extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, true)
            val intent = Intent(mActivity, WebActivity::class.java)
            intent.putExtras(extra)
            mActivity.startActivity(intent)
        }

    }

    private var url: String? = ""

    private var shareObj: ShareObject? = null
    private var shareFragment: BaseDialogFragment? = null

    //上个页面来源
    private var source: String? = null

    //右上角操作
    private var operate: String? = ""

    override fun getLayoutId(): Int = R.layout.activity_web

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initWebView()
        initData()
    }

    private fun initWebView() {
        webView.setWebViewListener(object : WebViewAdapter() {
            override fun titleChange(title: String) {
                this@WebActivity.tvTitle?.text = title
            }

            override fun onUrlAction(url: String) {
                takeUrlAction(url)
            }

            override fun jumpToRecharge() {
                //                    val intent = Intent(this@WebActivity, RechargeCenterActivityNew::class.java)
//                    intent.putExtra(IntentParamKey.NEED_GO_HOME.name, true)
//                    startActivity(intent)
//                val bundle = Bundle()
//                bundle.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//                JuLunLibrary.getInstance().recharge(this@WebActivity, bundle)


            }

            override fun perFormAppAction(actionBean: ActionBean) {
                performAction(actionBean)
            }
        })
        webView?.setWebViewFileListener(object : WebViewFileListener {
            override fun openAndCheckFile(accept: String) {
                when {
                    accept.startsWith("image") -> {
                        //图片
                        checkPermission()
                    }
                    accept.startsWith("video") -> {
                        //视频
                        checkPermission(true)
                    }
                }
            }
        })
    }

    /**
     * 这个处理 必须不同地方 专门处理 因为跳转可能不同
     */
    fun performAction(actionBean: ActionBean) {
        try {
            if (actionBean.action != null)
                when (actionBean.action) {
                    BaseWebView.OPENUSERHOMEPAGE -> {
                        if (!MixedHelper.checkLogin()) {
                            return
                        }
                        if (actionBean.param != null) {
                            val roomId = actionBean.param!!["roomId"] as Int
                            val userId = actionBean.param!!["userId"] as Int
                        }
                    }
                    BaseWebView.JUMPTORECHARGE -> {
//                        val reload = actionBean.param!!["isRefresh"] as? String
//                        val bundle = Bundle()
//                        if (reload != null && reload == "True") {
//                            bundle.putBoolean(IntentParamKey.RELOAD.name, reload.toBoolean())
//                            JuLunLibrary.getInstance().recharge(this, bundle, BusiConstant.RequestCode.RELOAD_URL)
//                            return
//                        }
//                        bundle.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//                        JuLunLibrary.getInstance().recharge(this, bundle)
                    }
                    BaseWebView.JUMPTOROOM -> {
//                        if (actionBean.param != null) {
//                            val roomId = actionBean.param!!["roomId"] as Int
//                            val gameType: String? = actionBean.param!!["gameType"] as? String
//                            val intent = Intent(this@WebActivity, PlayerActivity::class.java)
//                            intent.putExtra(IntentParamKey.PROGRAM_ID.name, roomId)
//                            if (gameType != null) {
//                                intent.putExtra(IntentParamKey.OPEN_OPERATE.name, OpenOperateBean(gameType,actionBean.param))
//                            }
//                            startActivity(intent)
//                        }
                    }
                    //跳转到活动页面
                    BaseWebView.JUMPTOACTIVITY -> {
                        if (actionBean.param != null) {
//                            val url = actionBean.param!!["url"] as String
//                            val intent = Intent(this@WebActivity, WebActivity::class.java)
//                            val extra = Bundle()
//                            extra.putString(BusiConstant.PUSH_URL, url)
//                            extra.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                            intent.putExtras(extra)
//                            startActivity(intent)
                        }
                    }
                    //打开登录
                    BaseWebView.OPENLOGINLAYER -> {
//                            val intent = Intent(this@WebActivity, GuideLoginActivity::class.java)
//                            startActivity(intent)
//                            ARouter.getInstance().build(ARouterConstant.GUIDE_LOGIN_ACTIVITY).navigation()
//                        showLoginDialogFragment()
                        EventBus.getDefault().post(LoginOutEvent())
                    }
                    //打开分享
                    BaseWebView.OPENSHARELAYER -> {
                        showShareView()
                    }
                    //打开分享 带参数
                    BaseWebView.OPENSHAREWITHPARAM -> {
                        actionBean.param?.let { param ->
                            val shareObj = ShareObject().apply {
                                this.shareUrl = param["shareUrl"] as? String
                                this.shareTitle = param["shareTitle"] as? String
                                this.shareContent = param["shareContent"] as? String
                                this.sharePic = param["sharePic"] as? String
                                this.shareKey = param["shareKey"] as? String
                                this.shareKeyId = param["shareId"] as? String
                                this.shareId = param["shareId"] as? String
                            }
                            showShareView(shareObj)
                        }
                    }
//                    BaseWebView.MUSTREFRESHGIFT -> {
//                        //通知刷新礼物面板
//                        EventBus.getDefault().post(EventAction(EventBusCode.RefreshGift))
//                    }
                    BaseWebView.CLOSEWEBVIEW -> {
                        if (actionBean.param != null) {
                            val type = actionBean.param!!["type"] as String
                            if ("recharge" == type) {
                                logger.info("是网页支付完成 有没有充不确定")
                                EventBus.getDefault()
                                    .post(PayResultEvent(PayResult.IS_PAY, PayType.WXPayH5))
                                finish()
                            }
                        }
                    }
                    else -> {
                        logger.info("DXC  action = ${actionBean.action}")
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理特殊url跳转
     */
    private fun takeUrlAction(mUrl: String) {
        var isQQInstal = true
        try {
            logger.info("当前的url:$mUrl")
            // 以下固定写法
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(mUrl)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        } catch (e: Exception) {
            // 防止没有安装的情况
            isQQInstal = false
            e.printStackTrace()
        }
        if (isQQInstal)
            finish()
    }

    private fun initData() {
        url = intent.getStringExtra(BusiConstant.WEB_URL)
        source = intent.getStringExtra(IntentParamKey.SOURCE.name)
        operate = intent.getStringExtra(IntentParamKey.OPERATE.name)
        shareObj = intent.getSerializableExtra(BusiConstant.ROOM_AD) as? ShareObject
        val params = webView.layoutParams as? RelativeLayout.LayoutParams
        when (StringHelper.getQueryString(url ?: "", "nav")) {
            "n" -> {
                //样式一
                rlTitleRootView.backgroundResource = R.color.transparent
                ivBack.imageResource = R.mipmap.icon_back_white_01
                ivClose.imageResource = R.mipmap.icon_close_white_01
                ivOperation.imageResource = R.mipmap.icon_more_white_01
                tvTitle.hide()
                params?.below(-1)
            }
            else -> {
                //默认样式
                rlTitleRootView.backgroundResource = R.color.white
                ivBack.imageResource = R.mipmap.icon_back_black_01
                ivClose.imageResource = R.mipmap.icon_close_black_01
                ivOperation.imageResource = R.mipmap.icon_more_black_01
                tvTitle.show()
//                ivClose.hide()
                params?.below(R.id.rlTitleRootView)
            }
        }
        if (params != null) {
            webView.layoutParams = params
        }

        if (shareObj != null) {
            ivOperation.visibility = View.VISIBLE
            ivOperation.onClickNew {
                showShareView()
            }
        }
        if (operate?.isNotEmpty() == true) {
            tvOperation.textColor = Color.parseColor("#333333")
            tvOperation.show()
            tvOperation.text = when (operate) {
                "royal_hot" -> "规则说明"
                else -> "操作"
            }

            tvOperation.onClickNew {
                when (operate) {

                }

            }
        }
        ULog.i("当前的url:$url")
        //这个是否返回首页不太一样 默认就是返回
//        goHome = intent.getBooleanExtra(IntentParamKey.EXTRA_FLAG_GO_HOME.name, true)
        tvTitle.text = "欢鹊"
//        tvTitle.onClick {   startActivity(Intent(this@WebActivity, MainActivity::class.java)) }
        ivBack.onClickNew { onBackPressed() }
        ivClose.onClickNew { super.onBackPressed() }
        webView.loadUrl(url)
    }

    private fun showShareView() {
//        shareObj?.let {
//            shareFragment = shareFragment
//                ?: ARouter.getInstance().build(ARouterConstant.SHARE_COMMON_FRAGMENT)
//                    .withObject("shareObj", it) as? BaseDialogFragment
//            shareFragment?.show(supportFragmentManager, "ShareCommonFragment")
//        }
    }

    private fun showShareView(shareObject: ShareObject) {
//        shareFragment = shareFragment
//            ?: ARouter.getInstance().build(ARouterConstant.SHARE_COMMON_FRAGMENT)
//                .withObject("shareObj", shareObject) as? BaseDialogFragment
//        shareFragment?.show(supportFragmentManager, "ShareCommonFragment")
    }

    override fun onBackPressed() {
        if (webView?.canGoBack()!!) {
            webView?.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        web_layout?.removeView(webView)
        webView?.removeAllViews()
//        webView?.setWebViewTitleChange(null)
        webView?.setWebViewListener(null)
        webView?.setWebViewFileListener(null)
        webView?.destroy()
        super.onDestroy()
    }


    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ActivityCodes.RESPONSE_CODE_REFRESH) {
            //刷新code 重载url
            webView?.reload()
            return
        }
        when (requestCode) {
            PictureConfig.CHOOSE_REQUEST -> {
                //获取到图片或者视频地址
                val selectList = PictureSelector.obtainMultipleResult(data)
                if (selectList.size > 0) {
                    val path = selectList[0].path
                    logger.info("文件path = $path")
                    val file = File(path)
                    if (!file.exists()) {
                        logger.info("要上传的图片或视频不存在")
                        ToastUtils.show("没有找到要发送的图片或视频")
                        webView?.clear()
                        return
                    }
                    try {
                        webView?.uploadCallback(Uri.fromFile(file))
                    } catch (e: NullPointerException) {
                        webView?.clear()
                    }
                } else {
                    webView?.clear()
                }
            }
        }
    }

    /**
     * 检查相关权限
     * @author WanZhiYuan
     * @date 2020/04/22
     */
    private fun checkPermission(isVideo: Boolean = false) {

        PermissionUtils.checkPhotoPermission(this, callback = {
            if (it) {
                chooseVideoOrPhoto(isVideo)
            } else {
                webView?.clear()
            }
        })
    }

    /**
     * 选择视频或者图片
     * @author WanZhiYuan
     * @date 2020/04/22
     */
    private fun chooseVideoOrPhoto(isVideo: Boolean = false) {
        PictureSelector.create(this@WebActivity).apply {
            modelConfig(
                !isVideo, if (isVideo) {
                    openGallery(PictureMimeType.ofVideo())
                } else {
                    openGallery(PictureMimeType.ofImage())
                }
            )
        }
    }

    /**
     * 图片或者视频相关配置
     * @author WanZhiYuan
     * @date 2020/04/22
     * @param isPhoto 图库或者是视频相关配置
     * @param model 构建的视频或者是图库对应属性
     */
    private fun modelConfig(isPhoto: Boolean, model: PictureSelectionModel) {
        if (isPhoto) {
            model.theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)// 是否可预览图片
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .enableCrop(false)// 是否裁剪
                .compress(false)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .isGif(false)// 是否显示gif图片
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
                .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
                .isDragFrame(false)
                .circleDimmedLayer(false)// 是否圆形裁剪
                .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .rotateEnabled(false) // 裁剪是否可旋转图片
                .scaleEnabled(false)// 裁剪是否可放大缩小图片
                .forResult(PictureConfig.CHOOSE_REQUEST)
        } else {
            model.theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
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
                .forResult(PictureConfig.CHOOSE_REQUEST)   //结果回调onActivityResult code
        }
    }
}

