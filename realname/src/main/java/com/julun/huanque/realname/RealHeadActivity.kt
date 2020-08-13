package com.julun.huanque.realname

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.events.RHVerifyResult
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.RealNameConstants
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_main_realname.*
import org.greenrobot.eventbus.EventBus

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/08/13 0013
 * @detail
 */
@Route(path = ARouterConstant.REAL_HEAD_ACTIVITY)
class RealHeadActivity : BaseActivity() {

    private var mRealNameService: IRealNameService? = null

    override fun getLayoutId(): Int = R.layout.activity_realhead

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        ivback.onClickNew {
            finish()
        }
        btnCommit.onClickNew {
            mRealNameService =
                mRealNameService ?: ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                    .navigation() as? IRealNameService
            mRealNameService?.startRealHead(this, object : RealNameCallback {
                override fun onCallback(status: String, des: String, percent: Int?) {
                    EventBus.getDefault().post(RHVerifyResult(status))
                    when (status) {
                        RealNameConstants.TYPE_SUCCESS -> {
                            //认证成功
                            ToastUtils.show(des)
                            finish()
                        }
                        RealNameConstants.TYPE_FAIL, RealNameConstants.TYPE_ERROR -> {
                            //认证失败 or 认证网络请求异常
                            ToastUtils.show(des)
                        }
                        else -> {
                            //认证取消
                            ToastUtils.show(
                                if (des.isNotEmpty()) {
                                    des
                                } else {
                                    "认证取消"
                                }
                            )
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealNameService?.release()
    }
}
