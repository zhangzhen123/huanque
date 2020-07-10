package com.julun.huanque.common.widgets.statepage

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import kotlinx.android.synthetic.main.layout_loading2.view.*
import kotlinx.android.synthetic.main.layout_network_unable.view.*
import kotlinx.android.synthetic.main.layout_state.view.*
import org.jetbrains.anko.imageResource


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/10 16:47
 *
 *@Description: StatePageView 封装一个页面加载状态页 方便页面状态切换
 * 分加载中  空白页  错误页
 *
 */
class StatePageView : FrameLayout {

    private val logger = ULog.getLogger("StatePageView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_state, this)
        setBackgroundResource(R.color.default_bg)
    }

    fun showLoading(loadingTxt: String = "") {
        this.show()
        loading_view.show()
        empty_view.hide()
        error_view.hide()
        if (loadingTxt.isNotEmpty()) {
            loading_text.text = loadingTxt
        }

    }

    fun showEmpty(@DrawableRes emptyRes: Int = R.mipmap.empty_data, emptyTxt: String = "") {
        this.show()
        loading_view.hide()
        empty_view.show()
        error_view.hide()
        no_data_image.imageResource = emptyRes
        if (emptyTxt.isNotEmpty()) {
            emptyText.text = emptyTxt
        }
    }

    fun showError(
        @DrawableRes errorRes: Int = R.mipmap.network_unable,
        errorTxt: String = "",
        showBtn: Boolean = true,
        btnClick: OnClickListener? = null
    ) {
        this.show()
        loading_view.hide()
        empty_view.hide()
        error_view.show()
        no_network_image.imageResource = errorRes
        if (errorTxt.isNotEmpty()) {
            no_network_Text.text = errorTxt
        }
        if (showBtn) {
            tv_error_reload.show()
            tv_error_reload.setOnClickListener(btnClick)
        } else {
            tv_error_reload.hide()
        }
    }

    fun showSuccess() {
        this.hide()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

    }
}
