package com.julun.huanque.activity

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import cn.jiguang.verifysdk.api.AuthPageEventListener
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.jiguang.verifysdk.api.JVerifyUIConfig
import cn.jiguang.verifysdk.api.LoginSettings
import com.alibaba.android.arouter.facade.annotation.Route
import com.fm.openinstall.OpenInstall
import com.fm.openinstall.listener.AppWakeUpAdapter
import com.fm.openinstall.model.AppData
import com.julun.huanque.R
import com.julun.huanque.adapter.LoginPicAdapter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.LoginPicBean
import com.julun.huanque.common.bean.beans.OpenInstallParamsBean
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ChannelCodeHelper
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.interfaces.LocalPreLoginListener
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.fragment.LoginFragment
import com.julun.huanque.fragment.PersonalInformationProtectionFragment
import com.julun.huanque.manager.FastLoginManager
import com.julun.huanque.support.LoginManager
import com.julun.huanque.support.WXApiManager
import com.julun.huanque.viewmodel.LoginViewModel
import com.julun.huanque.viewmodel.PersonalInformationProtectionViewModel
import com.julun.huanque.viewmodel.PhoneNumLoginViewModel
import com.julun.huanque.viewmodel.WelcomeViewModel
import com.julun.platform_push.receiver.RPushUtil
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.act_welcome.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/6/30 13:56
 *@描述 欢迎页
 */
@Route(path = ARouterConstant.Welcome_Activity)
class WelcomeActivity : BaseActivity() {
    private val mLoginViewModel: LoginViewModel by viewModels()

    //是否同意过隐私弹窗
    private var mShowFragment = false

    //openinstall返回的bean
    private var mOpenInstallBean: OpenInstallParamsBean? = null

    //预取号结束的标识
    private var mPreLoginSuccess = false

    //逻辑执行完成标识
    private var mLogicSuccess = false

    //页面需要关闭的标记位
    private var finishFlag = false

    //登录成功标识
    private val CODE_LOGIN_SUCCESS = 6000

//    private val mLoginFragment = LoginFragment()

    //是否显示过登录弹窗
    private var mShowedLoginView = false

    //是否跳转登录页面
    private var loginActivity = false

    //是否是首次进入
    private var first = true

    private var mLocalPreLoginListener = object : LocalPreLoginListener {
        override fun preLoginResult(success: Boolean) {
            mPreLoginSuccess = true

            if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
                //处于登录状态
                return
            }
            startActivity()
        }
    }

    //是否处于测试模式。用于控制Toast的显示
    private var mTest = false

    //自动滚动背景图片Adapter
    private var mAdapter = LoginPicAdapter()

    private var viewModel: WelcomeViewModel? = null
    private var mPersonalInformationProtectionViewModel: PersonalInformationProtectionViewModel? =
        null

    override fun isRegisterEventBus() = true

    override fun getLayoutId() = R.layout.act_welcome

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        ActivitiesManager.INSTANCE.finishActivityExcept("com.julun.huanque.activity.WelcomeActivity")
        mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Phone
        view_white.alpha = 1f
        HuanViewModelManager.huanQueViewModel.clearFateData()
        initRecyclerView()
        mShowFragment =
            SharedPreferencesUtils.getBoolean(SPParamKey.Welcome_privacy_Fragment, false)
        initViewModel()
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
        SharedPreferencesUtils.commitBoolean(SPParamKey.ANONYMOUS_VOICE_ON_LINE, false)
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
        //移除缓存的私信气泡数据
        SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
//        VoiceManager.startRing(false)
        getWakeUp(intent)
        getPushClickData()
        if (mShowFragment) {
            //开始预取号
            FastLoginManager.mPreListener = mLocalPreLoginListener
            FastLoginManager.preLogin()
            checkPermissions()
        } else {
            val mPersonalInformationProtectionFragment =
                PersonalInformationProtectionFragment.newInstance(
                    PersonalInformationProtectionFragment.WelcomeActivity
                )
            mPersonalInformationProtectionFragment.show(
                supportFragmentManager,
                "PersonalInformationProtectionFragment"
            )
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.needTouchStop = false
        recycler_view.noTouchAble = true
        val tagPic = mutableListOf<LoginPicBean>().apply {
            add(LoginPicBean(R.mipmap.tag_1, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_2, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_3, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_4, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_5, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_6, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_7, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_8, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_9, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_10, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_11, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_12, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_13, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_14, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_15, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_16, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_17, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_18, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_19, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_20, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_21, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_22, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_23, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_24, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_25, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_26, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_27, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_28, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_29, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_30, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_31, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_32, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_33, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_34, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_35, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_36, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_37, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_38, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_39, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_40, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_41, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_42, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_43, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_44, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_45, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_46, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_47, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_48, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_49, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_50, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_51, 17, "杭州", "乐网"))
            add(LoginPicBean(R.mipmap.tag_52, 18, "上海", "勒沃"))
            add(LoginPicBean(R.mipmap.tag_53, 19, "苏州", "推图"))
            add(LoginPicBean(R.mipmap.tag_54, 20, "无锡", "离领"))
            add(LoginPicBean(R.mipmap.tag_55, 21, "常州", "匹配"))
            add(LoginPicBean(R.mipmap.tag_56, 22, "南京", "看v"))
            add(LoginPicBean(R.mipmap.tag_57, 23, "天津", "啃维"))
            add(LoginPicBean(R.mipmap.tag_58, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_59, 24, "北京", "一环"))
            add(LoginPicBean(R.mipmap.tag_60, 24, "北京", "一环"))
        }
        val showList = mutableListOf<LoginPicBean>()
        while (tagPic.isNotEmpty()) {
            val tempData = tagPic.random()
            tagPic.remove(tempData)
            showList.add(tempData)
        }
        recycler_view.adapter = mAdapter.apply { setList(showList) }
        recycler_view.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        recycler_view.startScroll()
    }

    private fun getPushClickData() {
        val pushData = intent.extras?.getString("appData")
        if (pushData != null) {
            Observable.timer(500, TimeUnit.MILLISECONDS).subscribe {
                RPushUtil.parseJson(pushData, this.applicationContext)
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(WelcomeViewModel::class.java)
        mPersonalInformationProtectionViewModel =
            ViewModelProvider(this).get(PersonalInformationProtectionViewModel::class.java)
        mPersonalInformationProtectionViewModel?.agreeClickState?.observe(this, Observer {
            if (it == true) {
                //开始预取号
                FastLoginManager.mPreListener = mLocalPreLoginListener
                FastLoginManager.preLogin()
                checkPermissions()
            }
        })
        mPersonalInformationProtectionViewModel?.cancelClickState?.observe(this, Observer {
            if (it == true) {
                finish()
            }
        })
        mLoginViewModel.weixinLoginFlag.observe(this, Observer {
            if (it == true) {
                //点击了微信登录
                WXApiManager.doLogin(this)
            }
        })
        mLoginViewModel.loginData.observe(this, Observer {
            if (it != null) {
                dismissLoginDialog()
            }
        })
        doOpenInstall(mOpenInstallBean ?: return)
    }

    /**
     * 跳转首页
     */
    private fun dismissLoginDialog() {
//        if (mLoginFragment.isResumed) {
//            mLoginFragment.dismiss()
//        }
        JVerificationInterface.dismissLoginAuthActivity()
    }


    /**
     * OpenInstall解析之后的操作
     */
    private fun doOpenInstall(bean: OpenInstallParamsBean) {
        //用户归因调用
//        viewModel?.channel = bean.h5ChannelCode
//        viewModel?.userOpenInstall(bean.h5SID, bean.h5PID, bean.h5ChannelCode)
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
            .subscribe { permission ->
                //申请存储权限，无论成功还是失败，直接跳转
                logger.info(
                    "appStart 是否调用过接口${SPUtils.getBoolean(
                        SPParamKey.APP_START,
                        false
                    )}，是否支持：${SharedPreferencesUtils.getBoolean(SPParamKey.Support_Oaid, false)}"
                )
                viewModel?.initUUID()

                if (!SPUtils.getBoolean(
                        SPParamKey.APP_START,
                        false
                    ) && (SharedPreferencesUtils.getBoolean(
                        SPParamKey.Oaid_Created,
                        false
                    ) || !SharedPreferencesUtils.getBoolean(SPParamKey.Support_Oaid, false))
                ) {
                    //调用激活接口  oaid已经获取成功或者不支持oaid无需等待接口返回
                    viewModel?.appStart()
                }
                mLogicSuccess = true
                startActivity()
            }
    }


    //获取唤醒参数
    private fun getWakeUp(intent: Intent?) {
        if (intent != null && intent.data?.host != null)
            OpenInstall.getWakeUp(intent, wakeUpAdapter)
    }

    /**
     * 唤醒参数获取回调
     * 如果在没有数据时有特殊的需求，可将AppWakeUpAdapter替换成AppWakeUpListener
     *
     * @param appData
     */
    private var wakeUpAdapter: AppWakeUpAdapter? = object : AppWakeUpAdapter() {
        override fun onWakeUp(appData: AppData?) {
            //获取渠道数据
            val channelCode = appData?.getChannel() ?: ""
            if (channelCode.isNotEmpty()) {
                ChannelCodeHelper.setChannelCode(channelCode)
            }
            //获取绑定数据
            val bindData = appData?.getData()
            ChannelCodeHelper.saveWakeParams(bindData ?: "")
            logger.info("获取wakeUpAdapter数据成功：$channelCode 额外参数$bindData")
//            val bean = ChannelCodeHelper.getWeakUpData(appData ?: return)
//            if (viewModel != null) {
//                //viewModel不为空，直接上传
//                doOpenInstall(bean ?: return)
//            } else {
//                //viewModel为空，保存数据
//                mOpenInstallBean = bean
//            }
        }
    }


    /**
     * 跳转页面
     */
    private fun startActivity() {
        if (!mLogicSuccess) {
            return
        }
        val registerUser = SessionUtils.getIsRegUser()
        if (registerUser && SessionUtils.getRegComplete()) {
            RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
//            UserHeartManager.startOnline()
            //登录成功并且数据已经填写完成
            if (SessionUtils.getWishComplete() == BusiConstant.False) {
                val intent = Intent(this, SelectTagActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finishFlag = true
        } else {
            if (!mPreLoginSuccess) {
                //需要登录，预取号未成功，直接返回
                return
            }
            if (registerUser) {
                SessionUtils.clearSession()
            }
            startAlphaAnimation()
//            Intent(this, LoginActivity::class.java)
        }


//        finish()
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
                        this@WelcomeActivity,
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
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            JVerificationInterface.loginAuth(this, settings) { code, token, operator ->
                val errorMsg = "operator=$operator,code=$code\ncontent=$token"
                if (mTest) {
                    Toast.makeText(
                        this,
                        "Step 5 授权页面回调 operator=$operator,code=$code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                logger.info("Login errorMsg = $errorMsg")
                runOnUiThread {
                    when (code) {
                        CODE_LOGIN_SUCCESS -> {
                            mLoginViewModel?.fastLogin(token)
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
    }

    private fun getDialogPortraitConfig(): JVerifyUIConfig {
        val widthPx = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth())
        val heightPx = dp2px(496)

        val uiConfigBuilder = JVerifyUIConfig.Builder()
            .setDialogTheme(
                DensityHelper.px2dp(widthPx.toFloat()),
                DensityHelper.px2dp(heightPx.toFloat()), 0, 0, true
            )
        //设置logo
        uiConfigBuilder.setLogoWidth(208)
        uiConfigBuilder.setLogoHeight(44)
        uiConfigBuilder.setLogoImgPath("content_login")
        uiConfigBuilder.setLogoHidden(false)
//        uiConfigBuilder.setLogoOffsetY(110)
        uiConfigBuilder.setLogoOffsetBottomY(230)
//        uiConfigBuilder.setNavTransparent(true)
        uiConfigBuilder.setStatusBarTransparent(true)
        uiConfigBuilder.setStatusBarDarkMode(true)
        uiConfigBuilder.setNavHidden(true)

        //设置手机号码
        uiConfigBuilder.setNumberFieldOffsetBottomY(160)
            .setNumberColor(GlobalUtils.getColor(R.color.white))
            .setNumberSize(16)
        //设置slogan
        uiConfigBuilder.setSloganBottomOffsetY(15)
        uiConfigBuilder.setSloganTextColor(Color.WHITE)

        //设置动画
        uiConfigBuilder.setNeedStartAnim(true)
        uiConfigBuilder.setNeedCloseAnim(false)


        //设置登录按钮
        val phoneNumWidthPx = widthPx - DensityHelper.dp2px(30) * 2
        val viewWidth = DensityHelper.px2dp(phoneNumWidthPx.toFloat())
//        uiConfigBuilder.setLogBtnText(GlobalUtils.getString(R.string.owner_phone_number_fast_login))
        uiConfigBuilder.setLogBtnText("")
        uiConfigBuilder.setLogBtnTextColor(GlobalUtils.getColor(R.color.black_333))
        uiConfigBuilder.setLogBtnImgPath("bg_fast_login")
        uiConfigBuilder.setLogBtnHeight(50)
        uiConfigBuilder.setLogBtnWidth(viewWidth)
        uiConfigBuilder.setLogBtnTextSize(16)
        uiConfigBuilder.setLogBtnBottomOffsetY(160)

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
            "未注册手机登录自动注册。同意", "、", "和"
            , "并授权获取本机号码"
        )
        uiConfigBuilder.setPrivacyCheckboxHidden(true)
        uiConfigBuilder.setPrivacyTextCenterGravity(true)
        uiConfigBuilder.setPrivacyWithBookTitleMark(true)
        uiConfigBuilder.setPrivacyTextWidth(viewWidth)
        uiConfigBuilder.setPrivacyOffsetX(30);
        uiConfigBuilder.setPrivacyTextSize(10)
        uiConfigBuilder.setPrivacyOffsetY(10)

//        //线
//        val lineParamPhoneLogin = RelativeLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        lineParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
//        lineParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
//        lineParamPhoneLogin.setMargins(0, 0, 0, DensityHelper.dp2px(115))
//        val line = LayoutInflater.from(this).inflate(R.layout.view_login_bottom, null)
//        line.layoutParams = lineParamPhoneLogin
//        uiConfigBuilder.addCustomView(line, false) { _, _ ->
//        }
        //顶部半透明区域
        val layoutParamTop = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp2px(180)
        )
        layoutParamTop.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamTop.setMargins(0, 0, 0, DensityHelper.dp2px(275))
        val topView = View(this).apply {
            backgroundResource = R.drawable.bg_login_transparent
        }
        topView.layoutParams = layoutParamTop
        uiConfigBuilder.addCustomView(topView, false) { _, _ ->
            //todo手机号登录
//            startActivity(Intent(this, PhoneNumLoginActivity::class.java))
        }


        // 自定义View   其它手机号登录
        val layoutParamPhoneLogin = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParamPhoneLogin.setMargins(0, 0, 0, DensityHelper.dp2px(130))
        val tvPhoneLogin = TextView(this)
        tvPhoneLogin.layoutParams = layoutParamPhoneLogin
        tvPhoneLogin.text = "其他手机号码登录"
        tvPhoneLogin.textColor = GlobalUtils.getColor(R.color.primary_color)
        tvPhoneLogin.textSize = 14f
        uiConfigBuilder.addCustomView(tvPhoneLogin, false) { _, _ ->
            //todo手机号登录
//            startActivity(Intent(this, PhoneNumLoginActivity::class.java))
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
//                mLoginViewModel.currentFragmentState.value = LoginViewModel.Fragment_State_Phone
//                mLoginFragment.show(supportFragmentManager, "LoginFragment")
                val intent = Intent(this, LoginActivity2::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    loginActivity = true
                    startActivity(intent)
                }
            }
        }

        val lastLogin = SharedPreferencesUtils.getInt(SPParamKey.Last_Login, -1)

        //微信登录
        val view_WX = LayoutInflater.from(this).inflate(R.layout.view_weixin, null)
        val tempWidth = dp2px(60)
        val layoutParamWXLogin =
            RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tempWidth)
        layoutParamWXLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamWXLogin.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParamWXLogin.setMargins(0, 0, 0, DensityHelper.dp2px(60))
        view_WX.layoutParams = layoutParamWXLogin
        val iv_last_login_wx = view_WX.findViewById<View>(R.id.iv_last_login_wx)
        if (lastLogin == LoginManager.WECHAT_LOGIN) {
            iv_last_login_wx.show()
        } else {
            iv_last_login_wx.hide()
        }
        uiConfigBuilder.addCustomView(view_WX, false) { _, _ ->
            mLoginViewModel.weixinLoginFlag.postValue(true)
//            view_weixin.performClick()
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

        //手机号上次登录视图
        val phoneLastLoginView = ImageView(this)

        val mPhoneLastLogin =
            RelativeLayout.LayoutParams(DensityHelper.dp2px(40), DensityHelper.dp2px(17))
        mPhoneLastLogin.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        mPhoneLastLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        mPhoneLastLogin.setMargins(0, 0, dp2px(75), DensityHelper.dp2px(200))
//        mLayoutParams1.setMargins(DensityHelper.dp2px(10), DensityHelper.dp2px(10), 0, 0)
        phoneLastLoginView.layoutParams = mPhoneLastLogin
        phoneLastLoginView.setImageResource(R.mipmap.icon_last_login)
        if (lastLogin == LoginManager.MOBILE_FAST_LOGIN) {
            uiConfigBuilder.addCustomView(phoneLastLoginView, false, null)
        }

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

    override fun onResume() {
        super.onResume()
        loginActivity = false
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeUpAdapter = null
        mAlphaAnimation?.cancel()
        FastLoginManager.mPreListener = null
    }

    override fun onPause() {
        super.onPause()
        if (finishFlag) {
            finish()
        }
    }

    //透明动画
    private var mAlphaAnimation: ObjectAnimator? = null

    /**
     * 播放透明动画
     */
    private fun startAlphaAnimation() {
        mAlphaAnimation?.cancel()
        mAlphaAnimation = mAlphaAnimation ?: ObjectAnimator.ofFloat(view_white, "alpha", 1f, 0f)
            .apply { duration = 500 }

        mAlphaAnimation?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                //唤醒一键登录弹窗
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val enableFast = JVerificationInterface.checkVerifyEnable(this@WelcomeActivity)
                    if (enableFast && FastLoginManager.getPreviewCode() == FastLoginManager.CODE_PRELOGIN_SUCCESS) {
                        loginAuth()
                    } else {
//                        mLoginViewModel.mShowLoginFragment = true
//                        mLoginFragment.show(supportFragmentManager, "LoginFragment")
                        val intent = Intent(this@WelcomeActivity, LoginActivity2::class.java)
                        if (ForceUtils.activityMatch(intent)) {
                            loginActivity = true
                            startActivity(intent)
                        }
                    }
                    mShowedLoginView = true
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
        mAlphaAnimation?.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWeiXinCode(event: WeiXinCodeEvent) {
        if(loginActivity){
            return
        }
        logger.info("收到微信登录code:${event.code}")
        mLoginViewModel.weiXinLogin(event.code)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (mShowedLoginView && hasFocus && !first) {
            val sessionId = SessionUtils.getSessionId()
            if (sessionId.isNotEmpty()) {
                if (SessionUtils.getRegComplete()) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    SelectSexActivity.newInstance(this)
                }
            }
            ScreenUtils.hideSoftInput(this)
            finish()
        }
        first = false
//        logger.info("2 hasFocus = $hasFocus,first = $first,mShowLoginFragment = $mShowLoginFragment")
    }
}