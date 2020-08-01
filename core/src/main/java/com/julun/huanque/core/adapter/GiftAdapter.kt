package com.julun.huanque.core.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.facebook.drawee.view.SimpleDraweeView
import org.jetbrains.anko.backgroundDrawable
import android.text.style.AbsoluteSizeSpan
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.LiveGiftDto
import com.julun.huanque.common.helper.DensityHelper.Companion.sp2px
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.TagView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.fragment.SendGiftFragment


/**
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/09/10
 * @iterativeVersion 4.17
 * @iterativeDetail 增加折扣券逻辑
 */
class GiftAdapter : BaseQuickAdapter<LiveGiftDto, BaseViewHolder>(R.layout.item_cmp_gift) {
    // 当前选中的礼物
    companion object {
        var selectedGift: LiveGiftDto? = null
    }

    override fun convert(vh: BaseViewHolder, item: LiveGiftDto) {
        if (vh == null || item == null) {
            return
        }
        //过滤空礼物
        if (item.giftId == SendGiftFragment.EMPTY_GIFT) {
            vh.setGone(R.id.gift_name, false)
                .setGone(R.id.gift_icon, false)
                .setGone(R.id.gift_price, false)
                .setGone(R.id.ll_tag, false)
                .setGone(R.id.process_info, false)
                .setGone(R.id.giftBagInfo, false)
            return
        }
        //tips
        val rootView = vh.itemView
        val llTag = vh.getView<LinearLayout>(R.id.ll_tag)
        val ivFunction = vh.getView<ImageView>(R.id.iv_function)
        if (item.giftId == selectedGift?.giftId) {
            rootView.backgroundDrawable = GlobalUtils.getDrawable(R.drawable.shape_gift_selected_bg)
            //有设置数据
            llTag.hide()
            ivFunction.show()
            if (item.luckyOrHigh || item.anonymous || (item.discountCount ?: 0) > 0) {
                //幸运礼物 || 匿名礼物 || 有折扣券，显示设置按钮
                ivFunction.setImageResource(R.mipmap.icon_gift_setting)
            } else {
                ivFunction.setImageResource(R.mipmap.icon_gift_explain)
            }
        } else {
            rootView.backgroundDrawable = GlobalUtils.getDrawable(R.drawable.bg_gift_item_bg)
            llTag.show()
            ivFunction.hide()
        }

        if (llTag.visibility == View.VISIBLE) {
            val tagData = item.tagContent?.split(",")
            llTag.removeAllViews()
            tagData?.forEach {
                val tagView = TagView(context)
                llTag.addView(tagView)
                tagView.show()
                tagView.setAlignRight()
                tagView.isGiftTag = true
                tagView.setData(it)
            }
        }
        val expTime = item.expTime
        val pText = if (expTime.isEmpty()) {
            "${item.beans}"
        } else {
            expTime
        }
        vh.setText(R.id.gift_name, item.giftName)
            .setText(R.id.gift_price, pText).setVisible(R.id.gift_price, true)
            .setVisible(R.id.gift_name, true)
        val giftPic = vh.getView<SimpleDraweeView>(R.id.gift_icon)
        giftPic.show()
        ImageUtils.loadImage(giftPic, item.pic ?: "", 78f, 69f)

        val giftBagInfo = vh.getView<TextView>(R.id.giftBagInfo)
        if (item.giftId == selectedGift?.giftId) {
            giftBagInfo.isSelected = true
            playAnim(giftPic)
        } else {
            giftBagInfo.isSelected = false
            giftPic.clearAnimation()
        }
        val str: String = StringHelper.paddingNumberWithEmptyCharEven(item.bagCount, 4)
        if (item.bagCount > 0) {
            giftBagInfo.show()
            giftBagInfo.text = str
        } else {
            giftBagInfo.hide()
        }
        if (item.processInfo != null) {
            val pro = "${item.processInfo!!.nowCount}/${item.processInfo!!.needCount}"
            vh.setText(R.id.process_info, pro).setGone(R.id.process_info, true)
        } else {
            vh.setGone(R.id.process_info, false)
        }

        //设置折扣券
        vh.setGone(R.id.clGiftDiscountRootView, false)
        if (item.discount != null) {
            //表示现在该礼物有折扣
            //配置富文本，删除线 -> StrikethroughSpan
            val content = "${item.discountBean} ${item.beans}"
            val spannable = SpannableString(content)
            spannable.setSpan(StrikethroughSpan(), "${item.discountBean}".length + 1, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                AbsoluteSizeSpan(sp2px(6f), false),
                "${item.discountBean}".length + 1,
                content.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            //.setTypeface(typeFaceTwo, R.id.ivGiftDiscountDiscountRatio)
            vh.setVisible(R.id.clGiftDiscountRootView, true)
                .setText(R.id.ivGiftDiscountDiscountRatio, "${item.discount!! / 10}折券")
                .setText(R.id.gift_price, spannable)

//            item.discountCount?.let {
//                if (it >= 99) {
//                    vh.setTypeface(typeFaceTwo, R.id.tvGiftDiscountDiscountCount).setText(R.id.tvGiftDiscountDiscountCount, "99")
//                } else {
//                    vh.setTypeface(typeFaceTwo, R.id.tvGiftDiscountDiscountCount).setText(R.id.tvGiftDiscountDiscountCount, "${item.discountCount}")
//                }
//            }
        }
    }


    //给相应的view做动画 之所以选择普通动画是因为属性动画不好控制 你无法通过相对应的view取消该view的动画 而且无限循环可能会内存泄漏
    //属性动画根本不能在list viewholder中实现闭环 满足条件开始动画 不满足则关掉动画
    private fun playAnim(view: View) {
        //普通动画
        val curAnim = view.animation
        if (curAnim != null) {
//                logger.info("当前已有动画无需设置")
            return
        }
        val ani = ScaleAnimation(
            0.7f, 1.0f, 0.7f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        ani.repeatMode = Animation.REVERSE
        ani.repeatCount = Animation.INFINITE
        ani.duration = 500
        view.startAnimation(ani)

    }
}