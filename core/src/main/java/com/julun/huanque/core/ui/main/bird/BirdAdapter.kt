package com.julun.huanque.core.ui.main.bird

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UpgradeBirdBean
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/21 17:10
 *
 *@Description: 棋盘上的鸟
 *
 */
class BirdAdapter : BaseQuickAdapter<UpgradeBirdBean, BaseViewHolder>(R.layout.item_bird) {
    var onItemOnTouchListener: View.OnTouchListener?=null
    override fun convert(holder: BaseViewHolder, item: UpgradeBirdBean) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_bird)
        if (item.upgradeId != 0) {
            imgView.loadImage(item.upgradeIcon, 93f, 93f)
            holder.setGone(R.id.black_bird, true).setGone(R.id.tv_level,false).setGone(R.id.sdv_bird, false)
            holder.setText(R.id.tv_level,"${item.upgradeLevel}")
        } else {
            holder.setGone(R.id.black_bird, false).setGone(R.id.tv_level,true).setGone(R.id.sdv_bird, true)
        }
        if(onItemOnTouchListener!=null){
            imgView.setOnTouchListener(onItemOnTouchListener)
        }


    }
}
