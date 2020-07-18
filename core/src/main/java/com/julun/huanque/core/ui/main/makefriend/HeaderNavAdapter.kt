package com.julun.huanque.core.ui.main.makefriend

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.HeadModule
import com.julun.huanque.core.R
import org.jetbrains.anko.imageResource

class HeaderNavAdapter : BaseQuickAdapter<HeadModule, BaseViewHolder>(R.layout.item_item_mkf_header) {

    override fun convert(holder: BaseViewHolder, item: HeadModule) {

        val container = holder.getView<ImageView>(R.id.sdv_nav_bg)
//        container.loadImage(item.bgPic, 90f, 70f)
//        val url = item.baseInfo.headPic
//        if (url.isNotEmpty()) {
//            imgView.show()
//            imgView.loadImage(url, 60f, 60f)
//        } else {
//            imgView.inVisiable()
//        }
        if (item.hot) {
            holder.setGone(R.id.iv_hot_tag, true)
        } else {
            holder.setGone(R.id.iv_hot_tag, false)
        }

        when (item.type) {
            HeadModule.MaskQueen -> {
                container.imageResource = R.mipmap.bg_header_mask
                holder.setText(R.id.tv_title1, "蒙面女王")

                holder.setText(R.id.tv_title2, "今日剩余${item.num}次")
            }
            HeadModule.AnonymousVoice -> {
                holder.setText(R.id.tv_title1, "匿名语音")
                holder.setText(R.id.tv_title2, "${item.num}人参与")
                container.imageResource = R.mipmap.bg_header_audio
            }
            HeadModule.MagpieParadise -> {
                holder.setText(R.id.tv_title1, "养鹊乐园")
                holder.setText(R.id.tv_title2, "养鹊也能赚钱")
                container.imageResource = R.mipmap.bg_header_audio
            }
            HeadModule.HotLive -> {
                holder.setText(R.id.tv_title1, "热门直播")
                holder.setText(R.id.tv_title2, "火${item.num}")
                container.imageResource = R.mipmap.bg_header_living
            }
            HeadModule.PlumFlower -> {
                holder.setText(R.id.tv_title1, "今日花魁")
                holder.setText(R.id.tv_title2, "${item.num}")
                container.imageResource = R.mipmap.bg_header_day_top
            }
            else -> {

            }
        }


    }

}
