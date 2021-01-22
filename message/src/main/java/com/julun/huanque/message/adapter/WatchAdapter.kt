package com.julun.huanque.message.adapter

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.MaskFilterSpan
import android.view.View
import android.widget.BaseAdapter
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import kotlin.math.max

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:28
 *@描述 访问历史Adapter
 */
class WatchAdapter : BaseQuickAdapter<WatchHistoryBean, BaseViewHolder>(R.layout.recycler_item_watch), LoadMoreModule {
    init {
        addChildClickViewIds(R.id.tv_private, R.id.sdv_header)
    }

    var maxCount = 0

    //是否需要模糊样式，默认不需要
    var needBlur = false

    override fun convert(holder: BaseViewHolder, item: WatchHistoryBean) {
        val adapterPosition = holder.adapterPosition
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        val tv_nickname = holder.getView<TextView>(R.id.tv_nickname)
        val tv_introduce = holder.getView<TextView>(R.id.tv_introduce)
        if (maxCount != -1 && adapterPosition >= maxCount && needBlur) {
            //模糊样式
            sdv_header.loadImage("${item.headPic}${BusiConstant.OSS_BLUR_02}", 56f, 56f)
            val nicknameSb = SpannableString(item.nickName)
            val introduceSb = SpannableString("${item.visitTime} 第${item.visitCount}次访问")
            nicknameSb.setSpan(
                MaskFilterSpan(BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)),
                0, nicknameSb.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
            introduceSb.setSpan(
                MaskFilterSpan(BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)),
                0, introduceSb.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
            tv_introduce.text = introduceSb
            tv_nickname.text = nicknameSb
            tv_introduce.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            tv_nickname.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        } else {
            //完全样式
            sdv_header.loadImage(item.headPic, 56f, 56f)
            tv_introduce.text = "${item.visitTime} 第${item.visitCount}次访问"
            tv_nickname.text = item.nickName
        }





        holder.setVisible(R.id.tv_private, !needBlur || maxCount == -1 || adapterPosition < maxCount)

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
            if (maxCount != -1 && adapterPosition >= maxCount && needBlur) {
                //模糊效果
                val sexSb = SpannableString("${age}")
                sexSb.setSpan(
                    MaskFilterSpan(BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)),
                    0, sexSb.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                tv_sex.text = sexSb
                tv_sex.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            } else {
                tv_sex.text = "${age}"
            }

        }

    }
}