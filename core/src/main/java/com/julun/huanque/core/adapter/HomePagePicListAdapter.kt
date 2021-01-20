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
import kotlinx.android.synthetic.main.view_single_video.view.*

/**
 *@创建者   dong
 *@创建时间 2020/11/5 10:47
 *@描述 主页左下角图片使用的Adapter
 */
class HomePagePicListAdapter : BaseQuickAdapter<HomePagePicBean, BaseViewHolder>(R.layout.recycler_item_homepage_pic) {
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
        if(item.blur){
            ImageUtils.loadImageWithBlur(sdv, item.coverPic, 3, 80,dp2px(40),dp2px(40))
        }else{
            sdv.loadImage(item.coverPic, 40f, 40f)
        }

    }
}