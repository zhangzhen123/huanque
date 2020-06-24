package com.julun.huanque.common.base

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.IntentParamKey
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.interfaces.routerservice.AppCommonService
import com.julun.huanque.common.manager.OrderDialogManager
import com.julun.huanque.common.suger.hideDialogs
import com.julun.huanque.common.utils.permission.data.PermissionRequest
import com.julun.huanque.common.utils.permission.data.PermissionsHelper
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import org.jetbrains.anko.contentView


/**
 * Created by nirack on 16-10-20.
 */
abstract class BaseActivity : RxAppCompatActivity(), BaseContainer {
    private val PACKAGE_URL_SCHEME = "package:" // 方案


    private var activityDestroyed: Boolean = false
    private var activityForeground: Boolean = false
    private var goHome: Boolean = false //整合所有界面的返回操作 重写onBackPressed()方法
    protected val logger = ULog.getLogger(this.javaClass.name)!!
    protected val TAG: String by lazy { this.javaClass.name }
    private var mHideTime = 0L

    private lateinit var mOrderDialogManager: OrderDialogManager

    //未成年校验间隔，2分钟
    private val YOUNGER_CHECK_INTERVAL = 2 * 60 * 1000

    //用户归因时间间隔 1个小时
    private val USER_FROM_INTERVAL = 60 * 60 * 1000

    //12个小时的间隔
    private val USER_FROM_FINDNEWS = 12 * 60 * 60 * 1000


    override fun onCreate(savedInstanceState: Bundle?) {
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
        }
        registerSelfAsEventHandler()
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

    /**
     * 是否需要校验未成年人
     * 默认是每个页面都需要验证。一些特殊页面需要关闭（eg：WelcomeActivity）
     */
    open fun needCheckYounger() = true

    /**
     * 是否需要校验隐私，默认不需要
     * 返回null 表示不需要调用接口
     * 返回true 表示 未登录的情况下调用接口
     * 返回false 表示 登录的情况下调用接口
     */
    open fun needCheckPrivacyNoLogin(): Boolean? = null

    override fun onResume() {
        activityForeground = true
        (ARouter.getInstance().build(ARouterConstant.APP_COMMON_SERVICE)
            .navigation() as? AppCommonService)?.umengCallBack(true, this)
        super.onResume()
//        if (needCheckYounger() && SessionUtils.getIsRegUser()) {
//            checkYounger()
//        }
        //将隐藏时间重置为0
        mHideTime = 0
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

    /**
     * 关闭时是否回首页的判断统一在此执行 相应页面的统一调用该方法
     */
    override fun onBackPressed() {
        if (goHome) {
            //如果没有特殊标记不要返回首页,则返回首页
            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
        }
        finish()
    }

    fun isThisActivityDestroyed(): Boolean = this.activityDestroyed

    //标记activity是否活动状态
    fun isThisActivityForeground(): Boolean = this.activityForeground

    /**
     * 启动应用的设置
     */
    fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse(PACKAGE_URL_SCHEME + packageName)
        startActivity(intent)
    }

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
        if (!appIsForeGround()) {
            //app置于后台，记录当前时间
            mHideTime = System.currentTimeMillis()
        }
    }

    /**
     * 是否可以杀掉进程
     * 微信，QQ，Alipay 有任意一个处于使用中  则不能杀掉进程
     */
//    fun isShouldKillProcess() = !(SessionUtils.getWeiXinUse() || SessionUtils.getQQUse() || SessionUtils.getAlipayUse())

    //程序是否处于前台
    fun appIsForeGround(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val packageName = applicationContext.packageName

        val appProcesses = activityManager?.runningAppProcesses ?: return false

        for (appProcess in appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName == packageName && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }

        return false
    }

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

    /**
     * 分发数据
     * @param data 分发出来的排序数据
     */
//    open fun dispatchOrderData(data: Any) {
//
//    }

//    override fun finish() {
//        if (!donNotGoHome) {
//            //如果没有特殊标记不要返回首页,则返回首页
//            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).navigation()
//        }
//        super.finish()
//    }
}
