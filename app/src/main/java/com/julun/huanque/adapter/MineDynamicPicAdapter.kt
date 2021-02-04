package com.julun.huanque.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.suger.loadImage

/**
 *@创建者   dong
 *@创建时间 2021/2/3 20:36
 *@描述
 */
class MineDynamicPicAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.recycler_item_mine_dynamic_pic) {
    override fun convert(holder: BaseViewHolder, item: String) {
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        sdv_pic.loadImage(item)
    }
}