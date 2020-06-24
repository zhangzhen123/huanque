package com.julun.huanque.common.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.interfaces.KeyBoardHideListener

/**
 *@创建者   dong
 *@创建时间 2020/1/7 11:45
 *@描述 监听软键盘向下按钮的EditText
 */
@SuppressLint("AppCompatCustomView")
class InputEditText(context: Context?, attrs: AttributeSet?) : EditText(context, attrs) {
    var mKeyBoardHideListener: KeyBoardHideListener? = null

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        ULog.i("InputEditText  keyCode = $keyCode,event = $event,event?.action = ${event?.action}")
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == 1) {
            mKeyBoardHideListener?.onKeyHide()
        }
        return super.onKeyPreIme(keyCode, event)
    }

}