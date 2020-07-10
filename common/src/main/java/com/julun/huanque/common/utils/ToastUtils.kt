package com.julun.huanque.common.utils

import android.content.Context
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.julun.huanque.common.R
import com.julun.huanque.common.helper.DensityHelper.Companion.dp2px
import com.julun.huanque.common.net.NAction
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

/**
 * 自定义样式屏幕中间显示提示消息
 * Created by djp on 2016/11/18.
 *
 * @author djp
 */
object ToastUtils {
    fun init(context: Context) {
        ToastMsg.INSTANCE.init(context.applicationContext)
    }

    fun show(resId: Int) {
        ToastMsg.INSTANCE.showToast(resId, 0)
    }

    fun show(text: String?) {
        ToastMsg.INSTANCE.showToast(text, Toast.LENGTH_SHORT)
    }

    fun showLong(text: String?) {
        ToastMsg.INSTANCE.showToast(text, Toast.LENGTH_LONG)
    }

    fun show(text: String?, duration: Int) {
        ToastMsg.INSTANCE.showToast(text, duration)
    }

    fun showSuccessMessage(text: String?) {
        ToastMsg.INSTANCE.showSuccessToast(text)
    }

    fun showErrorMessage(text: String?) {
        ToastMsg.INSTANCE.showErrorToast(text)
    }

    fun showCustom(text: String?, duration: Int, gravity: Int) {
        ToastMsg.INSTANCE.showToastCustom(text, duration, gravity)
    }

    enum class ToastMsg {
        /**
         * ToastMsg对象
         */
        INSTANCE;

        private var toast: Toast? = null
        private var view: View? = null
        private var tvContent: TextView? = null
        private var customImage: ImageView? = null
        private var mContext: Context? = null
        fun init(context: Context) {
            mContext = context
            view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null)
            tvContent = view!!.findViewById<View>(R.id.toastContent) as TextView
            customImage = view!!.findViewById<View>(R.id.customImage) as ImageView
            if (toast == null) {
                toast = Toast(context)
            }
            toast!!.view = view
            toast!!.setGravity(Gravity.CENTER, 0, 0)
        }

        fun showSuccessToast(text: CharSequence?) {
            runOnMain {
                customImage?.visibility = View.VISIBLE
                customImage?.setImageResource(R.mipmap.toast_success)
                tvContent?.text = text
                toast?.setGravity(Gravity.CENTER, 0, 0)
                toast?.show()
            }

        }

        fun showErrorToast(text: CharSequence?) {
            runOnMain {
                customImage?.visibility = View.VISIBLE
                customImage?.setImageResource(R.mipmap.toast_error)
                tvContent?.text = text
                toast?.setGravity(Gravity.CENTER, 0, 0)
                toast?.show()
            }

        }

        @JvmOverloads
        fun showToast(resId: Int, duration: Int, gravity: Int = Gravity.CENTER) {
            if (resId != 0) {
                runOnMain {
                    customImage?.visibility = View.GONE
                    tvContent?.setText(resId)
                    val toast = Toast(mContext)
                    toast.view = view
                    toast.duration = duration
                    toast.setGravity(gravity, 0, 0)
                    toast.show()
                }

            }
        }

        fun showToast(text: CharSequence?, duration: Int) {
            showToastCustom(text, duration, Gravity.CENTER)
        }

        fun showToastCustom(text: CharSequence?, duration: Int, gravity: Int) {
            if (!TextUtils.isEmpty(text)) {
                runOnMain {
                    customImage!!.visibility = View.GONE
                    tvContent!!.text = text
                    val toast = Toast(mContext)
                    toast.view = view
                    toast.duration = duration
                    toast.setGravity(gravity, 0, 0)
                    toast.show()
                }

            }
        }

        private fun runOnMain(action: NAction) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                action()
            } else {
                Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
                    action()
                }.subscribe()
            }
        }
    }
}