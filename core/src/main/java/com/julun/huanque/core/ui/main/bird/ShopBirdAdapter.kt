package com.julun.huanque.core.ui.main.bird

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.DragEvent
import android.view.View
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.core.animation.addListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UpgradeBirdBean
import com.julun.huanque.common.bean.beans.UpgradeShopBirdBean
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.view_pk_mic.view.*
import java.math.BigInteger
import java.util.concurrent.TimeUnit

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

        imgView.loadImage(item.upgradeIcon, 90f, 90f)
        holder.setText(R.id.tv_level, "Lv${item.upgradeLevel}${item.upgradeName}")
            .setText(R.id.tv_bird_price, "${StringHelper.formatBigNum(item.upgradeCoins)}金币")
//                playAnim(imgView, holder.getView(R.id.tv_produce_sec))
        holder.setGone(R.id.viewLock, item.unlocked)


    }

}
