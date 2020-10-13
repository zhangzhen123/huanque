package com.julun.huanque.message.adapter

import android.graphics.Color
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.FateState
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundColor

/**
 *@创建者   dong
 *@创建时间 2020/10/12 20:11
 *@描述 派单周数据  详情弹窗
 */
class FateWeekDetailAdapter : BaseQuickAdapter<FateState, BaseViewHolder>(R.layout.recycler_item_fate_wek_detail) {
    override fun convert(holder: BaseViewHolder, item: FateState) {
        holder.setText(R.id.tv_date, item.dateStr)
            .setText(R.id.tv_total_count, "${item.totalNum}")
            .setText(R.id.tv_reply_count, "${item.replyNum}")
        val bg = holder.getView<View>(R.id.view_bg)
        val position = getItemPosition(item)
        if (position % 2 == 0) {
            //偶数
            bg.backgroundColor = Color.WHITE
        } else {
            //奇数
            bg.backgroundColor = GlobalUtils.formatColor("#F8F8F8")
        }
    }
}