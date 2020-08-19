package com.julun.huanque.core.ui.main.makefriend

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.*
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.core.R
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import kotlinx.android.synthetic.main.act_anonymous_voice.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/18 16:08
 *@描述 匿名语音页面
 */
class AnonymousVoiceActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_anonymous_voice

    //匹配时长的计时器
    private var mDisposable: Disposable? = null

    //匹配动画，第一个动画
    private val animatorFirstSet = AnimatorSet()

    //匹配动画，第二个动画
    private val animatorSecondSet = AnimatorSet()

    //左侧头像缩放动画
    private val leftHeaderAnimatorSet = AnimatorSet()
    private val topHeaderAnimatorSet = AnimatorSet()
    private val rightHeaderAnimatorSet = AnimatorSet()

    //匹配过程中
    private val matchCompositeDisposable = CompositeDisposable()

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        StatusBarUtil.setTransparent(this)
        header_page.textTitle.text = "匿名语音"
        header_page.textTitle.textColor = Color.WHITE
        header_page.imageViewBack.imageResource = R.mipmap.icon_back_white_01

        ImageUtils.loadImage(sdv_header, SessionUtils.getHeaderPic(), 75f, 75f)
        ImageUtils.loadImage(sdv_left, SessionUtils.getHeaderPic(), 60f, 60f)
        ImageUtils.loadImage(sdv_top, SessionUtils.getHeaderPic(), 60f, 60f)
        ImageUtils.loadImage(sdv_right, SessionUtils.getHeaderPic(), 60f, 60f)
        stopMatch()
    }

    override fun initEvents(rootView: View) {
        header_page.imageViewBack.onClickNew {
            stopMatch()
            finish()
        }
        tv_match.onClickNew {
            if (!tv_match.isSelected) {
                startMatch()
            } else {
                stopMatch()
            }
        }
    }

    /**
     * 开始匹配
     */
    private fun startMatch() {
        mDisposable = ObservableTake.interval(0, 1, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                tv_duration.text = "等待 ${TimeUtils.countDownTimeFormat1(it)}"
                val remainder = it % 3
                val waitingContent = when (remainder) {
                    0L -> {
                        "请耐心等待哦."
                    }
                    1L -> {
                        "请耐心等待哦.."
                    }
                    else -> {
                        "请耐心等待哦..."
                    }
                }
                tv_waiting_content.text = waitingContent
            }, {})

        tv_match.isSelected = true
        tv_match.text = "取消匹配"
        tv_content.text = "欢鹊正在努力寻找最适合您的对象"
        tv_waiting_content.show()

        val disposableSweep = Observable.interval(0, 1500L, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val remainder = it % 2
                if (remainder == 0L) {
                    startMatchAnimation(iv_animate_first, animatorFirstSet)
                } else {
                    startMatchAnimation(iv_animate_second, animatorSecondSet)
                }
            }, {})

        matchCompositeDisposable.add(disposableSweep)

        val disposableHeader = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(3)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    0L -> {
                        startHeaderAnimation(sdv_left, leftHeaderAnimatorSet)
                    }
                    1L -> {
                        startHeaderAnimation(sdv_top, topHeaderAnimatorSet)
                    }
                    2L -> {
                        startHeaderAnimation(sdv_right, rightHeaderAnimatorSet)
                    }
                    else -> {
                    }
                }
            }, {})
        matchCompositeDisposable.add(disposableHeader)

    }

    /**
     * 停止匹配
     */
    private fun stopMatch() {
        mDisposable?.dispose()
        tv_match.isSelected = false
        tv_duration.text = "剩余3次"
        tv_match.text = "开始匹配"
        tv_content.text = "欢鹊会为你匹配最合拍的对象"
        tv_waiting_content.hide()
        animatorFirstSet.cancel()
        animatorSecondSet.cancel()
        leftHeaderAnimatorSet.cancel()
        topHeaderAnimatorSet.cancel()
        rightHeaderAnimatorSet.cancel()
        matchCompositeDisposable.clear()
    }

    /**
     * 开始匹配动画
     */
    private fun startMatchAnimation(view: View, set: AnimatorSet) {
        if (set.childAnimations.size > 0) {
            set.start()
            return
        }

        set.apply {
            val scaleXAnimate = ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1.57f)
            val scaleYAnimate = ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1.57f)
            val alphaAnimate = ObjectAnimator.ofFloat(view, "alpha", 1f, 1f, 1f, 1f, 0.3f)

            scaleXAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }

            scaleYAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }
            alphaAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }

            playTogether(scaleXAnimate, scaleYAnimate, alphaAnimate)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    view.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    view.show()
                }

            })
        }
        set.start()
    }

    /**
     * 开始头像缩放动画
     */
    private fun startHeaderAnimation(view: View, set: AnimatorSet) {
        if (set.childAnimations.size > 0) {
            set.start()
            return
        }
        set.apply {
            val scaleXAnimate = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0.5f, 1f, 1f, 0.6f, 0f, 0f)
            val scaleYAnimate = ObjectAnimator.ofFloat(view, "scaleY", 0f, 0.5f, 1f, 1f, 0.6f, 0f, 0f)
            val alphaAnimate = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 0.65f, 1f, 1f, 0.65f, 0.3f, 0.3f)

            scaleXAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }
            scaleYAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }
            alphaAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }

            playTogether(scaleXAnimate, scaleYAnimate, alphaAnimate)
        }
        set.start()
    }

    override fun onStop() {
        super.onStop()
        stopMatch()
        matchCompositeDisposable.clear()
    }

}