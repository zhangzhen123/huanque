package com.julun.huanque.wxapi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.julun.huanque.activity.MainActivity
import com.julun.huanque.app.HuanQueApp
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.suger.hideDialogs
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.viewmodel.WechatViewModel
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * @author WanZhiYuan
 * @since 4.27
 * @date 2020/03/05 0005
 */
class WXEntryActivity : AppCompatActivity(), IWXAPIEventHandler {
    private val mViewModel: WechatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareViewModel()
        // 微信事件回调接口注册
        logger("设置微信组件回调接口")
        this.handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        this.handleIntent(intent ?: return)
    }

    private fun handleIntent(intent: Intent) {
        HuanQueApp.wxApi?.handleIntent(intent, this)
    }

    private fun prepareViewModel() {
        mViewModel.finish.observe(this, Observer {
            if (it) {
                finish()
            }
        })
        //

        mViewModel.showDialog.observe(this, Observer<String> { it ->
            var str = it
            if (TextUtils.isEmpty(str)) {
                str = ""
            }
            MyAlertDialog(this@WXEntryActivity, true).showAlertWithOK(str, null, "提示", "确认")
        })
    }

    /**
     * 第三方应用发送到微信的请求处理后的结果，会回调该方法
     * 微信登录 和 分享都会回调
     *
     * @param baseResp
     */
    override fun onResp(p0: BaseResp?) {
        if (p0?.type == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
//            SessionUtils.setWeiXinUse(false)
            if (SessionUtils.getIsRegUser()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
//                startActivity(Intent(this, CodeCheckActivity::class.java))
            }
            finish()
        } else {
            mViewModel.onEntryResp(p0)
        }
    }

    /**
     * 微信发送请求到第三方应用的回调方法
     *
     * @param baseReq
     */
    override fun onReq(p0: BaseReq?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        this.hideDialogs()
    }

}