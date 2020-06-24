package com.julun.huanque.common.widgets.bounceview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import org.jetbrains.anko.dip

/**
 * Created by dong on 2018/5/9.
 */
class BounceView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val mHeight = dip(23)
    private val mWidth = dip(80)

    constructor(context: Context?) : this(context, null)

    private val animationC = ValueAnimator.ofFloat(0f, 1500f)

    init {
        context?.let {
            val view1 = BounceItemView(context)
            addView(view1)
            val params1 = RelativeLayout.LayoutParams(mHeight, it.dip(38))
            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            view1.layoutParams = params1

            val view2 = BounceItemView(context)
            addView(view2)
            val params2 = RelativeLayout.LayoutParams(mHeight, it.dip(38))
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params2.addRule(RelativeLayout.CENTER_HORIZONTAL)
            view2.layoutParams = params2

            val view3 = BounceItemView(context)
            addView(view3)
            val params3 = RelativeLayout.LayoutParams(mHeight, it.dip(38))
            params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            view3.layoutParams = params3

            view1.repeatListener = object : BounceItemView.RepeatListener {
                override fun onRepeat() {
                    if(ViewCompat.isAttachedToWindow(view2)){
                        view2.startPlay()
                    }
                }
            }

            view2.repeatListener = object : BounceItemView.RepeatListener {
                override fun onRepeat() {
                    if(ViewCompat.isAttachedToWindow(view3)){
                        view3.startPlay()
                    }
                }
            }

            animationC.duration = 1500
            animationC.interpolator = LinearInterpolator()
            animationC.repeatCount = ValueAnimator.INFINITE
            animationC.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    if(ViewCompat.isAttachedToWindow(view1)){
                        view1.startPlay()
                    }
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    if(ViewCompat.isAttachedToWindow(view1)){
                        view1.startPlay()
                    }
                }

            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var tempHeightSpec = heightMeasureSpec
        var tempWidthSpec = widthMeasureSpec
        if (MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
            tempHeightSpec = MeasureSpec.makeMeasureSpec(mHeight, View.MeasureSpec.EXACTLY)
        }
        if (MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.AT_MOST) {
            tempWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, View.MeasureSpec.AT_MOST)
        }
        super.onMeasure(tempWidthSpec, tempHeightSpec)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animationC?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationC?.removeAllListeners()
        animationC?.cancel()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.GONE) {
            animationC?.cancel()
        } else {
            animationC?.start()
        }
    }

}