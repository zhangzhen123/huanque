package com.julun.huanque.message.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.BaseAdapter
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:28
 *@描述 访问历史Adapter
 */
class WatchAdapter : BaseQuickAdapter<WatchHistoryBean, BaseViewHolder>(R.layout.recycler_item_watch),LoadMoreModule {
    init {
        addChildClickViewIds(R.id.tv_private, R.id.sdv_header)
    }

    override fun convert(holder: BaseViewHolder, item: WatchHistoryBean) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 56f, 56f)
        holder.setText(R.id.tv_nickname, item.nickName)
            .setText(R.id.tv_introduce, "${item.visitTime} 第${item.visitCount}次访问")
        val tv_sex = holder.getView<TextView>(R.id.tv_sex)

        val age = item.age
        if (age == 0) {
            tv_sex.hide()
        } else {
            val sex = item.sex
            tv_sex.show()
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
            tv_sex.text = "$age"
        }

    }
}