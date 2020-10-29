package com.julun.huanque.core.ui.live.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.GiftRuleAward
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/10/29 10:59
 *@描述 盲盒说明页面  可以中奖的礼物Adapter
 */
class BlindBoxRuleGiftAdapter : BaseQuickAdapter<GiftRuleAward, BaseViewHolder>(R.layout.recycler_item_blind_box_rule_gift) {

    override fun convert(holder: BaseViewHolder, item: GiftRuleAward) {

        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        if (item.awardName.isEmpty()) {
            //添加的占位布局
            sdv_pic.hide()
        } else {
            sdv_pic.show()
            sdv_pic.loadImage(StringHelper.getOssImgUrl(item.pic), 150f, 150f)
        }
    }
}