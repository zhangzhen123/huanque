package com.julun.huanque.message.adapter

import android.widget.BaseAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.IntimatePrivilege
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/7/9 19:29
 *@描述 亲密度特权
 */
class PrivilegeAdapter : BaseQuickAdapter<IntimatePrivilege, BaseViewHolder>(R.layout.recycler_item_intimate_privilege) {
    override fun convert(holder: BaseViewHolder, item: IntimatePrivilege) {
        if (holder == null || item == null) {
            return
        }
        ImageUtils.loadImage(holder.getView(R.id.sdv_pic),item.icon,33f,33f)
        holder.setText(R.id.tv_title,"Lv.${item.minLevel}${item.title}")
    }
}