package com.julun.huanque.core.ui.live.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.bean.beans.ManagerOptionInfo
import com.julun.huanque.common.bean.beans.Option
import com.julun.huanque.core.R

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/31 0031
 * @detail
 */
class CardManagerAdapter : BaseQuickAdapter<Option, BaseViewHolder>(R.layout.item_manager_list) {
    override fun convert(holder: BaseViewHolder, item: Option) {
        when (item) {
            is ManagerInfo -> {
                holder.setText(R.id.tvName, item.itemName)
            }
            is ManagerOptionInfo -> {
                holder.setText(R.id.tvName, item.mangeTypeDesc)
            }
        }
    }
}