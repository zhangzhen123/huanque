package com.julun.huanque.core.widgets.live.simpleffects

import android.animation.*
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.backgroundDrawable

/**
 * Created by djp on 2016/12/13.
 */
object GiftAnimationHelper {
    /**
     * 创建一个从左到右的飞入动画礼物飞入动画
     * @param target
     * @param star 动画起始坐标
     * @param end 动画终止坐标
     * @param duration 持续时间
     * @return
     */
    fun createFlyFromLtoR(target: View, star: Float, end: Float, duration: Long = 300, interpolator: TimeInterpolator = AnticipateOvershootInterpolator()): ObjectAnimator {
        val anim1 = PropertyValuesHolder.ofFloat("translationX", star, end)
        val anim2 = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        val anim = ObjectAnimator.ofPropertyValuesHolder(target, anim1, anim2)
        anim.interpolator = interpolator
        anim.duration = duration
        return anim
    }

    /**
     * @param target
     * *
     * @return
     * * 播放帧动画
     */
    fun startAnimationDrawable(target: ImageView): AnimationDrawable {
        val animationDrawable = target.drawable as AnimationDrawable
        if (animationDrawable != null) {
            target.visibility = View.VISIBLE
            animationDrawable.start()
        }
        return animationDrawable
    }

    /**
     * 设置帧动画
     * @param target
     * @param drawable
     */
    fun setAnimationDrawable(target: ImageView, drawable: AnimationDrawable) {
        target.backgroundDrawable = drawable
    }

    /**
     * 送礼数字变化
     * @param target
     * @param num
     * @return
     */
    fun scaleGiftNum(target: TextView, duration: Long = 300, interpolator: TimeInterpolator = BounceInterpolator()): ObjectAnimator {
        val anim1 = PropertyValuesHolder.ofFloat("scaleX", 3.5f, 0.8f, 1.5f)
        val anim2 = PropertyValuesHolder.ofFloat("scaleY", 3.5f, 0.8f, 1.5f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(target, anim1, anim2)
        animator.interpolator = interpolator
        animator.duration = duration
        return animator
    }
    /**
     * 送礼数字变化
     * @param target
     * @param num
     * @return
     */
    fun scaleGiftNumNew(target: TextView): Animator {
        //第一阶段
        val anim11 = PropertyValuesHolder.ofFloat("scaleX", 3.5f, 0.9f )
        val anim12 = PropertyValuesHolder.ofFloat("scaleY", 3.5f, 0.9f )
        val animator1 = ObjectAnimator.ofPropertyValuesHolder(target, anim11, anim12)
        animator1.duration = 250
        //第二阶段
        val anim21 = PropertyValuesHolder.ofFloat("scaleX",  0.9f ,1.1f,0.9f,1.0f)
        val anim22 = PropertyValuesHolder.ofFloat("scaleY",  0.9f,1.1f,0.9f ,1.0f)
        val animator2 = ObjectAnimator.ofPropertyValuesHolder(target, anim21, anim22)
        animator2.duration = 250/*430*/
        //第三阶段
//        val anim31 = PropertyValuesHolder.ofFloat("scaleX",  1.0f ,1.0f)
//        val anim32 = PropertyValuesHolder.ofFloat("scaleY",  1.0f,1.0f )
//        val animator3 = ObjectAnimator.ofPropertyValuesHolder(target, anim31, anim32)
//        animator3.duration = 250
        val set=AnimatorSet()
        set.playSequentially(animator1,animator2)
        return set
    }

    /**
     * 送礼数字变化
     * @param target
     * @param num
     * @return
     */
    fun scaleGiftNumNewWishCard(target: TextView): Animator {
        //第一阶段
        val anim11 = PropertyValuesHolder.ofFloat("scaleX", 2f, 0.9f )
        val anim12 = PropertyValuesHolder.ofFloat("scaleY", 2f, 0.9f )
        val animator1 = ObjectAnimator.ofPropertyValuesHolder(target, anim11, anim12)
        animator1.duration = 250
        //第二阶段
        val anim21 = PropertyValuesHolder.ofFloat("scaleX",  0.9f ,1.1f,0.9f,1.0f)
        val anim22 = PropertyValuesHolder.ofFloat("scaleY",  0.9f,1.1f,0.9f ,1.0f)
        val animator2 = ObjectAnimator.ofPropertyValuesHolder(target, anim21, anim22)
        animator2.duration = 250/*430*/
        //第三阶段
//        val anim31 = PropertyValuesHolder.ofFloat("scaleX",  1.0f ,1.0f)
//        val anim32 = PropertyValuesHolder.ofFloat("scaleY",  1.0f,1.0f )
//        val animator3 = ObjectAnimator.ofPropertyValuesHolder(target, anim31, anim32)
//        animator3.duration = 250
        val set=AnimatorSet()
        set.playSequentially(animator1,animator2)
        return set
    }
    /**
     * 向左飞 淡出
     * @param target
     * @param star
     * @param end
     * @param duration
     * @param startDelay
     * @return
     */
    fun createFadeAnimator(target: View, star: Float, end: Float, duration: Long = 300): ObjectAnimator {
        val translationY = PropertyValuesHolder.ofFloat("translationX", star, end)
        val alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(target, translationY, alpha)
        animator.duration = duration
        return animator
    }

    /**
     * 按顺序播放动画
     * @param animators
     * @return
     */
    fun startAnimation(animator1: ObjectAnimator, animator2: ObjectAnimator, animator3: ObjectAnimator, animator4: ObjectAnimator, animator5: ObjectAnimator): AnimatorSet {
        val animSet = AnimatorSet()
        animSet.play(animator1).before(animator2)
        animSet.play(animator3).after(animator2)
        animSet.play(animator4).after(animator3)
        animSet.play(animator5).after(animator4)
        animSet.start()
        return animSet
    }
}