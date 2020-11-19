package com.julun.huanque.common.base.dialog

import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.GlobalUtils

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
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mGravity: Int = Gravity.BOTTOM
    private var mTopAndBottomMargin: Int = 0
    private var mLeftAndRightMargin: Int = 0
    private var mBg: Int = R.color.white
    private var mLeftResid: Int? = null
    private var mRightResid: Int? = null
    private var mNeedAnimator: Boolean = false

    private var mBottomBtn: String? = null

    private var mCallback: WebCallback? = null

    class WebCallback(val onLeft: () -> Unit = {}, val onRight: (Boolean?) -> Unit = {}, val onBottom: () -> Unit = {})

    fun setViewParams(
        width: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        gravity: Int = Gravity.BOTTOM,
        topAndBottomMargin: Int = 0,
        leftAndRightMargin: Int = 0
    ): WebDialogFragment {
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

    fun isNeedBottomBtn(text: String): WebDialogFragment {
        mBottomBtn = text
        return this
    }

    fun setListener(callback: WebCallback) {
        this.mCallback = callback
    }

    override fun needEnterAnimation(): Boolean = mNeedAnimator

    override fun onStart() {
        super.onStart()
        var width = WindowManager.LayoutParams.MATCH_PARENT
        var height = WindowManager.LayoutParams.MATCH_PARENT
        var gravity = Gravity.BOTTOM
        if (mWidth > 0) {
            width = mWidth
        }
        if (mHeight > 0) {
            height = mHeight
        }
        gravity = mGravity
        val params = dialog?.window?.attributes
        params?.dimAmount = 0.4f
        dialog?.window?.attributes = params
        setDialogSize(width = width, height = height, gravity = gravity)
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
                tvWebTitle?.textColor = GlobalUtils.getColor(R.color.black_333)
            } else {
                tvWebTitle?.textColor = GlobalUtils.getColor(mTextColor!!)
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
            if (mBottomBtn != null) {
                tv_bottom.text = mBottomBtn
                tv_bottom.show()
            } else {
                tv_bottom.hide()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val params = webView.layoutParams as? ConstraintLayout.LayoutParams
        if (mTopAndBottomMargin > 0) {
            params?.topToBottom = DensityHelper.dp2px(mTopAndBottomMargin).toInt()
            params?.bottomMargin = DensityHelper.dp2px(mTopAndBottomMargin).toInt()
        }
        if (mLeftAndRightMargin > 0) {
            params?.leftMargin = DensityHelper.dp2px(mLeftAndRightMargin).toInt()
            params?.rightMargin = DensityHelper.dp2px(mLeftAndRightMargin).toInt()
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
        tv_bottom.onClickNew {
            this.mCallback?.let {
                it.onBottom()
            }

        }
    }
}
