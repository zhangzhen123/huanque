package com.julun.huanque.core.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2021/1/7 14:59
 *@描述
 */
class EditPicAdapter : BaseQuickAdapter<HomePagePicBean, BaseViewHolder>(R.layout.recycler_item_edit_pic) {
    override fun convert(holder: BaseViewHolder, item: HomePagePicBean) {
        val rl_empty = holder.getView<View>(R.id.rl_empty)

        val iv_mark = holder.getView<ImageView>(R.id.iv_mark)
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        val tv_header = holder.getView<View>(R.id.tv_header)
        if (item.coverPic.isEmpty()) {
            //空布局
            rl_empty.show()
            iv_mark.hide()
            sdv_pic.hide()
            tv_header.hide()
        } else {
            rl_empty.hide()
            sdv_pic.show()
            if (item.headerPic == BusiConstant.True) {
                //头像
                iv_mark.show()
                tv_header.show()
                iv_mark.isSelected = item.realPic == BusiConstant.True
            } else {
                iv_mark.hide()
                tv_header.hide()
            }
            sdv_pic.loadImageNoResize(item.coverPic)
        }

        val tv_no_move = holder.getView<View>(R.id.tv_no_move)
        if (item.showNoMoveAttention == BusiConstant.True) {
            //显示不可移动提示
            tv_no_move.show()
        } else {
            tv_no_move.hide()
        }


    }
}