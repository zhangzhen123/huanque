package com.julun.huanque.common.widgets.live.chatInput

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.julun.huanque.common.R
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.animtors.evaluators.HsvEvaluator
import org.jetbrains.anko.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


/**
 * 简单的难以置信的switcch :-(
 * Created by nirack on 17-5-9.
 */
class AnIncredibleSimpleSwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {

    private val margin = dip(1)
    private var textOn: String? by Delegates.vetoable("") { _: KProperty<*>, _: String?, newValue: String? ->
        newValue != null
    }
    private var textOff: String? by Delegates.vetoable("") { _: KProperty<*>, _: String?, newValue: String? ->
        newValue != null
    }
    //选中的背景色
    private var mBgcolorOn = Color.GRAY
    //未被选中的背景色
    private var mBgcolorOff = Color.GRAY
    //选中的字体颜色
    private var mTextColorOn = Color.WHITE
    //未被选中的字体颜色
    private var mTextColorOff = Color.WHITE
    //边框的颜色  0表示没有边框，需要设置边框的时候给个颜色值就ok
    private var mBorderColor = 0
    //文案背景色选中
    private var mTvBgColorOn = Color.WHITE
    //文案背景色未选中
    private var mTvBgColorOff = Color.WHITE
    //圆角 高度的一半
    private var mCorner = 0

    private var checked: Boolean by Delegates.observable(false) { _, _, newValue ->
        restTextLayoutParam(newValue)
        onExtraCheckChangeListener(this@AnIncredibleSimpleSwitchView, newValue)
        resetAppearance()
    }

    fun isChecked(): Boolean = this.checked

    private fun restTextLayoutParam(newValue: Boolean) {
        if (triggerButton.width == 0) {
            //view没有绘制完成，获取不到宽高，无法准确进行动画，直接通过布局设置
            val params = triggerButton.layoutParams as? RelativeLayout.LayoutParams ?: return
            if (checked) {
                params.alignParentRight()
            } else {
                params.alignParentLeft()
            }
            return
        }
        val params = triggerButton.layoutParams as RelativeLayout.LayoutParams

        triggerButton.setLayerType(LAYER_TYPE_HARDWARE, null)
        if (newValue) {
            //选中
            val xLeng = layoutParams.width - rightPadding - params.rightMargin - triggerButton.width
            triggerButton.animate().x((xLeng).toFloat())
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            triggerButton.setLayerType(View.LAYER_TYPE_NONE, null)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            triggerButton.setLayerType(View.LAYER_TYPE_NONE, null)
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    }).start()
        } else {
            triggerButton.animate().x((leftPadding + params.leftMargin).toFloat())
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            triggerButton.setLayerType(View.LAYER_TYPE_NONE, null)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            triggerButton.setLayerType(View.LAYER_TYPE_NONE, null)
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }
                    }).start()

        }
    }

    private val triggerButton: TextView by lazy {
        textView {
            text = getSwitchText()
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = 12f
            backgroundDrawable = shapeOfTextBackGround
            textColor = if (checked) mTextColorOn else mTextColorOff
        }.apply {
            //            setPadding(margin * 3, margin, margin * 3, margin)
            //设置初始状态.....
//            val h = this@AnIncredibleSimpleSwitchView.height - margin * 2
//            ULog.i("DXC  h = $h,margin = $margin,外层height = ${this@AnIncredibleSimpleSwitchView.height}")
            val params = layoutParams as? RelativeLayout.LayoutParams
//            params.centerVertically()
            params?.setMargins(margin, margin, margin, margin)
////            params.alignParentLeft()
            layoutParams = params
//            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
    }

//    private val shapeOfCtrGbSelected: GradientDrawable by lazy {
//        GradientDrawable().apply {
//            cornerRadius = dip(15).toFloat()
//            //getColorStateList 低版本不兼容
////            color = resources.getColorStateList(R.color.text_highlight)
//            setColor(resources.getColor(R.color.text_highlight))
//        }
//    }

    private val shapeOfCtrGbBackground: GradientDrawable by lazy {
        GradientDrawable().apply {
            cornerRadius = dip(15).toFloat()
            if (mBorderColor != 0) {
                setStroke(dip(1), mBorderColor)
            }
//            color = resources.getColorStateList(R.color.bg_trible_db)
            setColor(mBgcolorOff)
        }
    }

    private val shapeOfTextBackGround: GradientDrawable by lazy {
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(1, 1)
//            cornerRadius = dip(13).toFloat()
//            setStroke(dip(1),resources.getColor(R.color.black_999))
//            color = resources.getColorStateList(R.color.white)
            setColor(mTvBgColorOff)
        }
    }

    private fun resetAppearance() {

        //字体颜色变化
        if (mTextColorOff != mTextColorOn) {
            colorGradientAnim(triggerButton, "textColor", mTextColorOff, mTextColorOn)
        }
        //背景颜色渐变
        if (mBgcolorOff != mBgcolorOn) {
            colorGradientAnim(shapeOfCtrGbBackground, "color", mBgcolorOff, mBgcolorOn)
        }
        //文本背景色
        if (mTvBgColorOff != mTvBgColorOn) {
            colorGradientAnim(shapeOfTextBackGround, "color", mTvBgColorOff, mTvBgColorOn)
        }
    }

    /**
     * 颜色渐变动画
     * @param target 目标对象
     * @param name 属性名称
     * @param offColor 关闭时候的颜色
     * @param onColor 开启时候的颜色
     */
    private fun colorGradientAnim(target: Any, name: String, @ColorInt offColor: Int, @ColorInt onColor: Int) {
        var startColor = offColor
        var endColor = onColor
        if (!checked) {
            startColor = onColor
            endColor = offColor
        }
        var animator = ObjectAnimator.ofInt(target, name, startColor, endColor)
        animator.setEvaluator(HsvEvaluator())
        animator.start()
    }

    private fun getSwitchText(): CharSequence {
        return if (!checked) textOff!! else textOn!!
    }

    init {
        initAttrs(attrs)
        triggerButton

//        triggerButton.layoutParams = LayoutParams(wrapContent, wrapContent).apply {
//            topMargin = 5
//            centerVertically()
//        }

//        restTextLayoutParam(checked)

        this.backgroundDrawable = shapeOfCtrGbBackground


        this.setOnClickListener {
            checked = !checked
        }

//        resetAppearance()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val tempHeight = MeasureSpec.getSize(heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            mCorner = tempHeight / 2

            val h = tempHeight - margin * 2
            val params = triggerButton.layoutParams as? RelativeLayout.LayoutParams
            params?.height = h
            params?.width = h
//        params.centerVertically()
            params?.setMargins(margin, margin, margin, margin)
//        params.alignParentLeft()
            triggerButton.layoutParams = params

        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        resetAppearance()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AnIncredibleSimpleSwitchView)
            if (ta != null) {
                textOff = ta.getString(R.styleable.AnIncredibleSimpleSwitchView_textOff)
                textOn = ta.getString(R.styleable.AnIncredibleSimpleSwitchView_textOn)
                mBgcolorOn = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_bgcolorOn, Color.GRAY)
                mBgcolorOff = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_bgcolorOff, Color.GRAY)
                mTextColorOn = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_textColorOn, Color.WHITE)
                mTextColorOff = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_textColorOff, Color.WHITE)
                mBorderColor = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_borderColor, 0)
                mTvBgColorOn = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_tvbgColorOn, 0)
                mTvBgColorOff = ta.getColor(R.styleable.AnIncredibleSimpleSwitchView_tvbgColorOff, 0)
                ta.recycle()
            }
        }
    }

    /**
     * 立刻设置之间的状态,
     * 这个方法必须在该组建渲染完毕之后才能调用
     * @param isChecked 新的状态
     */
    fun setCheckedImmediately(isChecked: Boolean) {
        checked = isChecked
    }

    /**
     * 选中状态改变之后的额外工作,
     */
    private var onExtraCheckChangeListener: (view: AnIncredibleSimpleSwitchView, checked: Boolean) -> Unit = { _, _ ->
    }

    fun setOnCheckedChangeListener(callback: (view: AnIncredibleSimpleSwitchView, checked: Boolean) -> Unit) {
        this.onExtraCheckChangeListener = callback
    }

}
