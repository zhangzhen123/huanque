package com.julun.huanque.message.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleHeartBean
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:28
 *@描述 访问历史Adapter
 */
class HeartBeatAdapter : BaseQuickAdapter<SingleHeartBean, BaseViewHolder>(R.layout.recycler_item_header_beat), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: SingleHeartBean) {
        val sdv = holder.getView<SimpleDraweeView>(R.id.sdv)
        if (item.unLock == BusiConstant.True) {
            //解锁
            sdv.loadImage(item.coverPic)
        } else {
            //未解锁
            sdv.loadImage("${item.coverPic}${BusiConstant.OSS_BLUR_01}")
        }

        val tv_distance = holder.getView<TextView>(R.id.tv_distance)
        val distance = item.distance

        if (distance == 0) {
            val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
            val currentStar = starList.random()
            tv_distance.text = currentStar
        } else {
            if (item.sameCity == BusiConstant.True) {
                if (distance == 0) {
                    val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
                    val currentStar = starList.random()
                    tv_distance.text = currentStar
                } else {
                    if (distance >= 1000) {
                        val df = DecimalFormat("#.0")
                        df.roundingMode = RoundingMode.DOWN
                        tv_distance.text = "${df.format(distance / 1000.0)}km ${item.area}"
                    } else {
                        tv_distance.text = "${distance}m ${item.area} "
                    }
                }
            } else {
                tv_distance.text = item.area
            }
        }


        val iv_vehicle = holder.getView<ImageView>(R.id.iv_vehicle)
        if (item.sameCity == BusiConstant.True) {
            //同市
            iv_vehicle.hide()
        } else {
            iv_vehicle.show()
            if (distance == 0) {
                iv_vehicle.imageResource = R.mipmap.icon_home_distance_rocket
            } else if (distance < 100 * 1000) {
                //显示汽车
                iv_vehicle.imageResource = R.mipmap.icon_home_distance_car
            } else if (distance > 800 * 1000) {
                //显示飞机
                iv_vehicle.imageResource = R.mipmap.icon_home_distance_air_plan
            } else {
                //显示动车
                iv_vehicle.imageResource = R.mipmap.icon_home_distance_rail_way
            }
        }



        holder.setVisible(R.id.iv_heart, item.matched == BusiConstant.True)
            .setText(R.id.tv_status, item.interactTips)
            .setVisible(R.id.iv_lock, item.unLock != BusiConstant.True)
    }
}