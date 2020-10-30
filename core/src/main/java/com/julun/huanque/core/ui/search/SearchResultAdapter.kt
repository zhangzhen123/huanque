package com.julun.huanque.core.ui.search

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/30 9:24
 *
 *@Description: SearchResultAdapter
 *
 */
class SearchResultAdapter : BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>(R.layout.item_search_result) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        val living = holder.getViewOrNull<SimpleDraweeView>(R.id.living_tag)
        if (living != null) {
            ImageUtils.loadGifImageLocal(living, R.mipmap.anim_living)
        }

        return holder
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param holder A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: ProgramLiveInfo) {

        holder.setText(R.id.tvNickname, item.programName)

        ImageUtils.loadImage(holder.getView(R.id.headImage), item.headPic, 50f, 50f)
        ImageUtils.loadImageWithHeight_2(holder.getView(R.id.levelIcon), item.anchorLevelPic, dp2px(16))


        holder.setGone(R.id.living_fg, !item.isLiving)
        holder.setGone(R.id.living_tag, !item.isLiving)
        if (item.isLiving) {
            holder.setText(R.id.tvDes, "热度:${item.heatValue}")
        } else {
            holder.setText(R.id.tvDes, "上次直播:${item.lastShowTime}")
        }
        if ((this.data.size - 1) == holder.adapterPosition) {
            holder.getView<View>(R.id.vLine).hide()
        } else {
            holder.getView<View>(R.id.vLine).hide()
        }

    }

}