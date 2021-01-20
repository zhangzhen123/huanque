package com.julun.huanque.common.base.dialog

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.CommonDialogInfo
import com.julun.huanque.common.suger.*
import kotlinx.android.synthetic.main.common_alert_dialog.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/1/7 10:43
 *
 *@Description: CommonDialogFragment
 *
 */
class CommonDialogFragment() : BaseDialogFragment() {
    companion object {
        const val COMMON_INFO = "common_info"


        /**
         * 首次创建
         */
        fun newInstance(info: CommonDialogInfo, callback: Callback? = null): CommonDialogFragment {
            val args = Bundle()
            args.putSerializable(COMMON_INFO, info)
            val fragment = CommonDialogFragment()
            fragment.arguments = args
            fragment.callback = callback
            return fragment
        }

        /**
         * 对于复用的弹窗 这里可以传入老的弹窗以复用
         */
        fun create(
            dialog: CommonDialogFragment? = null,
            info: CommonDialogInfo,
            callback: Callback? = null
        ): CommonDialogFragment {
            return if (dialog == null) {
                newInstance(info).apply {
                    this.callback = callback
                }
            } else {
                dialog.setInfo(info)
                dialog.callback = callback
                dialog
            }
        }

        /**
         * 创建
         */
        fun create(
            dialog: CommonDialogFragment? = null,
            title: String? = null,
            content: String? = null,
            image: String? = null,
            imageRes: Int? = null,
            okText: String? = null,
            cancelText: String? = null,
            cancelable: Boolean = true,
            callback: Callback? = null
        ): CommonDialogFragment {
            return create(dialog, CommonDialogInfo(title, content, image, imageRes, okText, cancelText, cancelable), callback)
        }
    }

    fun show(activity: Activity) {
        if(activity is FragmentActivity)
        super.show(activity.supportFragmentManager,null)
    }
    override fun getLayoutId(): Int {
        return R.layout.common_alert_dialog
    }

    private var info: CommonDialogInfo? = null
    var callback: Callback? = null
    fun setInfo(info: CommonDialogInfo) {
        arguments?.putSerializable(COMMON_INFO, info)
    }


    override fun configDialog() {
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 20)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        info = arguments?.getSerializable(COMMON_INFO) as? CommonDialogInfo
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cInfo = info ?: return
        if (cInfo.image.isNullOrEmpty() && cInfo.imageRes == null) {
            sdv_head.hide()
        } else {
            sdv_head.show()
            if (cInfo.imageRes != null) {
                sdv_head.loadImageLocal(cInfo.imageRes!!)
            } else if (!cInfo.image.isNullOrEmpty()) {
                sdv_head.loadImage(cInfo.image!!, 300f, 200f)
            }
        }
        if (!cInfo.title.isNullOrEmpty()) {
            titleText.text = cInfo.title
        }
        if (!cInfo.content.isNullOrEmpty()) {
            msgText.text = cInfo.content
        }
        if (cInfo.okText.isNullOrEmpty()) {
            okText.hide()
        } else {
            okText.show()
            okText.text = cInfo.okText
            okText.onClickNew {
                callback?.onOk?.invoke()
                dismiss()
            }
        }
        if (cInfo.cancelText.isNullOrEmpty()) {
            cancelText.hide()
        } else {
            cancelText.show()
            cancelText.text = cInfo.cancelText
            cancelText.onClickNew {
                callback?.onCancel?.invoke()
                dismiss()
            }

        }
        isCancelable = cInfo.cancelable
    }

    override fun initViews() {
    }


    override fun dismiss() {
        super.dismiss()
        callback?.onDismiss?.invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class Callback(val onCancel: () -> Unit = {}, val onOk: () -> Unit = {}, val onDismiss: () -> Unit = {})
}