package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/7 11:50
 *@描述 编辑页面  社交意愿
 */
class EditSocialWishAdapter : BaseQuickAdapter<SocialWishBean, BaseViewHolder>(R.layout.recycler_item_edit_social_wish) {
    override fun convert(holder: BaseViewHolder, item: SocialWishBean) {
        holder.setText(R.id.tv_content, item.wishTypeText)
        holder.itemView.isSelected = item.selected == BusiConstant.True
    }
}