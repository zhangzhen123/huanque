package com.julun.huanque.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.activity.LoginActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.events.FinishToLoginEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.LoginStatusUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.support.LoginManager
import com.julun.huanque.viewmodel.PersonalInformationProtectionViewModel
import io.rong.imlib.RongIMClient
import kotlinx.android.synthetic.main.fragment_personal_information_protection.*
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/3 10:48
 *@描述 个人信息保护指引弹窗
 */
class PersonalInformationProtectionFragment : BaseDialogFragment() {
    companion object {
        //欢迎页使用
        const val WelcomeActivity = "WelcomeActivity"

        //登录页面使用
        const val SelectActivity = "SelectActivity"

        //首页使用
        const val MainActivity = "MainActivity"

        const val From = "From"
        fun newInstance(type: String): PersonalInformationProtectionFragment {
            val fragment = PersonalInformationProtectionFragment()
            val bundle = Bundle()
            bundle.putString(From, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    //欢迎页显示
    private var mType: String = ""

    private val mViewModel: PersonalInformationProtectionViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_personal_information_protection

    override fun initViews() {
        mType = arguments?.getString(From, "") ?: ""
        initListener()
        val style = SpannableStringBuilder()
        //设置文字
        val str = GlobalUtils.getString(R.string.agreement_content)
        val replaceStr = GlobalUtils.getString(R.string.app_name)
        val content = String.format(str, replaceStr, replaceStr)
        style.append(content)
        updateTextColorAndClick(
            style,
            content,
            GlobalUtils.getString(R.string.agreement_conent_clickable),
            Agreement.UserAgreement
        )
        updateTextColorAndClick(
            style,
            content,
            GlobalUtils.getString(R.string.agreement_register_conent_clickable),
            Agreement.PrivacyAgreement
        )
        tv_content.text = style
        //配置给TextView
        tv_content.movementMethod = LinkMovementMethod.getInstance()
        tv_content.highlightColor = GlobalUtils.getColor(android.R.color.transparent)
        tv_content.text = style
        initViewModel()
    }

    override fun reCoverView() {
        initViewModel()
    }
    private fun initListener() {
        tv_agree.onClickNew {
            if (mType == WelcomeActivity) {
                mViewModel.agreeClickState.value = true
                SharedPreferencesUtils.commitBoolean(SPParamKey.Welcome_privacy_Fragment, true)
                dismiss()
            } else {
                mViewModel.agree(XYCode.YHYSXY)
            }

        }
        tv_exit.onClickNew {
            when (mType) {
                WelcomeActivity -> {
                    //欢迎页
                    mViewModel.cancelClickState.value = true
                }
                SelectActivity -> {
                    //选择性别页面
                    EventBus.getDefault().post(FinishToLoginEvent())
                }
                MainActivity -> {
                    //首页使用
                    SessionUtils.clearSession()
                    //登出融云
                    if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                        RongCloudManager.logout()
                    }
                    UserHeartManager.stopBeat()
                    LoginStatusUtils.logout()
                    //跳转登录页面
                    ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
                }
            }
            dismiss()
        }
    }


    private fun initViewModel() {
        mViewModel.agreeState.observe(this, Observer {
            if (it == true) {
                //已经同意协议
                mViewModel.agreeState.value = null
                dismiss()
            }
        })
    }

    override fun needEnterAnimation() = false
    private fun setWindowConfig() {
        this.setDialogSize(Gravity.CENTER, 35)
//        //不需要半透明遮罩层
////        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        if (mType != WelcomeActivity) {
            val win = dialog?.window ?: return
            win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        }
    }

    //    override fun setWindowAnimations() {
//        dialog?.window?.setWindowAnimations(com.julun.huanque.core.R.style.dialog_bottom_enter_style)
//    }
    override fun onStart() {
        super.onStart()
        setWindowConfig()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }


    /**
     * 修改文案颜色并添加点击跳转
     * @param sb
     * @param content 全部文案内容
     * @param condition 识别的问题
     * @param address 跳转地址
     */
    private fun updateTextColorAndClick(
        sb: SpannableStringBuilder,
        content: String,
        condition: String,
        address: String
    ) {
        if (content.contains(condition)) {
            //设置部分文字点击事件
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    //点击事件
                    WebActivity.startWeb(requireActivity(), address)
//                    val extra = Bundle()
//                    extra.putString(BusiConstant.PUSH_URL, LMUtils.getDomainName(address))
//                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                    ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY)
//                        .with(extra).navigation()
                    ///rules/protocolUserPrivacy.html
                }
            }
            val startIndex = content.indexOf(condition)

            sb.setSpan(
                clickableSpan,
                startIndex,
                startIndex + condition.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )


            //设置部分文字颜色
            val foregroundColorSpan =
                ForegroundColorSpan(GlobalUtils.getColor(R.color.agreement_blue))
            sb.setSpan(
                foregroundColorSpan,
                startIndex,
                startIndex + condition.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
    }
    override fun order() = 1
}