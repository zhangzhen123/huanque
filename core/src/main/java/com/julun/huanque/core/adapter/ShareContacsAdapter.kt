package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/11/26 10:09
 *@描述 分享 选择 密友页面使用的Adapter
 */
class ShareContacsAdapter : BaseQuickAdapter<SocialUserInfo, BaseViewHolder>(R.layout.recycler_item_share_contacs),LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: SocialUserInfo) {
        val header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        header.loadImage(item.headPic, 46f, 46f)
        holder.setText(R.id.tv_nickname, item.nickname)
    }
}