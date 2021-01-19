package com.julun.huanque.common.widgets.adapter_animator

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.chad.library.adapter.base.animation.BaseAnimation

class ScaleFadeInAnimation @JvmOverloads constructor() : BaseAnimation {

    override fun animators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f,1.3f, 1f)
        scaleX.duration = 500L
        scaleX.interpolator = DecelerateInterpolator()
        scaleX.startDelay=200L

        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f,1.3f, 1f)
        scaleY.duration = 500L
        scaleY.startDelay=200L
        scaleY.interpolator = DecelerateInterpolator()
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f)
        animator.duration = 500L
        animator.startDelay=200L

        return arrayOf(scaleX, scaleY,animator)
    }
}