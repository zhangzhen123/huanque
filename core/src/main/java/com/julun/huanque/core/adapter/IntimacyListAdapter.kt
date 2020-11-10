package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.CloseConfidantBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setTFDinCdc2
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/11/9 10:32
 *@描述 主页亲密榜  使用的Adapter
 */
class IntimacyListAdapter : BaseQuickAdapter<CloseConfidantBean, BaseViewHolder>(R.layout.recycler_item_intimacy_list) {
    override fun convert(holder: BaseViewHolder, item: CloseConfidantBean) {
        val position = holder.adapterPosition
        val tv_rank = holder.getView<TextView>(R.id.tv_rank)
        tv_rank.text = "$position"
        tv_rank.setTFDinCdc2()

        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage("${StringHelper.getOssImgUrl(item.headPic)}${BusiConstant.OSS_160}")
        holder.setText(R.id.tv_nickname,item.nickname)
            .setText(R.id.tv_score,StringHelper.formatNumber(item.score))

    }
}