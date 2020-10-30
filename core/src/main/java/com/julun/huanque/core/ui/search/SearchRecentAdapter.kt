package com.julun.huanque.core.ui.search

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/30 10:00
 *
 *@Description: RecentAnchorAdapter
 *
 */
class SearchRecentAdapter : BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>(R.layout.item_recent_anchor) {
    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param holder A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: ProgramLiveInfo) {

        holder.setText(R.id.tv_name, item.programName)

        ImageUtils.loadImage(holder.getView(R.id.sdv), item.headPic, 50f, 50f)

        val living = holder.getView<SimpleDraweeView>(R.id.living_tag)
        when (item.isLiving) {
            true -> {
                ImageUtils.loadGifImageLocal(living, R.mipmap.anim_living)
            }
            else -> {
                living.hide()
            }
        }
    }
}