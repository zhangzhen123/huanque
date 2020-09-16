package com.julun.huanque.core.ui.main.bird

import android.graphics.Color
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UpgradeShopBirdBean
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R
import org.jetbrains.anko.backgroundResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/21 17:10
 *
 *@Description: 商店上的鸟
 *
 */
class ShopBirdAdapter : BaseQuickAdapter<UpgradeShopBirdBean, BaseViewHolder>(R.layout.item_bird_shop) {

    override fun convert(holder: BaseViewHolder, item: UpgradeShopBirdBean) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_bird)


        holder.setText(R.id.tv_level, "Lv${item.upgradeLevel}${item.upgradeName}")
            .setText(R.id.tv_bird_price, "${StringHelper.formatBigNum(item.upgradeCoins)}金币")
        val text = holder.getView<TextView>(R.id.tv_bird_price)
        if (item.unlocked) {
            text.text = "${StringHelper.formatBigNum(item.upgradeCoins)}金币"
            val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_bird_coin)
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                text.setCompoundDrawables(drawable, null, null, null)
            }
            text.backgroundResource=R.mipmap.bg_bird_btn_green
            imgView.loadImage(item.upgradeIcon, 90f, 90f)
        } else {
            text.text = "Lv.${item.unlockLevel}解锁"
            val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_bird_shop_lock)
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                text.setCompoundDrawables(drawable, null, null, null)
            }
            text.backgroundResource=R.mipmap.bg_bird_shop_lock
            ImageUtils.loadImageWithShadow(imgView,item.upgradeIcon,90, 90, intArrayOf(Color.parseColor("#80000000")))
        }
//                playAnim(imgView, holder.getView(R.id.tv_produce_sec))
//        holder.setGone(R.id.viewLock, item.unlocked)


    }

}
