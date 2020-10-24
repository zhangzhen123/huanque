package com.julun.huanque.common.adapter

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.facebook.drawee.view.SimpleDraweeView
import org.jetbrains.anko.backgroundDrawable
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.beans.LiveGiftDto
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.TagView


/**
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/09/10
 * @iterativeVersion 4.17
 * @iterativeDetail 增加折扣券逻辑
 */
class GiftAdapter : BaseQuickAdapter<LiveGiftDto, BaseViewHolder>(R.layout.item_cmp_gift) {
    // 当前选中的礼物
    companion object {
        //选中的礼物
        var selectedGift: LiveGiftDto? = null
    }

    override fun convert(vh: BaseViewHolder, item: LiveGiftDto) {
        //过滤空礼物
        if (item.giftId == BusiConstant.EMPTY_GIFT) {
            vh.setGone(R.id.gift_name, true)
                .setGone(R.id.gift_icon, true)
                .setGone(R.id.gift_price, true)
                .setGone(R.id.ll_tag, true)
                .setGone(R.id.giftBagInfo, true)
            return
        }
        //tips
        val rootView = vh.itemView
        val llTag = vh.getView<LinearLayout>(R.id.ll_tag)
        //判断内存地址
        if (item.giftId == selectedGift?.giftId && item.prodType == selectedGift?.prodType && selectedGift?.bag == item.bag) {
            rootView.backgroundDrawable = GlobalUtils.getDrawable(R.drawable.shape_gift_selected_bg)
        } else {
            rootView.backgroundDrawable = GlobalUtils.getDrawable(R.drawable.bg_gift_item_bg)
        }

        val tagData = item.tagContent?.split(",")
        llTag.removeAllViews()
        if(tagData.isNullOrEmpty()){
            llTag.hide()
        }else{
            llTag.show()
            tagData.forEach {
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
        //判断地址
        if (item === selectedGift) {
            giftBagInfo.isSelected = true
            playAnim(giftPic)
        } else {
            giftBagInfo.isSelected = false
            giftPic.clearAnimation()
        }
        val viewDot = vh.getView<View>(R.id.view_dot)
        val str: String = StringHelper.paddingNumberWithEmptyCharEven(item.bagCount, 4)
        if (item.bag) {
            //处于背包当中，显示数量
            giftBagInfo.show()
            giftBagInfo.text = str
            if (item.changeMark) {
                viewDot.show()
            } else {
                viewDot.hide()
            }
        } else {
            giftBagInfo.hide()
            viewDot.hide()
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