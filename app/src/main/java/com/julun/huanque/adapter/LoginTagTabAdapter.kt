package com.julun.huanque.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.LoginTagTabBean

/**
 *@创建者   dong
 *@创建时间 2020/12/23 15:06
 *@描述
 */
class LoginTagTabAdapter :
    BaseQuickAdapter<LoginTagTabBean, BaseViewHolder>(R.layout.recycler_item_login_tag_tab) {
    override fun convert(holder: BaseViewHolder, item: LoginTagTabBean) {
        var selectCount = 0
        item.tagList.forEach {
            if (it.selected) {
                selectCount++
            }
        }

        val showTypeContent = if (selectCount > 0) {
            "${item.tagTypeText} $selectCount"
        } else {
            item.tagTypeText
        }

        val position = holder.layoutPosition
        holder.setVisible(R.id.iv_arrow, item.selected)
            .setVisible(R.id.view_left, position != 0)
            .setVisible(R.id.view_right, position != itemCount - 1)
            .setText(R.id.tv_tag_name, showTypeContent)
        val iv_tag_sel = holder.getView<View>(R.id.iv_tag_sel)
        val tv_tag_name = holder.getView<TextView>(R.id.tv_tag_name)
        iv_tag_sel.isSelected = item.selected
        if (item.selected) {
            tv_tag_name.textSize = 14f
        } else {
            tv_tag_name.textSize = 12f
        }

    }
}