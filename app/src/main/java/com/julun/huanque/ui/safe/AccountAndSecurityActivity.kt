package com.julun.huanque.ui.safe

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.UserSecurityInfo
import com.julun.huanque.common.bean.events.AliAuthCodeEvent
import com.julun.huanque.common.bean.events.BindPhoneSuccessEvent
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.interfaces.routerservice.LoginAndShareService
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.manager.AliPayManager
import com.julun.huanque.core.viewmodel.UserBindViewModel
import kotlinx.android.synthetic.main.act_account_and_security.*
import kotlinx.android.synthetic.main.act_setting.*
import kotlinx.android.synthetic.main.act_setting.header_view
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor


/**
 *@创建者   dong
 *@创建时间 2020/8/12 14:50
 *@描述 账号与安全
 */
class AccountAndSecurityActivity : BaseVMActivity<AccountAndSecurityViewModel>() {


    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE)
            .navigation() as? LoginAndShareService
    }

    private val userBindViewModel: UserBindViewModel by viewModels()

    private var mSecurityInfo: UserSecurityInfo? = null

    override fun getLayoutId() = R.layout.act_account_and_security
    override fun isRegisterEventBus(): Boolean {
        return true
    }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "账号与安全"
        initViewModel()
        mViewModel.queryInfo()
    }

    private fun initViewModel() {
        mViewModel.securityInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.getT())
            }
        })
        userBindViewModel.bindWinXinData.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                ToastUtils.show("绑定微信成功")
                mViewModel.queryInfo(QueryType.REFRESH)
            } else if (it.state == NetStateType.ERROR) {
                logger.info("绑定微信报错")
                if (it.error?.busiCode == ErrorCodes.HAS_BIND_OTHER) {
                    ToastUtils.show("该微信号已绑定其他欢鹊账号，请更换其他微信号")
                } else {
                    ToastUtils.show("${it.error?.busiMessage}")
                }
            }
        })
        userBindViewModel.aliPayAuth.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                logger.info("获取后台支付宝授权信息成功 开始发起授权")
                AliPayManager.aliAuth(this, data.authInfo)
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("${it.error?.busiMessage}")
            }
        })

        userBindViewModel.bindAliData.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                ToastUtils.show("绑定支付宝成功")
                mViewModel.queryInfo(QueryType.REFRESH)
            } else if (it.state == NetStateType.ERROR) {
                logger.info("绑定支付宝报错")
                if (it.error?.busiCode == ErrorCodes.HAS_BIND_OTHER) {
                    ToastUtils.show("该支付宝账号已绑定其他欢鹊账号，请更换其他支付宝账号")
                } else {
                    ToastUtils.show("${it.error?.busiMessage}")
                }

            }
        })

    }

    private fun renderData(securityInfo: UserSecurityInfo) {
        mSecurityInfo = securityInfo
        tv_security_level.text = if (securityInfo.security) "安全等级：高" else "安全等级：低"
        if (securityInfo.mobile.isNotEmpty()) {
            tv_phone_binding_type.text = securityInfo.mobile
            tv_phone_binding_type.textColor = Color.parseColor("#333333")
        } else {
            tv_phone_binding_type.text = "未绑定"
            tv_phone_binding_type.textColor = Color.parseColor("#FF5757")
        }

        if (securityInfo.bindWeChat) {
            tv_wx_binding.text = "已绑定"
            tv_wx_binding.textColor = Color.parseColor("#11D713")
        } else {
            tv_wx_binding.text = "未绑定"
            tv_wx_binding.textColor = Color.parseColor("#FF5757")
        }

        if (securityInfo.bindAliPay) {
            tv_ali_binding.text = "已绑定"
            tv_ali_binding.textColor = Color.parseColor("#11D713")
        } else {
            tv_ali_binding.text = "未绑定"
            tv_ali_binding.textColor = Color.parseColor("#FF5757")
        }


    }

    override fun initEvents(rootView: View) {
        header_view.imageViewBack.onClickNew {
            finish()
        }
        view_phone_binding.onClickNew {
            if (mSecurityInfo?.mobile.isNullOrEmpty()) {
                ARouter.getInstance().build(ARouterConstant.PHONE_NUM_LOGIN_ACTIVITY).with(Bundle().apply {
                    putInt(IntentParamKey.TYPE.name, PhoneLoginType.TYPE_BIND)
                }).navigation()
            } else {

            }
        }
        view_wx_binding.onClickNew {
            if (mSecurityInfo?.bindWeChat == true) {

            } else {
                wxService?.weiXinAuth(this)
            }
        }

        view_ali_binding.onClickNew {
            if (mSecurityInfo?.bindAliPay == true) {

            } else {
                userBindViewModel.getAliPayAuthInfo()
            }
        }
        view_log_off.onClickNew {
            startActivityForResult(Intent(this,DestroyAccountActivity::class.java),BusiConstant.DESTROY_ACCOUNT_RESULT_CODE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == BusiConstant.DESTROY_ACCOUNT_RESULT_CODE) {
            logger.info("注销返回码")
            finish()
            return
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveWeiXinCode(event: WeiXinCodeEvent) {
        logger.info("收到微信授权code:${event.code}")
        userBindViewModel.bindWinXin(event.code)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveAliCode(event: AliAuthCodeEvent) {
        logger.info("收到支付宝授权code:${event.code}")
        userBindViewModel.bindAliPay(event.code)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveBindPhone(event: BindPhoneSuccessEvent) {
        logger.info("收到手机绑定成功")
        mViewModel.queryInfo(QueryType.REFRESH)
    }

    override fun showLoadState(state: NetState) {

    }

}