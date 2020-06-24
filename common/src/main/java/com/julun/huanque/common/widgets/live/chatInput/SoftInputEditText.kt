package com.julun.huanque.common.widgets.live.chatInput

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 直播间发消息输入框
 * Created by djp on 2016/11/8.
 */
class SoftInputEditText(context: Context?, attrs: AttributeSet?) : EditText(context, attrs) {

    private var onToggleListener: OnToggleListener? = null
    private var isShowSoftInput: Boolean = false

    /**
     * 在输入法之前处理键盘事件
     */
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onToggleListener?.activeBackKey(this)
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_DOWN && !this.isShowSoftInput) {
            this.isShowSoftInput = true
            this.onToggleListener?.activeInputKey(this)
        }
        return super.onTouchEvent(event)
    }

    /**
     * 显示软键盘
     */
    fun showSoftInput() {
        this.isShowSoftInput = true
        (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, 0)
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInput() {
        this.isShowSoftInput = false
        (this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun isShowSoftInput(): Boolean {
        return this.isShowSoftInput
    }

    fun setOnToggleListener(onToggleListener: OnToggleListener) {
        this.onToggleListener = onToggleListener
    }

    interface OnToggleListener {
        fun activeBackKey(paramEditText: EditText)

        fun activeInputKey(paramEditText: EditText)
    }

}