package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/22 20:33
 *@描述 主页tag Adapter
 */
class HomePageTagAdapter : BaseQuickAdapter<UserTagBean,BaseViewHolder>(R.layout.recycler_item_home_tag) {
    //是否是喜欢的标签
    var like = false
    override fun convert(holder: BaseViewHolder, item: UserTagBean) {
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        sdv_pic.loadImage(item.tagPic)
        holder.setText(R.id.tv_tag_name,item.tagName)
        val sdv_icon = holder.getView<SimpleDraweeView>(R.id.sdv_icon)
        sdv_icon.loadImage(item.tagIcon)

    }
}