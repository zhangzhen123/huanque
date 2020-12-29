package com.julun.huanque.common.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.View
import android.widget.TextView
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show


/**
 *@创建者   dong
 *@创建时间 2020/2/6 9:39
 *@描述 礼物面板使用的标题View。（TextView+红点）
 */
class GiftTitleView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    //显示文案
    private var mTv: TextView? = null
    //红点
    private var mViewDot: View? = null
    //指示线
    private var mViewLine: View? = null

    constructor(context: Context) : this(context, null)

    init {
        context?.let { con ->
            val view = LayoutInflater.from(con).inflate(R.layout.view_gift_title, this)
            val ta = con.obtainStyledAttributes(attrs, R.styleable.GiftTitleView)
            mTv = view.findViewById<TextView>(R.id.tv)
            mViewDot = view.findViewById<View>(R.id.view_dot)
            mViewLine = view.findViewById<View>(R.id.view_line)
            //文案
            val content = ta.getString(R.styleable.GiftTitleView_text) ?: ""
            setContent(content)
            //红点
            val showDot = ta.getBoolean(R.styleable.GiftTitleView_dot, false)
            showDot(showDot)

            //底部的指示器
            val showLine = ta.getBoolean(R.styleable.GiftTitleView_showLine, false)
            showLine(showLine)
            ta.recycle()
        }
    }

    /**
     * 设置显示内容
     */
    fun setContent(content: String) {
        mTv?.text = content
    }

    /**
     * 控制红点的显示与隐藏
     */
    fun showDot(show : Boolean){
        if (show) {
            mViewDot?.show()
        } else {
            mViewDot?.hide()
        }
    }

    /**
     * 控制指示线的显示与隐藏
     */
    fun showLine(show : Boolean){
        if (show) {
            mViewLine?.show()
        } else {
            mViewLine?.hide()
        }
    }

    /**
     * 设置文本风格
     */
    fun setTextType(tf : Typeface){
        mTv?.typeface = tf
    }


}