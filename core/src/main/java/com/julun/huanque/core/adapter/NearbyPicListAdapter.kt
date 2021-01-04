package com.julun.huanque.core.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/23 11:02
 *
 *@Description: NearbyPicListAdapter
 *
 */
class NearbyPicListAdapter : BaseQuickAdapter<HomePagePicBean, BaseViewHolder>(R.layout.recycler_item_nearby_pic) {
    override fun convert(holder: BaseViewHolder, item: HomePagePicBean) {
        val sdv = holder.getView<SimpleDraweeView>(R.id.sdv)
        holder.setVisible(R.id.view_border, item.selected == BusiConstant.True)
//        val roundParams = RoundingParams.fromCornersRadius(dp2pxf(4))
//        if (item.selected == BusiConstant.True) {
//            //选中
//            roundParams.setBorder(Color.WHITE, 2f)
//        } else {
//            roundParams.setBorder(Color.TRANSPARENT, 0f)
//        }
//        val hierarchy = GenericDraweeHierarchyBuilder.newInstance(context.resources)
//            .setRoundingParams(roundParams)
//            .build()
//        sdv.hierarchy = hierarchy
        sdv.loadImage(item.pic+BusiConstant.OSS_120, 40f, 40f)
    }
}