package com.julun.huanque.message.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleUsefulWords
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/10/12 9:58
 *@描述 常用语Adapter
 */
class UsefulWordsAdapter : BaseQuickAdapter<SingleUsefulWords, BaseViewHolder>(R.layout.recycler_item_useful_words) {

    init {
        addChildClickViewIds(R.id.iv_action)
    }

    override fun convert(holder: BaseViewHolder, item: SingleUsefulWords) {
        holder.setText(R.id.tv_title, "常用语${holder.adapterPosition + 1}")
            .setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, item.words))
    }
}