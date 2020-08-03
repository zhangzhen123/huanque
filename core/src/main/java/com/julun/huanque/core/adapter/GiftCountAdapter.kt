package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.GoodsOptionCount
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/3 10:42
 *@描述 礼物赠送数量选择弹窗
 */
class GiftCountAdapter : BaseQuickAdapter<GoodsOptionCount, BaseViewHolder>(R.layout.item_cmp_gift_price) {
    override fun convert(holder: BaseViewHolder, item: GoodsOptionCount) {
        holder.setText(R.id.num_txt, item.countValue.toString())
            .setText(R.id.num_desc, item.countName)
        if (holder.layoutPosition == itemCount - 1) {
            holder.setGone(R.id.fgx_view, true)
        } else {
            holder.setVisible(R.id.fgx_view, true)
        }
    }
}