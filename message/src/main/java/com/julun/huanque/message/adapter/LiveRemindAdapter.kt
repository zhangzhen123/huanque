package com.julun.huanque.message.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.LiveRemindBeans
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.message.R

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
class LiveRemindAdapter :
    BaseQuickAdapter<LiveRemindBeans, BaseViewHolder>(R.layout.item_live_remind_list),
    LoadMoreModule {
    init {
        addChildClickViewIds(R.id.ivPush)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        if (ForceUtils.isIndexNotOutOfBounds(position, data)) {
            val item = this.data[position]
            setPushStatus(holder.getView(R.id.ivPush), item.pushOpen)
        }
    }

    override fun convert(holder: BaseViewHolder, info: LiveRemindBeans) {
        ImageUtils.loadImage(holder.getView(R.id.ivHead),info.anchorPic,56f,56f)
        holder.setText(R.id.tvNickname,info.nickname).setText(R.id.tvSign,info.mySign)
        setPushStatus(holder.getView(R.id.ivPush), info.pushOpen)
    }

    private fun setPushStatus(view: ImageView, isPush: Boolean) {
        view.isSelected = isPush
    }
}