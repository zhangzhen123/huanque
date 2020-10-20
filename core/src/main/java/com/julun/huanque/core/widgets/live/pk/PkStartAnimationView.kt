package com.julun.huanque.core.widgets.live.pk

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.R
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.view_pk_start.view.*

/**
 *@创建者   dong
 *@创建时间 2020/10/20 10:09
 *@描述 PK 开始动画
 */
class PkStartAnimationView(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    init {
        if (context != null) {
            LayoutInflater.from(context).inflate(R.layout.view_pk_start, this)
        }
    }

    //左侧滑入动画
    private var mTranslationAnimatorSet: AnimatorSet? = null

    //光线移动动画
    private var mLightTranslateSet: AnimatorSet? = null

    //光线渐变动画
    private var mLeftLightAlphaSet: AnimatorSet? = null
    private var mRightLightAlphaSet: AnimatorSet? = null

    //View整体的透明度动画
    private var mViewAlphaSet: AnimatorSet? = null


    //VS动画
    private var mVsSet: AnimatorSet? = null

    //胜利动画
    private var mWinScaleXSet: AnimatorSet? = null
    private var mWinScaleYSet: AnimatorSet? = null

    //胜利移动动画
    private var mWinTranslateSet: AnimatorSet? = null

    /**
     * 显示左侧的视图
     */
    fun setLeftViewData(header: String, nickname: String) {
        sdv_pk_left.loadImage("${StringHelper.getOssImgUrl(header)}${BusiConstant.OSS_160}")
        tv_pk_nickname_left.text = nickname
    }

    /**
     * 显示右侧的视图
     */
    fun setRightViewData(header: String, nickname: String) {
        sdv_pk_right.loadImage("${StringHelper.getOssImgUrl(header)}${BusiConstant.OSS_160}")
        tv_pk_nickname_right.text = nickname
    }


    /**
     * 开始动画
     */
    fun startAnimation() {
        //透明度动画
        this@PkStartAnimationView.show()
        mViewAlphaSet?.cancel()

        if (mViewAlphaSet == null) {
            mViewAlphaSet = AnimatorSet()
            val alphaAnimation1 = ObjectAnimator.ofFloat(view_bg, "alpha", 0f, 0.4f).apply { duration = 300 }
            val alphaAnimation2 = ObjectAnimator.ofFloat(view_bg, "alpha", 0.4f, 0.4f).apply { duration = 1400 }
            val alphaAnimation3 = ObjectAnimator.ofFloat(this@PkStartAnimationView, "alpha", 0.4f, 0f).apply { duration = 300 }
            mViewAlphaSet!!.playSequentially(alphaAnimation1, alphaAnimation2, alphaAnimation3)
            mViewAlphaSet!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    this@PkStartAnimationView.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    this@PkStartAnimationView.alpha = 1f
                }
            })

        }
        mViewAlphaSet?.start()


        mTranslationAnimatorSet?.cancel()
        //左右视图  位移动画
        if (mTranslationAnimatorSet == null) {
            val leftTranslationAnimator = ObjectAnimator.ofFloat(con_left, "translationX", -dp2pxf(232), 0f)
                .apply {
                    duration = 400
                }

            val leftAlphaAnimator = ObjectAnimator.ofFloat(con_left, "alpha", 0f, 1f)
                .apply {
                    duration = 400
                }
            val rightTranslationAnimator = ObjectAnimator.ofFloat(con_right, "translationX", dp2pxf(232), 0f)
                .apply {
                    duration = 400
                }
            val rightAlphaAnimator = ObjectAnimator.ofFloat(con_right, "alpha", 0f, 1f)
                .apply {
                    duration = 400
                }

            mTranslationAnimatorSet = AnimatorSet()
            mTranslationAnimatorSet?.playTogether(leftTranslationAnimator, leftAlphaAnimator, rightTranslationAnimator, rightAlphaAnimator)
        }

        mTranslationAnimatorSet?.start()

        //光线移动动画
        //左侧光移动动画
        mLightTranslateSet?.cancel()
        if (mLightTranslateSet == null) {
            val leftLightXTranslationAnimator = ObjectAnimator.ofFloat(iv_light_left, "translationX", dp2pxf(31), -dp2pxf(7))
                .apply {
                    interpolator = DecelerateInterpolator()
                }
            val leftLightYTranslationAnimator = ObjectAnimator.ofFloat(iv_light_left, "translationY", -dp2pxf(50), dp2pxf(10))
                .apply {
                    interpolator = DecelerateInterpolator()
                }

            val rightLightXTranslationAnimator = ObjectAnimator.ofFloat(iv_light_right, "translationX", -dp2pxf(31), dp2pxf(7))
                .apply {
                    interpolator = DecelerateInterpolator()
                }
            val rightLightYTranslationAnimator = ObjectAnimator.ofFloat(iv_light_right, "translationY", dp2pxf(50), -dp2pxf(10))
                .apply {
                    interpolator = DecelerateInterpolator()
                }

            mLightTranslateSet = AnimatorSet()
            mLightTranslateSet?.playTogether(
                leftLightXTranslationAnimator,
                leftLightYTranslationAnimator,
                rightLightXTranslationAnimator,
                rightLightYTranslationAnimator
            )
            mLightTranslateSet?.duration = 1000
        }
        mLightTranslateSet?.startDelay = 100
        mLightTranslateSet?.start()

        //光线渐变动画
        mLeftLightAlphaSet?.cancel()
        mRightLightAlphaSet?.cancel()
        if (mLeftLightAlphaSet == null || mRightLightAlphaSet == null) {

            val leftFirstAlphaAnimator = ObjectAnimator.ofFloat(iv_light_left, "alpha", 0f, 1f).apply {
                duration = 500

            }
            val leftSecondAlphaAnimator = ObjectAnimator.ofFloat(iv_light_left, "alpha", 1f, 1f).apply {
                duration = 150
            }
            val leftThirdAlphaAnimator = ObjectAnimator.ofFloat(iv_light_left, "alpha", 1f, 0f).apply {
                duration = 350
            }

            val rightFirstAlphaAnimator = ObjectAnimator.ofFloat(iv_light_right, "alpha", 0f, 1f).apply {
                duration = 500
            }
            val rightSecondAlphaAnimator = ObjectAnimator.ofFloat(iv_light_right, "alpha", 1f, 1f).apply {
                duration = 150
            }
            val rightThirdAlphaAnimator = ObjectAnimator.ofFloat(iv_light_right, "alpha", 1f, 0f).apply {
                duration = 350
            }


            mLeftLightAlphaSet = AnimatorSet()
            mRightLightAlphaSet = AnimatorSet()

            mLeftLightAlphaSet?.playSequentially(
                leftFirstAlphaAnimator,
                leftSecondAlphaAnimator,
                leftThirdAlphaAnimator
            )
            mRightLightAlphaSet?.playSequentially(
                rightFirstAlphaAnimator,
                rightSecondAlphaAnimator,
                rightThirdAlphaAnimator
            )
        }
        mLeftLightAlphaSet?.startDelay = 100
        mRightLightAlphaSet?.startDelay = 100

        mLeftLightAlphaSet?.start()
        mRightLightAlphaSet?.start()

        mVsSet?.cancel()
        if (mVsSet == null) {
            mVsSet = AnimatorSet()
            val vsXScaleAnimation = ObjectAnimator.ofFloat(iv_vs, "scaleX", 1.5f, 1f)
                .apply {
                    interpolator = OvershootInterpolator()
                }
            val vsYScaleAnimation = ObjectAnimator.ofFloat(iv_vs, "scaleY", 1.5f, 1f)
                .apply {
                    interpolator = OvershootInterpolator()
                }

            val vsAlphaAnimation = ObjectAnimator.ofFloat(iv_vs, "alpha", 0f, 1f)

            mVsSet?.playTogether(vsXScaleAnimation, vsYScaleAnimation, vsAlphaAnimation)
            mVsSet?.duration = 900
        }
        mVsSet?.start()


    }


    /**
     * 胜利动画
     */
    fun startWinAnimation() {
        mWinScaleXSet?.cancel()
        mWinScaleYSet?.cancel()
        if (mWinScaleXSet == null || mWinScaleYSet == null) {
            mWinScaleXSet = AnimatorSet()
            mWinScaleYSet = AnimatorSet()
            val scaleXAnimation1 = ObjectAnimator.ofFloat(iv_win, "scaleX", 0f, 1.1f).apply {
                duration = 400
                interpolator = LinearInterpolator()
            }
            val scaleXAnimation2 = ObjectAnimator.ofFloat(iv_win, "scaleX", 1.1f, 1f).apply { duration = 250 }
            val scaleXAnimation3 = ObjectAnimator.ofFloat(iv_win, "scaleX", 1f, 1f).apply { duration = 850 }
            val scaleXAnimation4 = ObjectAnimator.ofFloat(iv_win, "scaleX", 1f, 0.5f).apply { duration = 500 }

            mWinScaleXSet?.playSequentially(scaleXAnimation1, scaleXAnimation2, scaleXAnimation3, scaleXAnimation4)

            val scaleYAnimation1 = ObjectAnimator.ofFloat(iv_win, "scaleY", 0f, 1.1f).apply { duration = 400 }
            val scaleYAnimation2 = ObjectAnimator.ofFloat(iv_win, "scaleY", 1.1f, 1f).apply { duration = 250 }
            val scaleYAnimation3 = ObjectAnimator.ofFloat(iv_win, "scaleY", 1f, 1f).apply { duration = 850 }
            val scaleYAnimation4 = ObjectAnimator.ofFloat(iv_win, "scaleY", 1f, 0.5f).apply { duration = 500 }
            mWinScaleYSet?.playSequentially(scaleYAnimation1, scaleYAnimation2, scaleYAnimation3, scaleYAnimation4)

            mWinScaleXSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    iv_win.show()
                }
            })
        }
        mWinScaleXSet?.start()
        mWinScaleYSet?.start()

        mWinTranslateSet?.cancel()
        if (mWinTranslateSet == null) {
            mWinTranslateSet = AnimatorSet()
            //X轴的移动位置
            val translationX = ScreenUtils.getScreenWidth(context) / 4 - dp2pxf(29) - dp2pxf(5)
            //Y轴移动位置
            val translationY = dp2pxf(45)

            val winXTranslationAnimator = ObjectAnimator.ofFloat(iv_win, "translationX", 0f, -translationX)
            val winYTranslationAnimator = ObjectAnimator.ofFloat(iv_win, "translationY", 0f, translationY)
            mWinTranslateSet!!.playTogether(winXTranslationAnimator, winYTranslationAnimator)
            mWinTranslateSet?.duration = 500

        }
        mWinTranslateSet?.startDelay = 1500
        mWinTranslateSet?.start()

    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mViewAlphaSet?.removeAllListeners()
        mViewAlphaSet?.cancel()
        mWinScaleXSet?.removeAllListeners()
        mWinScaleXSet?.cancel()
        mTranslationAnimatorSet?.cancel()
        mLightTranslateSet?.cancel()
        mLeftLightAlphaSet?.cancel()
        mRightLightAlphaSet?.cancel()
        mVsSet?.cancel()
        mWinScaleYSet?.cancel()
        mWinTranslateSet?.cancel()
    }
}