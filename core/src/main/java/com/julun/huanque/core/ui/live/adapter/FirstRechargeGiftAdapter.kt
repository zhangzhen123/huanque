package com.julun.huanque.core.ui.live.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.GiftIcon
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/10/23 13:40
 *@描述 首充礼物Adapter
 */
class FirstRechargeGiftAdapter : BaseQuickAdapter<GiftIcon, BaseViewHolder>(R.layout.recycler_item_first_recharge_gift) {
    override fun convert(holder: BaseViewHolder, item: GiftIcon) {
        val giftImage = holder.getView<SimpleDraweeView>(R.id.sdvGiftImage)
        ImageUtils.loadImage(giftImage, item.pic, 66f, 50f)
        holder.setText(R.id.tv_price, item.desc)
            .setText(R.id.tvGiftMsg, item.name)
            .setText(R.id.tv_count, item.count)
    }
}