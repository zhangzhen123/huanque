package com.julun.huanque.message.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundColor

/**
 *@创建者   dong
 *@创建时间 2021/1/21 9:52
 *@描述 私信排序adapter
 */
class PrivateOrderAdapter : BaseQuickAdapter<ChatGift, BaseViewHolder>(R.layout.recycler_item_private_order) {
    //默认选中第二个
    var selectPosition = 1
    override fun convert(holder: BaseViewHolder, item: ChatGift) {
        val sdv_gift_no_sel = holder.getView<SimpleDraweeView>(R.id.sdv_gift_no_sel)
        sdv_gift_no_sel.loadImage(item.pic, 52f, 52f)
        val sdv_gift_sel = holder.getView<SimpleDraweeView>(R.id.sdv_gift_sel)
        sdv_gift_sel.loadImage(item.pic, 65f, 65f)

        val adapterPosition = holder.adapterPosition
        holder.setText(R.id.tv_gift_count_sel, item.giftName)
            .setText(R.id.tv_gift_count, item.giftName)
            .setText(R.id.tv_price_sel, "${item.beans}")
            .setText(R.id.tv_price, "${item.beans}")
            .setVisible(R.id.tv_most_send_sel, adapterPosition == 1)
            .setVisible(R.id.tv_most_send, adapterPosition == 1)
            .setVisible(R.id.card_sel, adapterPosition == selectPosition)
            .setVisible(R.id.card_no_sel, adapterPosition != selectPosition)
    }
}