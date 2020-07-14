package com.julun.huanque.core.ui.main.makefriend

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HeadModule
import com.julun.huanque.common.suger.inVisiable
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R

class HeaderNavAdapter : BaseQuickAdapter<HeadModule, BaseViewHolder>(R.layout.item_item_mkf_header) {

    override fun convert(holder: BaseViewHolder, item: HeadModule) {

        val container = holder.getView<SimpleDraweeView>(R.id.sdv_nav_bg)
        container.loadImage(item.bgPic, 90f, 70f)
//        val url = item.baseInfo.headPic
//        if (url.isNotEmpty()) {
//            imgView.show()
//            imgView.loadImage(url, 60f, 60f)
//        } else {
//            imgView.inVisiable()
//        }

        holder.setText(R.id.tv_title1, item.moduleName)

        val info = when (item.moduleType) {
            HeadModule.MaskQueen -> {
                "今日剩余${item.baseInfo.remainTimes}次"
            }
            HeadModule.AnonymousVoice -> {
                "${item.baseInfo.joinNum}人参与"
            }
            HeadModule.MagpieParadise -> {
                "养鹊也能赚钱"
            }
            HeadModule.HotLive -> {
                "火${item.baseInfo.hotValue}"
            }
            HeadModule.PlumFlower -> {
                "${item.baseInfo.nickname}"
            }
            else -> ""
        }
        holder.setText(R.id.tv_title2, info)

    }

}
