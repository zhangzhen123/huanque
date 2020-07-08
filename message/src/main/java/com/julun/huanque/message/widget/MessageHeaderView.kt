package com.julun.huanque.message.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.julun.huanque.common.bean.MessageHeaderBean
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.message.R
import kotlinx.android.synthetic.main.item_header_conversions.view.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/12/10 10:56
 *
 *@Description: MessageHeaderView 消息头部的单个消息view
 *
 */
class MessageHeaderView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_header_conversions, this)
    }

    fun setViewData(bean: MessageHeaderBean) {
        if (bean.headPic.isNotEmpty()) {
            ImageUtils.loadImage(sdv_header, bean.headPic, 50f, 50f)
        } else {
            ImageUtils.loadImageLocal(sdv_header, bean.headRes)
        }
        tv_nickname.text = bean.title
        tv_content.text = bean.content
        tv_time.text = if (bean.time > 0) {
            TimeUtils.formatDetailTime(bean.time)
        } else {
            ""
        }
        if (bean.messageCount > 0) {
            tv_unread_count.show()
            tv_unread_count.text = "${StringHelper.formatMessageCount(bean.messageCount)}"
        } else {
            tv_unread_count.hide()
        }
    }
}
