package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.Agreement
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.manager.FloatingManager
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
    //一键登录是否可用
    private var verifyEnable: Boolean = false

    //是否处于测试模式。用于控制Toast的显示
    private var mTest = false

    //预取号成功标识
    private var mPreviewSuccess = false

    //登录成功标识
    private val CODE_LOGIN_SUCCESS = 6000

    private var mViewModel: LoginViewModel? = null


    override fun getLayoutId() = R.layout.act_login

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //切换账号的时候移除气泡缓存
        SPUtils.commitObject(SPParamKey.PRIVATE_CHAT_BUBBLE, ChatBubble())
        initViewModel()
        initFastLogin()
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        mViewModel?.loginData?.observe(this, Observer {
            if (it != null) {
                if (it.regComplete) {
                    //跳转首页
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    FillInformationActivity.newInstance(this)
                }
            }
        })
        mViewModel?.loginStatus?.observe(this, Observer {
            if (it == true) {
                //登录成功
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        FloatingManager.hideFloatingView()
    }

    override fun initEvents(rootView: View) {
        view_phone_number_fast_login.onClickNew {
//            ToastUtils.show("mPreviewSuccess = $mPreviewSuccess")
            if (mPreviewSuccess) {
                //预取号成功，跳转一键登录页面
                loginAuth()
            } else {
                //预取号未成功，跳转手机号登录页面
                val intent = Intent(this, PhoneNumLoginActivity::class.java)
                startActivity(intent)
            }
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
    }

    /**
     * 初始化一键登录相关
     */
    private fun initFastLogin() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //没有对应权限，直接退出
            return
        }
        logger.info("JPush success = ${JVerificationInterface.isInitSuccess()} time = ${System.currentTimeMillis()}")
        //判断是否初始化成功
        if (!JVerificationInterface.isInitSuccess()) {
            //初始化未成功，直接返回
            return
        }

        //判断环境是否可用
        verifyEnable = JVerificationInterface.checkVerifyEnable(this)
        if (!verifyEnable) {
            //环境不可用，直接返回
            logger.info("当前网络环境不支持认证")
            return
        }

        //预取号
        JVerificationInterface.preLogin(this, 5000) { code, content ->
            //预取号结果
            mPreviewSuccess = code == 7000
            logger.info("onResult [$code] message=$content")
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
                    Toast.makeText(this@LoginActivity, "Step 4 设置回调  cmd=$cmd,msg=$msg", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@LoginActivity, "Step 5 授权页面回调 operator=$operator,code=$code", Toast.LENGTH_SHORT).show()
            }
            runOnUiThread {
                when (code) {
                    CODE_LOGIN_SUCCESS -> {
                        mViewModel?.fastLogin(token)
                    }
                    else -> {
                        JVerificationInterface.dismissLoginAuthActivity()
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

        val uiConfigBuilder = JVerifyUIConfig.Builder().setDialogTheme(
            DensityHelper.px2dp(widthPx.toFloat()),
            DensityHelper.px2dp(heightPx.toFloat()), 0, 0, true
        )
        //设置logo
        uiConfigBuilder.setLogoHeight(161)
        uiConfigBuilder.setLogoWidth(198)
        uiConfigBuilder.setLogoImgPath("bg_logo_login")
        uiConfigBuilder.setLogoHidden(false)
        uiConfigBuilder.setLogoOffsetY(110)
        //设置手机号码
        uiConfigBuilder.setNumberFieldOffsetBottomY(197)
            .setNumberColor(GlobalUtils.getColor(R.color.black_333))
            .setNumberSize(16)
        //设置slogan
        uiConfigBuilder.setSloganBottomOffsetY(15)
        uiConfigBuilder.setSloganTextColor(Color.WHITE)

        //设置动画
        uiConfigBuilder.setNeedStartAnim(true)
        uiConfigBuilder.setNeedCloseAnim(true)


        //设置登录按钮
        val phoneNumWidthPx = widthPx - DensityHelper.dp2px(38) * 2
        val viewWidth = DensityHelper.px2dp(phoneNumWidthPx.toFloat())
        uiConfigBuilder.setLogBtnText(GlobalUtils.getString(R.string.owner_phone_number_fast_login))
        uiConfigBuilder.setLogBtnTextColor(GlobalUtils.getColor(R.color.black_333))
        uiConfigBuilder.setLogBtnImgPath("bg_phone_number_fast_login")
        uiConfigBuilder.setLogBtnHeight(50)
        uiConfigBuilder.setLogBtnWidth(viewWidth)
        uiConfigBuilder.setLogBtnTextSize(16)
        uiConfigBuilder.setLogBtnBottomOffsetY(127)

        //设置隐私协议相关
        uiConfigBuilder.setAppPrivacyOne(GlobalUtils.getString(R.string.register_rule_02), Agreement.UserAgreement)
        uiConfigBuilder.setAppPrivacyTwo(GlobalUtils.getString(R.string.register_rule_pravicy), Agreement.PrivacyAgreement)

        uiConfigBuilder.setPrivacyState(true)
        uiConfigBuilder.setAppPrivacyColor(GlobalUtils.getColor(R.color.black_999), GlobalUtils.getColor(R.color.black_333))
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


        // 自定义View   其它手机号登录
        val layoutParamPhoneLogin = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DensityHelper.dp2px(18))
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParamPhoneLogin.setMargins(0, 0, 0, DensityHelper.dp2px(93))
        val tvPhoneLogin = TextView(this)
        tvPhoneLogin.text = GlobalUtils.getString(R.string.others_phone_login)
        tvPhoneLogin.layoutParams = layoutParamPhoneLogin
        tvPhoneLogin.textColor = GlobalUtils.getColor(R.color.black_999)
        tvPhoneLogin.textSize = 12f
        tvPhoneLogin.gravity = Gravity.CENTER
        uiConfigBuilder.addCustomView(tvPhoneLogin, true) { _, _ ->
            startActivity(Intent(this, PhoneNumLoginActivity::class.java))
        }


        // 关闭按钮
        val closeButton = ImageView(this)

        val mLayoutParams1 = RelativeLayout.LayoutParams(DensityHelper.dp2px(40), DensityHelper.dp2px(40))
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        mLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        closeButton.setPadding(dip(8))
//        mLayoutParams1.setMargins(DensityHelper.dp2px(10), DensityHelper.dp2px(10), 0, 0)
        closeButton.layoutParams = mLayoutParams1
        closeButton.setImageResource(R.mipmap.icon_back_black_01)
        uiConfigBuilder.addCustomView(closeButton, true, null)

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

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
//            //注册成功，跳转首页
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        } else {
//            SessionUtils.clearSession()
//        }
//        finish()
//    }

    override fun finish() {
        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
            //登录成功，直接finish
        } else {
            //未登录成功，需要退出APP
            SessionUtils.clearSession()
            ActivitiesManager.finishApp()
        }
        super.finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWeiXinCode(event: WeiXinCodeEvent) {
        logger.info("收到微信登录code:${event.code}")
        mViewModel?.weiXinLogin(event.code)

    }

}