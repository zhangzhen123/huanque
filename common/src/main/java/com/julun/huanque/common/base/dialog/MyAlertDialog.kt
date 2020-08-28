package com.julun.huanque.common.base.dialog

import android.app.Activity
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import com.julun.huanque.common.R
import com.julun.huanque.common.base.AppBaseDialog
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import org.jetbrains.anko.textColor

/**
 * Created by djp on 2016/12/6.
 *
 *
 */
class MyAlertDialog : AppBaseDialog {
    override fun initData() {
    }

    override fun getLayoutId(): Int = R.layout.alert_dialog

    private var titleText: TextView? = null        //标题文本
    private var msgText: TextView? = null         // 提示文本
    private var rightText: TextView? = null       // 确定按钮
    private var fgxView: View? = null             // 分割线
    private var cancelText: TextView? = null      // 取消按钮

    private var callback: MyDialogCallback? = MyDialogCallback()

    /**
     * @param context 不解释了
     * @param dissmissWhenTouchOutSide 是否点击外部的时候 dissmiss 该组件
     */
    constructor(context: Activity, dissmissWhenTouchOutSide: Boolean = true) : this(context, R.style.Alert_Dialog, dissmissWhenTouchOutSide)

    constructor(context: Activity, themeResId: Int? = R.style.Alert_Dialog, dwto: Boolean = true) : super(context, dwto, themeResId!!)

    override fun initView() {
        ULog.getLogger("MyAlertDialog").info("MyAlertDialog intiView")
        titleText = window?.findViewById(R.id.titleText)
        fgxView = window?.findViewById(R.id.fgxView)
        msgText = window?.findViewById(R.id.msgText)
        rightText = window?.findViewById(R.id.rightText)
        cancelText = window?.findViewById(R.id.cancelText)
    }

    /**
     *
     *  所有的样式风格属性
     */

    fun showAlertCustom(
        message: String,
        callback: MyDialogCallback? = MyDialogCallback(),
        hasOk: Boolean = true,
        okText: String = "确认",
        hasNo: Boolean = true,
        noText: String = "取消",
        title: String = "提示"
        ,
        hasTitle: Boolean = false,
        cancelable: Boolean = false
    ) {
        //要不要确定按钮
        if (hasOk) {
            rightText?.show()
            rightText?.text = okText
        } else {
            rightText?.hide()
            fgxView?.hide()
        }
        //要不要取消按钮
        if (hasNo) {
            cancelText?.show()
            cancelText?.text = noText
        } else {
            cancelText?.hide()
            fgxView?.hide()
        }
        if (hasTitle) {
            titleText?.show()
            titleText?.text = title
            msgText?.text = message
        } else {
            titleText?.hide()
            msgText?.text = message
        }

        this.callback = callback
        setCancelable(cancelable)
        show()
    }

    /**
     *
     *  所有的样式风格属性 支持富文本内容
     */

    fun showAlertCustomWithSpannable(
        message: SpannableString,
        callback: MyDialogCallback? = MyDialogCallback(),
        hasOk: Boolean = true,
        okText: String = "确认",
        hasNo: Boolean = true,
        noText: String = "取消",
        title: String = "提示"
        ,
        hasTitle: Boolean = false,
        cancelable: Boolean = true
    ) {
        //要不要确定按钮
        if (hasOk) {
            rightText?.show()
            rightText?.text = okText
        } else {
            rightText?.hide()
            fgxView?.hide()
        }
        //要不要取消按钮
        if (hasNo) {
            cancelText?.show()
            cancelText?.text = noText
        } else {
            cancelText?.hide()
            fgxView?.hide()
        }
        if (hasTitle) {
            titleText?.show()
            titleText?.text = title
            msgText?.text = message
        } else {
            titleText?.hide()
            msgText?.text = message
        }

        this.callback = callback
        setCancelable(cancelable)
        show()
    }

    //有确认和取消  可取消的弹窗
    fun showAlertWithOKAndCancelAny(
        message: String,
        callback: MyDialogCallback? = MyDialogCallback(),
        title: String = "提示",
        okText: String = "确认",
        noText: String = "取消",
        cancelable: Boolean = false
    ) {
        showAlertCustom(message, callback, true, okText, true, noText, title, true, cancelable)
    }

    //有确认和取消
    fun showAlertWithOKAndCancel(
        message: String,
        callback: MyDialogCallback? = MyDialogCallback(),
        title: String = "提示",
        okText: String = "确认",
        noText: String = "取消",
        hasTitle: Boolean = true
    ) {
        showAlertCustom(message, callback, true, okText, true, noText, title, hasTitle, true)
    }

    //只有确认
    fun showAlertWithOK(message: String, callback: MyDialogCallback? = MyDialogCallback(), title: String = "提示", okText: String = "确认") {
        showAlertCustom(message, callback, true, okText, false, "", title, true, true)
    }

    //只有确认 无标题
    fun showAlertWithOKNoTitle(message: String, callback: MyDialogCallback? = MyDialogCallback(), okText: String = "确定") {
        showAlertCustom(message, callback, true, okText, false, "", "", false, true)
    }

    //不可取消的ok提醒
    fun showAlertWithOKAny(message: String, callback: MyDialogCallback? = MyDialogCallback(), title: String = "提示") {
        showAlertCustom(message, callback, true, "确定", false, "", title, true, false)
    }

    //只是弹窗提示 无callback
    fun showAlertMessage(message: String, title: String = "提示") {
        showAlertWithOK(message, null, title)
    }

    //只是弹窗提示 无callback 且没标头
    fun showAlertMessageNoTitle(message: String) {
        showAlertWithOKNoTitle(message, null)
    }

    /**
     * 设置dialog标题  控制是否显示  以及文本颜色
     * @param title 标题
     * @param color 标题颜色
     * @param showTitle true显示标题  false影藏标题
     */
    fun setTitleType(title: String, color: Int = context.resources.getColor(R.color.black_333), showTitle: Boolean): MyAlertDialog {
        if (!showTitle) {
            if (titleText!!.visibility != View.GONE) {
                titleText!!.visibility = View.GONE
            }
            return this
        }
        if (titleText!!.visibility != View.VISIBLE) {
            titleText!!.visibility = View.VISIBLE
        }
        title?.let { titleText!!.text = title }
        color?.let { titleText!!.textColor = color }
        return this
    }

    /**
     * 设置dialog文本  以及颜色
     */
    fun setMsgType(msg: String, color: Int = context.resources.getColor(R.color.black_999)): MyAlertDialog {
        msg?.let { msgText!!.text = msg }
        color?.let { msgText!!.textColor = color }
        return this
    }

    /**
     * 设置取消按钮
     */
    fun setCancelButtonType(
        content: String = "取消",
        color: Int = context.resources.getColor(R.color.black_333),
        isShow: Boolean = true
    ): MyAlertDialog {
        if (!isShow) {
            cancelText!!.visibility = View.GONE
            fgxView!!.visibility = View.GONE
            return this
        }
        cancelText!!.visibility = View.VISIBLE
        content?.let { cancelText!!.text = content }
        color?.let { cancelText!!.textColor = color }
        return this
    }

    /**
     * 设置确定按钮
     */
    fun setConfirmButtonType(
        content: String = "确定",
        color: Int = context.resources.getColor(R.color.black_333),
        isShow: Boolean = true
    ): MyAlertDialog {
        if (!isShow) {
            rightText?.visibility = View.GONE
            fgxView?.visibility = View.GONE
            return this
        }
        rightText!!.visibility = View.VISIBLE
        content?.let { rightText!!.text = content }
        color?.let { rightText!!.textColor = color }
        return this
    }

    /**
     * 设置按钮颜色
     */
    fun setButtonColor(
        leftColor: Int = context.resources.getColor(R.color.black_333),
        rightColor: Int = context.resources.getColor(R.color.black_333)
    ) {
        cancelText?.textColor = leftColor
        rightText?.textColor = rightColor
    }

    override fun initEvents() {
        cancelText!!.setOnClickListener(this)
        rightText!!.setOnClickListener(this)
        this.setOnCancelListener {
            if (this.callback != null) {
                this.callback!!.onDissmiss()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancelText -> if (callback != null) {
                this.callback!!.onCancel()
            }
            R.id.rightText -> if (callback != null) {
                this.callback!!.onRight()
            }
        }
        this.dismiss()
    }

    class MyDialogCallback(val onCancel: () -> Unit = {}, val onRight: () -> Unit = {}, val onDissmiss: () -> Unit = {})

}