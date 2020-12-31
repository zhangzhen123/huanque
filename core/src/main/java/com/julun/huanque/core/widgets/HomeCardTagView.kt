package com.julun.huanque.core.widgets

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import com.julun.huanque.common.bean.beans.ManagerTagBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.view_card_tag.view.*


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/22 11:52
 *
 *@Description: HomeCardTagView 首页的卡片滑动
 *
 */
class HomeCardTagView : LinearLayout {

    private val logger = ULog.getLogger("HomeCardTagView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_card_tag, this)
        this.gravity = Gravity.CENTER
    }

    var listener: Animator.AnimatorListener? = null
    private var isLeft: Boolean = false
    fun setLeftDot(left: Boolean) {
        isLeft = left
        if (left) {
            fl_start_dot.show()
            fl_end_dot.hide()
        } else {
            fl_start_dot.hide()
            fl_end_dot.show()
        }
    }

    private var aniSet: AnimatorSet? = null
    private var _currentData: ManagerTagBean? = null
    fun getCurrentData(): ManagerTagBean? {
        return _currentData
    }

    fun startSetDataAndAni(data: ManagerTagBean) {
        _currentData = data
//        this.show()
        tv_tag.text = data.tagName
        sdv_tag.loadImage(data.tagIcon, 18f, 18f)
        logger.info("当前的动画值：$data")
        aniSet?.cancel()
        if (aniSet == null) {
            val dotView =
                if (isLeft) {
                    fl_start_dot
                } else {
                    fl_end_dot
                }
            val ani1 = ObjectAnimator.ofFloat(dotView, View.ALPHA, 0f, 1f)
            val ani2 = ObjectAnimator.ofFloat(dotView, View.SCALE_X, 0f, 1.2f, 1.0f)
            val ani3 = ObjectAnimator.ofFloat(dotView, View.SCALE_Y, 0f, 1.2f, 1.0f)
            val aniSet1 = AnimatorSet()
            aniSet1.duration = 500
            aniSet1.playTogether(ani1, ani2, ani3)
            //

            val ani21 = ObjectAnimator.ofFloat(ll_content, View.ALPHA, 0f, 1f)
            ani21.duration = 300
            val width = dp2px(16 + 18 + 16) + (data.tagName).length * tv_tag.textSize
            val ani22 = ValueAnimator.ofInt(0, width.toInt())
            logger.info("ll_content.width=${ll_content.width}")
            val llp = ll_content.layoutParams
            ani22.duration = 300
            ani22.addUpdateListener { value ->
//                logger.info("value=${value.animatedValue}")
                llp.width = (value.animatedValue as Int)
                ll_content.requestLayout()
            }
            val aniSet2 = AnimatorSet()
            aniSet2.playTogether(ani21, ani22)

            val aniShowSet = AnimatorSet()
            aniShowSet.playSequentially(aniSet1, aniSet2)

            //消失动画
            val ani31 = ObjectAnimator.ofFloat(ll_content, View.ALPHA, 1f, 0f)
            ani31.duration = 300
            val ani32 = ValueAnimator.ofInt(width.toInt(), 0)
            ani32.duration = 300
            ani32.addUpdateListener { value ->
                llp.width = (value.animatedValue as Int)
                ll_content.requestLayout()
            }
            val aniSet3 = AnimatorSet()
            aniSet3.playTogether(ani31, ani32)


            val ani41 = ObjectAnimator.ofFloat(dotView, View.ALPHA, 1f, 0f)
            val ani42 = ObjectAnimator.ofFloat(dotView, View.SCALE_X, 1f, 1.2f, 0f)
            val ani43 = ObjectAnimator.ofFloat(dotView, View.SCALE_Y, 1f, 1.2f, 0f)
            val aniSet4 = AnimatorSet()
            aniSet4.duration = 500
            aniSet4.playTogether(ani41, ani42, ani43)

            val aniHideSet = AnimatorSet()
            aniHideSet.playSequentially(aniSet3, aniSet4)
            aniHideSet.startDelay = 3000

            aniSet = AnimatorSet()
            aniSet!!.playSequentially(aniShowSet, aniHideSet)

            aniSet!!.addListener(onStart = {
                this.show()
                dotView.alpha = 0f
                llp.width = 0
                ll_content.requestLayout()
            })
        }
        if (listener != null) {
            aniSet!!.addListener(listener)
        }
        aniSet?.start()
    }

    fun removeListener() {
        if (aniSet != null && listener != null) {
            aniSet!!.removeListener(listener)
        }
        listener = null
    }

    fun stopAni() {
        removeListener()
        if (this.isVisible) {
            this.hide()
        }
        if (aniSet?.isRunning == true) {
            aniSet?.cancel()
        }
    }
}
