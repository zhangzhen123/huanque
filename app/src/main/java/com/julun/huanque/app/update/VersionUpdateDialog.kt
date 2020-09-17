package com.julun.huanque.app.update

import android.app.Activity
import android.app.ActivityManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.julun.huanque.R
import com.julun.huanque.common.base.AppBaseDialog
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.suger.logger


/**
 * 升级的的弹窗
 */
class VersionUpdateDialog : AppBaseDialog {
    override fun initData() {
    }

    override fun getLayoutId(): Int = R.layout.dialog_version_updata

    //    private var version_date: TextView? = null
    private var version_num: TextView? = null
    private var version_info: TextView? = null
    private var ok_btn: TextView? = null
    private var dialog_cancel: ImageView? = null      // 取消按钮

    private var callback: MyDialogCallback? = null
    private var force = false

    /**
     * @param context 不解释了
     * @param dissmissWhenTouchOutSide 是否点击外部的时候 dissmiss 该组件
     */
    constructor(context: Activity, dissmissWhenTouchOutSide: Boolean = false) : this(
        context,
        R.style.Alert_Dialog,
        dissmissWhenTouchOutSide
    )

    constructor(context: Activity, themeResId: Int? = R.style.Alert_Dialog, dwto: Boolean = true) : super(
        context,
        dwto,
        themeResId!!
    )

    override fun initView() {
//        version_date = window.findViewById(R.id.version_date)
        version_num = window?.findViewById(R.id.version_num)
        version_info = window?.findViewById(R.id.version_info)
        ok_btn = window?.findViewById(R.id.ok_btn)
        dialog_cancel = window?.findViewById(R.id.dialog_cancel)
        setOnCancelListener {
            this.callback?.onCancel?.invoke()
        }
    }

    // 显示
    fun showUpdateDialog(
        versionInfo: String,
        versionNum: String,
        versionDate: String,
        isForce: Boolean = false,
        callback: MyDialogCallback
    ) {
//        version_date!!.text = versionDate
        version_num!!.text = "($versionNum)"
        version_info!!.text = versionInfo
        force = isForce
        //强制升级 不现实取消键
//        if (force)
//            dialog_cancel?.visibility = View.INVISIBLE
//        else
//            dialog_cancel?.visibility = View.VISIBLE
        this.callback = callback
        show()
    }


    override fun initEvents() {
        ok_btn?.setOnClickListener(this)
        dialog_cancel?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dialog_cancel -> if (callback != null) {
                this.callback!!.onCancel()
                if (force){
                    //直接关闭app
                    ActivitiesManager.INSTANCE.finishApp()
                }
            }
            R.id.ok_btn -> if (callback != null) {
                this.callback!!.onOk()
            }
        }
        if (!force) {
            this.dismiss()
        }
    }

    class MyDialogCallback(val onCancel: () -> Unit = {}, val onOk: () -> Unit = {})

}