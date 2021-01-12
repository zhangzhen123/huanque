package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/12 14:39
 *@描述
 */
class SocialDetailAdapter : BaseQuickAdapter<SocialWishBean, BaseViewHolder>(R.layout.recycler_item_social_detail) {
    override fun convert(holder: BaseViewHolder, item: SocialWishBean) {
        val sdv_wish = holder.getView<SimpleDraweeView>(R.id.sdv_wish)
        sdv_wish.loadImage(item.icon, 64f, 64f)
        holder.setText(R.id.tv_wish_title, item.wishTypeText)
            .setText(R.id.tv_wish, item.wishTypeExplain)
    }
}