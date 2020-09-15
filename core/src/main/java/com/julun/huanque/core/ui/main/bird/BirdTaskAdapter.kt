package com.julun.huanque.core.ui.main.bird

import android.graphics.Color
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.BirdTask
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.core.R
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import org.jetbrains.anko.textColor

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/21 17:10
 *
 *@Description: 商店上的鸟
 *
 */
class BirdTaskAdapter : BaseQuickAdapter<BirdTask, BaseViewHolder>(R.layout.item_bird_task) {
    init {
        addChildClickViewIds(R.id.tvAction)
    }

    override fun convert(holder: BaseViewHolder, item: BirdTask) {
//        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_img)
        val title = if (item.targetNum == 0) {
            item.taskName
        } else {
            "${item.taskName}(${item.currentNum}/${item.targetNum})"
        }
        holder.setText(R.id.tv_title, title)
            .setText(R.id.tv_desc, item.taskDesc).setText(R.id.tv_active, "活跃度+${item.awardActive}")
            .setText(R.id.tvAction, item.taskStatusText)
        val tvAction = holder.getView<TextView>(R.id.tvAction)
        when (item.taskStatus) {
            BirdTaskStatus.NotFinish -> {
                tvAction.setBackgroundResource(R.mipmap.bg_bird_btn_red)
                tvAction.textColor = Color.WHITE
            }
            BirdTaskStatus.NotReceive -> {
                tvAction.setBackgroundResource(R.mipmap.bg_bird_btn_green)
                tvAction.textColor = Color.WHITE
            }
            BirdTaskStatus.Received -> {
                tvAction.background = null
                tvAction.textColor = Color.parseColor("#9E8282")
            }
            else -> {
                tvAction.setBackgroundResource(R.mipmap.bg_bird_btn_red)
                tvAction.textColor = Color.WHITE
            }

        }
        val sdvImg = holder.getView<SimpleDraweeView>(R.id.sdv_img)

        when (item.awardType) {
            BirdTaskAwardType.Small -> {
                holder.setText(R.id.tv_coin_title, "少量金币")
                sdvImg.loadImageLocal(R.mipmap.icon_bird_coin_little)
            }
            BirdTaskAwardType.Middle -> {
                holder.setText(R.id.tv_coin_title, "中量金币")
                sdvImg.loadImageLocal(R.mipmap.icon_bird_coin_middle)
            }

            BirdTaskAwardType.Big -> {
                holder.setText(R.id.tv_coin_title, "大量金币")
                sdvImg.loadImageLocal(R.mipmap.icon_bird_coin_big)
            }
            else -> {
                holder.setText(R.id.tv_coin_title, "少量金币")
                sdvImg.loadImageLocal(R.mipmap.icon_bird_coin_little)
            }

        }

    }

}
