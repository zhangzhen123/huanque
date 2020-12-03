package com.julun.huanque.message.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleAccountMsg
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/12/3 14:49
 *@描述 分身账号
 */
class AccountMsgAdapter : BaseQuickAdapter<SingleAccountMsg, BaseViewHolder>(R.layout.recycler_item_account_msg) {
    override fun convert(holder: BaseViewHolder, item: SingleAccountMsg) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 56f, 56f)
        holder.setText(R.id.tv_nickname, item.nickname)
            .setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, item.content))
            .setText(R.id.tv_time, TimeUtils.formatMessageListTime(item.msgTime))
            .setVisible(R.id.tv_time, item.msgTime > 0L)
            .setText(R.id.tv_unread_count, "${item.msgCnt}")
            .setVisible(R.id.tv_unread_count, item.msgCnt > 0)


    }
}