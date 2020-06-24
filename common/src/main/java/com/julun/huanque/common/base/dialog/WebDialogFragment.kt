package com.julun.huanque.common.base.dialog

import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.DensityUtils

import com.julun.huanque.common.widgets.WebViewAdapter
import kotlinx.android.synthetic.main.dialog_web.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

/**
 * 一个通用的H5弹窗
 * @createAuthor WanZhiYuan
 * @createDate 2019/10/16
 * @iterativeVersion 4.19.1
 */
class WebDialogFragment(@NonNull url: String) : BaseDialogFragment() {

    private var mUrl: String = url
    private var mTitle: String? = null
    private var mTextSize: Float? = null
    private var mTextColor: Int? = null
    private var mWidth: Float = 0f
    private var mHeight: Float = 0f
    private var mGravity: Int = Gravity.BOTTOM
    private var mTopAndBottomMargin:Int = 0
    private var mLeftAndRightMargin:Int = 0
    private var mBg: Int = Color.WHITE
    private var mLeftResid: Int? = null
    private var mRightResid: Int? = null
    private var mNeedAnimator: Boolean = false

    private var mCallback: WebCallback? = null

    class WebCallback(val onLeft: () -> Unit = {}, val onRight: (Boolean?) -> Unit = {})

    fun setViewParams(width: Float, height: Float, gravity: Int,topAndBottomMargin:Int = 0,leftAndRightMargin:Int = 0): WebDialogFragment {
        mWidth = width
        mHeight = height
        mGravity = gravity
        mTopAndBottomMargin = topAndBottomMargin
        mLeftAndRightMargin = leftAndRightMargin
        return this
    }

    fun setDialogBg(bg: Int): WebDialogFragment {
        mBg = bg
        return this
    }

    fun setDialogTitle(title: String? = null, size: Float? = null, color: Int? = null): WebDialogFragment {
        mTitle = title
        mTextSize = size
        mTextColor = color
        return this
    }

    fun setLeftView(image: Int): WebDialogFragment {
        mLeftResid = image
        return this
    }

    fun setRightView(image: Int): WebDialogFragment {
        mRightResid = image
        return this
    }

    fun setUrl(url: String): WebDialogFragment {
        mUrl = url
        return this
    }

    fun isNeedAnimator(isNeed: Boolean): WebDialogFragment {
        mNeedAnimator = isNeed
        return this
    }

    fun setListener(callback: WebCallback) {
        this.mCallback = callback
    }

    override fun needEnterAnimation(): Boolean = mNeedAnimator

    override fun onStart() {
        super.onStart()
        var width = WindowManager.LayoutParams.MATCH_PARENT
        var height = WindowManager.LayoutParams.WRAP_CONTENT
        var gravity = Gravity.BOTTOM
        if (mWidth != 0f) {
            width = DensityUtils.dp2px(mWidth)
        }
        if (mHeight != 0f) {
            height = DensityUtils.dp2px(mHeight)
        }
        gravity = mGravity
        val params = dialog?.window?.attributes
        params?.dimAmount = 0.1f
        dialog?.window?.attributes = params
        setDialogParams(width = width, height = height, gravity = gravity)
    }

    override fun getLayoutId(): Int = R.layout.dialog_web

    override fun initViews() {
        prepareViews()
        prepareEvents()
    }

    override fun reCoverView() {
        prepareViews()
    }
    private fun prepareViews() {
        try {
            clWebRootiVew.backgroundResource = mBg
            tvWebTitle.text = mTitle ?: ""
            if (mTextSize == null || mTextSize == 0f) {
                tvWebTitle?.textSize = 16f
            } else {
                tvWebTitle?.textSize = mTextSize!!
            }
            if (mTextColor == null || mTextColor == 0) {
                tvWebTitle?.textColor = MixedHelper.getColor(R.color.white)
            } else {
                tvWebTitle?.textColor = MixedHelper.getColor(mTextColor!!)
            }
            if (mLeftResid == null) {
                ivWebLeft.hide()
            } else {
                ivWebLeft.show()
                ivWebLeft.imageResource = mLeftResid!!
            }
            if (mRightResid == null) {
                ivWebRight.hide()
            } else {
                ivWebRight.show()
                ivWebRight.imageResource = mRightResid!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val params = webView.layoutParams as? ConstraintLayout.LayoutParams
        if(mTopAndBottomMargin > 0){
            params?.topToBottom =  DensityUtils.dp2px(mTopAndBottomMargin).toInt()
            params?.bottomMargin =  DensityUtils.dp2px(mTopAndBottomMargin).toInt()
        }
        if(mLeftAndRightMargin > 0){
            params?.leftMargin = DensityUtils.dp2px(mLeftAndRightMargin).toInt()
            params?.rightMargin = DensityUtils.dp2px(mLeftAndRightMargin).toInt()
        }

        webView.loadUrl(AppHelper.getDomainName(mUrl))
    }

    private fun prepareEvents() {
        webView.setWebViewListener(object : WebViewAdapter() {
            override fun titleChange(title: String) {
                if (tvWebTitle != null && TextUtils.isEmpty(this@WebDialogFragment.tvWebTitle.text) && !TextUtils.isEmpty(title)) {
                    this@WebDialogFragment.tvWebTitle?.text = title
                    mTitle = title
                }
            }
        })
        ivWebRight.onClickNew {
            this.mCallback?.let {
                it.onRight
            }
        }
        ivWebLeft?.onClickNew {
            this.mCallback?.let {
                it.onLeft()
            }
        }
    }
}
