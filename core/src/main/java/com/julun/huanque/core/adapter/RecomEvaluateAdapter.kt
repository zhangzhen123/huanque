package com.julun.huanque.core.adapter

import android.widget.BaseAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/10/27 14:44
 *@描述 推荐评价列表
 */
class RecomEvaluateAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.recycler_item_recom_evaluate) {
    //选中的评论列表
    var evaluateList = mutableListOf<String>()
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tv_content, item)
        holder.itemView.isSelected = evaluateList.contains(item)
    }
}