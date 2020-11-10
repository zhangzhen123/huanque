package com.julun.huanque.core.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.setTFDinCdc2
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.TagView
import com.julun.huanque.core.R
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 只有封面item 不带视频播放和banner
 */
class ProgramNormalAdapter : BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>(R.layout.item_live_square_anchor_list),
    LoadMoreModule {
    companion object{
        fun convertNormal(holder: BaseViewHolder, item: ProgramLiveInfo){
            holder.setText(R.id.anchor_nickname, item.programName)
            val textHot = holder.getView<TextView>(R.id.user_count)
            textHot.setTFDinCdc2()
            if (item.heatValue < 10000) {
                textHot.text = "${item.heatValue}"
                holder.setGone(R.id.user_count_w, true)
            } else {
                val format = DecimalFormat("#.0")
                format.roundingMode = RoundingMode.HALF_UP
                textHot.text = "${format.format((item.heatValue / 10000.0))}"
                holder.setGone(R.id.user_count_w, false)
            }
            ImageUtils.loadImage(
                holder.getView(R.id.anchorPicture)
                    ?: return, item.coverPic + BusiConstant.OSS_350, 150f, 150f
            )
            if (item.city.isEmpty()) {
                holder.setGone(R.id.anchor_city, true)
            } else {
                holder.setGone(R.id.anchor_city, false)

                holder.setText(R.id.anchor_city, item.city)
            }
            ImageUtils.loadImageLocal(holder.getView(R.id.bg_shadow), R.mipmap.bg_shadow_home_item)
            val tag_right_top = holder.getView<TagView>(R.id.tag_right_top)
            val tag_left_top = holder.getView<TagView>(R.id.tag_left_top)

            if (item.rightTopTag.isNotEmpty()) {
                tag_right_top.show()
                tag_right_top.setData(item.rightTopTag)
                holder.setGone(R.id.tv_author_status, true)
            } else {
                tag_right_top.hide()
                if (item.isLiving) {
                    holder.setVisible(R.id.tv_author_status, true)
                } else {
                    holder.setGone(R.id.tv_author_status, true)
                }
            }
            if (item.leftTopTag.isNotEmpty()) {
                tag_left_top.show()
                tag_left_top.initProgramLTV()
                tag_left_top.setData(item.leftTopTag)
            } else {
                tag_left_top.hide()
            }

        }
    }
    override fun convert(holder: BaseViewHolder, item: ProgramLiveInfo) {
        convertNormal(holder, item)
    }

}
