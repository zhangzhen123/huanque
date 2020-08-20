package com.julun.huanque.agora.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.*
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.agora.R
import com.julun.huanque.agora.viewmodel.AnonymousVoiceViewModel
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.AnonymousBasicInfo
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.TimeUtils
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import kotlinx.android.synthetic.main.act_anonymous_voice.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/18 16:08
 *@描述 匿名语音页面
 */
@Route(path = ARouterConstant.ANONYMOUS_VOICE_ACTIVITY)
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

    private val voiceShowAnimatorSet = AnimatorSet()
    private val showBtnAnimatorSet = AnimatorSet()

    //匹配过程中
    private val matchCompositeDisposable = CompositeDisposable()


    //通话过程中
    private val voiceCompositeDisposable = CompositeDisposable()

    private var mAnonymousVoiceViewModel: AnonymousVoiceViewModel? = null

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        StatusBarUtil.setTransparent(this)
        header_page.textTitle.text = "匿名语音"
        header_page.textTitle.textColor = Color.WHITE
        header_page.imageViewBack.imageResource = R.mipmap.icon_back_white_01

        con_match.show()
        con_voice.hide()

        ImageUtils.loadImage(sdv_header, SessionUtils.getHeaderPic(), 75f, 75f)
        ImageUtils.loadImage(sdv_left, SessionUtils.getHeaderPic(), 60f, 60f)
        ImageUtils.loadImage(sdv_top, SessionUtils.getHeaderPic(), 60f, 60f)
        ImageUtils.loadImage(sdv_right, SessionUtils.getHeaderPic(), 60f, 60f)
        stopMatch()

        initViewModel()
        mAnonymousVoiceViewModel?.getBasicData()
    }

    private fun initViewModel() {
        mAnonymousVoiceViewModel = ViewModelProvider(this).get(AnonymousVoiceViewModel::class.java)
        mAnonymousVoiceViewModel?.basicData?.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
    }

    private fun showViewByData(info: AnonymousBasicInfo) {
        tv_duration.text = "剩余${info.surplusTimes}次"
        info.headPics.forEachIndexed { index, header ->
            when (index) {
                0 -> {
                    sdv_left.loadImage(header, 60f, 60f)
                }
                1 -> {
                    sdv_top.loadImage(header, 60f, 60f)
                }
                2 -> {
                    sdv_right.loadImage(header, 60f, 60f)
                }
            }
        }
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
//        Observable.timer(2, TimeUnit.SECONDS)
//            .bindUntilEvent(this, ActivityEvent.STOP)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ matchSuccess() }, {})

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
        tv_duration.text = "剩余${mAnonymousVoiceViewModel?.basicData?.value?.surplusTimes}次"
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
        set.addListener(object : Animator.AnimatorListener {
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
        set.start()
    }


    /**
     * 匹配成功
     */
    private fun matchSuccess() {
        con_match.hide()
        header_page.imageViewBack.hide()
        if (voiceShowAnimatorSet.childAnimations.size <= 0) {
            //会话内容显示动画
            val voiceShowAnimator = ObjectAnimator.ofFloat(con_voice, "alpha", 0f, 1f)
            voiceShowAnimator.duration = 200
            voiceShowAnimator.interpolator = LinearInterpolator()
            //头像移动动画
            val mineHeaderTranslate = ObjectAnimator.ofFloat(sdv_mine, "translationX", 0f, -(dp2pxf(40) + dip(75) / 2))
            mineHeaderTranslate.duration = 200
            mineHeaderTranslate.interpolator = LinearInterpolator()
            //对方头像移动动画
            val otherHeaderTranslate = ObjectAnimator.ofFloat(sdv_other, "translationX", 0f, (dp2pxf(40) + dip(75) / 2))
            otherHeaderTranslate.duration = 200
            otherHeaderTranslate.interpolator = LinearInterpolator()

            voiceShowAnimatorSet.playTogether(voiceShowAnimator, mineHeaderTranslate, otherHeaderTranslate)
            voiceShowAnimatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    con_voice.show()
                }

            })

        }
        voiceShowAnimatorSet.start()
        ll_quiet.show()
        ll_close.show()
        ll_hands_free.show()
        val countDownDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(181)
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this, ActivityEvent.STOP)
            .subscribe({
                tv_reminder_time.text = "剩余时间 ${TimeUtils.countDownTimeFormat1(180 - it)}"
            }, {})

        val timerDisposable = Observable.timer(200, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                ImageUtils.loadGifImageLocal(sdv_animation, R.drawable.heat_bounce)
                showBtn()
            }, {})

        voiceCompositeDisposable.add(countDownDisposable)
        voiceCompositeDisposable.add(timerDisposable)
    }

    private fun showBtn() {

        if (showBtnAnimatorSet.childAnimations.size <= 0) {
            val mineAlphaAnimator = ObjectAnimator.ofFloat(tv_open_mine, "alpha", 0f, 1f)
            mineAlphaAnimator.duration = 200
            mineAlphaAnimator.interpolator = LinearInterpolator()

            val otherAlphaAnimator = ObjectAnimator.ofFloat(tv_open_other, "alpha", 0f, 1f)
            otherAlphaAnimator.duration = 200
            otherAlphaAnimator.interpolator = LinearInterpolator()

            showBtnAnimatorSet.playTogether(mineAlphaAnimator, otherAlphaAnimator)
        }

        showBtnAnimatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                tv_open_mine.show()
                tv_open_other.show()
            }

        })
        showBtnAnimatorSet.start()
    }

    override fun onStop() {
        super.onStop()
        stopMatch()
        matchCompositeDisposable.clear()
        voiceCompositeDisposable.clear()
    }

}