package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.FamousUser
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setTFDINCondensedBold
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/24 17:32
 *@描述 名人榜 用户Adapter
 */
class FlowerFamousUserAdapter : BaseQuickAdapter<FamousUser, BaseViewHolder>(R.layout.recycler_item_famous_user) {
    override fun convert(holder: BaseViewHolder, item: FamousUser) {
        holder.getView<SimpleDraweeView>(R.id.sdv).loadImage(item.headPic)
        val tvDay = holder.getView<TextView>(R.id.tv_day)
        val day = item.day
        val dayContent = if (day >= 10) {
            "$day"
        } else {
            "0${day}"
        }
        tvDay.setTFDINCondensedBold()
        tvDay.text = dayContent
        holder.setText(R.id.tv_nickname, item.nickname)
    }
}