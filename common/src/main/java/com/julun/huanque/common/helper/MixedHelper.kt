package com.julun.huanque.common.helper

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SessionUtils
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.layout_empty_data.view.*


/**
 *@Anchor zhangzhen
 *@Date 2019/7/22 22:53
 *@Description 综合工具类
 *
 **/

object MixedHelper {

//    // 创建来MyAlertDialog，免得重复创建
//    fun showNotLoginAlert(alertDialog: NoLoginDialog, callback: () -> Unit = {}): Boolean {
//        if (SessionUtils.getIsRegUser()) {
//            callback()
//            return true
//        }
////        alertDialog.showAlertWithOKAndCancel("亲，您还没登录喔~", MyAlertDialog.MyDialogCallback(onRight = {
////            WXApiManager.doLogin(context)
////        }), "登录", "取消")
//        alertDialog.show()
//        return false
//    }

    /**
     * 检查是否登录
     */
    fun checkLogin(): Boolean {
        //未登录
        if (!SessionUtils.getIsRegUser()) {
//            ARouter.getInstance().build(ARouterConstant.GUIDE_LOGIN_ACTIVITY).navigation()
//            showLoginDialogFragment()
            return false
        }
        return true
    }

    /**
     * 检查是否登录
     * 只检查，不跳转
     */
    fun checkLoginNoJump() = SessionUtils.getIsRegUser()


    fun showAlertMessage(message: String) {
        Observable.just(message).compose(DefaultRxTransformer<String>()).subscribe {
            try {
                //不依附于主页界面
                MyAlertDialog(
                    CommonInit.getInstance().getCurrentActivity()
                        ?: return@subscribe
                ).showAlertMessage(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 通用下拉刷新headerLayoutLabel
    fun getCommonHeaderLayout(context: Context): String {
        return DateUtils.formatDateTime(
            context, System.currentTimeMillis(),
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL
        )
    }


    /**
     * 新增Adapter展示加载中
     */
    fun getLoadingView(context: Context): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_loading2, null)
    }

    /**
     * 获取网络错误页
     */
    fun getErrorView(
        ctx: Context,
        msg: String = "",
        btnTex: String = "",
        showImage: Boolean = true,
        onClick: View.OnClickListener = View.OnClickListener { },
        showBtn: Boolean = true
    ): View {

        val mErrorView: View = LayoutInflater.from(ctx).inflate(R.layout.layout_network_unable, null)
        mErrorView.setBackgroundResource(R.color.transparent)
        val image: ImageView = mErrorView.findViewById(R.id.no_network_image)
        val text: TextView = mErrorView.findViewById(R.id.no_network_Text)
        val btn: TextView = mErrorView.findViewById(R.id.tv_error_reload)

        if (!TextUtils.isEmpty(msg)) {
            text.text = msg
        } else {
            text.text = ctx.resources.getString(R.string.load_error)
        }
        if (!TextUtils.isEmpty(btnTex)) {
            btn.text = btnTex
        } else {
            btn.text = ctx.resources.getString(R.string.reload)
        }
        if (!showBtn) {
            btn.hide()
        } else {
            btn.show()
        }
        btn.setOnClickListener(onClick)
        if (showImage) {
            image.show()
        } else {
            image.hide()
        }
        return mErrorView
    }


    fun getEmptyView(
        context: Context,
        msg: String? = null,
        isImageHide: Boolean = false,
        imgResId: Int? = null,
        btnTex: String = "",
        onClick: View.OnClickListener? = null

    ): View {
        val emptyView = LayoutInflater.from(context).inflate(R.layout.layout_empty_data, null)
        if (msg != null)
            emptyView.emptyText.text = msg

        if (isImageHide) {
            emptyView.no_data_image.hide()
        } else {
            emptyView.no_data_image.show()

        }
        if (imgResId != null) {
            emptyView.no_data_image.setImageResource(imgResId)
        }
        if (!TextUtils.isEmpty(btnTex)) {
            emptyView.tv_button.text = btnTex
        } else {
            emptyView.tv_button.text = context.resources.getString(R.string.reload)
        }
        if (onClick == null) {
            emptyView.tv_button.hide()
        } else {
            emptyView.tv_button.show()
            emptyView.tv_button.setOnClickListener(onClick)
        }


        return emptyView
    }

    /**
     * 设置下拉刷新的风格
     */
    fun setSwipeRefreshStyle(swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary_lib, android.R.color.holo_green_light)
        swipeRefreshLayout.setProgressViewOffset(
            true,
            -dp2px(20),
            CommonInit.getInstance().getContext().resources.getDimensionPixelOffset(R.dimen.progress_view_end)
        )
    }


    /**
     * 程序是否在前台运行

     * @return
     */
    @Deprecated("有些手机无效 不用了")
    fun isAppOnForeground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                //前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo?.packageName == context.packageName) {
                isInBackground = false
            }
        }

        return isInBackground
    }

    /**
     * 判断当前activity是否在前台
     */
    fun isActivityForeground(context: Context, className: String): Boolean {
        if (context == null || TextUtils.isEmpty(className)) {
            return false
        }

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(1)
        if (list != null && list.size > 0) {
            val cpn = list[0].topActivity
            if (className == cpn?.className) {
                return true
            }
        }

        return false
    }

    fun getSupportABIs(): Array<String> {
        val supportedAbis: Array<String>
        if (Build.VERSION.SDK_INT >= 21) {
            supportedAbis = Build.SUPPORTED_ABIS
        } else {
            supportedAbis = arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        }

        return supportedAbis
    }


    fun getParseMap(str: String): HashMap<String, Float> {
        val map = HashMap<String, Float>()
        val str1s = str.split("?")
        if (str1s.size > 1) {
            try {
                val pairList: List<Pair<String, Float>> = str1s[1].split('&')//此时应该是 key->value 对
                    .filter { StringHelper.isNotEmpty(it) }    //过滤掉空字符串
                    .map { it.split("=") }.filter { it.size == 2 }//直接拆分成数组，并且过滤掉有空值的属性
                    .map { it[0] to it[1].toFloat() }
                map.putAll(pairList)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return map
    }


    /**
     * 获取Context对应的Activity
     */
    fun getActivity(context: Context): Activity? {
        var mContext = context
        while (mContext is ContextWrapper) {
            if (mContext is Activity) {
                return mContext
            }
            mContext = mContext.baseContext
        }
        return null
    }


    /**
     * 移除父布局
     */
    fun removeParent(view: View?) {
        view?.let {
            (it.parent as ViewGroup?)?.removeView(it)
        }
    }

    /**
     * 安全的刷新item方式 防止刷新的item不在屏幕上导致的报错 只适合LinearLayoutManager和GridLayoutManager
     */
    fun safeNotifyItem(index: Int, rv: RecyclerView, adapter: BaseQuickAdapter<*, BaseViewHolder>) {
        val mLayoutManager = rv.layoutManager
        if (mLayoutManager is LinearLayoutManager) {
            val firstVisibleItemPosition: Int = mLayoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition: Int = mLayoutManager.findLastVisibleItemPosition()
            if (index != -1) {
                val position = index + adapter.headerLayoutCount
                if (position - firstVisibleItemPosition >= 0 && position <= lastVisibleItemPosition) {
                    adapter.notifyItemChanged(position)
                }
            }

        }

    }

}