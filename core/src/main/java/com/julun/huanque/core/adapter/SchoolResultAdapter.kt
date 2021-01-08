package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleSchool
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/6 11:35
 *@描述
 */
class SchoolResultAdapter : BaseQuickAdapter<SingleSchool, BaseViewHolder>(R.layout.recycler_item_school_result) {
    override fun convert(holder: BaseViewHolder, item: SingleSchool) {
        holder.setText(R.id.tv_content, item.schoolName)
    }
}