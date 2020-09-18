package com.julun.huanque.fragment

import android.content.DialogInterface
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment

/**
 *@创建者   dong
 *@创建时间 2020/9/16 20:34
 *@描述 预取号的加载中弹窗
 */
class FastDialogFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {
    override fun getLayoutId() = R.layout.loading_alert

    override fun needEnterAnimation() = false

    override fun initViews() {
    }

    override fun onStart() {
        super.onStart()
        dialog?.setOnKeyListener(this)
        setDialogSize(Gravity.CENTER, 30, 30)
//        updateParams()
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true;
//        } else {
//            //这里注意当不是返回键时需将事件扩散，否则无法处理其他点击事件
//            return false;
//        }
        return if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) true else false
    }
}