package com.julun.huanque.common.widgets.viewpager


import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

/**
 * 联动viewpager防止被带的viewpager同时滑动
 * [noScroll]控制改vp是否可滑动
 *
 */
class LinkedViewPager : ViewPager {
    private var mCustomPager: LinkedViewPager?=null
    private var forSuper: Boolean = false
    //是否可以滑动
    var noScroll:Boolean=false
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onInterceptTouchEvent(arg0: MotionEvent): Boolean {
        if (!forSuper) {

           mCustomPager?.forSuper(true)
           mCustomPager?.onInterceptTouchEvent(arg0)
           mCustomPager?.forSuper(false)
        }
        if (noScroll){
            return false;
        }else{
            return super.onInterceptTouchEvent(arg0);
        }
    }

    override fun onTouchEvent(arg0: MotionEvent): Boolean {
        if (!forSuper) {
           mCustomPager?.forSuper(true)
           mCustomPager?.onTouchEvent(arg0)
           mCustomPager?.forSuper(false)
        }
        return if (noScroll){
            false
        }else{
            super.onTouchEvent(arg0)
        }
    }

    fun setViewPager(customPager: LinkedViewPager) {
       mCustomPager = customPager
    }

    fun forSuper(forSuper: Boolean) {
        this.forSuper = forSuper
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (!forSuper) {
           mCustomPager?.forSuper(true)
           mCustomPager?.setCurrentItem(item, smoothScroll)
           mCustomPager?.forSuper(false)
        }
        super.setCurrentItem(item, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        if (!forSuper) {
           mCustomPager?.forSuper(true)
           mCustomPager?.currentItem = item
           mCustomPager?.forSuper(false)
        }
        super.setCurrentItem(item)

    }

}