package com.julun.huanque.common.base

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.R
import com.julun.huanque.common.commonviewmodel.OrderViewModel
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.helper.DensityHelper
import com.trello.rxlifecycle4.components.support.RxAppCompatDialogFragment
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.configuration
import org.jetbrains.anko.padding


/**
 * Created by djp on 2016/12/4.
 *
 * 修改弹窗基类 2017/9/19 by zz
 */
abstract class BaseDialogFragment : RxAppCompatDialogFragment() {


    protected var logger = ULog.getLogger(this.javaClass.name)

    //作为唯一的弹窗别名
    val TAG = this.javaClass.name

    /**
     * 排序使用的ViewModel
     */
    private var mOrderViewModel: OrderViewModel? = null

    /**
     * 从调用show方法开始 到dismiss结束
     * 用于标识是否已经开始显示该窗口 防止连续重复显示报重复添加
     */
    private var isShowing: Boolean = false

    private var isRootViewHasCreated: Boolean = false//为了复用rootview 防止重复加载试图增加的标记

    /**
     * 记录是不是用户积极手动弹出 只对order的弹窗有效
     */
    private var positiveShow: Boolean = false

    /**
     * 是不是要注册eventbus
     */
    open fun isRegisterEventBus(): Boolean {
        return false
    }

    /**
     * 该弹窗是否需要排序（-1默认不需要 设置的话必须大于0）
     */
    open fun order(): Int = -1

    /**
     * 需要全屏 默认需要
     */
    open fun needFullScreen(): Boolean {
        return true
    }

    private fun unregisterSelfAsEventHandler() {
        if (isRegisterEventBus())
            EventBus.getDefault().unregister(this)
    }

    private fun registerSelfAsEventHandler() {
        if (isRegisterEventBus())
            EventBus.getDefault().register(this)
    }

    private var rootContainer: View? = null
    open fun getRootView(inflater: LayoutInflater?, container: ViewGroup?, layoutId: Int): View {

        if (rootContainer == null) {
            isRootViewHasCreated = false
            logger.info("getRootView 开始初始化")
            rootContainer = inflater!!.inflate(layoutId, container)
            dialog?.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    dismissAllowingStateLoss()
                    return@OnKeyListener true
                }
                false
            })
        } else {
            logger.info("getRootView 已经初始化过了")
            if (null != rootContainer!!.parent) {
                (rootContainer!!.parent as ViewGroup).removeView(rootContainer)
            }
            isRootViewHasCreated = true
        }
        return rootContainer!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = getLayoutId()
        if (layoutId <= 0) {
            throw NotImplementedError("设置的layoutId 必须是真实存在的")
        }
        return getRootView(inflater, container, layoutId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isRootViewHasCreated) {
            initViews()
        } else {
            reCoverView()
        }
    }

    /**
     * 返回容器需要的layout文件
     */
    abstract fun getLayoutId(): Int

    abstract fun initViews()

    /**
     * 当已经初始化view 再次显示view时调用
     */
    open fun reCoverView() {

    }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSelfAsEventHandler()
        //初始化队列弹窗相关
        val act = activity
        //只有符合排队的窗口才会初始化mOrderViewModel
        if (order() > 0 && (!positiveShow) && act is BaseActivity) {
            mOrderViewModel = ViewModelProvider(act).get(OrderViewModel::class.java)
        }
        val orientation = activity?.configuration?.orientation
        logger.info("当前横竖屏情况：$orientation")
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BottomDialogTransparent)
        } else {
            if (needFullScreen()) {
                setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_fullScreen)
            } else {
                setStyle(DialogFragment.STYLE_NO_FRAME, R.style.BottomDialogTransparent)
            }
        }
    }

    /**
     * 配置弹窗参数 由于基本上每个弹窗都要配置宽高和位置 这里写个接口方法  还有就是控制它的执行时机在[setWindowAnimations]之前 以让默认动画生效
     */
    abstract fun configDialog()

    /**
     * 宽度显示比例
     */
    fun setDialogSize(
        gravity: Int = Gravity.BOTTOM,
        width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        padding: Int = 0
    ) {
        val window = dialog?.window ?: return
        val params = window.attributes
        window.decorView.padding = DensityHelper.dp2px(padding)
        params.gravity = gravity
        if (width > 0) {
            params.width = DensityHelper.dp2px(width)
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == width || ViewGroup.LayoutParams.MATCH_PARENT == width) {
            params.width = width
        }
        if (height > 0) {
            params.height = DensityHelper.dp2px(height)
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == height || ViewGroup.LayoutParams.MATCH_PARENT == height) {
            params.height = height
        }
        window.attributes = params
    }

    /**
     * 宽度显示比例
     */
    fun setDialogSize(
        gravity: Int = Gravity.BOTTOM,
        marginWidth: Int,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ) {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = gravity
        params.width = window.windowManager.defaultDisplay.width - DensityHelper.dp2px(marginWidth.toFloat()) * 2
        if (height > 0) {
            params.height = DensityHelper.dp2px(height.toFloat())
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == height || ViewGroup.LayoutParams.MATCH_PARENT == height) {
            params.height = height
        }
        window.attributes = params
    }

    // 滑出动画，默认从底部滑入，底部滑出
    open fun setWindowAnimations() {
        val window = dialog?.window ?: return
        val params = window.attributes

        when (params.gravity) {
            Gravity.CENTER -> {
                window.setWindowAnimations(R.style.dialog_center_open_ani)
            }
            Gravity.TOP -> {
                window.setWindowAnimations(R.style.dialog_top_top_style)
            }
            Gravity.BOTTOM -> {
                window.setWindowAnimations(R.style.dialog_bottom_bottom_style)
            }
            else -> {
                window.setWindowAnimations(R.style.dialog_bottom_bottom_style)
            }
        }

    }

    open fun needEnterAnimation(): Boolean {
        return true
    }

    //为了收集当前的生命周期状态
    var currentLife = ""

    override fun onResume() {
        currentLife = "onResume"
        super.onResume()
    }

    override fun onStart() {
        configDialog()
        if (needEnterAnimation())
            setWindowAnimations()
        super.onStart()
        currentLife = "onStart"
    }

    override fun onStop() {
        super.onStop()
        currentLife = "onStop"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentLife = "onDestroyView"
    }

    override fun onDestroy() {
//        this.cancelAllRequest()
        unregisterSelfAsEventHandler()
        super.onDestroy()
        isShowing = false
        currentLife = "onDestroy"
        logger.info("BaseDialog onDestroy")
        mOrderViewModel?.popState?.value = true
    }

    override fun onDetach() {
        super.onDetach()
        currentLife = "onDetach"
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            //每次先判断是否已经被添加
            if (isAdded || isShowing) {
                logger.info("当前的已经添加 不再处理")
//                dismiss()
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

    /**
     * 由用户自主打开的 每次打开时检测队列 关闭后不参与触发队列
     * 此方法只对需要排队order>0的有效
     */
    fun showPositive(manager: FragmentManager, tag: String?) {
        val act = activity
        if (order() > 0 && act is BaseActivity) {
            act.checkOrderDialog(this)
        }
        positiveShow = true
        show(manager, tag)
    }

    override fun dismiss() {
        //Can not perform this action after onSaveInstanceState
        // com.julun.huanque.common.base.BaseDialogFragment.dismiss(BaseDialogFragment.kt:248)
        //针对报这个错的处理
        //
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            return
        }
        super.dismissAllowingStateLoss()
        isShowing = false
    }

    override fun dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss()
        isShowing = false
    }


    /***
     * 注意 刷新操作由各自具体处理 因为分复用不复用弹窗  如果统一去刷新 对于不复用又不在显示的弹窗是一种浪费操作
     */
    open fun refreshDialog() {}
}