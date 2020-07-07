package com.julun.huanque.core.ui.main.makefriend

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.julun.huanque.core.R

class TagAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_tag) {

    override fun convert(holder: BaseViewHolder?, item: String?) {
        if (holder == null || item == null) {
            return
        }
        val tv = holder.getView<TextView>(R.id.tv_tag)

        tv.text = "$item"


    }
}
