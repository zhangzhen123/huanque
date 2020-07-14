package com.julun.huanque.common.widgets

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import kotlinx.android.synthetic.main.layout_pager_header.view.*
import org.jetbrains.anko.imageResource


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/13 11:23
 *
 *@Description: HeaderPageView 封装一个头部控件 方便跨module调用（由于layout不能跨module使用kt的自动导入）
 *
 */
class HeaderPageView : FrameLayout {

    private val logger = ULog.getLogger("HeaderPageView")

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
        LayoutInflater.from(context).inflate(R.layout.layout_pager_header, this)
    }

    //向外暴露几个常用view
    val imageViewBack:ImageView by lazy { ivback }
    val textTitle:TextView by lazy { tvTitle }
    val textOperation:TextView by lazy { tvOperation }
    val imageOperation:ImageView by lazy { ivOperation }
    fun initHeaderView(
        @DrawableRes emptyRes: Int = R.mipmap.icon_back_black_01,
        titleTxt: String = "",
        @DrawableRes operateRes: Int? = null,
        operateTxt: String? = null
    ) {
        ivback.imageResource = emptyRes
        if (titleTxt.isNotEmpty()) {
            tvTitle.text = titleTxt
        }
        if (operateRes != null) {
            ivOperation.show()
            ivOperation.imageResource = operateRes
        } else {
            ivOperation.hide()
        }

        if (operateTxt != null) {
            tvOperation.show()
            tvOperation.text = operateTxt
        } else {
            tvOperation.hide()
        }

    }
}
