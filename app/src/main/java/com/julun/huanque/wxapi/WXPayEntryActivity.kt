package com.julun.huanque.wxapi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.julun.huanque.app.HuanQueApp
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.core.ui.recharge.RechargeCenterActivity
import com.julun.huanque.viewmodel.WechatViewModel
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * Created by djp on 2016/11/20.
 */

class WXPayEntryActivity : BaseActivity(), IWXAPIEventHandler {

    private var mViewModel: WechatViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareViewModel()
        // 微信事件回调接口注册
        this.handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        this.handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        HuanQueApp.wxApi?.handleIntent(intent, this)
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProviders.of(this).get(WechatViewModel::class.java)
        mViewModel?.finish?.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }

    /**
     * 微信发送请求到第三方应用的回调方法
     *
     * @param baseReq
     */
    override fun onReq(resp: BaseReq) {}

    /**
     * 第三方应用发送到微信的请求处理后的结果，会回调该方法
     * 微信登录 和 分享都会回调
     *
     * @param baseResp
     */
    override fun onResp(resp: BaseResp) {
        mViewModel?.onPayEntryResp(resp)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, RechargeCenterActivity::class.java))
        finish()
    }

    override fun getLayoutId(): Int = 0

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {}
}
