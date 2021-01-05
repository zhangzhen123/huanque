package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/4 18:42
 *@描述 标记过的人文Adapter
 */
class MarkCultureAdapter : BaseQuickAdapter<SingleCultureConfig, BaseViewHolder>(R.layout.recycler_item_mark_culture) {
    override fun convert(holder: BaseViewHolder, item: SingleCultureConfig) {
        holder.setText(R.id.tv_content, item.name)
    }
}