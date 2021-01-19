package com.julun.huanque.core.adapter

import android.animation.Animator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.widgets.adapter_animator.ScaleFadeInAnimation
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/31 10:01
 *@描述 家乡人文 Adapter
 */
class HomeTownFoodAdapter : BaseQuickAdapter<SingleCultureConfig, BaseViewHolder>(R.layout.recycler_item_culture) {
    init {
        animationEnable = true
        isAnimationFirstOnly=false
        adapterAnimation = ScaleFadeInAnimation()
        addChildClickViewIds(R.id.tv_content)
    }

    override fun convert(holder: BaseViewHolder, item: SingleCultureConfig) {
        holder.setText(R.id.tv_content, item.name)
//        val tv_content = holder.getView<MarqueeTextView2>(R.id.tv_content)
//        tv_content.text = "0123456789一二三四五六七八九"
//        tv_content.startScroll()
//        tv_content.setMarqueeEnable(true)
//        tv_content.requestFocus()
    }
}