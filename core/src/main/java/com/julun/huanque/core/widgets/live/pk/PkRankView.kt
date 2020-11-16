package com.julun.huanque.core.widgets.live.pk

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.julun.huanque.common.bean.beans.PkUserRankBean
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.view_pk_rank.view.*
import org.jetbrains.anko.backgroundResource


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/8/21 16:29
 *
 *@Description : pk显示排行itemView 关联[PkMicView]
 *
 */
class PkRankView : FrameLayout {

    private val logger = ULog.getLogger("PkRankView")

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
        LayoutInflater.from(context).inflate(R.layout.view_pk_rank, this)
    }

    fun setIsLeft(left: Boolean) {
        if (left) {
            sdv_header.backgroundResource = R.drawable.shape_pk_rank_head_left
            tv_tag.backgroundResource = R.drawable.shape_pk_rank_tag_left
        } else {
            sdv_header.backgroundResource = R.drawable.shape_pk_rank_head_right
            tv_tag.backgroundResource = R.drawable.shape_pk_rank_tag_right

        }
    }

    fun setData(data: PkUserRankBean, tag: String) {
        sdv_header.loadImage(data.headPic, 33f, 33f)
        tv_tag.text = tag
    }
    fun resetView(tag: String){
        sdv_header.loadImage("", 33f, 33f)
        tv_tag.text = tag
    }
}
