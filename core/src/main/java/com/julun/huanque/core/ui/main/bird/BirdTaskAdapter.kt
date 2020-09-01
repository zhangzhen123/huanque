package com.julun.huanque.core.ui.main.bird

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.BirdTask
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/21 17:10
 *
 *@Description: 商店上的鸟
 *
 */
class BirdTaskAdapter : BaseQuickAdapter<BirdTask, BaseViewHolder>(R.layout.item_bird_task) {
    init {
        addChildClickViewIds(R.id.tvAction)
    }
    override fun convert(holder: BaseViewHolder, item: BirdTask) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_img)

        imgView.loadImage(item.taskImage, 60f, 60f)
        holder.setText(R.id.tv_title, "${item.taskName}(${item.currentNum}/${item.targetNum})")
            .setText(R.id.tv_desc, item.taskDesc).setText(R.id.tv_active,"活跃度+${item.awardActive}")
            .setText(R.id.tvAction, item.taskStatusText)

    }

}
