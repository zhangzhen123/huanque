package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/29 11:31
 *@描述 家乡人文Adapter
 */
class HomeTownCircumListAdapter : BaseQuickAdapter<SingleCultureConfig, BaseViewHolder>(R.layout.recycler_item_home_town_circum) {
    var markContent = ""
    override fun convert(holder: BaseViewHolder, item: SingleCultureConfig) {
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        val tv_status = holder.getView<TextView>(R.id.tv_status)
        sdv_pic.loadImageNoResize(item.coverPic)
        holder.setText(R.id.tv_name, item.name)
        if (item.mark == BusiConstant.True) {
            tv_status.show()
            tv_status.text = markContent
        } else {
            tv_status.hide()
        }
    }
}