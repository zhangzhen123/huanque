package com.julun.huanque.core.ui.recharge

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.RechargeTpl
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R


/**
 *@创建者   dong
 *@创建时间 2019/9/6 9:44
 *@描述 快速充值弹窗的Adapter
 */
class RechargeAdapter : BaseQuickAdapter<RechargeTpl, BaseViewHolder>(R.layout.item_charge) {
    private var selectedPosition = -1

    companion object {
        const val Hot = "Hot"
        const val Super = "Super"
    }

    fun setSelection(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    fun getSelectionInfo(): RechargeTpl? {
        if (selectedPosition < 0 || selectedPosition >= data.size) {
            return null
        }
        return data[selectedPosition]
    }

    override fun convert(helper: BaseViewHolder, item: RechargeTpl) {
        if (helper == null || item == null) {
            return
        }

        //如果需要,是这优惠折扣角标
        val discountType = item.discountType
        val discountLabel = helper.getView<ImageView>(R.id.discount_label)
        if (StringHelper.isNotEmpty(discountType)) {
            discountLabel.show()
            when (discountType) {//现在只有返点优惠
//                "Rebate" -> discountLabel.setImageResource(R.drawable.recharge_present)//返点
//                "FirstCharge" -> discountLabel.setImageResource(R.mipmap.icon_mark_first_recharge)
            }
        } else {
            discountLabel.hide()
        }

        val rechargeCountTv = helper.getView<TextView>(R.id.rechargeCount)
        val mengdouTextView = helper.getView<TextView>(R.id.mengdouCount)
        mengdouTextView.text = item.rcvBeans
        val tempPosition = helper.adapterPosition
        helper.getView<View>(R.id.chargeItem).isSelected = selectedPosition == tempPosition
        val sdvTag = helper.getView<SimpleDraweeView>(R.id.sdv_tag)
        when (item.discountTag) {
            Hot -> {
                sdvTag.show()
                val params = sdvTag.layoutParams
                params?.width = dp2px(33f)
//                ImageUtils.loadGifImageLocal(sdvTag, R.mipmap.tag_hot)
            }
            Super -> {
                sdvTag.show()
                val params = sdvTag.layoutParams
                params?.width =dp2px(42f)
//                ImageUtils.loadGifImageLocal(sdvTag, R.drawable.tag_super)
            }
            else -> {
                sdvTag.hide()
            }
        }
        //

        rechargeCountTv.text = "${item.money}"
    }
}