package com.julun.huanque.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.Agreement
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.DataCleanManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.activity.MessageSettingActivity
import com.julun.huanque.support.LoginManager
import com.julun.huanque.ui.safe.AccountAndSecurityActivity
import kotlinx.android.synthetic.main.act_setting.*

/**
 *@创建者   dong
 *@创建时间 2020/7/18 10:33
 *@描述 设置页面
 */
class SettingActivity : BaseActivity() {

    companion object {
        fun newInstance(act: Activity, royalLevel: Int) {
            val intent = Intent(act, SettingActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.RoyalLevel, royalLevel)
                act.startActivity(intent)
            }
        }
    }

    override fun getLayoutId() = R.layout.act_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "设置"
        val royalLevel = intent?.getIntExtra(ParamConstant.RoyalLevel, 0) ?: 0
        if (royalLevel > 0) {
            view_anchor_agreement.show()
            tv_anchor_agreement.show()
            iv_anchor_agreement.show()
        } else {
            view_anchor_agreement.hide()
            tv_anchor_agreement.hide()
            iv_anchor_agreement.hide()
        }

        if (!BuildConfig.DEBUG) {
            tvChange.hide()
        }
        calclateCacheSize()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        header_view.imageViewBack.onClickNew {
            finish()
        }

        tvChange.onClickNew {
            ARouter.getInstance().build(ARouterConstant.ENVIRONMENT_CONFIGURATION_ACTIVITY)
                .navigation()
        }

        tv_logout.onClickNew {
            //退出登录
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "退出登录后将无法收到TA的消息了，确定退出吗？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    LoginManager.doLoginOut {
                        if (it) {
                            //退出登录成功
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }, onCancel = {
                }), "退出提示", "确定"
            )
        }

        view_clear_cache.onClickNew {
            //清空缓存
            if (cache_size.visibility == View.VISIBLE) {
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "确定清空缓存？",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        DataCleanManager.clearAllCache(applicationContext)
                        ToastUtils.show("清空缓存成功")
                        cache_size.hide()
                    }, onCancel = {
                    }), "清空提示", "确定"
                )
            } else {
                ToastUtils.show("当前没有缓存")
            }

        }

        view_accountandsecurity.onClickNew {
            //账号与安全
            val intent = Intent(this, AccountAndSecurityActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }
        view_about_us.onClickNew {
            //关于我们
            val intent = Intent(this, AboutUsActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }
//        view_service.onClickNew {
//            //客服
//            val extra = Bundle()
//            extra.putString(BusiConstant.WEB_URL, "http://q.url.cn/s/raPDfcm?_type=wpa")
//            var intent = Intent(this, WebActivity::class.java)
//            intent.putExtras(extra)
//            startActivity(intent)
//        }
        view_privacy.onClickNew {
            //隐私设置
            jump(PrivacyActivity::class.java)
        }

        view_user_agreement.onClickNew {
            //用户协议
            WebActivity.startWeb(this, Agreement.UserAgreement)
        }

        view_privacy_agreement.onClickNew {
            //隐私协议
            WebActivity.startWeb(this, Agreement.PrivacyAgreement)
        }

        view_message_setting.onClickNew {
            //消息设置
            val intent = Intent(this, MessageSettingActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }

        view_anchor_agreement.onClickNew {
            //主播管理规范
            WebActivity.startWeb(this, Agreement.AnchorAgreement)
        }
    }

    /**
     * 计算缓存
     */
    private fun calclateCacheSize() {
        val cacheSize: String = DataCleanManager.getTotalCacheSize(CommonInit.getInstance().getApp())
        if (cacheSize == "0K") {
            cache_size.hide()
        } else {
            cache_size.text = cacheSize
        }
    }
}