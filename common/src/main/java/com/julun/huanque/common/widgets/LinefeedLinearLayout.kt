package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 *@创建者   dong
 *@创建时间 2020/7/30 16:44
 *@描述 可换行的LinearLayout
 */
class LinefeedLinearLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    //子View的数量
    private var mChildCount = 0

    //行数
    private var mLineCount = 0

    //
    private var mWidth = 0
    private var mHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        mLineCount = 0
        mChildCount = childCount

        //没有子控件的时候，给AT_MOST模式默认大小
        if (mChildCount == 0) {
            if (heightMode == MeasureSpec.AT_MOST) {
                height = 0
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                width = 0
            }
            setMeasuredDimension(width, height)
            return
        }
        //至少有一行
        mLineCount++


        //每一行子view的宽度加左右padding的值
        var totalWidthPerLine = paddingLeft + paddingRight
        var layoutParams: LayoutParams? = null
        var child: View? = null
        for (i in 0 until mChildCount) {
            child = getChildAt(i)
            layoutParams = child.layoutParams as? LayoutParams
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            totalWidthPerLine += child.measuredWidth + layoutParams!!.leftMargin + layoutParams.rightMargin
            if (totalWidthPerLine > width) { //需要换行
                //换行后，当前子view需要排到新的一行，totalWidthPerLine也需要更新为新行的宽度，需要加上当前子view的宽度和margin
                totalWidthPerLine =
                    child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + paddingLeft + paddingRight
                mLineCount++
            } else {
            }
        }
        //此viewGroup的高度
        var layoutHeight = paddingTop + paddingBottom
        //在这里每一个子view的高度一致、故只取第一个子view的高度及其marginTop和marginBottom属性
        child = getChildAt(0)
        layoutParams = child.layoutParams as? LayoutParams
        //计算此viewGroup的高度 通过每一行子view的高度 x 行数 + padding 得到总高度
        layoutHeight += (child.measuredHeight + layoutParams!!.topMargin + layoutParams.bottomMargin) * mLineCount

        //AT_MOST模式下、就取上面计算出来的高度
        if (heightMode == MeasureSpec.AT_MOST) {
            height = layoutHeight
        }
        this.mWidth = width
        this.mHeight = height
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mLineCount > 1) {
            var child: View
            var layoutParams: LayoutParams? = null
            val childCountPerLine = IntArray(mLineCount) //每行的个数
            var currentLine = 0
            var hasInArrayCount = 0 //已入数组的个数
            var childrenWidth = paddingLeft + paddingRight
            for (i in 0 until mChildCount) {
                child = getChildAt(i)
                layoutParams = child.layoutParams as LayoutParams
                childrenWidth += child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                if (childrenWidth > mWidth) {
                    childCountPerLine[currentLine] = i - hasInArrayCount
                    hasInArrayCount += i - hasInArrayCount
                    currentLine++
                    childrenWidth =
                        paddingLeft + paddingRight + child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                }
            }

            //计算最后一行有多少个子view
            if (currentLine == childCountPerLine.size - 1) {
                childCountPerLine[currentLine] = mChildCount - hasInArrayCount
            }
            var left: Int
            var top: Int
            var bottom: Int
            var totalCountInLines = 0 //遍历过的每一行的子view的个数的和

            //由于默认每一个子view的高度和margin属性都一致。所以选取第一个子view测量即可
            val c = getChildAt(0)
            val lp = c.layoutParams as LayoutParams

            //遍历行
            for (j in childCountPerLine.indices) {
                top = j * (c.measuredHeight + lp.topMargin + lp.bottomMargin) + lp.topMargin
                bottom = c.measuredHeight + top
                left = 0
                for (i in totalCountInLines until childCountPerLine[j] + totalCountInLines) { //遍历每行的子view，布局每行子view
                    child = getChildAt(i)
                    layoutParams = child.layoutParams as LayoutParams
                    left += layoutParams!!.leftMargin
                    child.layout(left + paddingLeft, top + paddingTop, left + child.measuredWidth, bottom)
                    left += child.measuredWidth + layoutParams.rightMargin
                }
                totalCountInLines += childCountPerLine[j] //将当前行子view的个数相加
            }
            //最后一行剩余的子view个数
            val countLastLine = mChildCount - totalCountInLines
            if (countLastLine > 0) {
                left = 0
                top = childCountPerLine.size * (c.measuredHeight + lp.topMargin + lp.bottomMargin) + lp.topMargin
                bottom = c.measuredHeight + top

                //遍历最后一行
                for (i in totalCountInLines until mChildCount) {
                    child = getChildAt(i)
                    layoutParams = child.layoutParams as LayoutParams
                    left += layoutParams!!.leftMargin
                    child.layout(left + paddingLeft, top + paddingTop, left + child.measuredWidth, bottom)
                    left += child.measuredWidth + layoutParams.rightMargin
                }
            }
        }
    }

}