package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cn.jiguang.verifysdk.api.AuthPageEventListener
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.jiguang.verifysdk.api.JVerifyUIConfig
import cn.jiguang.verifysdk.api.LoginSettings
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.bean.events.FastLoginEvent
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.helper.ChannelCodeHelper
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.fragment.FastDialogFragment
import com.julun.huanque.manager.FastLoginManager
import com.julun.huanque.support.WXApiManager
import com.julun.huanque.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.act_login.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/7/3 10:04
 *@描述 登录页面
 */
@Route(path = ARouterConstant.LOGIN_ACTIVITY)
class LoginActivity : BaseActivity() {
    //是否处于测试模式。用于控制Toast的显示
    private var mTest = false


    //登录成功标识
    private val CODE_LOGIN_SUCCESS = 6000


    private var mViewModel: LoginViewModel? = null

    private var clickCount = 0
    private var lastClickTime = 0L
    private var resume = false

    //预取号加载中状态
    private val mFastDialogFragment = FastDialogFragment()

    override fun getLayoutId() = R.layout.act_login

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //切换账号的时候移除气泡缓存
        SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
        initViewModel()
//        initFastLogin()
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
//        mViewModel?.loginData?.observe(this, Observer {
//            if (it != null) {
//                if (it.regComplete) {
//                    //跳转首页
//                    startActivity(Intent(this, MainActivity::class.java))
//                } else {
//                    FillInformationActivity.newInstance(this)
//                }
//            }
//        })
//        mViewModel?.loginStatus?.observe(this, Observer {
//            if (it == true) {
//                //登录成功
//                finish()
//            }
//        })
    }

    override fun onResume() {
        super.onResume()
        FloatingManager.hideFloatingView()
        HuanViewModelManager.huanQueViewModel.clearFateData()

        if (!resume) {
            resume = true
            val enableFast = JVerificationInterface.checkVerifyEnable(this)
            if (enableFast && FastLoginManager.getPreviewCode() == FastLoginManager.CODE_PRELOGIN_SUCCESS) {
                con.hide()
                loginAuth()
            } else {
                con.show()
                showOrHidePhoneLogin(enableFast)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (con.visibility == View.GONE) {
            finish()
        }
    }

    override fun initEvents(rootView: View) {
        con.onClickNew {
            if (BuildConfig.DEBUG) {
                //切换环境
                val intent = Intent(this, EnvironmentConfigurationActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    startActivity(intent)
                }
            } else {
                //
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 1000) {
                    clickCount++
                }
                if (clickCount > 8) {
                    ToastUtils.show(ChannelCodeHelper.getInnerChannel())
                    clickCount = 0
                }
                lastClickTime = currentTime
            }
        }
        view_phone_number_fast_login.onClickNew {
//            ToastUtils.show("mPreviewSuccess = $mPreviewSuccess")
            mFastDialogFragment.dismiss()
            when (FastLoginManager.getPreviewCode()) {
                FastLoginManager.CODE_PRELOGIN_SUCCESS -> {
                    //预取号成功，跳转一键登录页面
                    loginAuth()
                }
                7002 -> {
                    //预取号中，显示弹窗
                    mFastDialogFragment.show(supportFragmentManager, "FastDialogFragment")
                }
                else -> {
                    //预取号失败
                    val intent = Intent(this, PhoneNumLoginActivity::class.java)
                    startActivity(intent)
                }
            }

//            if (mPreviewCode == CODE_PRELOGIN_SUCCESS) {
//                //预取号成功，跳转一键登录页面
//                loginAuth()
//            } else {
//                //预取号未成功，跳转手机号登录页面
//                val intent = Intent(this, PhoneNumLoginActivity::class.java)
//                startActivity(intent)
//            }
        }
        view_weixin.onClickNew {
            WXApiManager.doLogin(this)
        }
        register_rule.onClickNew {
            WebActivity.startWeb(this, Agreement.UserAgreement)
        }
        tv_register_privacy.onClickNew {
            WebActivity.startWeb(this, Agreement.PrivacyAgreement)
        }
        iv_other_phone.onClickNew {
            startActivity(Intent(this, PhoneNumLoginActivity::class.java))
        }
    }

    /**
     * 本地号码登录View 的显示与隐藏
     */
    private fun showOrHidePhoneLogin(show: Boolean) {
        if (show) {
            view_phone_number_fast_login.show()
            tv_phone.show()
        } else {
            view_phone_number_fast_login.hide()
            tv_phone.hide()
        }
    }

    private fun doWithSession(session: Session) {
        val act = CommonInit.getInstance().getCurrentActivity() ?: return
        if (session.regComplete) {
            //跳转首页
            startActivity(Intent(act, MainActivity::class.java))
            finishFastLogin()
            finish()
        } else {
            SelectSexActivity.newInstance(act)
        }
    }


    /**
     * 开始登录（一键登录）
     */
    private fun loginAuth() {
        val settings = LoginSettings()
        settings.isAutoFinish = false;//设置登录完成后是否自动关闭授权页
        settings.timeout = 15 * 1000;//设置超时时间，单位毫秒。 合法范围（0，30000],范围以外默认设置为10000
        //type = 1,授权页被关闭;
        // type=2,授权页面被拉起；
        // type=3,运营商协议被点击；
        // type=4,自定义协议1被点击；
        // type=5,自定义协议2被点击；
        // type=6,checkBox变为选中；
        // type=7,checkBox变为未选中；
        // type=8,登录按钮被点击
        settings.authPageEventListener = object : AuthPageEventListener() {
            override fun onEvent(cmd: Int, msg: String?) {
                if (mTest) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Step 4 设置回调  cmd=$cmd,msg=$msg",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                when (cmd) {
                    1 -> {
                        //授权页面被关闭

                    }
                    else -> {
                        if (cmd == 8) {
                            //一键登录
                        }
                    }
                }
            }
        };//设置授权页事件监听
        JVerificationInterface.setCustomUIWithConfig(getDialogPortraitConfig())
        JVerificationInterface.loginAuth(this, settings) { code, token, operator ->
            val errorMsg = "operator=$operator,code=$code\ncontent=$token"
            if (mTest) {
                Toast.makeText(
                    this@LoginActivity,
                    "Step 5 授权页面回调 operator=$operator,code=$code",
                    Toast.LENGTH_SHORT
                ).show()
            }
            logger.info("Login errorMsg = $errorMsg")
            runOnUiThread {
                when (code) {
                    CODE_LOGIN_SUCCESS -> {
                        mViewModel?.fastLogin(token)
                    }
                    else -> {
//                        JVerificationInterface.dismissLoginAuthActivity()
//                        if (!isGuideLoginActivity) {
//                            EventBus.getDefault().post(LoginFragmentDismissEvent())
//                        }
                    }
                }
            }
        }
    }

    private fun getDialogPortraitConfig(): JVerifyUIConfig {
        val widthPx = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())
        val heightPx = Math.max(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())

        val uiConfigBuilder = JVerifyUIConfig.Builder()
//            .setDialogTheme(
//            DensityHelper.px2dp(widthPx.toFloat()),
//            DensityHelper.px2dp(heightPx.toFloat()), 0, 0, true
//        )
        //设置logo
        uiConfigBuilder.setLogoWidth(206)
        uiConfigBuilder.setLogoHeight(144)
        uiConfigBuilder.setLogoImgPath("bg_logo_login")
        uiConfigBuilder.setLogoHidden(false)
        uiConfigBuilder.setLogoOffsetY(110)
//        uiConfigBuilder.setNavTransparent(true)
        uiConfigBuilder.setStatusBarTransparent(true)
        uiConfigBuilder.setStatusBarDarkMode(true)
        uiConfigBuilder.setNavHidden(true)
        //设置手机号码
        uiConfigBuilder.setNumberFieldOffsetBottomY(151)
            .setNumberColor(GlobalUtils.getColor(R.color.black_333))
            .setNumberSize(16)
        //设置slogan
        uiConfigBuilder.setSloganBottomOffsetY(15)
        uiConfigBuilder.setSloganTextColor(Color.WHITE)

        //设置动画
        uiConfigBuilder.setNeedStartAnim(true)
        uiConfigBuilder.setNeedCloseAnim(false)


        //设置登录按钮
        val phoneNumWidthPx = widthPx - DensityHelper.dp2px(38) * 2
        val viewWidth = DensityHelper.px2dp(phoneNumWidthPx.toFloat())
//        uiConfigBuilder.setLogBtnText(GlobalUtils.getString(R.string.owner_phone_number_fast_login))
        uiConfigBuilder.setLogBtnText("")
        uiConfigBuilder.setLogBtnTextColor(GlobalUtils.getColor(R.color.black_333))
        uiConfigBuilder.setLogBtnImgPath("bg_fast_login")
        uiConfigBuilder.setLogBtnHeight(50)
        uiConfigBuilder.setLogBtnWidth(viewWidth)
        uiConfigBuilder.setLogBtnTextSize(16)
        uiConfigBuilder.setLogBtnBottomOffsetY(151)

        //设置隐私协议相关
        uiConfigBuilder.setAppPrivacyOne(
            GlobalUtils.getString(R.string.register_rule_02),
            Agreement.UserAgreement
        )
        uiConfigBuilder.setAppPrivacyTwo(
            GlobalUtils.getString(R.string.register_rule_pravicy),
            Agreement.PrivacyAgreement
        )

        uiConfigBuilder.setPrivacyState(true)
        uiConfigBuilder.setAppPrivacyColor(
            GlobalUtils.getColor(R.color.black_999),
            GlobalUtils.getColor(R.color.black_666)
        )
        uiConfigBuilder.setPrivacyText(
            "同意", "、", "和"
            , "并授权获取本机号码"
        )
        uiConfigBuilder.setPrivacyCheckboxHidden(true)
        uiConfigBuilder.setPrivacyTextCenterGravity(true)
        uiConfigBuilder.setPrivacyWithBookTitleMark(true)
        uiConfigBuilder.setPrivacyTextWidth(viewWidth)
        //        uiConfigBuilder.setPrivacyOffsetX(52-15);
        uiConfigBuilder.setPrivacyTextSize(10)
        uiConfigBuilder.setPrivacyOffsetY(15)

        //线
        val lineParamPhoneLogin = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lineParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        lineParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        lineParamPhoneLogin.setMargins(0, 0, 0, DensityHelper.dp2px(115))
        val line = LayoutInflater.from(this).inflate(R.layout.view_login_bottom, null)
        line.layoutParams = lineParamPhoneLogin
        uiConfigBuilder.addCustomView(line, false) { _, _ ->
        }

        // 自定义View   其它手机号登录
        val layoutParamPhoneLogin = RelativeLayout.LayoutParams(
            DensityHelper.dp2px(36),
            DensityHelper.dp2px(36)
        )
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParamPhoneLogin.setMargins(0, 0, 0, DensityHelper.dp2px(64))
        val ivPhoneLogin = ImageView(this)
        ivPhoneLogin.layoutParams = layoutParamPhoneLogin
        ivPhoneLogin.setImageResource(R.mipmap.icon_phone_other)
        uiConfigBuilder.addCustomView(ivPhoneLogin, false) { _, _ ->
            startActivity(Intent(this, PhoneNumLoginActivity::class.java))
        }

        //微信登录
        val view_WX = LayoutInflater.from(this).inflate(R.layout.view_weixin, null)
        val tempWidth = ScreenUtils.getScreenWidth() - dp2px(38) * 2
        val layoutParamWXLogin = RelativeLayout.LayoutParams(tempWidth, dp2px(50))
        layoutParamWXLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamWXLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParamWXLogin.setMargins(0, 0, 0, DensityHelper.dp2px(221))
        view_WX.layoutParams = layoutParamWXLogin
        uiConfigBuilder.addCustomView(view_WX, false) { _, _ ->
            view_weixin.performClick()
        }


        // 关闭按钮
        val closeButton = ImageView(this)

        val mLayoutParams1 =
            RelativeLayout.LayoutParams(DensityHelper.dp2px(40), DensityHelper.dp2px(40))
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        closeButton.setPadding(dip(8))
//        mLayoutParams1.setMargins(DensityHelper.dp2px(10), DensityHelper.dp2px(10), 0, 0)
        closeButton.layoutParams = mLayoutParams1
        closeButton.setImageResource(R.mipmap.icon_back_black_01)
        //关闭按钮不显示
//        uiConfigBuilder.addCustomView(closeButton, true, null)

        //设置web导航栏
        uiConfigBuilder.setPrivacyNavColor(Color.WHITE)
        uiConfigBuilder.setPrivacyNavTitleTextColor(GlobalUtils.getColor(R.color.black_333))
        uiConfigBuilder.setPrivacyNavTitleTextSize(18)

//        val closeView = ImageView(this)
//        val mLayoutParams2 = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        mLayoutParams2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
//        mLayoutParams2.setMargins(DensityHelper.dp2px(10), 0, 0, 0)
//        closeView.setImageResource(R.mipmap.nav_back)
//        closeView.layoutParams = mLayoutParams2
//        uiConfigBuilder.setPrivacyNavReturnBtn(closeView)

        return uiConfigBuilder.build()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra(ParamConstant.TYPE) == "EXIT") {
            //退出APP
            ActivitiesManager.INSTANCE.finishApp()
        }
    }

    override fun finish() {
        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
            //登录成功，直接finish
        } else {
            //未登录成功，需要退出APP
            SessionUtils.clearSession()
            ActivitiesManager.INSTANCE.finishApp()
        }
        super.finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWeiXinCode(event: WeiXinCodeEvent) {
        logger.info("收到微信登录code:${event.code}")
        mViewModel?.weiXinLogin(event.code)

    }

    /**
     * 登录获取到Sesion
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun loginData(data: Session) {
        doWithSession(data)
    }

    /**
     * 关闭一键登录页面
     */
    private fun finishFastLogin() {
        val fastContentView = CommonInit.getInstance().getFastLoginContentView()
        if (fastContentView != null) {
            //隐藏所有的TextView
            hideAllTextView(fastContentView)
        }
        JVerificationInterface.dismissLoginAuthActivity()
    }

    /**
     * 隐藏所有的TextView（保留ImageView）
     */
    private fun hideAllTextView(view: View) {
        if (view is ViewGroup) {
            val childCount = view.childCount
            (0 until childCount).forEach {
                val tempView = view.getChildAt(it)
                if (tempView is View) {
                    tempView.hide()
                } else {
                    hideAllTextView(tempView)
                }
            }
        }

    }

}