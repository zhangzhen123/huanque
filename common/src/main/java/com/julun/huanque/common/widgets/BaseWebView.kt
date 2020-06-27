package com.julun.huanque.common.widgets

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.interceptors.HeaderInfoHelper

import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.adblock.AdBlocker
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer

import java.io.Serializable
import java.lang.ref.WeakReference


/**
 * 自定义webView 与native交互 过滤广告 进度条
 * Created by djp on 2016/11/23.
 */
class BaseWebView : WebView {

    companion object {
        //跳转到支付界面
        const val JUMPTORECHARGE = "jumpToRecharge"

        //跳转到指定房间
        const val JUMPTOROOM = "jumpToRoom"

        //跳转到新的网页
        const val JUMPTOACTIVITY = "jumpToActivity"

        //跳转到背包
        const val JUMPTOBAG = "jumpToBag"

        //显示快捷支付
        const val JUMPTOQUICKRECHARGE = "jumpToQuickRecharge"

        //需要刷新礼物面板
        const val MUSTREFRESHGIFT = "mustRefreshGift"

        //关闭
        const val CLOSETURNTABLE = "closeTurntable"

        //打开登录页面
        const val OPENLOGINLAYER = "openLoginLayer"

        //打开分享 无参数
        const val OPENSHARELAYER = "openShareLayer"

        //打开分享 带参数
        const val OPENSHAREWITHPARAM = "openShareWithParam"

        //直播间banner跳转H5
        const val ROOMBANNER = "roomBanner"

        //关闭当前的网页activity
        const val CLOSEWEBVIEW = "closeWebView"

        //直播间banner匹配弹窗
        const val OPEN_ACTIVITY_YAER = "openActivityLayer"

        //直播间banner弹窗匹配类型 h5 -> h5弹窗 native -> 原生弹窗
        const val H5 = "h5"
        const val NATIVE = "native"

        //新增打开用户主页
        const val OPENUSERHOMEPAGE = "openUserHomePage"

        //打开萌币商城
        const val OPEN_COINS_SHOP = "openCoinsShop"
    }


    val logger = ULog.getLogger("BaseWebView")
    private var progressBar: ProgressBar? = null
    private var webViewListener: WebViewListener? = null
    private var mFileListener: WebViewFileListener? = null
    private var mContext: Context? = null

    constructor(context: Context) : this(context, null)

    /**
     *
     */
    constructor(context: Context, attr: AttributeSet?) : super(context.applicationContext, attr) {
        mContext = context

        if (attr != null) {
            val a = context.obtainStyledAttributes(attr, R.styleable.BaseWebView)
            val needProcessBar = a.getBoolean(R.styleable.BaseWebView_need_process_bar, true)
            if (needProcessBar) {
                progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
                progressBar!!.layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, DensityHelper.dp2px(2f), 0, 0)
                progressBar!!.progressDrawable = resources.getDrawable(R.drawable.progress_bar_web)
                addView(progressBar)

            }
            //用完需要回收
            a?.recycle()
        }
        webChromeClient = WebChromeClient()
        webViewSetting()
        if (!isInEditMode) {
            getLoginState()
        }
        //移除webview隐藏的接口，等保需要
        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");

//        if (Build.VERSION.SDK_INT >= 19) // KITKAT
//        {
//            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        }
    }

    fun setWebViewListener(webViewTitleChange: WebViewListener?) {
        this.webViewListener = webViewTitleChange
    }

    fun setWebViewFileListener(fileCallback: WebViewFileListener?) {
        this.mFileListener = fileCallback
    }

    private fun webViewSetting() {
        if (isInEditMode) {
            return
        }
        val settings = this.settings
        settings.javaScriptCanOpenWindowsAutomatically = true
//        settings.userAgentString = "huanque_Android"
        settings.setSupportZoom(true)
        if (Build.VERSION.SDK_INT >= 17)
            settings.mediaPlaybackRequiresUserGesture = false
        settings.defaultTextEncodingName = "UTF-8"
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.javaScriptEnabled = true
//        settings.pluginState = WebSettings.PluginState.ON
        this.webViewClient = MyWebViewClient()

        this.addJavascriptInterface(this, "android")
    }

    /***
     * 新增js调起本地方法 跳转到充值界面
     *
     */
    @JavascriptInterface
    fun jumpToRecharge() {
        //跳转到充值页面
        handler?.post {
            webViewListener?.jumpToRecharge()
        }
    }

    /**
     *  @action：要app执行什么操作
    @extra:相应操作的参数没有可不传
     *
     */
    @JavascriptInterface
    fun perFormAppAction(actionBean: String): Boolean {
        logger.info("调用外部操作成功+++++action:$actionBean ")
        try {
            //兼容老的版本的调用 只有jumpToLiveRoom这个action 这里转化成jumpToRoom 变成ActionBean
            if (actionBean.contains("jumpToLiveRoom")) {
                val action = JsonUtil.deserializeAsObject<OldActionBean>(
                    actionBean,
                    OldActionBean::class.java
                )
                val ac = ActionBean().apply {
                    this.action = "jumpToRoom"
                    this.param = HashMap()
                    this.param!!.put("roomId", action.param!!.toInt())
                }
                handler?.post {
                    webViewListener?.perFormAppAction(ac)
                }
            } else {
                val action =
                    JsonUtil.deserializeAsObject<ActionBean>(actionBean, ActionBean::class.java)
                handler.post {
                    webViewListener?.perFormAppAction(action)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @JavascriptInterface
    fun getUserId(): String {
        logger.info("调用getUserId")
        return "${SessionUtils.getUserId()}"
    }

    @JavascriptInterface
    fun getSessionId(): String {
        val sessionId = SessionUtils.getSessionId()
        logger.info("调用getSessionId$sessionId")
        return sessionId
    }

    @JavascriptInterface
    fun getRoomId(): String {
        val roomId = webViewListener?.getRoomId() ?: ""
        logger.info("调用getRoomId$roomId")
        return roomId
    }

    @JavascriptInterface
    fun getMobileDeviceInfo(): String {
        logger.info("调用getMobileDeviceInfo")
        return HeaderInfoHelper.getMobileDeviceInfoToJson()
    }

    override fun setOverScrollMode(mode: Int) {
        try {
            super.setOverScrollMode(mode)
        } catch (e: Throwable) {
            val messageCause = if (e.cause == null) e.toString() else e.cause.toString()
            val trace = Log.getStackTraceString(e)
            if (trace.contains("android.content.pm.PackageManager\$NameNotFoundException")
                || trace.contains("java.lang.RuntimeException: Cannot load WebView")
                || trace.contains("android.webkit.WebViewFactory\$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed")
            ) {
                e.printStackTrace()
            } else {
                throw e
            }
        }

    }

    private var disposable: Disposable? = null
    var isFirst = true
    private fun getLoginState() {
        //todo
//        if (disposable?.isDisposed == false) {
//            disposable?.dispose()
//        }
//        val consumer = StaticConsumer(this)
//        disposable = SessionUtils.getLoginState()
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(/*{
//                    logger.info("当前的登录状态改变了：" + it)
//                    if (!isFirst) {
//                        this.reload()
//                    }
//                    isFirst = false
//                }*/consumer
//            )
    }

    override fun onDetachedFromWindow() {
        disposable?.dispose()
        super.onDetachedFromWindow()
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        val lp = progressBar?.layoutParams as? LayoutParams
        lp?.x = scrollX
        lp?.y = scrollY
        progressBar?.layoutParams = lp
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
    }

    inner class WebChromeClient : android.webkit.WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            //为解决6.0系统，这个api会调用两次，而且第一次是显示url的系统bug
            if (view.url?.contains(title) != true) {
                //设置页面title
                webViewListener?.titleChange(title)
            }
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                progressBar?.progress = newProgress
                progressBar?.postDelayed({
                    progressBar?.visibility = View.GONE
                }, 500)
                webViewListener?.onLoadComplete()
            } else {
                if (progressBar?.visibility == View.GONE) {
                    progressBar?.visibility = View.VISIBLE
                }
                progressBar?.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            logger.info("来自WebView:" + consoleMessage?.message())
            if (BuildConfig.DEBUG) {
                LogWriteUtils.addLog("WebView", consoleMessage?.message())
            }
            return super.onConsoleMessage(consoleMessage)
        }

        //Android >= 5.0
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            logger.info("来自WebView:onShowFileChooser")
            val acceptTypes = fileChooserParams?.acceptTypes
            if (acceptTypes == null || acceptTypes.isEmpty()) return super.onShowFileChooser(
                webView,
                filePathCallback,
                fileChooserParams
            )
            try {
                fileCallBackOne = filePathCallback
                mFileListener?.openAndCheckFile(acceptTypes[0])
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
//                reportCrash("解析webview acceptTypes 异常", e)
            }
            return true
        }

        //Android  >= 4.1
        fun openFileChooser(
            valueCallback: ValueCallback<Uri>,
            acceptType: String,
            capture: String
        ) {
            logger.info("来自WebView:openFileChooser")
            fileCallBackTwo = valueCallback
            mFileListener?.openAndCheckFile(acceptType)
        }
    }

    var fileCallBackOne: ValueCallback<Array<Uri>>? = null
    var fileCallBackTwo: ValueCallback<Uri>? = null

    inner class MyWebViewClient : WebViewClient() {
        //适配8.0webview无法返回上一级的问题 Android8.0开始WebView的shouldOverrideUrlLoading返回值是false才会自动重定向
        //并且无需调用loadUrl，与8.0之前的效果刚好相反
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            logger.info("DXC  url = $url")
            // 如下方案可在非微信内部WebView的H5页面中调出微信支付
            if (url?.startsWith(BusiConstant.WEIXIN_FLAG) == true) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse(url)
                CommonInit.getInstance().getCurrentActivity()?.startActivity(intent)
                return true
            }
            if (url != null)
                if (!StringHelper.isHttpUrl(url)) {
                    webViewListener?.onUrlAction(url)
                } else {
                    if (Build.VERSION.SDK_INT < 26) {
                        val extraHeaders: HashMap<String, String> = HashMap()
                        if (url.startsWith("https://wx.tenpay.com/")) {
                            val header: String = if (BuildConfig.DEBUG) {
                                "http://testwechat.katule.cn/show/"
                            } else {
                                "https://www.51lm.tv/show/"
                            }
                            extraHeaders["Referer"] = header

                        }
                        view!!.loadUrl(url, extraHeaders)
                    }

                }
            return Build.VERSION.SDK_INT < 26
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler!!.proceed()
        }

        private val loadedUrls = HashMap<String, Boolean>()

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            val cUrl = request.url.toString()
            val ad: Boolean?
            if (!loadedUrls.containsKey(cUrl)) {
                ad = AdBlocker.isAd(cUrl)
                loadedUrls.put(cUrl, ad)
            } else {
                ad = loadedUrls[cUrl]
            }
            return if (ad != null && ad) {
                logger.info("检测到广告:$cUrl")
                AdBlocker.createEmptyResource()
            } else {
                super.shouldInterceptRequest(view, request)
            }
        }


        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            val ad: Boolean?
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url)
                loadedUrls.put(url, ad)
            } else {
                ad = loadedUrls[url]
            }
            return if (ad != null && ad) {
                logger.info("检测到广告:$url")
                AdBlocker.createEmptyResource()
            } else {
                super.shouldInterceptRequest(view, url)
            }
        }
    }

    /**
     * 回调通知h5
     * @author WanZhiYuan
     * @date 2020/04/22
     */
    fun uploadCallback(uri: Uri) {
        if (fileCallBackOne != null) {
            fileCallBackOne?.onReceiveValue(arrayOf(uri))
            fileCallBackOne = null
        }
        if (fileCallBackTwo != null) {
            fileCallBackTwo?.onReceiveValue(uri)
            fileCallBackTwo = null
        }
    }

    fun clear() {
        //清除相关的callback，要不然下一次就接收不到相关的消息
        if (fileCallBackOne != null) {
            fileCallBackOne?.onReceiveValue(null)
            fileCallBackOne = null
        }
        if (fileCallBackTwo != null) {
            fileCallBackTwo?.onReceiveValue(null)
            fileCallBackTwo = null
        }
    }
}

@UiThread
interface WebViewListener {
    fun titleChange(title: String)
    fun jumpToRecharge()
    fun perFormAppAction(actionBean: ActionBean)
    fun onLoadComplete()
    fun getRoomId(): String
    fun onUrlAction(url: String)
}

@UiThread
interface WebViewFileListener {
    /**
     * 打开并选择文件
     * @param accept image 表示打开类似于选择头像的弹窗，video表示获取手机上的视频列表页
     */
    fun openAndCheckFile(accept: String)
}

open class WebViewAdapter : WebViewListener {

    override fun titleChange(title: String) {
    }

    override fun jumpToRecharge() {
    }

    override fun perFormAppAction(actionBean: ActionBean) {
    }

    override fun onLoadComplete() {
    }

    override fun getRoomId(): String {
        return ""
    }

    override fun onUrlAction(url: String) {
    }
}

class StaticConsumer(webView: BaseWebView) : Consumer<Boolean> {
    private var mWebViewRef: WeakReference<BaseWebView>? = null

    init {
        mWebViewRef = WeakReference(webView)
    }

    override fun accept(t: Boolean) {
        ULog.i("当前的登录状态改变了：$t")
        val webView = mWebViewRef?.get() ?: return
        if (!webView.isFirst) {
            webView.reload()
        }
        webView.isFirst = false
    }

}

class ActionBean : Serializable {
    var action: String? = null
    var param: HashMap<String, Any>? = null
}

class OldActionBean : Serializable {
    var action: String? = null
    var param: String? = null
}