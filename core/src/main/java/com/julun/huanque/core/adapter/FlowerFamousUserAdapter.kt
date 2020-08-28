package com.julun.huanque.core.adapter

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.FamousUser
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ViewUtils
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/24 17:32
 *@描述 名人榜 用户Adapter
 */
class FlowerFamousUserAdapter(val border: Int) : BaseQuickAdapter<FamousUser, BaseViewHolder>(R.layout.recycler_item_famous_user) {
    override fun convert(holder: BaseViewHolder, item: FamousUser) {
        val sdv = holder.getView<SimpleDraweeView>(R.id.sdv)
        ViewUtils.updateViewBorder(holder.itemView, border, border)
        ImageUtils.loadImageInPx(sdv, item.headPic, border, border)

        val view_border = holder.getView<View>(R.id.view_border)
        val view_shader = holder.getView<View>(R.id.view_shader)
        if (item.myself == BusiConstant.True) {
            //本人
            view_border.show()
            val padding = dp2px(1)
            sdv.setPadding(padding, padding, padding, padding)
            updateViewMargin(view_shader, true)
        } else {
            view_border.hide()
            updateViewMargin(view_shader, false)
            sdv.setPadding(0, 0, 0, 0)
        }
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

    /**
     * 更新阴影的margin
     */
    private fun updateViewMargin(view: View, self: Boolean) {
        val params = view.layoutParams as? ConstraintLayout.LayoutParams
        val margin = if (self) {
            dp2px(2)
        } else {
            0
        }
        params?.leftMargin = margin
        params?.rightMargin = margin
        view.layoutParams = params
    }
}