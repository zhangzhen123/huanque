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
import com.julun.huanque.common.suger.logger
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
        ToastMsg.INSTANCE.showToast(resId, Toast.LENGTH_SHORT)
    }

    fun show(text: String?) {
        ToastMsg.INSTANCE.showToast(text, Toast.LENGTH_SHORT)
    }

    /**
     * 这个toast对于重复的内容在显示期间进行过滤
     */
    fun show2(text: String?) {
        ToastMsg.INSTANCE.showToastCustomNew(text, Toast.LENGTH_SHORT, Gravity.CENTER)
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
                val text = mContext?.resources?.getText(resId) ?: return
                showToastCustom(text, duration, gravity)
//                runOnMain {
//                    customImage?.visibility = View.GONE
//                    tvContent?.setText(resId)
//                    val toast = Toast(mContext)
//                    toast.view = view
//                    toast.duration = duration
//                    toast.setGravity(gravity, 0, 0)
//                    toast.show()
//                }

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

        /**
         * 这个toast对于重复的内容在显示期间进行过滤 至少显示1秒
         */
        private var currentText: CharSequence? = null
        fun showToastCustomNew(text: CharSequence?, duration: Int, gravity: Int) {
            if (!TextUtils.isEmpty(text)) {
                if(currentText==text){
                    logger("当前新text与正在显示的text相同不再重复显示")
                    return
                }
                currentText = text
                runOnMain {
                    customImage!!.visibility = View.GONE
                    tvContent!!.text = text
                    val toast = Toast(mContext)
                    toast.view = view
                    toast.duration = duration
                    toast.setGravity(gravity, 0, 0)
                    toast.show()
                    //这里根据类型获取显示持续时间 实际持续时间是4000 7000 减去1秒是为了保证后续不重复显示的内容至少显示1秒
                    val time = when (duration) {
                        Toast.LENGTH_SHORT -> {
                            3000L
                        }
                        else -> {
                            6000L
                        }
                    }
                    view?.postDelayed({
                        currentText=null
                    }, time)
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