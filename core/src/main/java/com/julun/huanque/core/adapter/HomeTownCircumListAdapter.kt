package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/29 11:31
 *@描述 家乡人文Adapter
 */
class HomeTownCircumListAdapter : BaseQuickAdapter<SingleCultureConfig, BaseViewHolder>(R.layout.recycler_item_home_town_circum) {
    override fun convert(holder: BaseViewHolder, item: SingleCultureConfig) {
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        sdv_pic.loadImageNoResize(item.coverPic)
        holder.setText(R.id.tv_name, item.name)
    }
}