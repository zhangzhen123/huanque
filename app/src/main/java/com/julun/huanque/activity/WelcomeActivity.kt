package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fm.openinstall.OpenInstall
import com.fm.openinstall.listener.AppWakeUpAdapter
import com.fm.openinstall.model.AppData
import com.julun.huanque.R
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.bean.beans.OpenInstallParamsBean
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.ChannelCodeHelper
import com.julun.huanque.common.interfaces.LocalPreLoginListener
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.fragment.PersonalInformationProtectionFragment
import com.julun.huanque.manager.FastLoginManager
import com.julun.huanque.viewmodel.PersonalInformationProtectionViewModel
import com.julun.huanque.viewmodel.WelcomeViewModel
import com.julun.platform_push.receiver.RPushUtil
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/6/30 13:56
 *@描述 欢迎页
 */
class WelcomeActivity : BaseActivity() {
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

    private var viewModel: WelcomeViewModel? = null
    private var mPersonalInformationProtectionViewModel: PersonalInformationProtectionViewModel? =
        null

    override fun getLayoutId() = R.layout.act_welcome

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //开始预取号
        FastLoginManager.mPreListener = mLocalPreLoginListener
        FastLoginManager.preLogin()

        mShowFragment =
            SharedPreferencesUtils.getBoolean(SPParamKey.Welcome_privacy_Fragment, false)
        initViewModel()
        if (!SPUtils.getBoolean(SPParamKey.APP_START, false)) {
            //调用激活接口
            viewModel?.appStart()
        }
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
        SharedPreferencesUtils.commitLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
        //移除缓存的私信气泡数据
        SPUtils.remove(SPParamKey.PRIVATE_CHAT_BUBBLE)
//        VoiceManager.startRing(false)
        getWakeUp(intent)
        getPushClickData()
        if (mShowFragment) {
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
                checkPermissions()
            }
        })
        mPersonalInformationProtectionViewModel?.cancelClickState?.observe(this, Observer {
            if (it == true) {
                finish()
            }
        })
        doOpenInstall(mOpenInstallBean ?: return)
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
                viewModel?.initUUID()
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
        val intent = if (registerUser && SessionUtils.getRegComplete()) {
            RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
            UserHeartManager.startOnline()
            //登录成功并且数据已经填写完成
            Intent(this, MainActivity::class.java)
        } else {
            if (!mPreLoginSuccess) {
                //需要登录，预取号未成功，直接返回
                return
            }
            if (registerUser) {
                SessionUtils.clearSession()
            }
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finishFlag = true
//        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeUpAdapter = null
        FastLoginManager.mPreListener = null
    }

    override fun onPause() {
        super.onPause()
        if (finishFlag) {
            finish()
        }
    }
}