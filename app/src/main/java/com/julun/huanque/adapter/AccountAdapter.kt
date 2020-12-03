package com.julun.huanque.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.SingleAccount
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.utils.SessionUtils

/**
 *@创建者   dong
 *@创建时间 2020/12/2 19:16
 *@描述
 */
class AccountAdapter : BaseQuickAdapter<SingleAccount, BaseViewHolder>(R.layout.recycler_item_account) {
    override fun convert(holder: BaseViewHolder, item: SingleAccount) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 56f, 56f)
        holder.setText(R.id.tv_nickname, item.nickname)
            .setText(R.id.tv_iden, "${item.userId}")
            .setVisible(R.id.iv_choose, SessionUtils.getUserId() == item.userId)
    }
}