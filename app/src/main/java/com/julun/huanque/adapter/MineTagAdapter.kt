package com.julun.huanque.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.suger.loadImage

/**
 *@创建者   dong
 *@创建时间 2021/2/4 11:41
 *@描述
 */
class MineTagAdapter : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.recycler_item_mine_tag) {
    override fun convert(holder: BaseViewHolder, item: UserTagBean) {
        val sdv_tag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
        sdv_tag.loadImage(item.tagIcon, 13f, 16f)
        holder.setText(R.id.tv_tag, item.tagName)
    }
}