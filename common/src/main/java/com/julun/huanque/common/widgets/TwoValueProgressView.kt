package com.julun.huanque.common.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.julun.huanque.common.R
import kotlinx.android.synthetic.main.view_user_level_progress.view.*
import org.jetbrains.anko.backgroundDrawable


/**
 *
 *@author zhangzhen
 *@data 2019/4/16
 * 双值进度条view
 *
 **/


class TwoValueProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    private var progressDrawable: Drawable? = null

    private var secondDrawable: Drawable? = null

    private var totalWidth: Int = 0

    init {
        val typedArray = if (attrs == null) {
            null
        } else {
            context.obtainStyledAttributes(attrs, R.styleable.TwoValueProgressView)

        }
        typedArray?.let {
            progressDrawable = typedArray.getDrawable(R.styleable.TwoValueProgressView_progress_first_color)
            secondDrawable = typedArray.getDrawable(R.styleable.TwoValueProgressView_progress_second_color)
            typedArray.recycle()
        }

        LayoutInflater.from(context).inflate(R.layout.view_user_level_progress, this)
        initViews()
    }


    fun initViews() {
        first_progress.backgroundDrawable = progressDrawable
        second_progress.backgroundDrawable = secondDrawable
    }

    //设置第一进度条0到100
    fun setProcess(process: Int) {
        post {
            totalWidth = width
            val fp = first_progress.layoutParams
            val width = if (process > 100) {
                totalWidth
            } else {
                process * totalWidth / 100
            }
            val ani1 = ValueAnimator.ofInt(fp.width, width)
            ani1.addUpdateListener { valueAnimate ->
                fp.width = valueAnimate.animatedValue as Int
                requestLayout()
            }
            ani1.duration = 200
            ani1.start()
        }

    }

    //设置第二进度条0到100
    fun setSecondProcess(process: Int) {
        post {
            totalWidth = width
            val sp = second_progress.layoutParams
            val width = if (process > 100) {
                totalWidth
            } else {
                process * totalWidth / 100
            }
            val ani1 = ValueAnimator.ofInt(sp.width, width)
            ani1.addUpdateListener { valueAnimate ->
                sp.width = valueAnimate.animatedValue as Int
                requestLayout()
            }
            ani1.duration = 200
            ani1.start()
        }
    }

}