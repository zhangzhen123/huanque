package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.bean.beans.SingleIncome
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/31 10:01
 *@描述 职业 收入Adapter
 */
class ProfessionIncomeAdapter : BaseQuickAdapter<SingleIncome, BaseViewHolder>(R.layout.recycler_item_profession_income) {

    //选中的年收入code
    var incomeCode = ""

    override fun convert(holder: BaseViewHolder, item: SingleIncome) {
        val tv_content = holder.getView<TextView>(R.id.tv_content)
        tv_content.text = item.incomeText
        tv_content.isSelected = item.incomeCode == incomeCode

    }
}