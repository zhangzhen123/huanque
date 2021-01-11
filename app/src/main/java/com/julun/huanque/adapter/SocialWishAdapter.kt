package com.julun.huanque.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.SocialWishBean

/**
 *@创建者   dong
 *@创建时间 2020/12/23 19:41recycler_item_social_wish
 *@描述 社交意愿
 */
class SocialWishAdapter : BaseQuickAdapter<SocialWishBean,BaseViewHolder>(R.layout.recycler_item_social_wish) {
    init {
        addChildClickViewIds(R.id.tv_social)
    }
    override fun convert(holder: BaseViewHolder, item: SocialWishBean) {
        holder.setText(R.id.tv_social,item.wishTypeText)
    }
}