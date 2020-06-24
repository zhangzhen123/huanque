package com.julun.huanque.common.base.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.R
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.utils.DensityUtils

/**
 * 底部弹出框
 * Created by djp on 2016/11/21.
 */
abstract class BottomDialog : AppCompatDialogFragment() {

    protected var logger = ULog.getLogger("BottomDialog")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BottomDialogTransparent)
    }

    open fun needWindowConfig(): Boolean {
        return true
    }

    private fun setWindowConfig() {
        val window = dialog?.window ?: return
        val lp = window.attributes
        val display = window.windowManager.defaultDisplay
        lp.width = display.width * 1 - DensityUtils.px2dp(20f)
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
        window.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.dialog_bottom_bottom_style)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        //每次先判断是否已经被添加
        try {
            //偶然发现崩溃,报出IllegalStateException: Fragment already added 异常，并没有catch
            //检查发现如果某一时间多次show出dialogFragment就会出现，所以在show时判断没有添加或展示时才会commit，并立即生效，避免状态没有同步问题
            val fragment = manager.findFragmentByTag(tag)
            if (!isAdded && fragment == null) {
                super.show(manager, tag)
            } else {
                logger.info("当前的已经添加 不再处理")
            }
            //立刻执行并同步状态
            manager.executePendingTransactions()
        } catch (e: Exception) {
            reportCrash("显示BottomDialog的时候报错 ", e)
        }
    }

    override fun onStart() {
        super.onStart()
        if (needWindowConfig())
            setWindowConfig()

        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
                return@OnKeyListener true
            }
            false
        })
    }
}