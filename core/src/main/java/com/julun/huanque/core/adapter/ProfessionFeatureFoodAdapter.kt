package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.bean.beans.SingleProfessionFeatureConfig
import com.julun.huanque.common.widgets.adapter_animator.ScaleFadeInAnimation
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/31 10:01
 *@描述 家乡人文 Adapter
 */
class ProfessionFeatureFoodAdapter : BaseQuickAdapter<SingleProfessionFeatureConfig, BaseViewHolder>(R.layout.recycler_item_culture) {
    init {
        animationEnable = true
        isAnimationFirstOnly=false
        adapterAnimation = ScaleFadeInAnimation()
    }
    override fun convert(holder: BaseViewHolder, item: SingleProfessionFeatureConfig) {
        holder.setText(R.id.tv_content, item.professionFeatureText)
    }
}