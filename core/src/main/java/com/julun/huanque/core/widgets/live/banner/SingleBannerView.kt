package com.julun.huanque.core.widgets.live.banner

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.animation.AnimatorSetCompat.playTogether
import com.julun.huanque.common.bean.beans.RoomBanner
import com.julun.huanque.common.bean.beans.ShareConfig
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.widgets.WebViewListener
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.viewpager_single_banner.view.*
import org.jetbrains.anko.dip
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2019/9/19 16:39
 *@描述  直播间的单个banner View  图片和WebView
 */
class SingleBannerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.viewpager_single_banner, this)
    }

    //    //显示的动画
//    private var mShowWebSet: AnimatorSet? = null
//    private var mShowWebAnimation: ObjectAnimator? = null
//    private var mShowPicAnimation: ObjectAnimator? = null
//
//    //隐藏的动画
//    private var mShowPicSet: AnimatorSet? = null
//    private var mHideWebAnimation: ObjectAnimator? = null
//    private var mHidePicAnimation: ObjectAnimator? = null
    //缩放动画
    private var mScaleAnimation: ObjectAnimator? = null
    private var data: RoomBanner? = null

    private var mBannerRotationFinishListener: BannerRotationFinishListener? = null

    //banner点击效果
    var mBannerClickListener: BannerClickListener? = null

    fun setBannerRotationFinishListener(listener: BannerRotationFinishListener) {
        mBannerRotationFinishListener = listener
    }

    /**
     * 返回是否在执行反转动画
     */
    fun isRotation(): Boolean {
        return mScaleAnimation?.isRunning == true || mScaleAnimation?.isRunning == true
    }

    /**
     * 设置WebViewListener
     */
    fun setWebViewListener(listener: WebViewListener) {
        webView.setWebViewListener(listener)
    }

    /**
     * 获取页面的数据
     */
    fun getData() = data

    /**
     * 显示数据
     * @param singleBanner banner数据
     * @param showAnimation 是否需要动画
     */
    fun showData(singleBanner: RoomBanner, programId: String, showAnimation: Boolean) {
        data = singleBanner
        if (singleBanner.showWeb) {
            try {
                val config = JsonUtil.deserializeAsObject<ShareConfig>(singleBanner?.extJsonCfg
                        ?: "", ShareConfig::class.java)
                val wHeight = config.maxHeight ?: return
                val query = URL(config.initUrl).query ?: ""
                val url = if (StringHelper.isEmpty(query)) {
                    "${config.initUrl}?pid=$programId&cnt=A&w=${dip(90)}&h=$wHeight"
                } else {
                    "${config.initUrl}&pid=$programId&cnt=A&w=${dip(90)}&h=$wHeight"
                }


                if (showAnimation) {
                    //动画显示H5
                    startAnimation(true, url)
                } else {
                    //显示H5
                    webView.loadUrl(url)
                    webView.show()
                    sdv.hide()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ImageUtils.loadImage(sdv, singleBanner.resUrl, 90f, 46f)
            if (showAnimation) {
                //动画显示Pic
                startAnimation(false)
            } else {
                webView.hide()
                sdv.show()
            }
        }
    }

    /**
     * 开始动画
     * @param showWeb true 表示从图片切换到H5,false表示从H5切换到图片
     */
    private fun startAnimation(showWeb: Boolean, url: String = "") {
        mScaleAnimation?.cancel()
        if (mScaleAnimation == null) {
            mScaleAnimation = mScaleAnimation
                    ?: ObjectAnimator.ofFloat(this, "scaleX", 1f, 0f)
            mScaleAnimation?.duration = 250
            mScaleAnimation?.repeatMode = ValueAnimator.REVERSE
            mScaleAnimation?.repeatCount = 1
        }
        mScaleAnimation?.removeAllListeners()
        mScaleAnimation?.cancel()
        mScaleAnimation?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                if (showWeb) {
                    sdv.hide()
                    webView.loadUrl(url)
                    webView.show()
                } else {
                    sdv.show()
                    webView.hide()
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                mBannerRotationFinishListener?.onFinish()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                if (showWeb) {
                    sdv.show()
                    webView.hide()
                } else {
                    sdv.hide()
                    webView.show()
                }

            }
        })
//        Observable.timer(2, TimeUnit.SECONDS)
//                .bindToLifecycle(this)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ mScaleAnimation?.start() }, {})
        mScaleAnimation?.start()
    }

    /**
     * 刷新web数据
     */
    fun refreshWeb(str: String) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            //效率高，仅4.4以上可用。
            webView?.evaluateJavascript("javascript:__refreshData__('$str')") { }
        } else {
            webView?.loadUrl("javascript:__refreshData__('$str')")
        }
    }

    /**
     * webview是否已经显示
     */
    fun isWebViewShow() = webView.visibility == View.VISIBLE

    override fun onFinishInflate() {
        super.onFinishInflate()
        webView.setBackgroundColor(0)
        webView?.background?.alpha = 0
        sdv.onClickNew {
            mBannerClickListener?.click(data ?: return@onClickNew)
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        mShowPicSet?.removeAllListeners()
//        mShowWebSet?.removeAllListeners()
//        mShowPicSet?.cancel()
//        mShowWebSet?.cancel()
        mScaleAnimation?.removeAllListeners()
        mScaleAnimation?.cancel()
    }
}