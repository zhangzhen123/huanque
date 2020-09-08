package com.julun.huanque.common.base

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.StyleRes
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.holdDialog
import com.julun.huanque.common.suger.logger
import java.lang.ref.WeakReference

/**
 * Created by nirack on 17-5-25.
 */

abstract class AppBaseDialog(context: Activity, dissmiss: Boolean, @StyleRes themeResId: Int) : Dialog(context, themeResId), View.OnClickListener {
    private val activity: WeakReference<Activity>

    private var dissmissWhenTouchOutSide = true//点击外部隐藏组件

    @JvmOverloads constructor(context: BaseActivity, dissmissWhenTouchOutSide: Boolean = true) : this(context, dissmissWhenTouchOutSide, 0) {}

    init {
        activity = WeakReference(context)
        this.dissmissWhenTouchOutSide = dissmiss
        window!!.setContentView(getLayoutId())
        initView()
        setCancelable(dissmissWhenTouchOutSide)
        setCanceledOnTouchOutside(this.dissmissWhenTouchOutSide)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initEvents()
    }

    /**
     * 处理事件
     */
    protected abstract fun initEvents()

    /**
     * 获取自定义的 layout
     * @return
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化 视图
     */
    protected abstract fun initView()

    /**
     * 初始化视图
     */
    protected  abstract  fun initData()
    override fun show() {
        val baseActivity = activity.get()
        if (baseActivity == null) {
            reportCrash("弹出框的context 不是 Activity $context ${Exception().stackTrace.map { it.toString() }}")
            return
        }
        baseActivity.holdDialog(this)
        if (!baseActivity.isFinishing) {
            super.show()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}
