package com.julun.huanque.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.ChooseTagBean
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils

/**
 *@创建者   dong
 *@创建时间 2020/12/17 14:48
 *@描述
 */
class TagPicAdapter(var login: Boolean = false) : BaseQuickAdapter<ChooseTagBean, BaseViewHolder>(R.layout.recycler_item_tag_pic) {
    override fun convert(holder: BaseViewHolder, item: ChooseTagBean) {
        val params = holder.itemView.layoutParams
        val position = holder.layoutPosition
        if (position == 0) {
            params.height = 400
        } else {
            params.height = 800
        }
        holder.itemView.layoutParams = params
        val headImage = holder.getView<SimpleDraweeView>(R.id.headImage)
        ImageUtils.loadImageLocal(headImage, item.resourceId)
        val view_sel = holder.getView<View>(R.id.view_sel)
        if (login) {
            view_sel.hide()
        } else {
            view_sel.show()
            view_sel.isSelected = item.selected
        }
    }


    override fun getItemCount(): Int {
        if (data.size == 0) {
            return 0
        } else {
            return Int.MAX_VALUE
        }
    }

    override fun getItem(position: Int): ChooseTagBean {
        val count = data.size
        return data[position % count]
    }

    override fun getItemViewType(position: Int): Int {
        var count = headerLayoutCount + data.size
        if (data.size == 0) {
            //没有设置数据，就使用默认的值
            return super.getItemViewType(position)
        }
        //刚开始进入包含该类的activity时,count为0。就会出现0%0的情况，这会抛出异常，所以我们要在下面做一下判断
        if (count <= 0) {
            count = 1
        }
        val newPosition = position % count
        return super.getItemViewType(newPosition)
    }
}