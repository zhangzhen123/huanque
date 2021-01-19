package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleProfessionFeatureConfig
import com.julun.huanque.common.widgets.adapter_animator.ScaleFadeInAnimation
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/12 11:47
 *@描述 主页弹窗  职业特点使用
 */
class ProfessionPeculiarityAdapter : BaseQuickAdapter<SingleProfessionFeatureConfig, BaseViewHolder>(R.layout.recycler_item_profession_peculiarity) {
    init {
        animationEnable = true
        isAnimationFirstOnly=false
        adapterAnimation = ScaleFadeInAnimation()
    }
    override fun convert(holder: BaseViewHolder, item: SingleProfessionFeatureConfig) {
        holder.setText(R.id.tv_content,item.professionFeatureText)
    }
}