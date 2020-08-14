package com.julun.huanque.common.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.widgets.emotion.Emotion

/**
 *@创建者   dong
 *@创建时间 2020/8/14 11:55
 *@描述 动画表情Adapter
 */
class AnimationAdapter : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_animation_emotion_item_layout) {
    override fun convert(holder: BaseViewHolder, item: Emotion) {
        holder.setImageResource(R.id.image, item.drawableRes)
    }
}