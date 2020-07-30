package com.julun.huanque.core.widgets.live.message

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.julun.huanque.common.suger.DefaultAnimatorListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.isVisible
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.bean.beans.AnchorUpgradeEvent
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.view_anchor_level_up_content.view.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import java.util.concurrent.TimeUnit

/**
 * Created by nirack on 17-5-11.
 */

class AnchorsLevelUpView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    init {
        visibility = View.GONE

        LayoutInflater.from(this.context).inflate(R.layout.view_anchor_level_up_content, this)
        if(!isInEditMode)
        resetApperance()
    }

    private fun resetApperance() {

        val imageWidth = ScreenUtils.screenWidthFloat
        //整个背景图 748 * 748 , 文字大概开始于 450的文字,稍作微调,445 更合适
        val marginTop = (imageWidth * 445 / 748).toInt()

        this.layoutParams = FrameLayout.LayoutParams(imageWidth.toInt(), imageWidth.toInt()).apply {
            gravity = Gravity.CENTER
        }

        imageView.layoutParams = RelativeLayout.LayoutParams(imageWidth.toInt(), imageWidth.toInt())

        verticalLayout.layoutParams = RelativeLayout.LayoutParams(matchParent, wrapContent).apply {
            topMargin = marginTop
        }

    }

    private val animatorUpdateListener: (ValueAnimator) -> Unit = { animation ->
        val cVal = animation.animatedValue as Float
        val value = if (currentAction == ACTION_SHOW) cVal else 1f - cVal
        this@AnchorsLevelUpView.alpha = value
        this@AnchorsLevelUpView.scaleX = value
        this@AnchorsLevelUpView.scaleY = value
    }

    //出来一秒,回去一秒,停留4秒
    private val anim: ValueAnimator by lazy { ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(1000) }

    private val defaultAnimatorListener: DefaultAnimatorListener = object : DefaultAnimatorListener {
        override fun onAnimationEnd(animation: Animator) {
            if (currentAction == ACTION_SHOW) {
                currentAction = ACTION_HIDE
                this@AnchorsLevelUpView.postDelayed({
                    if(ViewCompat.isAttachedToWindow(this@AnchorsLevelUpView)){
                        startAnimation()
                    }
                }, 4000L)
            } else if (currentAction == ACTION_HIDE) {
                currentAction = ACTION_SHOW
                this@AnchorsLevelUpView.hide()
            }
        }
    }

    private val ACTION_SHOW = 1
    private val ACTION_HIDE = -1

    private var currentAction = ACTION_SHOW

    private val mDisposables = CompositeDisposable()

    fun showAnim(data: AnchorUpgradeEvent) {
        val mDispose = Observable.timer(6L, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!this.isVisible()) {
                        this.show()
                    }
                    updateTextInfo(data)
                    startAnimation()
                }
        mDisposables.add(mDispose)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDisposables.clear()//解除所有的订阅
    }

    private fun startAnimation() {
        anim.removeAllListeners()
        anim.addListener(defaultAnimatorListener)
        anim.start()
        anim.addUpdateListener(animatorUpdateListener)
    }

    private fun updateTextInfo(data: AnchorUpgradeEvent) {
        firstTextView.text = "恭喜${data.sendNickname}"
        secondTextView.text = "获得${data.awardValue}萌豆"
    }

}
