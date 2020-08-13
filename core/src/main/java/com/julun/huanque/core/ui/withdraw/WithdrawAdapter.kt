package com.julun.huanque.core.ui.withdraw

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.RechargeTpl
import com.julun.huanque.common.bean.beans.WithdrawTpl
import com.julun.huanque.common.constant.WithdrawCode
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 15:48
 *
 *@Description: WithdrawAdapter
 *
 */
class WithdrawAdapter : BaseQuickAdapter<WithdrawTpl, BaseViewHolder>(R.layout.item_withdraw) {
    private var selectedPosition = -1
    fun setSelection(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    fun getSelectionInfo(): WithdrawTpl? {
        if (selectedPosition < 0 || selectedPosition >= data.size) {
            return null
        }
        return data[selectedPosition]
    }

    override fun convert(helper: BaseViewHolder, item: WithdrawTpl) {
        val selected = selectedPosition == helper.adapterPosition
        helper.getView<View>(R.id.view_bg_holder).isSelected = selected
        val withdrawNum = helper.getView<TextView>(R.id.withdraw_num)
        val withdrawTitle = helper.getView<TextView>(R.id.withdraw_title)
        val withdrawTips = helper.getView<TextView>(R.id.withdraw_tips)
        //
        if (item.quick) {
            withdrawTips.show()
            withdrawTips.text = "秒到账"
        } else {
            withdrawTips.hide()
        }

        withdrawNum.text = item.money

        if (item.remark.isNotEmpty()) {
            withdrawTitle.show()
            withdrawTitle.text = item.remark
        } else {
            val realMoney = item.realMoney
            if (selected && realMoney.isNotEmpty()) {
                withdrawTitle.show()
                withdrawTitle.text = "实际到账$realMoney"
            } else {
                withdrawTitle.hide()
            }
        }

    }
}