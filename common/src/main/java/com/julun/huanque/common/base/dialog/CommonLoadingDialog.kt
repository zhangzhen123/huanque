package com.julun.huanque.common.base.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.R
import com.julun.huanque.common.helper.reportCrash

/**
 * Created by dong on 2018/5/10.
 */
class CommonLoadingDialog : AppCompatDialogFragment() {

    private var logger = ULog.getLogger("CommonLoadingDialog")

    private var isShowing: Boolean = false

    companion object {
        const val TYPE = "TYPE"
        fun newInstance(loadingText: String): CommonLoadingDialog {
            val fragment = CommonLoadingDialog()
            val bundle = Bundle()
            bundle.putString(TYPE, loadingText)
            fragment.arguments = bundle
            return fragment
        }
    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LoadingDialogTransparent)
//        lifecycle.addObserver(GenericLifecycleObserver { _, event ->
//            when (event) {
//                Lifecycle.Event.ON_START -> {
//                    val text = arguments?.getString(TYPE)
//                    if (text?.isNotEmpty() == true) {
//                        tv_loading_text.show()
//                        tv_loading_text.text = text
//                    } else {
//                        tv_loading_text.hide()
//                    }
//                }
//            }
//        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_loading_view, container, false)
    }
    override fun show(manager: FragmentManager, tag: String?) {
        try {
            //每次先判断是否已经被添加
            if (isAdded || isShowing) {
                logger.info("当前的已经添加 不再处理")
            } else {
                if (!manager.isStateSaved) {
                    isShowing = true
                    super.show(manager, tag)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportCrash("显示所有的dialogFragment的时候报错 ", e)
        }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
        isShowing = false
    }

    override fun onStart() {
        super.onStart()
        setWindowConfig()

        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun setWindowConfig() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = Gravity.CENTER
//        params.width = DensityHelper.dp2px(80f)
//        params.height =DensityHelper.dp2px(60f)
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window.attributes = params
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.setCanceledOnTouchOutside(false)
    }

}