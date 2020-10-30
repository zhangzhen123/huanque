package com.julun.huanque.message.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.BaseAdapter
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.FateInfo
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/9/22 11:39
 *@描述 缘分页面使用
 */
class YuanFenAdapter : BaseQuickAdapter<FateInfo, BaseViewHolder>(R.layout.recycler_item_yuanfen), LoadMoreModule {

    init {
        addChildClickViewIds(R.id.sdv_header, R.id.iv_reply)
    }

    override fun convert(holder: BaseViewHolder, item: FateInfo) {
        val tvMatchTime = holder.getView<TextView>(R.id.tv_match_time)
        tvMatchTime.text = "缘分时刻：${item.matchTime}"
        ImageUtils.loadImage(holder.getView(R.id.sdv_header), item.headPic)
        holder.setText(R.id.tv_nickname, item.nickname)
        val tv_sex = holder.getView<TextView>(R.id.tv_sex)
        val sex = item.sexType
        var sexDrawable: Drawable? = null
        //性别
        when (sex) {
            Sex.MALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                tv_sex.textColor = Color.parseColor("#58CEFF")
            }
            Sex.FEMALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                tv_sex.textColor = Color.parseColor("#FF9BC5")
            }
            else -> sexDrawable = null
        }
        if (sexDrawable != null) {
            sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
            tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
        } else {
            tv_sex.setCompoundDrawables(null, null, null, null)
        }
        tv_sex.text = "${item.age}"


        val city = item.city
        val tv_location = holder.getView<TextView>(R.id.tv_location)
        if (city.isEmpty()) {
            tv_location.hide()
        } else {
            tv_location.show()
            tv_location.text = city
        }

        val wealthAddrss = GlobalUtils.getString(R.string.wealth_address)
        holder.getView<SimpleDraweeView>(R.id.sdv_caifu_level).loadImage(String.format(wealthAddrss, item.userLevel), 40f, 20f)
        holder.getView<SimpleDraweeView>(R.id.sdv_royal_level).loadImage(item.royalPic, 55f, 16f)

        //处理状态相关
        var statusDrawable: Drawable? = null
        var statusColor: Int? = null
        var statusContent = ""
        val ttl = item.ttl
        val tv_status = holder.getView<TextView>(R.id.tv_status)
        tvMatchTime.textColor = GlobalUtils.getColor(R.color.black_999)
        if (item.quickChat == BusiConstant.True && item.status == FateInfo.Wait) {
            //新用户并且未回复
            holder.setGone(R.id.iv_arrow, true)
                .setVisible(R.id.iv_reply, true)
        } else {
            holder.setGone(R.id.iv_reply, true)
                .setVisible(R.id.iv_arrow, true)
        }
        when (item.status) {
            FateInfo.Wait -> {
                //等待回复
                tvMatchTime.textColor = GlobalUtils.getColor(R.color.black_666)
                statusDrawable = if (ttl > 0) {
                    statusColor = GlobalUtils.formatColor("#FF3F3F")
                    statusContent = "${item.ttl}s"
                    GlobalUtils.getDrawable(R.mipmap.icon_fate_count_down)
                } else {
                    statusColor = GlobalUtils.formatColor("#FFAC09")
                    statusContent = "等待回复"
                    GlobalUtils.getDrawable(R.mipmap.icon_fate_wait)
                }
            }
            FateInfo.Finish -> {
                //准时回复
                statusColor = GlobalUtils.getColor(R.color.black_999)
                statusDrawable = GlobalUtils.getDrawable(R.mipmap.icon_fate_finish)
                statusContent = "准时回复"
            }
            FateInfo.Timeout -> {
                //超时回复
                statusColor = GlobalUtils.getColor(R.color.black_999)
                statusDrawable = GlobalUtils.getDrawable(R.mipmap.icon_fate_timeout)
                statusContent = "超时回复"
            }
            else -> {
            }
        }

        val iv_new_user = holder.getView<SimpleDraweeView>(R.id.iv_new_user)
        if (item.userTag.isNotEmpty()) {
            iv_new_user.show()
            iv_new_user.loadImage(item.userTag, 28f, 12f)
        } else {
            iv_new_user.hide()
        }
        holder.setVisible(R.id.iv_new_user, item.newUser == BusiConstant.True)

        if (statusDrawable != null) {
            statusDrawable.setBounds(0, 0, statusDrawable.minimumWidth, statusDrawable.minimumHeight)
            tv_status.setCompoundDrawables(statusDrawable, null, null, null)
        } else {
            tv_status.setCompoundDrawables(null, null, null, null)
        }
        if (statusColor != null) {
            tv_status.textColor = statusColor
        }
        tv_status.text = statusContent
    }
}