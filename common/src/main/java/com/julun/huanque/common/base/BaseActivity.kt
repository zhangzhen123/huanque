package com.julun.huanque.common.base

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.bean.beans.FateQuickMatchBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.OrderDialogManager
import com.julun.huanque.common.suger.hideDialogs
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.permission.data.PermissionRequest
import com.julun.huanque.common.utils.permission.data.PermissionsHelper
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import org.jetbrains.anko.contentView


/**
 *@Anchor zhangzhen
 *@Date 2020/6/27 12:22
 *@Description activity基类封装
 *
 **/

abstract class BaseActivity : RxAppCompatActivity(), BaseContainer {

    private var activityDestroyed: Boolean = false
    private var activityForeground: Boolean = false
    protected var goHome: Boolean = false //整合所有界面的返回操作 重写onBackPressed()方法
    protected val logger = ULog.getLogger(this.javaClass.name)
    protected val mHuanQueViewModel = HuanViewModelManager.huanQueViewModel
    private var mFragment: DialogFragment? = null

    //不需要显示派单弹窗的页面列表
    private val mNoFateActivityList = mutableListOf<String>("com.julun.huanque.agora.activity.VoiceChatActivity")

    private lateinit var mOrderDialogManager: OrderDialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activityDestroyed = false
        super.onCreate(savedInstanceState)
        mOrderDialogManager = OrderDialogManager(this)
        val layoutId = getLayoutId()
        if (layoutId > 0) {
            goHome = intent.getBooleanExtra(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
            this.setContentView(layoutId)
//            val start: Long = System.currentTimeMillis()
            setHeader()//添加对二级页面使用pager_header时对api19的侵入式适配
            initViews(contentView!!, savedInstanceState)//此时rootView不应该为空
            initEvents(contentView!!)//此时rootView不应该为空
//            val end: Long = System.currentTimeMillis()
//            println("初始化 activity <${this.javaClass.name}> 耗时: ${end - start}")
        } else {
            throw NotImplementedError("activity没有设置有效的layout")
        }
        registerSelfAsEventHandler()
        var canShowFate = true
        mNoFateActivityList.forEach {
            if (it.contains(this.localClassName)) {
                canShowFate = false
                return@forEach
            }
        }
        if (canShowFate) {
            initHuanQueViewModel()
        }
    }

    /**
     * 初始化全部ViewModel
     */
    private fun initHuanQueViewModel() {
        mHuanQueViewModel.fateQuickMatchData.observe(this, Observer<FateQuickMatchBean> { it ->
            logger.info("FateQuick Activity接收到数据 ${it}")
            if (it != null) {
                showPaidanFragment()
            }
        })
    }

    /**
     * 显示派单Fragment
     */
    private fun showPaidanFragment() {
        mFragment?.dismiss()
        mFragment = ARouter.getInstance().build(ARouterConstant.FATE_QUICK_MATCH_FRAGMENT).navigation() as? BaseDialogFragment
        mFragment?.show(supportFragmentManager, "PaidanFragment")
    }

    open fun setHeader() {
        //暂时不要
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
//            StatusBarUtil.setTransparent(this)
//            header_container?.setPadding(0, StatusBarUtil.getStatusBarHeight(this),0,0)
        //6.0以上的状态栏一体化   //不再需要 因为已经在style 中配置好了
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.white))
//        }
    }

    override fun onResume() {
        activityForeground = true
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.umengCallBack(true, this)
        super.onResume()
    }


    override fun onPause() {
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.umengCallBack(false, this)
        super.onPause()
    }

    override fun onDestroy() {
//        huanqueApp.ACTIVITIES_STACK.remove(UUID)
        hideDialogs()
        unregisterSelfAsEventHandler()
        onViewDestroy()
        activityDestroyed = true
        super.onDestroy()
    }

    fun isThisActivityDestroyed(): Boolean = this.activityDestroyed

    //标记activity是否活动状态
    fun isThisActivityForeground(): Boolean = this.activityForeground

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        PermissionsHelper.result(requestCode, permissions, grantResults)
    }

    protected fun requestPermission(request: PermissionRequest) {
        ActivityCompat.requestPermissions(this, request.permissionsRequest, request.requestCode)
    }

    // QQ分享回调  //增加qq登陆回调 2017/8/24
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.activityResultCallBack(
            requestCode,
            resultCode,
            data
        )
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        activityForeground = false
    }

    /**
     * 是否可以杀掉进程
     * 微信，QQ，Alipay 有任意一个处于使用中  则不能杀掉进程
     */
//    fun isShouldKillProcess() = !(SessionUtils.getWeiXinUse() || SessionUtils.getQQUse() || SessionUtils.getAlipayUse())

    /**
     * 添加顺序Dialog
     */
    fun addOrderDialog(dialogFragment: BaseDialogFragment?) {
        if (dialogFragment != null) {
            mOrderDialogManager.addOrderDialogBean(dialogFragment)
        }
    }

    /**
     * 对于既要排队显示又能用户主动打开的弹窗 用户打开时移除队列的弹窗
     */
    fun checkOrderDialog(dialogFragment: DialogFragment?) {
        mOrderDialogManager.checkAndRemoveOrderDialog(dialogFragment ?: return)
    }

    override fun finish() {
        if (goHome) {
            //如果有特殊标记返回首页,则返回首页
            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
        }
        super.finish()
    }
}
