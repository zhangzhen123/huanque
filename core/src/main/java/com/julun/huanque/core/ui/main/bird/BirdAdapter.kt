package com.julun.huanque.core.ui.main.bird

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.core.animation.addListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.UpgradeBirdBean
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.ColorfulTextView
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
 *@Description: 棋盘上的鸟
 *
 */
class BirdAdapter(var programId: Long? = null) : BaseQuickAdapter<UpgradeBirdBean, BaseViewHolder>(R.layout.item_bird) {


    companion object {
        //几秒中产生一次收益
        const val CoinsPerSec = 5L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        val colorTv = holder.getView<ColorfulTextView>(R.id.tv_produce_sec)
        colorTv.isColorsVertical = true
        colorTv.colors = intArrayOf(Color.parseColor("#FFFBEF"), Color.parseColor("#E5A441"))
        colorTv.setStrokeColorAndWidth(Color.parseColor("#B96E23"), 2f)
        return holder
    }

    override fun convert(holder: BaseViewHolder, item: UpgradeBirdBean) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_bird)
        when {

            item.isActive && item.upgradeId != null -> {
                imgView.loadImage(item.upgradeIcon, 93f, 93f)
                holder.setGone(R.id.black_bird, true).setGone(R.id.tv_level, false).setGone(R.id.sdv_bird, false)
                    .setGone(R.id.bird_masking, false)
                holder.setText(R.id.tv_level, "${item.upgradeLevel}")
//                imgView.clearAnimation()
            }
            item.upgradeId != null -> {
                imgView.loadImage(item.upgradeIcon, 93f, 93f)
                holder.setGone(R.id.black_bird, true).setGone(R.id.tv_level, false).setGone(R.id.sdv_bird, false)
                    .setGone(R.id.bird_masking, true)
                val pSec = if (programId != null) {
                    item.programCoinsPerSec.multiply(BigInteger.valueOf(CoinsPerSec))
                } else {
                    item.onlineCoinsPerSec.multiply(BigInteger.valueOf(CoinsPerSec))
                }
                holder.setText(R.id.tv_level, "${item.upgradeLevel}")
                    .setText(R.id.tv_produce_sec, "+${StringHelper.formatBigNum(pSec)}")
//                playAnim(imgView, holder.getView(R.id.tv_produce_sec))
            }
            else -> {
                holder.setGone(R.id.black_bird, false).setGone(R.id.tv_level, true).setGone(R.id.sdv_bird, true)
                    .setGone(R.id.bird_masking, true)
            }
        }

    }


    //给相应的view做动画 之所以选择普通动画是因为属性动画不好控制 你无法通过相对应的view取消该view的动画 而且无限循环可能会内存泄漏
    //属性动画根本不能在list viewholder中实现闭环 满足条件开始动画 不满足则关掉动画
    private fun playAnim(view: View, textView: TextView) {
        //普通动画
        val curAnim = view.animation
        if (curAnim != null) {
//                logger.info("当前已有动画无需设置")
            return
        }
        val ani = ScaleAnimation(
            1.0f, 1.02f, 1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        ani.interpolator = AccelerateDecelerateInterpolator()
        ani.repeatMode = Animation.RESTART
        ani.repeatCount = Animation.INFINITE
        ani.duration = 200
        ani.startOffset = CoinsPerSec * 1000L
        view.startAnimation(ani)
        ani.setAnimationListener(object : AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
//                logger("onAnimationRepeat")
                val aniText1 = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, 0f, -dp2pxf(40))
                aniText1.interpolator = AccelerateDecelerateInterpolator()
                aniText1.duration = 200
                //目的是继续显示
                val aniText2 = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, -dp2pxf(40), -dp2pxf(45))
                aniText2.startDelay = 100
                aniText2.duration = 300
                val set = AnimatorSet()
                set.playSequentially(aniText1, aniText2)
                set.addListener(onEnd = {
//                    logger("数字执行完成  开始隐藏")
                    textView?.hide()
                })
                textView.show()
                set.start()

            }

            override fun onAnimationEnd(animation: Animation?) {
//                logger("onAnimationEnd")

            }


            override fun onAnimationStart(animation: Animation?) {
//                logger("onAnimationStart")
            }

        })

    }

    //只用属性动画
    fun playAnim2(view: View, textView: TextView) {


        val aniX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.02f, 1.0f)
        val aniY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.2f, 1.0f)


        val set1 = AnimatorSet()
        set1.interpolator = AccelerateDecelerateInterpolator()
        set1.duration = 200
        set1.playTogether(aniX, aniY)
        set1.addListener(onEnd = {
            textView.show()
        })

        val aniText1 = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, 0f, -dp2pxf(40))
        aniText1.interpolator = AccelerateDecelerateInterpolator()
        aniText1.duration = 200
        //目的是继续显示
        val aniText2 = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, -dp2pxf(40), -dp2pxf(45))
        aniText2.startDelay = 100
        aniText2.duration = 300 * 100
        val set2 = AnimatorSet()
        set2.playSequentially(aniText1, aniText2)
        set2.addListener(onEnd = {
//                    logger("数字执行完成  开始隐藏")
            textView?.hide()
        })

        val ani = AnimatorSet()
        ani.playSequentially(set1, set2)
        ani.start()
    }
}
