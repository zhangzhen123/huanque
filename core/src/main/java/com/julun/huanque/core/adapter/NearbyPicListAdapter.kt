package com.julun.huanque.core.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.utils.ImageUtils
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
        holder.setVisible(R.id.view_border, item.selected)
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
        if(item.blur){
            ImageUtils.loadImageWithBlur(sdv,item.coverPic + BusiConstant.OSS_120, 2, 10,dp2px(28),dp2px(28))
        }else{
            sdv.loadImage(item.coverPic + BusiConstant.OSS_120, 28f, 28f)
        }

    }
}